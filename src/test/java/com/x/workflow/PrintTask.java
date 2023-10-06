package com.x.workflow;

import com.x.workflow.task.Task;
import com.x.workflow.task.TaskInput;
import com.x.workflow.task.TaskOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrintTask implements Task {
    private static final Logger LOGGER = LogManager.getLogger(PrintTask.class);
    private static final String TASK_NAME = "PrintTask";

    private String taskId;

    public PrintTask() {
    }

    public PrintTask(String id) {
        this.taskId = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public static String getTaskName() {
        return TASK_NAME;
    }

    public PrintTask setTaskId(String taskID) {
        this.taskId = taskID;
        return this;
    }

    @Override
    public TaskOutput run(TaskInput input) {
        // LOGGER.info("Task: {}-{} Input: {} Output: {}", taskName, taskId, input.getParameters(), true);
        System.out.printf("Thread: %s Task: %s-%s Output: %s\n", Thread.currentThread().getId(), TASK_NAME, taskId, true);
        Map<String, String> output = new LinkedHashMap<>();
        output.put(taskId + "_Result", Boolean.toString(true));

        return new TaskOutput()
                .setTaskId(taskId)
                .setSucceed(true)
                .setOutput(output);
    }
}