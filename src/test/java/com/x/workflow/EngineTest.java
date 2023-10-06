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

        PrintTask printTask = new PrintTask();
        Node<PrintTask> nodeA = new DefaultNode<>("A", printTask);
        Node<PrintTask> nodeB = new DefaultNode<>("B", printTask);
        Node<PrintTask> nodeC = new DefaultNode<>("C", printTask);
        Node<PrintTask> nodeD = new DefaultNode<>("D", printTask);
        Node<PrintTask> nodeE = new DefaultNode<>("E", printTask);
        Node<PrintTask> nodeF = new DefaultNode<>("F", printTask);

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        Engine<PrintTask> engine = new Engine<>();

        Result result = engine.execute(graph, parameters);
        System.out.printf("Exec Flow:%s Succeed:%s Output:%s\n", graph.getId(), result.isSucceed(), result.getMessage());
    }
}


