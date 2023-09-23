package com.x.workflow.dag;

import com.x.workflow.constant.State;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DefaultDAG<T> implements DAG<T> {
    private final Set<Node<T>> nodes = new HashSet<>();
    private State state;
    private String id;

    public DefaultDAG() {
        this.state = State.INIT;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public void addEdge(Node<T> from, Node<T> to) {
        if (from.equals(to)) {
            return;
        }
        from.addChildren(to);
        to.addParent(from);
        nodes.add(from);
        nodes.add(to);
    }

    @Override
    public Set<Node<T>> getAllNodes() {
        return nodes;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public String getId() {
        return id;
    }
}
