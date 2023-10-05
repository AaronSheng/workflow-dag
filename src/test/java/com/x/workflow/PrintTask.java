package com.x.workflow;

import com.x.workflow.task.Task;
import com.x.workflow.task.TaskInput;
import com.x.workflow.task.TaskOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PrintTask implements Task {
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
        System.out.printf("Thread: %s Task: %s-%s Output: %s\n", Thread.currentThread().getId(), taskName, taskId, true);
        Map<String, String> output = new LinkedHashMap<>();
        output.put(taskId + "_Result", Boolean.toString(true));

        return new TaskOutput()
                .setTaskId(taskId)
                .setSucceed(true)
                .setOutput(output);
    }
}