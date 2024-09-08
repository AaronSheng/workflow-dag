package com.x.workflow.engine;

import com.x.workflow.dag.DAG;
import com.x.workflow.dag.Node;
import com.x.workflow.task.Task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Context {
    private final DAG<Task> graph;
    private final Set<Node<Task>> processed = new HashSet<>();
    private final Map<String, Object> parameters = new HashMap<>();

    public Context(DAG<Task> graph) {
        this.graph = graph;
    }

    public Context(DAG<Task> graph, Map<String, Object> parameters) {
        this.graph = graph;
        this.parameters.putAll(parameters);
    }

    public DAG<Task> getGraph() {
        return graph;
    }

    public Set<Node<Task>> getProcessed() {
        return processed;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
