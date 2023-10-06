package com.x.workflow.task;

public interface Task {

    String getTaskId();

    TaskOutput run(TaskInput input);
}

