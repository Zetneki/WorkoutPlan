package com.example.workoutplan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;

public class AddWorkoutPlanActivity extends AppCompatActivity {

    private static final String TAG = "AddWorkoutPlan";
    private EditText etPlanName;
    private EditText etDayName;
    private RecyclerView daysRecyclerView;
    private WorkoutDayAdapter adapter;
    private ArrayList<WorkoutDay> days = new ArrayList<>();
    private String userId;
    private WorkoutRepository repository;
    private boolean isEditMode = false;
    private String planIdToEdit;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_workout_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = WorkoutRepository.getInstance(userId);

        etPlanName = findViewById(R.id.et_plan_name);
        etDayName = findViewById(R.id.et_day_name);
        daysRecyclerView = findViewById(R.id.days_recycler);

        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        if (loadingProgressBar == null) {
            Log.e(TAG, "Progress bar not found in layout - please add it");
        }

        adapter = new WorkoutDayAdapter(days, this);
        daysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        daysRecyclerView.setAdapter(adapter);

        adapter.setOnDayActionListener(new WorkoutDayAdapter.OnDayActionListener() {
            @Override
            public void onDayDelete(int position) {
                days.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(AddWorkoutPlanActivity.this, "Day deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDayEdit(int position, WorkoutDay day) {
                showEditDayDialog(position, day);
            }
        });

        findViewById(R.id.btn_add_day).setOnClickListener(v -> addNewDay());
        findViewById(R.id.btn_save_plan).setOnClickListener(v -> savePlan());

        if (getIntent().hasExtra("EDIT_MODE") && getIntent().hasExtra("PLAN_ID")) {
            isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
            planIdToEdit = getIntent().getStringExtra("PLAN_ID");
            loadPlanForEditing();
        }
    }

    private void loadPlanForEditing() {

        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Loading plan with ID: " + planIdToEdit);

        repository.getPlanByIdAsync(planIdToEdit, new WorkoutRepository.OnPlanLoadedListener() {
            @Override
            public void onPlanLoaded(WorkoutPlan planToEdit) {
                Log.d(TAG, "Plan loaded successfully: " + planToEdit.getName());

                etPlanName.setText(planToEdit.getName());

                days.clear();
                days.addAll(planToEdit.getDays());
                adapter.notifyDataSetChanged();

                Button saveButton = findViewById(R.id.btn_save_plan);
                saveButton.setText(R.string.update_plan);

                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading plan for editing", e);
                Toast.makeText(AddWorkoutPlanActivity.this,
                        "Error loading workout plan: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();

                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showEditDayDialog(int position, WorkoutDay day) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_day, null);
        EditText etDayName = dialogView.findViewById(R.id.et_day_name);
        etDayName.setText(day.getDayName());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Edit Day Name")
                .setPositiveButton("Save", (d, which) -> {
                    String newName = etDayName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        day.setDayName(newName);
                        adapter.notifyItemChanged(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void addNewDay() {

        String dayName = etDayName.getText().toString().trim();
        if (dayName.isEmpty()) {
            Toast.makeText(this, "No day name given", Toast.LENGTH_SHORT).show();
            return;
        }
        etDayName.setText("");
        WorkoutDay newDay = new WorkoutDay(dayName);
        days.add(newDay);
        adapter.notifyItemInserted(days.size() - 1);
    }

    private void savePlan() {
        String planName = etPlanName.getText().toString().trim();
        if (planName.isEmpty()) {
            Toast.makeText(this, "No plan name given", Toast.LENGTH_SHORT).show();
            return;
        }
        if (days.isEmpty()) {
            Toast.makeText(this, "Plan is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            Log.d(TAG, "Updating existing plan with ID: " + planIdToEdit);

            WorkoutPlan updatedPlan = new WorkoutPlan(userId, planName);
            updatedPlan.setPlanId(planIdToEdit);
            updatedPlan.setDays(days);

            repository.updatePlan(updatedPlan);
            Toast.makeText(this, "Workoutplan updated", Toast.LENGTH_SHORT).show();
        } else {
            WorkoutPlan newPlan = new WorkoutPlan(userId, planName);
            newPlan.setDays(days);
            newPlan.setCreatedAt(new Date());
            repository.addPlan(newPlan, new WorkoutRepository.OnPlanSavedListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AddWorkoutPlanActivity.this, "Workoutplan saved", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(AddWorkoutPlanActivity.this, "Error saving workout plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        finish();
    }
}