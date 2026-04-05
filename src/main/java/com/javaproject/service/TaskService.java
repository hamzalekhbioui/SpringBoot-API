package com.javaproject.service;

import com.javaproject.dto.TaskEvent;
import com.javaproject.exception.TaskNotFoundException;
import com.javaproject.model.Task;
import com.javaproject.model.TaskStatus;
import com.javaproject.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    @Cacheable(value = "tasks", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Task> getAllTasks(TaskStatus status, Pageable pageable) {
        if (status != null) {
            return taskRepository.findByStatus(status, pageable);
        }
        return taskRepository.findAll(pageable);
    }

    @Cacheable(value = "task", key = "#id")
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "task", allEntries = true)
    })
    public Task createTask(Task task) {
        Task saved = taskRepository.save(task);
        taskEventService.broadcast(TaskEvent.Action.CREATED, saved);
        return saved;
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "task", key = "#id")
    })
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

    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "task", key = "#id")
    })
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

    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "task", key = "#id")
    })
    public void deleteTask(Long id) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.deleteById(id);
        taskEventService.broadcast(TaskEvent.Action.DELETED, existing);
    }
}
