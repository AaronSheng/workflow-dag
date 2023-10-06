package com.x.workflow.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeVO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("task_name")
    private String taskName;

    public String getId() {
        return id;
    }

    public NodeVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getTaskName() {
        return taskName;
    }

    public NodeVO setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }
}
