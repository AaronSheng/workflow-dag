package com.x.workflow.dag;

import java.util.HashSet;
import java.util.Set;

public class DefaultNode<T> implements Node<T> {
    private final Set<Node<T>> parents = new HashSet<>();
    private final Set<Node<T>> children = new HashSet<>();

    private String id;
    private T data;

    public DefaultNode(String id, T data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public Set<Node<T>> getChildren() {
        return children;
    }

    @Override
    public Set<Node<T>> getParents() {
        return parents;
    }

    @Override
    public void addChildren(Node<T> child) {
        children.add(child);
    }

    @Override
    public void addParent(Node<T> parent) {
        parents.add(parent);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public T getData() {
        return data;
    }
}
