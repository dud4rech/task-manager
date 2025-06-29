package com.project.task_manager.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShareTaskRequest {
    private List<String> emails;
}
