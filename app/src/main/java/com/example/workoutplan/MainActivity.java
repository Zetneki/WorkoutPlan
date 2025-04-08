package com.example.workoutplan;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {
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
        String dummyUserId = "test_user_1";
        repository = new WorkoutRepository(dummyUserId);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<WorkoutPlan> plans = repository.getAllPlans();
        adapter = new WorkoutPlanAdapter(plans);
        recyclerView.setAdapter(adapter);
    }

    public void onAddPlanClicked(View view) {
        WorkoutPlan newPlan = new WorkoutPlan("test_user_1", "New Plan " + System.currentTimeMillis());
        repository.addPlan(newPlan);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Plan added!", Toast.LENGTH_SHORT).show();
    }
}