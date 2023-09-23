package com.x.workflow.task;

import java.util.Map;

public class TaskInput {
    private Map<String, String> parameters;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public TaskInput setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }
}
