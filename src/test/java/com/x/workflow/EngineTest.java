package com.x.workflow;

import com.x.workflow.task.impl.PrintTask;
import com.x.workflow.dag.DAG;
import com.x.workflow.dag.impl.DefaultDAG;
import com.x.workflow.dag.impl.DefaultNode;
import com.x.workflow.dag.Node;
import com.x.workflow.engine.Engine;
import com.x.workflow.engine.Result;
import com.x.workflow.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EngineTest {
    private static final Logger LOGGER = LogManager.getLogger(EngineTest.class);

    @Test
    public void testExecute() {
        DAG<Task> graph = new DefaultDAG<>();

        PrintTask printTask = new PrintTask();
        Node<Task> nodeA = new DefaultNode<>("A", printTask);
        Node<Task> nodeB = new DefaultNode<>("B", printTask);
        Node<Task> nodeC = new DefaultNode<>("C", printTask);
        Node<Task> nodeD = new DefaultNode<>("D", printTask);
        Node<Task> nodeE = new DefaultNode<>("E", printTask);
        Node<Task> nodeF = new DefaultNode<>("F", printTask);

        /**
         *    ->B->C
         *   |      \
         * A         ->E->F
         *   \      |
         *      ->D
         */
        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        Engine engine = new Engine();
        engine.register(printTask);

        Result result = engine.execute(graph, parameters);
        LOGGER.info("Exec Flow:{} Succeed:{} Output:{}", graph.getId(), result.isSucceed(), result.getOutput());
    }
}


