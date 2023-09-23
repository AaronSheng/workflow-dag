package com.x.workflow.dag;

import com.x.workflow.constant.State;

import java.util.Set;

public interface DAG<T> {
    void addEdge(Node<T> from, Node<T> to);
    Set<Node<T>> getAllNodes();

    void setState(State state);
    State getState();

    String getId();
}
