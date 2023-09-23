package com.x.workflow.exec;

import com.x.workflow.constant.State;
import com.x.workflow.dag.DAG;
import com.x.workflow.dag.Node;
import com.x.workflow.task.Task;
import com.x.workflow.task.TaskInput;
import com.x.workflow.task.TaskOutput;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DAGExecutor<T extends Task> {
    private final DAG<T> graph;
    private Map<String, String> parameters;
    private final Set<Node<T>> processed = new HashSet<>();

    public DAGExecutor(DAG<T> graph) {
        this.graph = graph;
    }

    public DAGExecutor(DAG<T> graph, Map<String, String> parameters) {
        this.graph = graph;
        this.parameters = parameters;
    }

    public ExecOutput execute() {
        graph.setState(State.RUNNING);

        ExecOutput result = doExecute(graph.getAllNodes());
        result.setId(graph.getId());

        graph.setState(result.isSucceed() ? State.SUCCEED : State.FAILED);
        return result;
    }

    private ExecOutput doExecute(Set<Node<T>> nodes) {
        for (Node<T> node : nodes) {
            if (!processed.contains(node) && processed.containsAll(node.getParents())) {
                Task task = node.getData();
                TaskOutput output = task.run(new TaskInput().setParameters(this.parameters));

                // process output
                node.setState(output.isSucceed() ? State.SUCCEED : State.FAILED);
                parameters.putAll(output.getOutput());
                processed.add(node);

                if (!output.isSucceed()) {
                    return new ExecOutput().setSucceed(false).setMessage(output.getMessage());
                }

                // process children
                ExecOutput execOutput = doExecute(node.getChildren());
                if (!execOutput.isSucceed()) {
                    return execOutput;
                }
            }
        }

        return new ExecOutput().setSucceed(true);
    }
}
