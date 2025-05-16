package com.example.workoutplan;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static void showWorkoutReminder(Context context) {
        // Check permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No notification permission");
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "WorkoutReminder")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Workout Reminder")
                .setContentText("Time to move your body a bit today!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, builder.build());
            Log.d(TAG, "Notification displayed successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission revoked", e);
        }
    }
}