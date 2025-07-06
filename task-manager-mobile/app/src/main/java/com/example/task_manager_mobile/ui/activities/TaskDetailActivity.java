package com.example.task_manager_mobile.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.task_manager_mobile.R;
import com.example.task_manager_mobile.databinding.ActivityTaskDetailBinding;
import com.example.task_manager_mobile.dto.Task;
import com.example.task_manager_mobile.dto.TaskDetailsResponse;
import com.example.task_manager_mobile.dto.User;
import com.example.task_manager_mobile.infrastructure.SessionManager;
import com.example.task_manager_mobile.requests.BaseApiCaller;
import com.example.task_manager_mobile.utils.Utils;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    public static final String EXTRA_TASK_ID = "task_id_extra";
    private ActivityTaskDetailBinding binding;
    private BaseApiCaller baseApiCaller;
    private SessionManager sessionManager;
    private long taskId;
    private TaskDetailsResponse currentTask;

    private final ActivityResultLauncher<Intent> editTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "Tarefa atualizada.", Toast.LENGTH_SHORT).show();
                    loadTaskDetails();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        baseApiCaller = new BaseApiCaller();
        sessionManager = new SessionManager(this);

        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);

        if (taskId == -1) {
            Toast.makeText(this, "ID da tarefa não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClickListeners();
        loadTaskDetails();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEdit.setOnClickListener(v -> {
            if (currentTask != null) {
                Intent intent = new Intent(TaskDetailActivity.this, CreateTaskActivity.class);
                intent.putExtra(CreateTaskActivity.EXTRA_TASK, currentTask.getTask());
                editTaskLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Aguarde os detalhes da tarefa carregarem.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void addChipForUser(String username) {
        Chip chip = new Chip(this);
        chip.setText(username);
        chip.setOnCloseIconClickListener(v -> {
            binding.chipgroupParticipants.removeView(chip);
            currentTask.getUsers().removeIf(user -> user.getUsername().equals(username));
        });
        binding.chipgroupParticipants.addView(chip);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Você tem certeza que deseja excluir esta tarefa? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    performTaskDeletion();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performTaskDeletion() {
        showLoading(true);
        String token = sessionManager.getAuthToken();
        baseApiCaller.deleteTask(String.valueOf(taskId), token, new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    Toast.makeText(TaskDetailActivity.this, "Tarefa excluída com sucesso.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(TaskDetailActivity.this, "Erro ao excluir: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadTaskDetails() {
        showLoading(true);
        String token = sessionManager.getAuthToken();
        baseApiCaller.getTaskById(String.valueOf(taskId), token, new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                currentTask = gson.fromJson(result, TaskDetailsResponse.class);
                runOnUiThread(() -> {
                    populateUI(currentTask.getTask());

                    binding.chipgroupParticipants.removeAllViews();

                    for (User user : currentTask.getUsers()) {
                        addChipForUser(user.getUsername());
                    }

                    showLoading(false);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(TaskDetailActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void populateUI(Task task) {
        binding.tvDetailTitle.setText(task.getTitle());
        binding.tvDetailDescription.setText(task.getDescription());
        binding.tvDetailDueDate.setText(sdf.format(task.getDeadline()));

        if (task.getStatus() != null) {
            binding.tvDetailStatus.setText(Utils.generateStatusTextFromStatus(task.getStatus()));
            switch (task.getStatus()) {
                case TO_DO:
                    binding.tvDetailStatus.setBackgroundResource(R.drawable.badge_todo);
                    break;
                case IN_PROGRESS:
                    binding.tvDetailStatus.setBackgroundResource(R.drawable.badge_in_progress);
                    break;
                case DONE:
                    binding.tvDetailStatus.setBackgroundResource(R.drawable.badge_done);
                    break;
            }
        }
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.mainContent.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }
}