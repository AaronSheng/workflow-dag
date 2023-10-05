package com.x.workflow.engine;

import com.x.workflow.dag.DAG;
import com.x.workflow.dag.Node;
import com.x.workflow.task.Task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Context<T extends Task> {
    private final DAG<T> graph;
    private final Set<Node<T>> processed = new HashSet<>();
    private final Map<String, String> parameters = new HashMap<>();

    public Context(DAG<T> graph) {
        this.graph = graph;
    }

    public Context(DAG<T> graph, Map<String, String> parameters) {
        this.graph = graph;
        this.parameters.putAll(parameters);
    }

    public DAG<T> getGraph() {
        return graph;
    }

    public Set<Node<T>> getProcessed() {
        return processed;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
