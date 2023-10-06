package com.x.workflow.dag;

import java.util.*;

public class DefaultDAG<T> implements DAG<T> {
    private final Set<Node<T>> nodes = new HashSet<>();
    private final String id;

    public DefaultDAG() {
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
    public boolean validate() {
        Node<T> root = null;
        for (Node<T> node : nodes) {
            if (node.getParents().isEmpty()) {
                root = node;
                break;
            }
        }
        if (root == null) {
            return false;
        }

        Map<String, Node<T>> visited = new HashMap<>();
        return dfs(root, visited);
    }

    private boolean dfs(Node<T> root, Map<String, Node<T>> visited) {
        if (root == null) {
            return true;
        }

        visited.put(root.getId(), root);
        for (Node<T> child : root.getChildren()) {
            if (visited.containsKey(child.getId())) {
                System.out.printf("Node: %s is circled\n", child.getId());
                return false;
            }

            visited.put(child.getId(), child);
            if (!dfs(child, visited)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getId() {
        return id;
    }
}
