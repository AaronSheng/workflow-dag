package com.x.workflow.dag;

import java.util.Set;

public interface DAG<T> {
    void addEdge(Node<T> from, Node<T> to);
    Set<Node<T>> getAllNodes();

    boolean validate();

    String getId();
}
