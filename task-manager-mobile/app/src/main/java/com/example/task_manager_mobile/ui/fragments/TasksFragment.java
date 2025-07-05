package com.example.task_manager_mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.task_manager_mobile.R;
import com.example.task_manager_mobile.databinding.FragmentTasksBinding;
import com.example.task_manager_mobile.dto.Task;
import com.example.task_manager_mobile.enums.TaskStatus;
import com.example.task_manager_mobile.infrastructure.SessionManager;
import com.example.task_manager_mobile.requests.BaseApiCaller;
import com.example.task_manager_mobile.ui.adapters.TaskAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// package com.example.task_manager_mobile.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
// ... outros imports

import com.example.task_manager_mobile.ui.activities.CreateTaskActivity; // Importe a nova Activity

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private SessionManager sessionManager;
    private BaseApiCaller baseApiCaller;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private TaskStatus currentStatusFilter = null;
    private String currentSearchQuery = "";

    private final ActivityResultLauncher<Intent> createTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadTasks();
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDependencies();
        setupRecyclerView();
        setupFilters();
        loadTasks();

        binding.fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateTaskActivity.class);
            createTaskLauncher.launch(intent);
        });
    }

    private void setupDependencies() {
        sessionManager = new SessionManager(requireContext());
        baseApiCaller = new BaseApiCaller();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(getContext());
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupFilters() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilters();
                return true;
            }
        });

        binding.chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentStatusFilter = null;
            } else if (checkedId == R.id.chip_todo) {
                currentStatusFilter = TaskStatus.TO_DO;
            } else if (checkedId == R.id.chip_in_progress) {
                currentStatusFilter = TaskStatus.IN_PROGRESS;
            } else if (checkedId == R.id.chip_completed) {
                currentStatusFilter = TaskStatus.DONE;
            }
            applyFilters();
        });
    }

    private void loadTasks() {
        String token = sessionManager.getAuthToken();
        if (token == null) return;

        baseApiCaller.listTasks(token, new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String jsonResult) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        Type listType = new TypeToken<ArrayList<Task>>() {}.getType();
                        allTasks = new Gson().fromJson(jsonResult, listType);

                        if (allTasks == null) {
                            allTasks = new ArrayList<>();
                        }
                        applyFilters();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Falha ao ler as tarefas.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void applyFilters() {
        List<Task> filteredList = new ArrayList<>(allTasks);

        // Filtrar por status
        if (currentStatusFilter != null) {
            filteredList = filteredList.stream()
                    .filter(task -> task.getStatus() != null && task.getStatus().equals(currentStatusFilter))
                    .collect(Collectors.toList());
        }

        // Filtrar por texto
        if (!currentSearchQuery.isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(task -> task.getTitle() != null && task.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        taskAdapter.submitList(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}