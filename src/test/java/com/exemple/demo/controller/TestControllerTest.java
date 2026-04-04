package com.exemple.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaproject.controller.TaskController;
import com.javaproject.exception.TaskNotFoundException;
import com.javaproject.model.Task;
import com.javaproject.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- GET /tasks ---

    @Test
    void shouldReturnAllTasks() throws Exception {
        // Arrange
        Task task1 = new Task(1L, "Task 1");
        Task task2 = new Task(2L, "Task 2");

        when(taskService.getAllTasks()).thenReturn(List.of(task1, task2));

        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Task 2"));

        verify(taskService).getAllTasks();
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() throws Exception {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(taskService).getAllTasks();
    }

    // --- POST /tasks ---

    @Test
    void shouldCreateTask() throws Exception {
        // Arrange
        Task task = new Task(null, "New Task");
        Task savedTask = new Task(1L, "New Task");

        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"));

        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void shouldCreateTaskWithAllFields() throws Exception {
        // Arrange
        Task task = new Task(null, "Full Task");
        task.setDescription("A description");
        task.setStatus("TODO");

        Task savedTask = new Task(2L, "Full Task");
        savedTask.setDescription("A description");
        savedTask.setStatus("TODO");

        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Full Task"))
                .andExpect(jsonPath("$.description").value("A description"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void shouldReturn400WhenCreateTaskBodyIsInvalid() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        // Arrange
        String body = "{\"title\": \"\", \"status\": \"TODO\"}";

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("Title is required"));

        verify(taskService, never()).createTask(any());
    }

    @Test
    void shouldReturn400WhenStatusIsInvalid() throws Exception {
        // Arrange
        String body = "{\"title\": \"My Task\", \"status\": \"INVALID\"}";

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("Status must be TODO, IN_PROGRESS, or DONE"));

        verify(taskService, never()).createTask(any());
    }

    @Test
    void shouldReturn400WhenDescriptionIsTooLong() throws Exception {
        // Arrange
        String longDescription = "a".repeat(501);
        String body = "{\"title\": \"My Task\", \"description\": \"" + longDescription + "\"}";

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("Description must not exceed 500 characters"));

        verify(taskService, never()).createTask(any());
    }

    // --- DELETE /tasks/{id} ---

    @Test
    void shouldDeleteTask() throws Exception {
        // Arrange
        Long taskId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        doThrow(new TaskNotFoundException(nonExistentId)).when(taskService).deleteTask(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));

        verify(taskService).deleteTask(nonExistentId);
    }
}
