package com.x.workflow.engine;

import java.util.Map;

public class Result {
    private boolean succeed;
    private String message;
    private Exception exception;
    private Map<String, Object> output;

    public boolean isSucceed() {
        return succeed;
    }

    public Result setSucceed(boolean succeed) {
        this.succeed = succeed;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public Result setException(Exception exception) {
        this.exception = exception;
        return this;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public Result setOutput(Map<String, Object> output) {
        this.output = output;
        return this;
    }
}
