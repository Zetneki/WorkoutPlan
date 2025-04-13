package com.example.workoutplan;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements WorkoutPlanAdapter.OnPlanDeleteListener, WorkoutPlanAdapter.OnProgressChangeListener {
    private WorkoutRepository repository;
    private WorkoutPlanAdapter adapter;
    private static final String LOG_TAG = MainActivity.class.getName();
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        // Initialize with dummy user ID
        repository = WorkoutRepository.getInstance(user.getUid());

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<WorkoutPlan> plans = repository.getAllPlans();
        adapter = new WorkoutPlanAdapter(plans, this);
        adapter.setOnProgressChangeListener(this);
        recyclerView.setAdapter(adapter);
    }

    public void onAddPlanClicked(View view) {
        /*
        WorkoutPlan newPlan = new WorkoutPlan(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                "New Plan"
        );

        repository.addPlan(newPlan);
        adapter.updateData(repository.getAllPlans());
        Toast.makeText(this, "Plan added!", Toast.LENGTH_SHORT).show();
        */

        Intent intent = new Intent(this, AddWorkoutPlan.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.updateData(repository.getAllPlans());
    }

    @Override
    public void onPlanDelete(int position) {
        String planId = repository.getAllPlans().get(position).getPlanId();
        repository.deletePlan(planId);
        adapter.updateData(repository.getAllPlans());
    }

    @Override
    public void onProgressChanged(String planId, int progress) {
        // 1. Frissítsd a repository-ban a napok állapotát
        WorkoutPlan plan = repository.getPlanById(planId);
        if (plan != null) {

            // Állítsd be a napok completed állapotát a progress alapján
            for (int i = 0; i < plan.getDays().size(); i++) {
                plan.getDays().get(i).setCompleted(i < progress);
            }

            // 2. Mentés a repository-ba
            repository.updatePlan(plan);

            // 3. Értesítsd az adaptert a változásról
            adapter.updateData(repository.getAllPlans());
        }
    }

    @Override
    public void onWorkoutCompleted(String planId) {
        // Extra logika teljesítéskor
        Toast.makeText(this, repository.getPlanById(planId).getName() + " completed!", Toast.LENGTH_SHORT).show();
    }
}