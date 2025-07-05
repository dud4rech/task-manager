package com.project.task_manager.repository;

import com.project.task_manager.model.Task;
import com.project.task_manager.model.TaskShared;
import com.project.task_manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskSharedRepository extends JpaRepository<TaskShared, Long> {
    boolean existsByTaskAndSharedWith(Task task, User user);

    boolean existsByTaskAndSharedWithUsername(Task task, String username);

    @Query("SELECT ts.task FROM TaskShared ts WHERE ts.sharedWith.username = :username")
    List<Task> findSharedTasksByUsername(@Param("username") String username);

    @Query("SELECT ts.task FROM TaskShared ts WHERE ts.sharedWith.username = :username AND ts.task.isActive = true")
    List<Task> findActiveSharedTasksByUsername(@Param("username") String username);

    @Query("SELECT ts.sharedWith FROM TaskShared ts WHERE ts.task.id = :taskId")
    List<User> findUsersByTaskId(@Param("taskId") Long taskId);

    void deleteAllByTaskAndSharedWithIn(Task task, List<User> sharedWith);
}
