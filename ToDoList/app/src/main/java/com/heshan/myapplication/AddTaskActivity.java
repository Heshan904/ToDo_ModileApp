package com.heshan.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "taskId";

    private TextInputEditText inputTitle;
    private TextInputEditText inputDescription;
    private Spinner spinnerDay;
    private Spinner spinnerMonth;
    private EditText inputHour;
    private EditText inputMinute;
    private MaterialButton buttonAm;
    private MaterialButton buttonPm;
    private MaterialButton buttonSave;
    private MaterialButton buttonCancel;
    private ImageView btnBack;

    private String selectedPeriod = "AM";
    private String editingTaskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        bindViews();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to manage tasks", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupSpinners();
        setupTimeToggle();
        editingTaskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        if (editingTaskId != null) {
            loadExistingTask(editingTaskId);
        } else {
            setPeriod("AM");
        }
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FooterBarHelper.setupFooter(this);
    }

    private void bindViews() {
        inputTitle = findViewById(R.id.inputTitle);
        inputDescription = findViewById(R.id.inputDescription);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        inputHour = findViewById(R.id.inputHour);
        inputMinute = findViewById(R.id.inputMinute);
        buttonAm = findViewById(R.id.buttonAm);
        buttonPm = findViewById(R.id.buttonPm);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSpinners() {
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);
    }

    private void setupTimeToggle() {
        buttonAm.setOnClickListener(v -> setPeriod("AM"));
        buttonPm.setOnClickListener(v -> setPeriod("PM"));
    }

    private void setPeriod(String period) {
        selectedPeriod = period;
        ColorStateList selectedTint = ColorStateList.valueOf(Color.parseColor("#A5D6A7"));
        ColorStateList unselectedTint = ColorStateList.valueOf(Color.parseColor("#E0E0E0"));
        int selectedText = Color.parseColor("#1B5E20");
        int unselectedText = Color.parseColor("#424242");

        if ("AM".equals(period)) {
            buttonAm.setBackgroundTintList(selectedTint);
            buttonAm.setTextColor(selectedText);
            buttonPm.setBackgroundTintList(unselectedTint);
            buttonPm.setTextColor(unselectedText);
        } else {
            buttonPm.setBackgroundTintList(selectedTint);
            buttonPm.setTextColor(selectedText);
            buttonAm.setBackgroundTintList(unselectedTint);
            buttonAm.setTextColor(unselectedText);
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        buttonSave.setOnClickListener(v -> saveTask());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void loadExistingTask(String taskId) {
        FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(taskId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(AddTaskActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String ownerId = snapshot.child("userId").getValue(String.class);
                        if (user == null || ownerId == null || !ownerId.equals(user.getUid())) {
                            Toast.makeText(AddTaskActivity.this, "You cannot edit this task", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (inputTitle != null) {
                            String title = snapshot.child("title").getValue(String.class);
                            inputTitle.setText(title != null ? title : "");
                        }
                        if (inputDescription != null) {
                            String description = snapshot.child("description").getValue(String.class);
                            inputDescription.setText(description != null ? description : "");
                        }

                        String date = snapshot.child("date").getValue(String.class);
                        if (!TextUtils.isEmpty(date)) {
                            String[] dateParts = date.trim().split("\\s+");
                            if (dateParts.length >= 2) {
                                List<String> days = new ArrayList<>();
                                for (int i = 1; i <= 31; i++) {
                                    days.add(String.valueOf(i));
                                }
                                List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
                                String first = dateParts[0];
                                String second = dateParts[1];
                                String dayValue = first;
                                String monthValue = second;
                                if (!first.matches("\\d+")) {
                                    monthValue = first;
                                    dayValue = "1";
                                }
                                if (dayValue.startsWith("0") && dayValue.length() > 1) {
                                    dayValue = dayValue.replaceFirst("^0+", "");
                                }
                                int dayIndex = days.indexOf(dayValue);
                                if (dayIndex >= 0) {
                                    spinnerDay.setSelection(dayIndex);
                                }
                                int monthIndex = months.indexOf(monthValue);
                                if (monthIndex >= 0) {
                                    spinnerMonth.setSelection(monthIndex);
                                }
                            }
                        }

                        String time = snapshot.child("time").getValue(String.class);
                        if (!TextUtils.isEmpty(time)) {
                            String[] tokens = time.trim().split("\\s+");
                            if (tokens.length >= 2) {
                                String period = tokens[tokens.length - 1].toUpperCase();
                                String hm = tokens[0];
                                if (hm.contains(":")) {
                                    String[] hmParts = hm.split(":");
                                    if (hmParts.length >= 2) {
                                        inputHour.setText(hmParts[0]);
                                        inputMinute.setText(hmParts[1]);
                                    }
                                }
                                if ("PM".equals(period)) {
                                    setPeriod("PM");
                                } else {
                                    setPeriod("AM");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AddTaskActivity.this, "Unable to load task: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveTask() {
        String title = inputTitle.getText() != null ? inputTitle.getText().toString().trim() : "";
        String description = inputDescription.getText() != null ? inputDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String day = spinnerDay.getSelectedItem().toString();
        String month = spinnerMonth.getSelectedItem().toString();
        String hour = inputHour.getText() != null ? inputHour.getText().toString().trim() : "";
        String minute = inputMinute.getText() != null ? inputMinute.getText().toString().trim() : "";
        String time = formatTime(hour, minute, selectedPeriod);
        String date = day + " " + month;

        Map<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("description", description);
        task.put("date", date);
        task.put("time", time);
        task.put("userId", user.getUid());

        if (editingTaskId != null) {
            FirebaseDatabase.getInstance()
                    .getReference("tasks")
                    .child(editingTaskId)
                    .updateChildren(task, (error, ref) -> {
                        if (error == null) {
                            Toast.makeText(AddTaskActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                            navigateHomeAndFinish();
                        } else {
                            Toast.makeText(AddTaskActivity.this,
                                    "Unable to update: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            String taskId = FirebaseDatabase.getInstance().getReference("tasks").push().getKey();
            if (taskId != null) {
                FirebaseDatabase.getInstance()
                        .getReference("tasks")
                        .child(taskId)
                        .setValue(task, (error, ref) -> {
                            if (error == null) {
                                Toast.makeText(AddTaskActivity.this, "Task saved to database", Toast.LENGTH_SHORT).show();
                                navigateHomeAndFinish();
                            } else {
                                Toast.makeText(AddTaskActivity.this,
                                        "Unable to save: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }
    }

    private void navigateHomeAndFinish() {
        Intent intent = new Intent(AddTaskActivity.this, home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String formatTime(String hour, String minute, String period) {
        String normalizedHour = TextUtils.isEmpty(hour) ? "08" : hour;
        String normalizedMinute = TextUtils.isEmpty(minute) ? "00" : minute;

        if (normalizedHour.length() == 1) {
            normalizedHour = "0" + normalizedHour;
        }
        if (normalizedMinute.length() == 1) {
            normalizedMinute = "0" + normalizedMinute;
        }

        return normalizedHour + ":" + normalizedMinute + " " + period;
    }
}
