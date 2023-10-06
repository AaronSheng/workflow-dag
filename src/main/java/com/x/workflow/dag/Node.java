package com.x.workflow.dag;

import com.x.workflow.constant.State;

import java.util.Set;

public interface Node<T> {
    // build children and parent
    Set<Node<T>> getChildren();
    Set<Node<T>> getParents();
    void addChildren(Node<T> child);
    void addParent(Node<T> parent);

    String getId();

    void setId(String id);

    void setData(T data);

    T getData();
}
