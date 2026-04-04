package com.exemple.demo.service;

import com.javaproject.model.Task;
import com.javaproject.repository.TaskRepository;
import com.javaproject.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    // getAllTasks

    @Test
    void shouldReturnAllTasks() {
        // Arrange
        Task task1 = new Task(1L, "Task 1");
        Task task2 = new Task(2L, "Task 2");

        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        // Act
        List<Task> tasks = taskService.getAllTasks();

        // Assert
        assertEquals(2, tasks.size());
        assertEquals(1L, tasks.get(0).getId());
        assertEquals("Task 1", tasks.get(0).getTitle());
        assertEquals(2L, tasks.get(1).getId());
        assertEquals("Task 2", tasks.get(1).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Task> tasks = taskService.getAllTasks();

        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
        verify(taskRepository, times(1)).findAll();
    }

    //  createTask

    @Test
    void shouldCreateTask() {
        // Arrange
        Task task = new Task(null, "New Task");
        Task savedTask = new Task(1L, "New Task");

        when(taskRepository.save(task)).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(task);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Task", result.getTitle());
        verify(taskRepository).save(task);
    }

    @Test
    void shouldCreateTaskWithAllFields() {
        // Arrange
        Task task = new Task(null, "Full Task");
        task.setDescription("A description");
        task.setStatus("TODO");

        Task savedTask = new Task(2L, "Full Task");
        savedTask.setDescription("A description");
        savedTask.setStatus("TODO");

        when(taskRepository.save(task)).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(task);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Full Task", result.getTitle());
        assertEquals("A description", result.getDescription());
        assertEquals("TODO", result.getStatus());
        verify(taskRepository).save(task);
    }

    //  deleteTask

    @Test
    void shouldDeleteTask() {
        // Arrange
        Long taskId = 1L;

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void shouldPropagateExceptionWhenDeletingNonExistentTask() {
        // Arrange
        Long nonExistentId = 99L;
        doThrow(new IllegalArgumentException("Task not found")).when(taskRepository).deleteById(nonExistentId);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.deleteTask(nonExistentId));
        verify(taskRepository, times(1)).deleteById(nonExistentId);
    }
}