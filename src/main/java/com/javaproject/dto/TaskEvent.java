package com.javaproject.dto;

import com.javaproject.model.Task;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Real-time task event broadcast via WebSocket")
public class TaskEvent {

    public enum Action {
        CREATED, UPDATED, DELETED
    }

    @Schema(description = "Type of action performed", example = "CREATED")
    private Action action;

    @Schema(description = "The task that was affected")
    private Task task;

    public TaskEvent(Action action, Task task) {
        this.action = action;
        this.task = task;
    }

    public Action getAction() {
        return action;
    }

    public Task getTask() {
        return task;
    }
}
