package com.javaproject.service;

import com.javaproject.dto.TaskEvent;
import com.javaproject.exception.TaskNotFoundException;
import com.javaproject.model.Task;
import com.javaproject.model.TaskStatus;
import com.javaproject.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventService taskEventService;

    public TaskService(TaskRepository taskRepository, TaskEventService taskEventService) {
        this.taskRepository = taskRepository;
        this.taskEventService = taskEventService;
    }

    public Page<Task> getAllTasks(TaskStatus status, Pageable pageable) {
        if (status != null) {
            return taskRepository.findByStatus(status, pageable);
        }
        return taskRepository.findAll(pageable);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task createTask(Task task) {
        Task saved = taskRepository.save(task);
        taskEventService.broadcast(TaskEvent.Action.CREATED, saved);
        return saved;
    }

    public Task updateTask(Long id, Task task) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        Task saved = taskRepository.save(existing);
        taskEventService.broadcast(TaskEvent.Action.UPDATED, saved);
        return saved;
    }

    public Task patchTask(Long id, Task task) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        if (task.getTitle() != null) existing.setTitle(task.getTitle());
        if (task.getDescription() != null) existing.setDescription(task.getDescription());
        if (task.getStatus() != null) existing.setStatus(task.getStatus());
        Task saved = taskRepository.save(existing);
        taskEventService.broadcast(TaskEvent.Action.UPDATED, saved);
        return saved;
    }

    public void deleteTask(Long id) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.deleteById(id);
        taskEventService.broadcast(TaskEvent.Action.DELETED, existing);
    }
}
