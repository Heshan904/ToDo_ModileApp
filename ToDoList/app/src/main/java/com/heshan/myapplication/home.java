package com.heshan.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class home extends AppCompatActivity implements TaskAdapter.Listener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ValueEventListener tasksListener;
    private TaskAdapter adapter;
    private View emptyHint;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.tasksRecycler);
        emptyHint = findViewById(R.id.emptyTasksHint);
        MaterialCardView addCard = findViewById(R.id.addTaskFabCard);
        addCard.setOnClickListener(v -> startActivity(new Intent(home.this, AddTaskActivity.class)));

        adapter = new TaskAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null) {
                detachTasksListener();
                Intent intent = new Intent(home.this, login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                attachTasksListenerForCurrentUser();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authStateListener != null) {
            mAuth.addAuthStateListener(authStateListener);
        }
        attachTasksListenerForCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
        detachTasksListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FooterBarHelper.setupFooter(this);
    }

    private void attachTasksListenerForCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            detachTasksListener();
            return;
        }
        if (tasksListener != null) {
            return;
        }
        tasksListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<TaskAdapter.TaskItem> list = new ArrayList<>();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    String userId = taskSnapshot.child("userId").getValue(String.class);
                    if (userId != null && userId.equals(user.getUid())) {
                        list.add(new TaskAdapter.TaskItem(
                                taskSnapshot.getKey(),
                                taskSnapshot.child("title").getValue(String.class),
                                taskSnapshot.child("time").getValue(String.class),
                                taskSnapshot.child("date").getValue(String.class),
                                taskSnapshot.child("description").getValue(String.class)
                        ));
                    }
                }
                adapter.submit(list);
                boolean empty = list.isEmpty();
                emptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(home.this,
                        "Could not load tasks: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };
        FirebaseDatabase.getInstance().getReference("tasks").addValueEventListener(tasksListener);
    }

    private void detachTasksListener() {
        if (tasksListener != null) {
            FirebaseDatabase.getInstance().getReference("tasks").removeEventListener(tasksListener);
            tasksListener = null;
        }
    }

    @Override
    public void onEditTask(String taskId) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    @Override
    public void onDeleteTask(String taskId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete task")
                .setMessage("Remove this task from the database?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(taskId)
                .removeValue((error, ref) -> {
                    if (error == null) {
                        Toast.makeText(home.this, "Task deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(home.this,
                                "Could not delete: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }))
                .show();
    }
}
