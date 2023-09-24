package com.x.workflow.task;

import java.util.Map;

public class TaskOutput {
    private String taskId;
    private boolean succeed;
    private String message;
    private Exception exception;
    private Map<String, String> output;

    public String getTaskId() {
        return taskId;
    }

    public TaskOutput setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public TaskOutput setSucceed(boolean succeed) {
        this.succeed = succeed;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public TaskOutput setMessage(String message) {
        this.message = message;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public TaskOutput setException(Exception exception) {
        this.exception = exception;
        return this;
    }

    public Map<String, String> getOutput() {
        return output;
    }

    public TaskOutput setOutput(Map<String, String> output) {
        this.output = output;
        return this;
    }
}
