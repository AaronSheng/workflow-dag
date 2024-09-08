package com.x.workflow.task;

import com.x.workflow.task.model.TaskInput;
import com.x.workflow.task.model.TaskOutput;

public interface Task {

    public String getTaskName();

    TaskOutput run(TaskInput input);
}

