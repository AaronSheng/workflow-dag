package com.x.workflow.dag;

import com.x.workflow.constant.State;

import java.util.Set;

public interface Node<T> {
    // build children and parent
    Set<Node<T>> getChildren();
    Set<Node<T>> getParents();
    void addChildren(Node<T> child);
    void addParent(Node<T> parent);

    // build exec state
    State getState();
    void setState(State state);

    T getData();
}
