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
import java.util.stream.Collectors;

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

    public List<User> findUsersByTaskId(Long taskId) {
        return taskSharedRepository.findUsersByTaskId(taskId);
    }

    public List<Task> findAllAccessibleTasks(String username) {
        List<Task> ownedTasks = taskRepository.findActiveTasksByOwnerUsername(username);
        List<Task> sharedTasks = taskSharedRepository.findActiveSharedTasksByUsername(username);

        Set<Task> allTasks = new HashSet<>(ownedTasks);
        allTasks.addAll(sharedTasks);

        return new ArrayList<>(allTasks);
    }

    public Task save(String username, Task task) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        task.setOwner(user);
        task.setIsActive(true);
        return taskRepository.save(task);
    }

    public Task update(String username, Long taskId, Task task) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if (!existingTask.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Você não está autorizado a atualizar esta tarefa.");
        }

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setDeadline(task.getDeadline());

        return taskRepository.save(existingTask);
    }

    public void softDelete(String username, Long taskId) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if (!existingTask.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Você não está autorizado a deletar esta tarefa.");
        }

        existingTask.setIsActive(false);
        taskRepository.save(existingTask);
    }

    public void shareTaskWithUsers(Long taskId, List<String> newUsernameList, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário responsável não encontrado."));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada."));

        if (!task.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("Você não está autorizado a compartilhar esta tarefa.");
        }

        List<User> currentlySharedUsers = taskSharedRepository.findUsersByTaskId(taskId);
        Set<String> currentSharedUsernames = currentlySharedUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

        Set<String> newUsernames = new HashSet<>(newUsernameList);

        List<User> usersToRemove = currentlySharedUsers.stream()
                .filter(user -> !newUsernames.contains(user.getUsername()))
                .toList();

        if (!usersToRemove.isEmpty()) {
            taskSharedRepository.deleteAllByTaskAndSharedWithIn(task, usersToRemove);
        }

        for (String usernameToAdd : newUsernames) {
            if (currentSharedUsernames.contains(usernameToAdd) || usernameToAdd.equalsIgnoreCase(ownerUsername)) {
                continue;
            }

            User userToShareWith = userRepository.findByUsername(usernameToAdd)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário com username " + usernameToAdd + " não encontrado."));

            TaskShared shared = new TaskShared();
            shared.setTask(task);
            shared.setSharedWith(userToShareWith);
            taskSharedRepository.save(shared);
        }
    }
}