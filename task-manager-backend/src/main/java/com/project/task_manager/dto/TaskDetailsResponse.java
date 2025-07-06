package com.project.task_manager.dto;

import com.project.task_manager.model.Task;
import com.project.task_manager.model.User;
import lombok.Data;

import java.util.List;

@Data
public class TaskDetailsResponse {
    Task task;
    List<User> users;
}
