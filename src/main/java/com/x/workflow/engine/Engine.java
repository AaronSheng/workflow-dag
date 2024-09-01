package com.x.workflow.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.x.workflow.config.DAGVO;
import com.x.workflow.config.EdgeVO;
import com.x.workflow.config.NodeVO;
import com.x.workflow.dag.DAG;
import com.x.workflow.dag.DefaultDAG;
import com.x.workflow.dag.DefaultNode;
import com.x.workflow.dag.Node;
import com.x.workflow.task.Task;
import com.x.workflow.task.TaskInput;
import com.x.workflow.task.TaskOutput;
import com.x.workflow.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Engine {
    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private static final int DEFAULT_EXEC_CONCURRENT = 1;
    private final ExecutorService executor;
    private final Map<String, Task> taskMapping = new HashMap<>();

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
        // 如果验证失败，抛出异常，提示DAG构建失败
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
            // 根据节点的任务名称获取任务，如果任务不存在则抛出异常
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
            // 通过边的起始和结束节点ID从节点映射中获取对应的节点
            Node<Task> from = nodeMapping.get(edgeVO.getFrom());
            Node<Task> to = nodeMapping.get(edgeVO.getTo());
            if (from == null || to == null) {
                throw new RuntimeException(String.format("Node %s or %s Not Found", edgeVO.getFrom(), edgeVO.getTo()));
            }
            graph.addEdge(from, to);
        }

        return graph;
    }

    public Result execute(DAG<Task> graph, Map<String, String> parameters) {
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
        Set<Node<Task>> processed = context.getProcessed();
        Map<String, String> parameters = context.getParameters();

        // pick up can execute node
        List<Node<Task>> canExecuteNodeList = new ArrayList<>();
        Map<String, Node<Task>> nodeMap = new HashMap<>();
        for (Node<Task> node : nodes) {
            nodeMap.put(node.getId(), node);
            if (!processed.contains(node) && processed.containsAll(node.getParents())) {
                canExecuteNodeList.add(node);
            }
        }

        // submit can execute node
        List<Future<TaskOutput>> futureList = new ArrayList<>();
        for (Node<Task> node : canExecuteNodeList) {
            Future<TaskOutput> future = executor.submit(() -> doExecute(node, context));
            futureList.add(future);
        }

        // process node execute output
        for (Future<TaskOutput> future : futureList) {
            TaskOutput output;
            try {
                // execute current node
                output = future.get();
                if (!output.isSucceed()) {
                    return new InnerResult().setSucceed(false).setMessage(output.getMessage());
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
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

    private TaskOutput doExecute(Node<Task> node, Context context) {
        Task task = node.getData();

        TaskOutput output = new TaskOutput();
        output.setSucceed(true);
        output.setTaskId(node.getId());

        try {
            TaskInput input = new TaskInput()
                    .setTaskId(node.getId())
                    .setParameters(context.getParameters());
            output = task.run(input);
        } catch (Exception e) {
            output.setSucceed(false);
            output.setException(e);
        }

        return output;
    }
}
