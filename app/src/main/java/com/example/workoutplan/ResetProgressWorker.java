package com.example.workoutplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.workoutplan.WorkoutPlan;
import com.example.workoutplan.WorkoutRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ResetProgressWorker extends Worker {

    public ResetProgressWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE);
            prefs.edit().putLong("lastResetTime", System.currentTimeMillis()).apply();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

            if (userId == null) {
                return Result.failure();
            }

            WorkoutRepository repo = new WorkoutRepository(userId);

            final CountDownLatch latch = new CountDownLatch(1);
            final Result[] workResult = {Result.failure()};

            repo.loadWorkoutPlans(new WorkoutRepository.OnPlansLoadedListener() {
                @Override
                public void onPlansLoaded(List<WorkoutPlan> plans) {
                    try {

                        for (WorkoutPlan plan : plans) {
                            if (plan.getDays().size() == plan.getCurrentProgress()) {
                                plan.setCurrentProgress(0);

                                repo.updatePlan(plan);
                            }
                        }
                        workResult[0] = Result.success();
                    } catch (Exception e) {
                        workResult[0] = Result.failure();
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onError(Exception e) {
                    workResult[0] = Result.failure();
                    latch.countDown();
                }
            });

            latch.await(30, TimeUnit.SECONDS);
            return workResult[0];

        } catch (Exception e) {
            return Result.failure();
        }
    }
}