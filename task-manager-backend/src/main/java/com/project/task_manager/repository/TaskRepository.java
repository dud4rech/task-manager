package com.project.task_manager.repository;

import com.project.task_manager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerId(Long ownerId);

    List<Task> findByOwnerUsername(String username);

    @Query("SELECT t FROM Task t WHERE t.owner.username = :username AND t.isActive = true")
    List<Task> findActiveTasksByOwnerUsername(@Param("username") String username);
}
