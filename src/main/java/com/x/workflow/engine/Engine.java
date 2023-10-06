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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Engine<T extends Task> {
    private static final int DEFAULT_EXEC_CONCURRENT = 1;
    private final ExecutorService executor;
    private final Map<String, Class<T>> taskMapping = new HashMap<>();

    public Engine() {
        this.executor = Executors.newFixedThreadPool(DEFAULT_EXEC_CONCURRENT);
    }

    public Engine(int concurrent) {
        this.executor = Executors.newFixedThreadPool(concurrent);
    }

    public Engine(ExecutorService executor) {
        this.executor = executor;
    }

    public void register(String taskName, Class<T> clazz) {
        taskMapping.put(taskName, clazz);
    }

    public DAG<T> load(String dag) {
        DAGVO dagDO = JsonUtil.parse(dag, new TypeReference<DAGVO>(){});
        if (dagDO.getEdges() == null || dagDO.getEdges().isEmpty()) {
            throw new RuntimeException("Empty Edges");
        }
        if (dagDO.getNodes() == null || dagDO.getNodes().isEmpty()) {
            throw new RuntimeException("Empty Nodes");
        }

        // parse node
        Map<String, Node<T>> nodeMapping = new HashMap<>();
        for (NodeVO nodeVO : dagDO.getNodes()) {
            Class<T> taskClazz = taskMapping.get(nodeVO.getTaskName());
            if (taskClazz == null) {
                throw new RuntimeException(String.format("Task %s Not Found", nodeVO.getTaskName()));
            }

            T taskInstance;
            try {
                taskInstance = taskClazz.newInstance();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            Node<T> node = new DefaultNode<>(nodeVO.getId(), taskInstance);
            nodeMapping.put(node.getId(), node);
        }

        // build edges
        DAG<T> graph = new DefaultDAG<>();
        for (EdgeVO edgeVO : dagDO.getEdges()) {
            Node<T> from = nodeMapping.get(edgeVO.getFrom());
            Node<T> to = nodeMapping.get(edgeVO.getTo());
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

    public Result execute(DAG<T> graph, Map<String, String> parameters) {
        if (!graph.validate()) {
            return new Result().setSucceed(false)
                    .setMessage("graph is circled");
        }

        Context<T> context = new Context<>(graph, parameters);
        ExecOutput output = doExecute(graph.getAllNodes(), context);

        return new Result()
                .setSucceed(output.isSucceed())
                .setException(output.getException())
                .setMessage(output.getMessage())
                .setOutput(context.getParameters());
    }

    private ExecOutput doExecute(Set<Node<T>> nodes, Context<T> context) {
        Set<Node<T>> processed = context.getProcessed();
        Map<String, String> parameters = context.getParameters();

        // pick up can execute node
        List<Node<T>> canExecuteNodeList = new ArrayList<>();
        Map<String, Node<T>> taskToNodeMap = new HashMap<>();
        for (Node<T> node : nodes) {
            taskToNodeMap.put(node.getData().getTaskId(), node);
            if (!processed.contains(node) && processed.containsAll(node.getParents())) {
                canExecuteNodeList.add(node);
            }
        }

        // submit can execute node
        List<Future<TaskOutput>> futureList = new ArrayList<>();
        for (Node<T> node : canExecuteNodeList) {
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

            Node<T> node = taskToNodeMap.get(output.getTaskId());
            processed.add(node);

            // process output
            parameters.putAll(output.getOutput());
            if (!output.isSucceed()) {
                return new ExecOutput().setSucceed(false).setMessage(output.getMessage());
            }

            // process children
            ExecOutput execOutput = doExecute(node.getChildren(), context);
            if (!execOutput.isSucceed()) {
                return execOutput;
            }
        }

        return new ExecOutput().setSucceed(true);
    }

    private TaskOutput doExecute(Node<T> node, Context<T> context) {
        Task task = node.getData();

        TaskOutput output = new TaskOutput();
        output.setTaskId(task.getTaskId());

        try {
            output = task.run(new TaskInput().setParameters(context.getParameters()));
        } catch (Exception e) {
            output.setSucceed(false);
            output.setException(e);
        }

        return output;
    }
}
