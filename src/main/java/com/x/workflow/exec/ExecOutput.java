package com.x.workflow.exec;

public class ExecOutput {
    private String id;
    private boolean succeed;
    private String message;
    private Exception exception;

    public String getId() {
        return id;
    }

    public ExecOutput setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public ExecOutput setSucceed(boolean succeed) {
        this.succeed = succeed;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ExecOutput setMessage(String message) {
        this.message = message;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public ExecOutput setException(Exception exception) {
        this.exception = exception;
        return this;
    }
}
