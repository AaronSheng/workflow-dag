package com.x.workflow.engine;

import com.x.workflow.constant.State;
import com.x.workflow.dag.DAG;
import com.x.workflow.dag.Node;
import com.x.workflow.engine.Context;
import com.x.workflow.engine.ExecOutput;
import com.x.workflow.task.Task;
import com.x.workflow.task.TaskInput;
import com.x.workflow.task.TaskOutput;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Engine<T extends Task> {
    private static final int DEFAULT_EXEC_CONCURRENT = 1;
    private final ExecutorService executor;

    public Engine() {
        this.executor = Executors.newFixedThreadPool(DEFAULT_EXEC_CONCURRENT);
    }

    public Engine(int concurrent) {
        this.executor = Executors.newFixedThreadPool(concurrent);
    }

    public Engine(ExecutorService executor) {
        this.executor = executor;
    }

    public DAG<T> load(String dag) {
        return null;
    }

    public Result execute(DAG<T> graph, Map<String, String> parameters) {
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
        node.setState(State.RUNNING);

        TaskOutput output = new TaskOutput();
        output.setTaskId(task.getTaskId());

        try {
            output = task.run(new TaskInput().setParameters(context.getParameters()));
        } catch (Exception e) {
            output.setSucceed(false);
            output.setException(e);
        } finally {
            node.setState(output.isSucceed() ? State.SUCCEED : State.FAILED);
        }

        return output;
    }
}
