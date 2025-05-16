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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_workout_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = WorkoutRepository.getInstance(userId);

        // Views inicializálása
        etPlanName = findViewById(R.id.et_plan_name);
        etDayName = findViewById(R.id.et_day_name);
        daysRecyclerView = findViewById(R.id.days_recycler);

        // Add loading progress bar - make sure to add this in your layout file
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        if (loadingProgressBar == null) {
            Log.e(TAG, "Progress bar not found in layout - please add it");
        }

        // Adapter beállítása
        adapter = new WorkoutDayAdapter(days, this);
        daysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        daysRecyclerView.setAdapter(adapter);

        // Nap törlés/szerkesztés kezelése
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



        // Gombok kezelése
        findViewById(R.id.btn_add_day).setOnClickListener(v -> addNewDay());
        findViewById(R.id.btn_save_plan).setOnClickListener(v -> savePlan());

        if (getIntent().hasExtra("EDIT_MODE") && getIntent().hasExtra("PLAN_ID")) {
            isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
            planIdToEdit = getIntent().getStringExtra("PLAN_ID");
            loadPlanForEditing();
        }
    }

    private void loadPlanForEditing() {
        // Show loading indicator
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Loading plan with ID: " + planIdToEdit);

        // Use the asynchronous method to load the plan from Firestore
        repository.getPlanByIdAsync(planIdToEdit, new WorkoutRepository.OnPlanLoadedListener() {
            @Override
            public void onPlanLoaded(WorkoutPlan planToEdit) {
                Log.d(TAG, "Plan loaded successfully: " + planToEdit.getName());

                // Terv nevének beállítása
                etPlanName.setText(planToEdit.getName());

                // Napok betöltése
                days.clear();
                days.addAll(planToEdit.getDays());
                adapter.notifyDataSetChanged();

                // Gomb szövegének módosítása
                Button saveButton = findViewById(R.id.btn_save_plan);
                saveButton.setText(R.string.update_plan);

                // Hide loading indicator
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

                // Hide loading indicator
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    // Új metódus a nap szerkesztéséhez
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
        // Új nap hozzáadása pár alap gyakorlattal
        String dayName = etDayName.getText().toString().trim();
        if (dayName.isEmpty()) {
            Toast.makeText(this, "No day name given", Toast.LENGTH_SHORT).show();
            return;
        }
        etDayName.setText("");
        WorkoutDay newDay = new WorkoutDay(dayName);
        //newDay.getExercises().add(new Exercise("New exercise", "3x10"));
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
            // MEGLÉVŐ TERV FRISSÍTÉSE
            Log.d(TAG, "Updating existing plan with ID: " + planIdToEdit);

            // Create a new plan object with the edited data
            WorkoutPlan updatedPlan = new WorkoutPlan(userId, planName);
            updatedPlan.setPlanId(planIdToEdit);
            updatedPlan.setDays(days);

            // Update in Firestore
            repository.updatePlan(updatedPlan);
            Toast.makeText(this, "Workoutplan updated", Toast.LENGTH_SHORT).show();
        } else {
            // ÚJ TERV LÉTREHOZÁSA
            WorkoutPlan newPlan = new WorkoutPlan(userId, planName);
            newPlan.setDays(days);
            newPlan.setCreatedAt(new Date());
            repository.addPlan(newPlan, new WorkoutRepository.OnPlanSavedListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AddWorkoutPlanActivity.this, "Workoutplan saved", Toast.LENGTH_SHORT).show();
                    finish();  // Terv mentése után visszatérés
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