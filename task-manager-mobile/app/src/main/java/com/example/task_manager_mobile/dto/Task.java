package com.example.task_manager_mobile.dto;

import com.example.task_manager_mobile.enums.TaskStatus;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Date deadline;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
}