package com.project.task_manager.controller;

import com.project.task_manager.dto.ShareTaskRequest;
import com.project.task_manager.model.Task;
import com.project.task_manager.model.User;
import com.project.task_manager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<?> listAllAccessibleTasks(@AuthenticationPrincipal UserDetails userDetails) {
        List<Task> tasks = taskService.findAllAccessibleTasks(userDetails.getUsername());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> listAccessibleTaskById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Optional<Task> task = taskService.findAccessibleTaskById(id, userDetails.getUsername());

        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Tarefa não encontrada."));
        }
    }

    @GetMapping("/{taskId}/shared-users")
    public ResponseEntity<?> getUsersByTaskId(@PathVariable Long taskId) {
        List<User> users = taskService.findUsersByTaskId(taskId);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Task task) {
        Task created = taskService.save(userDetails.getUsername(), task);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody Task task) {
        try {
            Task updatedTask = taskService.update(userDetails.getUsername(), id, task);
            return ResponseEntity.ok(updatedTask);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não está autorizado a atualizar este usuário.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tarefa não encontrada.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            taskService.softDelete(userDetails.getUsername(), id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não está autorizado a deletar este usuário.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tarefa não encontrada.");
        }
    }

    @PostMapping("/{taskId}/share")
    public ResponseEntity<?> shareTask (
            @PathVariable Long taskId,
            @RequestBody ShareTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            taskService.shareTaskWithUsers(taskId, request.getUsernames(), userDetails.getUsername());
            return ResponseEntity.ok("Tarefa compartilhada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
