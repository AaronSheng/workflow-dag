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

    public DAG<Task> load(String dag) {
        DAGVO dagDO = JsonUtil.parse(dag, new TypeReference<DAGVO>(){});
        if (dagDO.getEdges() == null || dagDO.getEdges().isEmpty()) {
            LOGGER.warn("Config Attr:edges Is Empty");
            throw new RuntimeException("Empty Edges");
        }
        if (dagDO.getNodes() == null || dagDO.getNodes().isEmpty()) {
            LOGGER.warn("Config Attr:nodes Is Empty");
            throw new RuntimeException("Empty Nodes");
        }

        // parse node
        Map<String, Node<Task>> nodeMapping = new HashMap<>();
        for (NodeVO nodeVO : dagDO.getNodes()) {
            Task task = taskMapping.get(nodeVO.getTaskName());
            if (task == null) {
                throw new RuntimeException(String.format("Task %s Not Found", nodeVO.getTaskName()));
            }

            Node<Task> node = new DefaultNode<>(nodeVO.getId(), task);
            nodeMapping.put(node.getId(), node);
        }

        // build edges
        DAG<Task> graph = new DefaultDAG<>();
        for (EdgeVO edgeVO : dagDO.getEdges()) {
            Node<Task> from = nodeMapping.get(edgeVO.getFrom());
            Node<Task> to = nodeMapping.get(edgeVO.getTo());
            if (from == null || to == null) {
                throw new RuntimeException(String.format("Node %s or %s Not Found", edgeVO.getFrom(), edgeVO.getTo()));
            }
            graph.addEdge(from, to);
        }

        // validate dag
        if (!graph.validate()) {
            throw new RuntimeException("Dag Is Invalid");
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
            Future<TaskOutput> future = executor.submit(() -> {
                return doExecute(node, context);
            });
            futureList.add(future);
        }

        // process node execute output
        for (Future<TaskOutput> future : futureList) {
            TaskOutput output;
            try {
                output = future.get();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            Node<Task> node = nodeMap.get(output.getTaskId());
            processed.add(node);

            // process output
            parameters.putAll(output.getOutput());
            if (!output.isSucceed()) {
                return new InnerResult().setSucceed(false).setMessage(output.getMessage());
            }

            // process children
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
            output = task.run(new TaskInput().setTaskId(node.getId()).setParameters(context.getParameters()));
        } catch (Exception e) {
            output.setSucceed(false);
            output.setException(e);
        }

        return output;
    }
}
