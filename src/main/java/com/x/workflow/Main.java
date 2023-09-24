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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

        DAGExecutor<PrintTask> executor = new DAGExecutor<>(graph, parameters, 2);
        ExecOutput output = executor.execute();
        System.out.printf("Exec Flow:%s Succeed:%s\n",  output.getId(), output.isSucceed());
    }
}

class PrintTask implements Task {
    private static final Logger LOGGER = LogManager.getLogger(PrintTask.class);

    private String taskId;
    private String taskName;

    public PrintTask(String taskName) {
        this.taskId = Integer.valueOf(UUID.randomUUID().hashCode()).toString();
        this.taskName = taskName;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    public PrintTask setTaskId(String taskID) {
        this.taskId = taskID;
        return this;
    }

    public PrintTask setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    @Override
    public TaskOutput run(TaskInput input) {
        // LOGGER.info("Task: {}-{} Input: {} Output: {}", taskName, taskId, input.getParameters(), true);
        System.out.printf("Thread: %s Task: %s-%s Input: %s Output: %s\n", Thread.currentThread().getId(), taskName, taskId, input.getParameters(), true);
        Map<String, String> output = new LinkedHashMap<>();
        output.put(taskId + "_Result", Boolean.toString(true));

        return new TaskOutput()
                .setTaskId(taskId)
                .setSucceed(true)
                .setOutput(output);
    }
}
