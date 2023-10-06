package com.x.workflow.task;

import java.util.Map;

public class TaskInput {
    private String taskId;
    private Map<String, String> parameters;

    public String getTaskId() {
        return taskId;
    }

    public TaskInput setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public TaskInput setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }
}
