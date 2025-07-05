package com.example.task_manager_mobile.ui.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.task_manager_mobile.R;
import com.example.task_manager_mobile.databinding.ActivityCreateTaskBinding;
import com.example.task_manager_mobile.dto.CreateTaskRequest;
import com.example.task_manager_mobile.dto.Task;
import com.example.task_manager_mobile.dto.User;
import com.example.task_manager_mobile.enums.TaskStatus;
import com.example.task_manager_mobile.infrastructure.SessionManager;
import com.example.task_manager_mobile.requests.BaseApiCaller;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CreateTaskActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private ActivityCreateTaskBinding binding;
    private BaseApiCaller baseApiCaller;
    private SessionManager sessionManager;
    private final Calendar calendar = Calendar.getInstance();
    private Task taskToEdit = null;
    public static final String EXTRA_TASK = "task_extra";
    private List<User> allUsersList = new ArrayList<>();
    private List<User> sharedUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        baseApiCaller = new BaseApiCaller();
        sessionManager = new SessionManager(this);

        if (getIntent().hasExtra(EXTRA_TASK)) {
            taskToEdit = (Task) getIntent().getSerializableExtra(EXTRA_TASK);
            populateFieldsForEdit();

            binding.participantsSection.setVisibility(View.VISIBLE);
            loadAllUsersForAutocomplete();
            loadInitialSharedUsers();
        }

        setupClickListeners();
    }

    private void populateFieldsForEdit() {
        binding.createTaskActivityTitle.setText("Editar tarefa");
        binding.etTaskTitle.setText(taskToEdit.getTitle());
        binding.etDescription.setText(taskToEdit.getDescription());
        binding.etDueDate.setText(sdf.format(taskToEdit.getDeadline()));

        calendar.setTime(taskToEdit.getDeadline());

        switch (taskToEdit.getStatus()) {
            case IN_PROGRESS:
                binding.rgStatus.check(R.id.rb_in_progress);
                break;
            case DONE:
                binding.rgStatus.check(R.id.rb_done);
                break;
            case TO_DO:
            default:
                binding.rgStatus.check(R.id.rb_todo);
                break;
        }
    }

    private void loadAllUsersForAutocomplete() {
        baseApiCaller.listAllUsers(sessionManager.getAuthToken(), new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String users) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<User>>() {}.getType();
                allUsersList = gson.fromJson(users, listType);
                List<String> usernames = allUsersList.stream().map(User::getUsername).collect(Collectors.toList());
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateTaskActivity.this,
                            android.R.layout.simple_dropdown_item_1line, usernames);
                    binding.actvParticipants.setAdapter(adapter);
                });
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void loadInitialSharedUsers() {
        String taskId = String.valueOf(taskToEdit.getId());

        baseApiCaller.getSharedUsersForTask(taskId, sessionManager.getAuthToken(), new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String users) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<User>>() {}.getType();
                sharedUsersList = gson.fromJson(users, listType);
                runOnUiThread(() -> {
                    for (User user : sharedUsersList) {
                        addChipForUser(user.getUsername());
                    }
                });
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void addParticipantFromInput() {
        String username = binding.actvParticipants.getText().toString();
        if (username.isEmpty()) return;

        // Verifica se o usuário existe na lista geral e se já não foi adicionado
        boolean userExists = allUsersList.stream().anyMatch(u -> u.getUsername().equals(username));
        boolean alreadyAdded = sharedUsersList.stream().anyMatch(u -> u.getUsername().equals(username));

        if (userExists && !alreadyAdded) {
            // Encontra o objeto User completo para adicionar à lista
            User userToAdd = allUsersList.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
            if (userToAdd != null) {
                sharedUsersList.add(userToAdd);
                addChipForUser(username);
                binding.actvParticipants.setText(""); // Limpa o campo
            }
        } else {
            Toast.makeText(this, "Usuário inválido ou já adicionado", Toast.LENGTH_SHORT).show();
        }
    }

    private void addChipForUser(String username) {
        Chip chip = new Chip(this);
        chip.setText(username);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            binding.chipgroupParticipants.removeView(chip);
            sharedUsersList.removeIf(user -> user.getUsername().equals(username));
        });
        binding.chipgroupParticipants.addView(chip);
    }

    private void setupClickListeners() {
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> validateAndSaveTask());
        binding.etDueDate.setOnClickListener(v -> showDatePickerDialog());
        binding.btnAddParticipant.setOnClickListener(v -> addParticipantFromInput());
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };

        new DatePickerDialog(CreateTaskActivity.this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateInView() {
        binding.etDueDate.setText(sdf.format(calendar.getTime()));
    }

    private void validateAndSaveTask() {
        String title = binding.etTaskTitle.getText().toString().trim();
        String deadline = binding.etDueDate.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String token = sessionManager.getAuthToken();

        if (title.isEmpty()) {
            binding.layoutTaskTitle.setError("O título é obrigatório");
            binding.etTaskTitle.requestFocus();
            return;
        }

        if (deadline.isEmpty()) {
            binding.layoutDueDate.setError("A data de entrega é obrigatória");
            return;
        }

        if (description.isEmpty()) {
            binding.layoutDescription.setError("A descrição é obrigatória");
            binding.etDescription.requestFocus();
            return;
        }

        TaskStatus status = TaskStatus.TO_DO;
        int selectedStatusId = binding.rgStatus.getCheckedRadioButtonId();
        if (selectedStatusId == R.id.rb_in_progress) {
            status = TaskStatus.IN_PROGRESS;
        } else if (selectedStatusId == R.id.rb_done) {
            status = TaskStatus.DONE;
        }

        Task taskData = new Task();
        taskData.setTitle(title);
        taskData.setDescription(description);
        taskData.setStatus(status);

        List<String> usernamesToShare = sharedUsersList.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTask(taskData);
        createTaskRequest.setUsernames(usernamesToShare);

        try {
            taskData.setDeadline(sdf.parse(deadline));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (taskToEdit != null) {
            String taskId = String.valueOf(taskToEdit.getId());
            baseApiCaller.updateTask(taskId, createTaskRequest, token, new BaseApiCaller.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateTaskActivity.this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                }
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(CreateTaskActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show());
                }
            });
        } else {
            baseApiCaller.createTask(createTaskRequest, token, new BaseApiCaller.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateTaskActivity.this, "Tarefa criada com sucesso!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                }
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(CreateTaskActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show());
                }
            });
        }
    }
}