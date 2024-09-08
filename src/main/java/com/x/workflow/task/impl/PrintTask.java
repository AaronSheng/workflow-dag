package com.x.workflow.task.impl;

import com.x.workflow.task.Task;
import com.x.workflow.task.model.TaskInput;
import com.x.workflow.task.model.TaskOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrintTask implements Task {
    private static final Logger LOGGER = LogManager.getLogger(PrintTask.class);
    private static final String TASK_NAME = "PrintTask";

    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public TaskOutput run(TaskInput input) {
        LOGGER.info("Task: {}-{} Input: {} Output: {}", getTaskName(), input.getTaskId(), input.getParameters(), true);
        String taskId = input.getTaskId();
        System.out.printf("Thread: %s Task: %s-%s Output: %s\n", Thread.currentThread().getId(), getTaskName(), taskId, true);
        Map<String, String> output = new LinkedHashMap<>();
        output.put(taskId + "_Result", Boolean.toString(true));

        return new TaskOutput()
                .setTaskId(taskId)
                .setSucceed(true)
                .setOutput(output);
    }
}