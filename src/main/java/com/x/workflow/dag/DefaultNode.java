package com.x.workflow.dag;

import com.x.workflow.constant.State;

import java.util.HashSet;
import java.util.Set;

public class DefaultNode<T> implements Node<T> {
    private final Set<Node<T>> parents = new HashSet<>();
    private final Set<Node<T>> children = new HashSet<>();

    private final T data;
    private volatile State state;

    public DefaultNode(T data) {
        this.data = data;
        this.state = State.INIT;
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
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public T getData() {
        return data;
    }
}
