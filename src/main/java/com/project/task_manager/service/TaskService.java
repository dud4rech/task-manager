package com.project.task_manager.service;

import com.project.task_manager.model.Task;
import com.project.task_manager.model.TaskShared;
import com.project.task_manager.model.User;
import com.project.task_manager.repository.TaskRepository;
import com.project.task_manager.repository.TaskSharedRepository;
import com.project.task_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskSharedRepository taskSharedRepository;

    public Optional<Task> findAccessibleTaskById(Long taskId, String username) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (taskOptional.isEmpty()) return Optional.empty();

        Task task = taskOptional.get();

        if (task.getOwner().getUsername().equals(username)) {
            return Optional.of(task);
        }

        boolean isShared = taskSharedRepository.existsByTaskAndSharedWithUsername(task, username);
        if (isShared) {
            return Optional.of(task);
        }

        return Optional.empty();
    }

    public List<Task> findAllAccessibleTasks(String username) {
        List<Task> ownedTasks = taskRepository.findByOwnerUsername(username);
        List<Task> sharedTasks = taskSharedRepository.findSharedTasksByUsername(username);

        Set<Task> allTasks = new HashSet<>(ownedTasks);
        allTasks.addAll(sharedTasks);

        return new ArrayList<>(allTasks);
    }

    public Task save(String username, Task task) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        task.setOwner(user);
        task.setIsActive(true);
        return taskRepository.save(task);
    }

    public Task update(String username, Long taskId, Task task) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found."));

        if (!existingTask.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Not authorized to update this task.");
        }

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setDeadline(task.getDeadline());

        return taskRepository.save(existingTask);
    }

    public void softDelete(String username, Long taskId) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found."));

        if (!existingTask.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Not authorized to delete this task.");
        }

        existingTask.setIsActive(false);
        taskRepository.save(existingTask);
    }

    public void shareTaskWithUsers(Long taskId, List<String> emails, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found."));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found."));

        if (!task.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("You are not the owner of this task.");
        }

        for (String email : emails) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " not found."));

            boolean alreadyShared = taskSharedRepository.existsByTaskAndSharedWith(task, user);
            if (!alreadyShared) {
                TaskShared shared = new TaskShared();
                shared.setTask(task);
                shared.setSharedWith(user);
                taskSharedRepository.save(shared);
            }
        }
    }
}
