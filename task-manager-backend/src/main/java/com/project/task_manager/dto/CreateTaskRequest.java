package com.project.task_manager.dto;

import com.project.task_manager.model.Task;
import lombok.Data;

import java.util.List;

@Data
public class CreateTaskRequest {
    private Task task;
    private List<String> usernames;
}