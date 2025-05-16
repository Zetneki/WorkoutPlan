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

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder receiver triggered");

        // Check if we should actually show the notification
        /*
        if (!shouldShowNotification(context)) {
            Log.d(TAG, "Skipping notification - already shown today");
            return;
        }*/

        // Mark that we've shown notification today
        markNotificationShown(context);

        // Show notification
        NotificationHelper.showWorkoutReminder(context);
    }

    private boolean shouldShowNotification(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE);
        long lastNotificationTime = prefs.getLong("lastNotificationTime", 0);

        // Check if notification was already shown today
        Calendar lastNotif = Calendar.getInstance();
        lastNotif.setTimeInMillis(lastNotificationTime);

        Calendar now = Calendar.getInstance();

        return lastNotif.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR) ||
                lastNotif.get(Calendar.YEAR) != now.get(Calendar.YEAR);
    }

    private void markNotificationShown(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE);
        prefs.edit().putLong("lastNotificationTime", System.currentTimeMillis()).apply();
    }
}