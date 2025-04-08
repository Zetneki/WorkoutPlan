package com.example.workoutplan;
import java.util.Arrays;
import java.util.List;

public class DummyDataGenerator {
    public static List<WorkoutPlan> generateSamplePlans(String userId) {
        // Plan 1 - Strength Training
        WorkoutPlan plan1 = new WorkoutPlan(userId, "12-Week Strength Program");
        WorkoutDay day1 = new WorkoutDay("Monday - Chest");
        day1.getExercises().addAll(Arrays.asList(
                new Exercise("Bench Press", "4x8"),
                new Exercise("Incline Dumbbell Press", "3x10")
        ));
        plan1.getDays().add(day1);

        // Plan 2 - Cardio
        WorkoutPlan plan2 = new WorkoutPlan(userId, "Marathon Prep");
        WorkoutDay day2 = new WorkoutDay("Wednesday - Running");
        day2.getExercises().add(new Exercise("Interval Running", "5x400m"));
        plan2.getDays().add(day2);

        return Arrays.asList(plan1, plan2);
    }
}
