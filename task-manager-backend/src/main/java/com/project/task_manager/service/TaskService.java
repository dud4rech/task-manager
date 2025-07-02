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

    public Optional<Task> findAccessibleTaskById(Long taskId, String email) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (taskOptional.isEmpty()) return Optional.empty();

        Task task = taskOptional.get();

        if (task.getOwner().getEmail().equals(email)) {
            return Optional.of(task);
        }

        boolean isShared = taskSharedRepository.existsByTaskAndSharedWithEmail(task, email);
        if (isShared) {
            return Optional.of(task);
        }

        return Optional.empty();
    }

    public List<Task> findAllAccessibleTasks(String email) {
        List<Task> ownedTasks = taskRepository.findByOwnerEmail(email);
        List<Task> sharedTasks = taskSharedRepository.findSharedTasksByEmail(email);

        Set<Task> allTasks = new HashSet<>(ownedTasks);
        allTasks.addAll(sharedTasks);

        return new ArrayList<>(allTasks);
    }

    public Task save(String email, Task task) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        task.setOwner(user);
        task.setIsActive(true);
        return taskRepository.save(task);
    }

    public Task update(String email, Long taskId, Task task) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if (!existingTask.getOwner().getEmail().equals(email)) {
            throw new SecurityException("Você não está autorizado a atualizar esta tarefa.");
        }

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setDeadline(task.getDeadline());

        return taskRepository.save(existingTask);
    }

    public void softDelete(String email, Long taskId) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if (!existingTask.getOwner().getEmail().equals(email)) {
            throw new SecurityException("Você não está autorizado a deletar esta tarefa.");
        }

        existingTask.setIsActive(false);
        taskRepository.save(existingTask);
    }

    public void shareTaskWithUsers(Long taskId, List<String> emails, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário responsável não encontrado."));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada."));

        if (!task.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("Você não está autorizado a compartilhar esta tarefa.");
        }

        for (String email : emails) {
            if (email.equalsIgnoreCase(ownerEmail)) {
                continue;
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário com email " + email + " não encontrado."));

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
