package com.x.workflow.dag;

import java.util.Set;

public interface Node<T> {
    // build children and parent
    Set<Node<T>> getChildren();
    Set<Node<T>> getParents();
    void addChildren(Node<T> child);
    void addParent(Node<T> parent);

    String getId();

    void setId(String id);

    Condition getCondition();

    void setCondition(Condition condition);

    void setData(T data);

    T getData();
}
