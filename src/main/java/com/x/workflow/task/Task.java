package com.x.workflow.task;

public interface Task {

    String getTaskId();

    String getTaskName();

    TaskOutput run(TaskInput input);
}

