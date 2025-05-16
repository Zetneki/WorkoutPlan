package com.example.workoutplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class AlarmScheduler {
    private static final String TAG = "AlarmScheduler";

    public static void scheduleAllAlarms(Context context) {
        // 1. Töröljük a meglévő alarmokat
        cancelReminder(context);
        cancelWeeklyReset(context);

        // 2. Beállítjuk az újakat
        scheduleDailyReminder(context);
        scheduleWeeklyReset(context);

        // 3. Logoljuk az állapotot
        logAlarmStatus(context);
    }

    private static void logAlarmStatus(Context context) {
        boolean dailySet = checkPendingIntent(context, 0);
        boolean weeklySet = checkPendingIntent(context, 1);

        Log.d(TAG, "Daily alarm set: " + dailySet);
        Log.d(TAG, "Weekly alarm set: " + weeklySet);
    }

    public static boolean checkPendingIntent(Context context, int requestCode) {
        Intent intent;
        if (requestCode == 0) {
            intent = new Intent(context, ReminderReceiver.class);
        } else {
            intent = new Intent(WeeklyResetReceiver.ACTION_WEEKLY_RESET);
            intent.setClass(context, WeeklyResetReceiver.class);
        }

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;
    }

    /**
     * Schedule daily reminder at specified hour
     */
    public static void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Check permission on Android S and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - missing permission");
                return;
            }
        }

        // Cancel any existing alarms to prevent duplicates
        cancelReminder(context);

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set alarm for 18:00 daily
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If it's already past 18:00, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Log.d(TAG, "Scheduling daily reminder for: " + calendar.getTime());

        // Use best method based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }
    }

    /**
     * Schedule weekly reset at specified day and time
     */
    public static void scheduleWeeklyReset(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Check permission on Android S and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - missing permission");
                return;
            }
        }

        // Cancel any existing reset alarms to prevent duplicates
        cancelWeeklyReset(context);

        Intent intent = new Intent(context, WeeklyResetReceiver.class);
        intent.setAction(WeeklyResetReceiver.ACTION_WEEKLY_RESET);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set alarm for Thursday at 20:00
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If we've already passed this time this week, move to next week
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        Log.d(TAG, "Scheduling weekly reset for: " + calendar.getTime());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }
    }

    /**
     * Cancel daily reminder alarm
     */
    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }

    /**
     * Cancel weekly reset alarm
     */
    public static void cancelWeeklyReset(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeeklyResetReceiver.class);
        intent.setAction(WeeklyResetReceiver.ACTION_WEEKLY_RESET);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }
}