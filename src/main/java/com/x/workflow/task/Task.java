package com.x.workflow.task;

public interface Task {

    public String getTaskName();

    TaskOutput run(TaskInput input);
}

