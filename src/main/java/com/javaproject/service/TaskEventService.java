package com.javaproject.service;

import com.javaproject.dto.TaskEvent;
import com.javaproject.model.Task;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public TaskEventService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(TaskEvent.Action action, Task task) {
        messagingTemplate.convertAndSend("/topic/tasks", new TaskEvent(action, task));
    }
}
