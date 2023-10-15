package com.x.workflow.engine;

class InnerResult {
    private boolean succeed;
    private String message;
    private Exception exception;

    public boolean isSucceed() {
        return succeed;
    }

    public InnerResult setSucceed(boolean succeed) {
        this.succeed = succeed;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public InnerResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public InnerResult setException(Exception exception) {
        this.exception = exception;
        return this;
    }
}
