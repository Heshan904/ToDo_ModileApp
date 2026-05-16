package com.heshan.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface Listener {
        void onEditTask(String taskId);

        void onDeleteTask(String taskId);
    }

    public static final class TaskItem {
        public final String id;
        public final String title;
        public final String time;
        public final String date;
        public final String description;

        public TaskItem(String id, String title, String time, String date, String description) {
            this.id = id;
            this.title = title;
            this.time = time;
            this.date = date;
            this.description = description;
        }
    }

    private final List<TaskItem> items = new ArrayList<>();
    private final Listener listener;

    public TaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<TaskItem> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskItem item = items.get(position);
        holder.title.setText(item.title != null ? item.title : "");
        String timePart = item.time != null ? item.time : "";
        String datePart = item.date != null ? item.date : "";
        holder.timeDate.setText(timePart + (timePart.isEmpty() || datePart.isEmpty() ? "" : "   ") + datePart);
        holder.description.setText(item.description != null ? item.description : "");

        holder.edit.setOnClickListener(v -> listener.onEditTask(item.id));
        holder.delete.setOnClickListener(v -> listener.onDeleteTask(item.id));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView timeDate;
        final TextView description;
        final ImageButton edit;
        final ImageButton delete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            timeDate = itemView.findViewById(R.id.taskTimeDate);
            description = itemView.findViewById(R.id.taskDescription);
            edit = itemView.findViewById(R.id.btnEditTask);
            delete = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}
