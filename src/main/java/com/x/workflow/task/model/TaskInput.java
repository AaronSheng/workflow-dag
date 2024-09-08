package com.x.workflow.task.model;

import java.util.Map;

public class TaskInput {
    private String taskId;
    private String condition;
    private Map<String, Object> parameters;

    public String getTaskId() {
        return taskId;
    }

    public TaskInput setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public TaskInput setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public TaskInput setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }
}