package com.javaproject.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Schema(description = "Task entity representing a to-do item")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the task", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Schema(description = "Title of the task", example = "Implement user authentication", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Detailed description of the task", example = "Add JWT-based auth with Spring Security")
    private String description;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Current status of the task", example = "TODO")
    private TaskStatus status;

    // Constructor vide (obligatoire)
    public Task() {}

    public Task(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Task(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}