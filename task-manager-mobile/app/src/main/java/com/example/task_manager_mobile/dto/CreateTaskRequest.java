package com.example.task_manager_mobile.dto;

import java.io.Serializable;
import java.util.List;

public class CreateTaskRequest implements Serializable {
    private Task task;
    private List<String> usernames;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}