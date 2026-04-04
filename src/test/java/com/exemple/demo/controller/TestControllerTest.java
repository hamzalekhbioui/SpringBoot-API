package com.exemple.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaproject.controller.TaskController;
import com.javaproject.exception.TaskNotFoundException;
import com.javaproject.model.Task;
import com.javaproject.model.TaskStatus;
import com.javaproject.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
        Task task1 = new Task(1L, "Task 1");
        Task task2 = new Task(2L, "Task 2");

        when(taskService.getAllTasks(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task1, task2)));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Task 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Task 2"));

        verify(taskService).getAllTasks(isNull(), any(Pageable.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() throws Exception {
        when(taskService.getAllTasks(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0));

        verify(taskService).getAllTasks(isNull(), any(Pageable.class));
    }

    @Test
    void shouldFilterTasksByStatus() throws Exception {
        Task task = new Task(1L, "Todo Task");
        task.setStatus(TaskStatus.TODO);

        when(taskService.getAllTasks(eq(TaskStatus.TODO), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));

        mockMvc.perform(get("/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("TODO"));

        verify(taskService).getAllTasks(eq(TaskStatus.TODO), any(Pageable.class));
    }

    @Test
    void shouldReturn400WhenFilterStatusIsInvalid() throws Exception {
        mockMvc.perform(get("/tasks").param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- GET /tasks/{id} ---

    @Test
    void shouldReturnTaskById() throws Exception {
        Task task = new Task(1L, "Task 1");

        when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task 1"));

        verify(taskService).getTaskById(1L);
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));

        verify(taskService).getTaskById(99L);
    }

    // --- POST /tasks ---

    @Test
    void shouldCreateTask() throws Exception {
        Task task = new Task(null, "New Task");
        Task savedTask = new Task(1L, "New Task");

        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

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
        Task task = new Task(null, "Full Task");
        task.setDescription("A description");
        task.setStatus(TaskStatus.TODO);

        Task savedTask = new Task(2L, "Full Task");
        savedTask.setDescription("A description");
        savedTask.setStatus(TaskStatus.TODO);

        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

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
        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        String body = "{\"title\": \"\", \"status\": \"TODO\"}";

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
        String body = "{\"title\": \"My Task\", \"status\": \"INVALID\"}";

        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verify(taskService, never()).createTask(any());
    }

    @Test
    void shouldReturn400WhenDescriptionIsTooLong() throws Exception {
        String longDescription = "a".repeat(501);
        String body = "{\"title\": \"My Task\", \"description\": \"" + longDescription + "\"}";

        mockMvc.perform(post("/tasks")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("Description must not exceed 500 characters"));

        verify(taskService, never()).createTask(any());
    }

    // --- PUT /tasks/{id} ---

    @Test
    void shouldUpdateTask() throws Exception {
        Task updatedTask = new Task(1L, "Updated Task");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/tasks/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateTask(eq(1L), any(Task.class));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        Task task = new Task(null, "Task");

        when(taskService.updateTask(eq(99L), any(Task.class)))
                .thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(put("/tasks/99")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(taskService).updateTask(eq(99L), any(Task.class));
    }

    // --- PATCH /tasks/{id} ---

    @Test
    void shouldPatchTask() throws Exception {
        Task patch = new Task(null, null);
        patch.setStatus(TaskStatus.DONE);

        Task patched = new Task(1L, "Existing Title");
        patched.setStatus(TaskStatus.DONE);

        when(taskService.patchTask(eq(1L), any(Task.class))).thenReturn(patched);

        mockMvc.perform(patch("/tasks/1")
                        .contentType("application/json")
                        .content("{\"status\": \"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.title").value("Existing Title"));

        verify(taskService).patchTask(eq(1L), any(Task.class));
    }

    @Test
    void shouldReturn404WhenPatchingNonExistentTask() throws Exception {
        when(taskService.patchTask(eq(99L), any(Task.class)))
                .thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(patch("/tasks/99")
                        .contentType("application/json")
                        .content("{\"status\": \"DONE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(taskService).patchTask(eq(99L), any(Task.class));
    }

    // --- DELETE /tasks/{id} ---

    @Test
    void shouldDeleteTask() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        Long nonExistentId = 99L;
        doThrow(new TaskNotFoundException(nonExistentId)).when(taskService).deleteTask(nonExistentId);

        mockMvc.perform(delete("/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));

        verify(taskService).deleteTask(nonExistentId);
    }
}
