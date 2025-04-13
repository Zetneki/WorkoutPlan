package com.example.workoutplan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddWorkoutPlan extends AppCompatActivity {

    private EditText etPlanName;
    private EditText etDayName;
    private RecyclerView daysRecyclerView;
    private WorkoutDayAdapter adapter;
    private ArrayList<WorkoutDay> days = new ArrayList<>();
    private String userId;
    private WorkoutRepository repository;
    private boolean isEditMode = false;
    private String planIdToEdit;

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

        // Adapter beállítása
        adapter = new WorkoutDayAdapter(days, this);
        daysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        daysRecyclerView.setAdapter(adapter);

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
        WorkoutPlan planToEdit = repository.getPlanById(planIdToEdit);
        if (planToEdit != null) {
            // Terv nevének beállítása
            etPlanName.setText(planToEdit.getName());

            // Napok betöltése
            days.clear();
            days.addAll(planToEdit.getDays());
            adapter.notifyDataSetChanged();

            // Gomb szövegének módosítása
            Button saveButton = findViewById(R.id.btn_save_plan);
            saveButton.setText(R.string.update_plan);
        }
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

        if (isEditMode) {
            // MEGLÉVŐ TERV FRISSÍTÉSE
            WorkoutPlan existingPlan = repository.getPlanById(planIdToEdit);
            if (existingPlan != null) {
                existingPlan.setName(planName);
                existingPlan.setDays(days);
                repository.updatePlan(existingPlan);
                Toast.makeText(this, "Workoutplan updated", Toast.LENGTH_SHORT).show();
            }
        } else {
            // ÚJ TERV LÉTREHOZÁSA
            WorkoutPlan newPlan = new WorkoutPlan(userId, planName);
            newPlan.setDays(days);
            repository.addPlan(newPlan);
            Toast.makeText(this, "Workoutplan saved", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}