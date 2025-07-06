package com.example.task_manager_mobile.dto;

import java.util.List;

public class TaskDetailsResponse {
    Task task;
    List<User> users;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}