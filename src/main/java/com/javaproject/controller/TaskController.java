package com.javaproject.controller;

import com.javaproject.dto.ErrorResponse;
import com.javaproject.model.Task;
import com.javaproject.model.TaskStatus;
import com.javaproject.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Task management operations")
public class TaskController {


    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Get all tasks", description = "Returns a paginated list of tasks, optionally filtered by status")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    @GetMapping
    public Page<Task> getTasks(
            @Parameter(description = "Filter by task status") @RequestParam(required = false) TaskStatus status,
            @Parameter(hidden = true) Pageable pageable) {
        return taskService.getAllTasks(status, pageable);
    }

    @Operation(summary = "Get task by ID", description = "Returns a single task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@Parameter(description = "Task ID") @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(summary = "Create a new task", description = "Creates a task with the provided title, description, and status")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task));
    }

    @Operation(summary = "Update a task", description = "Fully replaces an existing task with the provided data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@Parameter(description = "Task ID") @PathVariable Long id,
                                           @Valid @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @Operation(summary = "Partially update a task", description = "Updates only the provided fields of an existing task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task patched successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Task> patchTask(@Parameter(description = "Task ID") @PathVariable Long id,
                                          @RequestBody Task task) {
        return ResponseEntity.ok(taskService.patchTask(id, task));
    }

    @Operation(summary = "Delete a task", description = "Deletes a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@Parameter(description = "Task ID") @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}