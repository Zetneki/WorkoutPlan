package com.example.workoutplan;
import android.Manifest;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WorkoutPlanAdapter.OnPlanDeleteListener, WorkoutPlanAdapter.OnProgressChangeListener {
    private WorkoutRepository repository;
    private WorkoutPlanAdapter adapter;
    private static final String LOG_TAG = MainActivity.class.getName();
    private FirebaseUser user;
    private TextView emptyStateText;

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

        emptyStateText = findViewById(R.id.empty_state_text);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
            // Initialize with dummy user ID
            repository = new WorkoutRepository(user.getUid());
            setupRecyclerView();
            AlarmScheduler.scheduleAllAlarms(this);
        } else {
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1); // 1 = requestCode
            }
        }

        createNotificationChannel();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<WorkoutPlan> plans = repository.getAllPlans();
        adapter = new WorkoutPlanAdapter(plans, this);
        adapter.setOnProgressChangeListener(this);
        recyclerView.setAdapter(adapter);

        updateEmptyState();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Workout Reminders";
            String description = "Channel for workout reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("WorkoutReminder", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleDailyReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {

                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Realtime frissítés figyelése
        repository.loadWorkoutPlans(new WorkoutRepository.OnPlansLoadedListener() {
            @Override
            public void onPlansLoaded(List<WorkoutPlan> plans) {
                adapter.updateData(new ArrayList<>(plans));
                updateEmptyState();
            }

            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "Error loading plans", e);
                Toast.makeText(MainActivity.this, "Error loading plans", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    public void onAddPlanClicked(View view) {

        Intent intent = new Intent(this, AddWorkoutPlanActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //adapter.updateData(repository.getAllPlans());
        repository.loadWorkoutPlans(new WorkoutRepository.OnPlansLoadedListener() {
            @Override
            public void onPlansLoaded(List<WorkoutPlan> plans) {
                adapter.updateData(new ArrayList<>(plans));
            }

            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "Error loading plans", e);
                Toast.makeText(MainActivity.this, "Error loading plans", Toast.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);            }
        }
    }

    @Override
    public void onPlanDelete(int position) {
        WorkoutPlan planToDelete = repository.getAllPlans().get(position);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Delete Workout Plan")
                .setMessage("Are you sure you want to delete '" + planToDelete.getName() + "' with all its days and exercises?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Törlés megerősítése esetén
                    String planId = planToDelete.getPlanId();
                    repository.deletePlan(planId);

                    // Adapter frissítése
                    repository.loadWorkoutPlans(new WorkoutRepository.OnPlansLoadedListener() {
                        @Override
                        public void onPlansLoaded(List<WorkoutPlan> plans) {
                            adapter.updateData(new ArrayList<>(plans));
                            Toast.makeText(MainActivity.this, "Plan deleted", Toast.LENGTH_SHORT).show();
                            updateEmptyState();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(LOG_TAG, "Error loading plans after deletion", e);
                            Toast.makeText(MainActivity.this, "Error deleting plan", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onProgressChanged(String planId, int progress) {
        // 1. Frissítsd a repository-ban a napok állapotát
        WorkoutPlan plan = repository.getPlanById(planId);
        if (plan != null) {
            plan.setCurrentProgress(progress);

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