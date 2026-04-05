package com.exemple.demo.service;

import com.javaproject.dto.TaskEvent;
import com.javaproject.exception.TaskNotFoundException;
import com.javaproject.model.Task;
import com.javaproject.model.TaskStatus;
import com.javaproject.repository.TaskRepository;
import com.javaproject.service.TaskEventService;
import com.javaproject.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskEventService taskEventService;

    @InjectMocks
    private TaskService taskService;

    // --- getAllTasks ---

    @Test
    void shouldReturnAllTasksWithPagination() {
        Task task1 = new Task(1L, "Task 1");
        Task task2 = new Task(2L, "Task 2");
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(task1, task2)));

        Page<Task> result = taskService.getAllTasks(null, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("Task 1", result.getContent().get(0).getTitle());
        verify(taskRepository).findAll(pageable);
        verify(taskRepository, never()).findByStatus(any(), any());
    }

    @Test
    void shouldReturnEmptyPageWhenNoTasks() {
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findAll(pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<Task> result = taskService.getAllTasks(null, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository).findAll(pageable);
    }

    @Test
    void shouldReturnTasksFilteredByStatus() {
        Task task = new Task(1L, "Todo Task");
        task.setStatus(TaskStatus.TODO);
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findByStatus(TaskStatus.TODO, pageable))
                .thenReturn(new PageImpl<>(List.of(task)));

        Page<Task> result = taskService.getAllTasks(TaskStatus.TODO, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(TaskStatus.TODO, result.getContent().get(0).getStatus());
        verify(taskRepository).findByStatus(TaskStatus.TODO, pageable);
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    // --- getTaskById ---

    @Test
    void shouldReturnTaskById() {
        Task task = new Task(1L, "Task 1");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Task 1", result.getTitle());
        verify(taskRepository).findById(1L);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(99L));
        verify(taskRepository).findById(99L);
    }

    // --- createTask ---

    @Test
    void shouldCreateTask() {
        Task task = new Task(null, "New Task");
        Task savedTask = new Task(1L, "New Task");

        when(taskRepository.save(task)).thenReturn(savedTask);

        Task result = taskService.createTask(task);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Task", result.getTitle());
        verify(taskRepository).save(task);
        verify(taskEventService).broadcast(TaskEvent.Action.CREATED, savedTask);
    }

    @Test
    void shouldCreateTaskWithAllFields() {
        Task task = new Task(null, "Full Task");
        task.setDescription("A description");
        task.setStatus(TaskStatus.TODO);

        Task savedTask = new Task(2L, "Full Task");
        savedTask.setDescription("A description");
        savedTask.setStatus(TaskStatus.TODO);

        when(taskRepository.save(task)).thenReturn(savedTask);

        Task result = taskService.createTask(task);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Full Task", result.getTitle());
        assertEquals("A description", result.getDescription());
        assertEquals(TaskStatus.TODO, result.getStatus());
        verify(taskRepository).save(task);
        verify(taskEventService).broadcast(TaskEvent.Action.CREATED, savedTask);
    }

    // --- updateTask ---

    @Test
    void shouldUpdateTask() {
        Task existing = new Task(1L, "Old Title");
        existing.setStatus(TaskStatus.TODO);

        Task update = new Task(null, "New Title");
        update.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task result = taskService.updateTask(1L, update);

        assertEquals("New Title", result.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(existing);
        verify(taskEventService).broadcast(TaskEvent.Action.UPDATED, existing);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(99L, new Task()));
        verify(taskRepository).findById(99L);
        verify(taskRepository, never()).save(any());
        verify(taskEventService, never()).broadcast(any(), any());
    }

    // --- patchTask ---

    @Test
    void shouldPatchOnlyProvidedFields() {
        Task existing = new Task(1L, "Existing Title");
        existing.setStatus(TaskStatus.TODO);

        Task patch = new Task(null, null);
        patch.setStatus(TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task result = taskService.patchTask(1L, patch);

        assertEquals("Existing Title", result.getTitle());
        assertEquals(TaskStatus.DONE, result.getStatus());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(existing);
        verify(taskEventService).broadcast(TaskEvent.Action.UPDATED, existing);
    }

    @Test
    void shouldThrowWhenPatchingNonExistentTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.patchTask(99L, new Task()));
        verify(taskRepository).findById(99L);
        verify(taskRepository, never()).save(any());
        verify(taskEventService, never()).broadcast(any(), any());
    }

    // --- deleteTask ---

    @Test
    void shouldDeleteTask() {
        Task existing = new Task(1L, "Task to delete");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        taskService.deleteTask(1L);

        verify(taskRepository).findById(1L);
        verify(taskRepository).deleteById(1L);
        verify(taskEventService).broadcast(TaskEvent.Action.DELETED, existing);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenDeletingNonExistentTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(99L));
        verify(taskRepository).findById(99L);
        verify(taskRepository, never()).deleteById(any());
        verify(taskEventService, never()).broadcast(any(), any());
    }
}
