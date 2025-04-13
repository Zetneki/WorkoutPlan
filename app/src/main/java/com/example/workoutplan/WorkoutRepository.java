package com.example.workoutplan;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkoutRepository {
    private static WorkoutRepository instance;
    private List<WorkoutPlan> localData;
    private final String userId;

    public WorkoutRepository(String userId) {
        this.userId = userId;
        this.localData = new ArrayList<>();
        initializeSampleData();
    }

    public static synchronized WorkoutRepository getInstance(String userId) {
        if (instance == null || !instance.userId.equals(userId)) {
            instance = new WorkoutRepository(userId);
        }
        return instance;
    }

    private void initializeSampleData() {
        // Only add sample data if empty (for fresh initialization)
        if (localData.isEmpty()) {
            localData.addAll(DummyDataGenerator.generateSamplePlans(userId));
        }
    }

    // CRUD Operations
    public ArrayList<WorkoutPlan> getAllPlans() {
        return new ArrayList<>(localData); // Return defensive copy
    }

    public void addPlan(WorkoutPlan newPlan) {
        // Generate unique ID if not set
        if (newPlan.getPlanId() == null || newPlan.getPlanId().isEmpty()) {
            newPlan.setPlanId(UUID.randomUUID().toString());
        }

        // Set user ID if not set
        if (newPlan.getUserId() == null || newPlan.getUserId().isEmpty()) {
            newPlan.setUserId(userId);
        }

        localData.add(0, newPlan); // Add to beginning of list
    }

    public void updateExerciseStatus(String planId, int dayIndex,
                                     int exerciseIndex, boolean isCompleted) {
        for (WorkoutPlan plan : localData) {
            if (plan.getPlanId().equals(planId)) {
                if (dayIndex >= 0 && dayIndex < plan.getDays().size()) {
                    WorkoutDay day = plan.getDays().get(dayIndex);
                    if (exerciseIndex >= 0 && exerciseIndex < day.getExercises().size()) {
                        day.getExercises().get(exerciseIndex).setCompleted(isCompleted);
                        return;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Plan, day or exercise not found");
    }

    public void deletePlan(String planId) {
        localData.removeIf(plan -> plan.getPlanId().equals(planId));
    }

    // Optional: Find plan by ID
    public WorkoutPlan getPlanById(String planId) {
        for (WorkoutPlan plan : localData) {
            if (plan.getPlanId().equals(planId)) {
                return plan;
            }
        }
        return null;
    }

    public void updatePlan(WorkoutPlan updatedPlan) {
        for (int i = 0; i < localData.size(); i++) {
            if (localData.get(i).getPlanId().equals(updatedPlan.getPlanId())) {
                localData.set(i, updatedPlan);
                break;
            }
        }
        // Itt implementáld a Firebase vagy lokális adatbázis mentést is
    }

    // Optional: Clear all data (for testing)
    public void clearAllData() {
        localData.clear();
    }
}