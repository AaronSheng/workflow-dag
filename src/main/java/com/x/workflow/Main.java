package com.x.workflow;

import com.x.workflow.dag.DAG;
import com.x.workflow.dag.DefaultDAG;
import com.x.workflow.dag.DefaultNode;
import com.x.workflow.dag.Node;
import com.x.workflow.exec.DAGExecutor;
import com.x.workflow.exec.ExecOutput;
import com.x.workflow.task.Task;
import com.x.workflow.task.TaskInput;
import com.x.workflow.task.TaskOutput;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        DAG<PrintTask> graph = new DefaultDAG<>();
        Node<PrintTask> nodeA = new DefaultNode<>(new PrintTask("A"));
        Node<PrintTask> nodeB = new DefaultNode<>(new PrintTask("B"));
        Node<PrintTask> nodeC = new DefaultNode<>(new PrintTask("C"));
        Node<PrintTask> nodeD = new DefaultNode<>(new PrintTask("D"));
        Node<PrintTask> nodeE = new DefaultNode<>(new PrintTask("E"));
        Node<PrintTask> nodeF = new DefaultNode<>(new PrintTask("F"));

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        DAGExecutor<PrintTask> executor = new DAGExecutor<>(graph, parameters);
        ExecOutput output = executor.execute();
        System.out.printf("Exec Flow:%s Succeed:%s\n",  output.getId(), output.isSucceed());
    }
}

class PrintTask implements Task {
    private String taskID;

    public PrintTask(String taskID) {
        this.taskID = taskID;
    }

    public String getTaskID() {
        return taskID;
    }

    public PrintTask setTaskID(String taskID) {
        this.taskID = taskID;
        return this;
    }

    @Override
    public TaskOutput run(TaskInput input) {
        System.out.printf("Task %s Input: %s Output: %s\n", taskID, input.getParameters(), true);
        Map<String, String> output = new LinkedHashMap<>();
        output.put(taskID + "_Result", Boolean.toString(true));

        return new TaskOutput().setSucceed(true).setOutput(output);
    }
}
