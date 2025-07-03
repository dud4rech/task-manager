package com.example.task_manager_mobile.dto;

import java.util.List;

public class ShareTaskRequest {
    private List<String> usernames;

    public ShareTaskRequest(List<String> usernames) {
        this.usernames = usernames;
    }
}