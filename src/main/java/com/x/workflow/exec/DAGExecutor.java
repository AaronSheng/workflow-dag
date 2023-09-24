package com.x.workflow.exec;

import com.x.workflow.constant.State;
import com.x.workflow.dag.DAG;
import com.x.workflow.dag.Node;
import com.x.workflow.task.Task;
import com.x.workflow.task.TaskInput;
import com.x.workflow.task.TaskOutput;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DAGExecutor<T extends Task> {
    private static final int DEFAULT_EXEC_CONCURRENT = 1;

    private final DAG<T> graph;
    private final Set<Node<T>> processed = new HashSet<>();
    private final ExecutorService executor;

    private Map<String, String> parameters;

    public DAGExecutor(DAG<T> graph) {
        this.graph = graph;
        this.executor = Executors.newFixedThreadPool(DEFAULT_EXEC_CONCURRENT);
    }

    public DAGExecutor(DAG<T> graph, Map<String, String> parameters) {
        this.graph = graph;
        this.parameters = parameters;
        this.executor = Executors.newFixedThreadPool(DEFAULT_EXEC_CONCURRENT);
    }

    public DAGExecutor(DAG<T> graph, Map<String, String> parameters, int concurrent) {
        this.graph = graph;
        this.parameters = parameters;
        this.executor = Executors.newFixedThreadPool(concurrent);
    }

    public DAGExecutor(DAG<T> graph, Map<String, String> parameters, ExecutorService executor) {
        this.graph = graph;
        this.parameters = parameters;
        this.executor = executor;
    }

    public ExecOutput execute() {
        graph.setState(State.RUNNING);

        ExecOutput result = doExecute(graph.getAllNodes());
        result.setId(graph.getId());

        graph.setState(result.isSucceed() ? State.SUCCEED : State.FAILED);
        return result;
    }

    private ExecOutput doExecute(Set<Node<T>> nodes) {
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
                return doExecute(node);
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
            ExecOutput execOutput = doExecute(node.getChildren());
            if (!execOutput.isSucceed()) {
                return execOutput;
            }
        }

        return new ExecOutput().setSucceed(true);
    }

    private TaskOutput doExecute(Node<T> node) {
        Task task = node.getData();
        node.setState(State.RUNNING);

        TaskOutput output = new TaskOutput();
        output.setTaskId(task.getTaskId());

        try {
            output = task.run(new TaskInput().setParameters(this.parameters));
        } catch (Exception e) {
            output.setSucceed(false);
            output.setException(e);
        } finally {
            node.setState(output.isSucceed() ? State.SUCCEED : State.FAILED);
        }

        return output;
    }
}
