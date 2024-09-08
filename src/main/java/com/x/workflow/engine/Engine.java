package com.x.workflow.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.x.workflow.condition.Condition;
import com.x.workflow.config.DAGVO;
import com.x.workflow.config.EdgeVO;
import com.x.workflow.config.NodeVO;
import com.x.workflow.dag.DAG;
import com.x.workflow.dag.impl.DefaultDAG;
import com.x.workflow.dag.impl.DefaultNode;
import com.x.workflow.dag.Node;
import com.x.workflow.task.Task;
import com.x.workflow.task.model.TaskInput;
import com.x.workflow.task.model.TaskOutput;
import com.x.workflow.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Engine {
    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private static final int DEFAULT_EXEC_CONCURRENT = 1;
    private final ExecutorService executor;
    private final Map<String, Task> taskMapping = new HashMap<>();
    private final Map<String, Condition> conditionMapping = new HashMap<>();

    public Engine() {
        this.executor = Executors.newFixedThreadPool(DEFAULT_EXEC_CONCURRENT);
    }

    public Engine(int concurrent) {
        this.executor = Executors.newFixedThreadPool(concurrent);
    }

    public Engine(ExecutorService executor) {
        this.executor = executor;
    }

    public <T extends Task> void register(T task) {
        taskMapping.put(task.getTaskName(), task);
    }

    public <T extends Condition> void register(T condition) {
        conditionMapping.put(condition.getConditionName(), condition);
    }

    /**
     * 根据字符串描述加载并构建一个有向无环图（DAG）
     *
     * @param dag 描述DAG的字符串，具体格式根据实际情况定义
     * @return 返回构建好的DAG实例
     * @throws RuntimeException 如果DAG验证失败，即图中存在循环依赖
     */
    public DAG<Task> load(String dag) {
        // 解析字符串描述，生成DAG的初步表示
        DAGVO dagVO = parse(dag);

        // 构建实际的DAG对象
        DAG<Task> graph = buildGraph(dagVO);

        // 验证图的正确性，确保其为一个有效的有向无环图
        if (!graph.validate()) {
            throw new RuntimeException("Dag Is Invalid");
        }

        return graph;
    }

    /**
     * 解析字符串为DAGVO对象
     *
     * @param dag JSON格式的字符串，表示一个DAG（有向无环图）的配置
     * @return 解析后的DAGVO对象
     * @throws RuntimeException 如果DAG配置中的edges或nodes为空，则抛出运行时异常
     */
    private DAGVO parse(String dag) {
        // 将JSON字符串解析为DAGVO对象
        DAGVO dagVO = JsonUtil.parse(dag, new TypeReference<DAGVO>(){});

        // 检查edges是否为空，如果为空则记录警告日志并抛出异常
        if (dagVO.getEdges() == null || dagVO.getEdges().isEmpty()) {
            LOGGER.warn("Config Attr: edges Is Empty");
            throw new RuntimeException("Empty Edges");
        }

        // 检查nodes是否为空，如果为空则记录警告日志并抛出异常
        if (dagVO.getNodes() == null || dagVO.getNodes().isEmpty()) {
            LOGGER.warn("Config Attr: nodes Is Empty");
            throw new RuntimeException("Empty Nodes");
        }

        return dagVO;
    }

    /**
     * 根据给定的DAGVO对象构建任务依赖图
     *
     * @param dagVO 描述依赖图的VO对象，包含了节点和边的信息
     * @return 构建完成的任务依赖图对象
     * @throws RuntimeException 如果任务或节点不存在，则抛出运行时异常
     */
    private DAG<Task> buildGraph(DAGVO dagVO) {
        // 创建节点到任务的映射，用于后续快速查找节点
        Map<String, Node<Task>> nodeMapping = new HashMap<>();
        for (NodeVO nodeVO : dagVO.getNodes()) {
            Task task = taskMapping.get(nodeVO.getTaskName());
            if (task == null) {
                throw new RuntimeException(String.format("Task %s Not Found", nodeVO.getTaskName()));
            }
            Node<Task> node = new DefaultNode<>(nodeVO.getId(), task);
            nodeMapping.put(node.getId(), node);
        }

        // 创建一个空的任务依赖图
        DAG<Task> graph = new DefaultDAG<>();
        for (EdgeVO edgeVO : dagVO.getEdges()) {
            Node<Task> from = nodeMapping.get(edgeVO.getFrom());
            Node<Task> to = nodeMapping.get(edgeVO.getTo());
            if (from == null || to == null) {
                throw new RuntimeException(String.format("Node %s or %s Not Found", edgeVO.getFrom(), edgeVO.getTo()));
            }
            graph.addEdge(from, to);
        }

        return graph;
    }

    public Result execute(DAG<Task> graph, Map<String, Object> parameters) {
        if (!graph.validate()) {
            return new Result().setSucceed(false)
                    .setMessage("Graph Is Circled");
        }

        Context context = new Context(graph, parameters);
        InnerResult result = doExecute(graph.getAllNodes(), context);
        return new Result()
                .setSucceed(result.isSucceed())
                .setException(result.getException())
                .setMessage(result.getMessage())
                .setOutput(context.getParameters());
    }

    private InnerResult doExecute(Set<Node<Task>> nodes, Context context) {
        // 找出可执行的节点
        List<Node<Task>> canExecuteNodeList = findCanExecuteNodeList(nodes, context);

        // 执行可执行的节点任务
        return execCanExecuteNodeList(canExecuteNodeList, context);
    }

    /**
     * 在给定的节点集中查找可以执行的节点列表
     * 可以执行的节点是指该节点未被处理过，且其所有父节点都已经被处理过的节点
     *
     * @param nodes 节点集，其中每个节点包含一个任务
     * @param context 上下文对象，用于存储已处理的节点
     * @return 返回可以执行的节点列表
     */
    private List<Node<Task>> findCanExecuteNodeList(Set<Node<Task>> nodes, Context context) {
        Set<Node<Task>> processed = context.getProcessed();
        return nodes.stream()
                .filter(node -> !processed.contains(node) && processed.containsAll(node.getParents()))
                .collect(Collectors.toList());
    }

    private InnerResult execCanExecuteNodeList(List<Node<Task>> nodeList, Context context) {
        // 上下文信息
        Set<Node<Task>> processed = context.getProcessed();
        Map<String, Object> parameters = context.getParameters();

        // 并行执行任务
        Map<String, Node<Task>> nodeMap = new HashMap<>();
        List<Future<TaskOutput>> futureList = new ArrayList<>();
        for (Node<Task> node : nodeList) {
            nodeMap.put(node.getId(), node);
            Future<TaskOutput> future = executor.submit(() -> executeNode(node, context));
            futureList.add(future);
        }

        for (Future<TaskOutput> future : futureList) {
            TaskOutput output;
            try {
                output = future.get();
                // 执行失败处理
                if (!output.isSucceed()) {
                    return new InnerResult().setSucceed(false).setMessage(output.getMessage());
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            // 不满足触发条件
            if (!output.isMatched()) {
                continue;
            }

            // process node output
            Node<Task> node = nodeMap.get(output.getTaskId());
            processed.add(node);
            parameters.putAll(output.getOutput());

            // process children node
            InnerResult result = doExecute(node.getChildren(), context);
            if (!result.isSucceed()) {
                return result;
            }
        }

        return new InnerResult().setSucceed(true);
    }

    /**
     * 执行一个任务节点
     *
     * @param node  任务节点对象，包含任务数据和唯一标识
     * @param context  上下文对象，包含执行任务所需的参数和状态
     * @return TaskOutput 任务执行结果对象，包含任务执行状态、匹配状态等信息
     */
    private TaskOutput executeNode(Node<Task> node, Context context) {
        Task task = node.getData();
        TaskOutput output = new TaskOutput();
        output.setSucceed(true);
        output.setTaskId(node.getId());

        try {
            // 不满足准入条件
            if (!matchCondition(node, context)) {
                output.setMatched(false);
                return output;
            }

            // 执行任务
            TaskInput input = new TaskInput()
                    .setTaskId(node.getId())
                    .setParameters(context.getParameters());
            output = task.run(input);
            output.setMatched(true);
        } catch (Exception e) {
            output.setSucceed(false);
            output.setException(e);
        }

        return output;
    }

    /**
     * 判断当前节点的任务是否满足执行条件
     *
     * @param node 当前节点，包含任务和条件信息
     * @param context 上下文对象，携带了任务执行相关的参数
     * @return 如果节点满足执行条件，则返回true；否则返回false
     */
    private boolean matchCondition(Node<Task> node, Context context) {
        if (node.getCondition() == null) {
            return true;
        }

        Condition condition = conditionMapping.get(node.getCondition().getConditionName());
        return condition.match(node.getCondition().getConditionRule(), context.getParameters());
    }
}
