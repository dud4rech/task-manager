package com.project.task_manager.controller;

import com.project.task_manager.dto.ShareTaskRequest;
import com.project.task_manager.model.Task;
import com.project.task_manager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<Task>> listAllAccessibleTasks(@AuthenticationPrincipal UserDetails userDetails) {
        List<Task> tasks = taskService.findAllAccessibleTasks(userDetails.getUsername());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> listAccessibleTaskById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        try {
            taskService.findAccessibleTaskById(id, userDetails.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(403).body("User not authorized.");
        }
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Task task) {
        Task created = taskService.save(userDetails.getUsername(), task);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody Task task) {
        Task updatedTask = taskService.update(userDetails.getUsername(), id, task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        taskService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/share")
    public ResponseEntity<?> shareTask (
            @PathVariable Long taskId,
            @RequestBody ShareTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            taskService.shareTaskWithUsers(taskId, request.getEmails(), userDetails.getUsername());
            return ResponseEntity.ok("Task shared successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
