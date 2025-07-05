package com.example.task_manager_mobile.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task_manager_mobile.R;
import com.example.task_manager_mobile.dto.Task;
import com.example.task_manager_mobile.ui.activities.TaskDetailActivity;
import com.example.task_manager_mobile.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private List<Task> taskList = new ArrayList<>();
    private Context context;

    public TaskAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.tvDueDate.setText("Data: " + sdf.format(task.getDeadline()));
        holder.tvStatus.setText(Utils.generateStatusTextFromStatus(task.getStatus()));

        switch (task.getStatus()) {
            case TO_DO:
                holder.tvStatus.setBackgroundResource(R.drawable.badge_todo);
                break;
            case IN_PROGRESS:
                holder.tvStatus.setBackgroundResource(R.drawable.badge_in_progress);
                break;
            case DONE:
                holder.tvStatus.setBackgroundResource(R.drawable.badge_done);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void submitList(List<Task> newTasks) {
        this.taskList.clear();
        this.taskList.addAll(newTasks);
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvDueDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDescription = itemView.findViewById(R.id.tv_task_description);
            tvStatus = itemView.findViewById(R.id.tv_task_status);
            tvDueDate = itemView.findViewById(R.id.tv_task_due_date);
        }
    }
}