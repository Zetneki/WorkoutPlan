package com.example.workoutplan;
import java.util.ArrayList;
import java.util.List;

public class WorkoutRepository {
    private List<WorkoutPlan> localData = new ArrayList<>();

    public WorkoutRepository(String userId) {
        this.localData = DummyDataGenerator.generateSamplePlans(userId);
    }

    // CRUD Operations
    public List<WorkoutPlan> getAllPlans() {
        return new ArrayList<>(localData);
    }

    public void addPlan(WorkoutPlan newPlan) {
        localData.add(newPlan);
    }

    public void updateExerciseStatus(String planId, int dayIndex,
                                     int exerciseIndex, boolean isCompleted) {
        // Implementation to find and update exercise
    }

    public void deletePlan(String planId) {
        localData.removeIf(plan -> plan.getPlanId().equals(planId));
    }
}
