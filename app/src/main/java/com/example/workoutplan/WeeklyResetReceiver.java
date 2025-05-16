package com.example.workoutplan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingWorkPolicy;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WeeklyResetReceiver extends BroadcastReceiver {
    public static final String ACTION_WEEKLY_RESET = "com.example.workoutplan.ACTION_WEEKLY_RESET";
    private static final String TAG = "WeeklyResetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_WEEKLY_RESET.equals(intent.getAction())) {
            Log.w(TAG, "Received incorrect action: " + intent.getAction());
            return;
        }

        Log.d(TAG, "Weekly reset triggered at: " + System.currentTimeMillis());

        // WorkManager indítása
        OneTimeWorkRequest resetWork = new OneTimeWorkRequest.Builder(ResetProgressWorker.class)
                .setInitialDelay(0, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "weekly_reset",
                        ExistingWorkPolicy.REPLACE,
                        resetWork);
    }
}