package com.x.workflow;

import com.x.workflow.dag.DAG;
import com.x.workflow.dag.DefaultDAG;
import com.x.workflow.dag.DefaultNode;
import com.x.workflow.dag.Node;
import com.x.workflow.engine.Engine;
import com.x.workflow.engine.Result;

import java.util.HashMap;
import java.util.Map;

public class EngineTest {
    public static void main(String[] args) {
        DAG<PrintTask> graph = new DefaultDAG<>();
        Node<PrintTask> nodeA = new DefaultNode<>("A", new PrintTask("A"));
        Node<PrintTask> nodeB = new DefaultNode<>("B", new PrintTask("B"));
        Node<PrintTask> nodeC = new DefaultNode<>("C", new PrintTask("C"));
        Node<PrintTask> nodeD = new DefaultNode<>("D", new PrintTask("D"));
        Node<PrintTask> nodeE = new DefaultNode<>("E", new PrintTask("E"));
        Node<PrintTask> nodeF = new DefaultNode<>("F", new PrintTask("F"));

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);
        graph.addEdge(nodeF, nodeE);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        Engine<PrintTask> engine = new Engine<>();

        Result result = engine.execute(graph, parameters);
        System.out.printf("Exec Flow:%s Succeed:%s Output:%s\n", graph.getId(), result.isSucceed(), result.getMessage());
    }
}


