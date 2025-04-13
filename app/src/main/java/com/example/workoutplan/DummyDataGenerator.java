package com.example.workoutplan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DummyDataGenerator {
    public static List<WorkoutPlan> generateSamplePlans(String userId) {
        // Plan 1 - Strength Training
        WorkoutPlan plan1 = new WorkoutPlan(userId, "12-Week Strength Program");
        WorkoutDay plan1Day1 = new WorkoutDay("Monday - Chest and Triceps");
        WorkoutDay plan1Day2 = new WorkoutDay("Tuesday - Back and Biceps");
        WorkoutDay plan1Day3 = new WorkoutDay("Thursday - Shoulders and Triceps");
        WorkoutDay plan1Day4 = new WorkoutDay("Thursday - Legs and Biceps ");

        plan1Day1.getExercises().addAll(Arrays.asList(
                new Exercise("Chest press", "4x8"),
                new Exercise("Triceps pushdown", "4x10"),
                new Exercise("Chest flys", "4x12"),
                new Exercise("Skull crushers", "4x12"),
                new Exercise("Push-ups", "3x10")
        ));
        plan1Day2.getExercises().addAll(Arrays.asList(
                new Exercise("Standing Rows", "4x10"),
                new Exercise("High Side Pulldown (seach side)", "4x10"),
                new Exercise("Standing Pullover", "4x10"),
                new Exercise("Lateral Raise", "4x10"),
                new Exercise("Standing Row to Bicep Curl", "4x12")
        ));
        plan1Day3.getExercises().addAll(Arrays.asList(
                new Exercise("Shoulder press", "4x10"),
                new Exercise("Dips", "4x10"),
                new Exercise("Standing Tricep Kickback(per side)", "4x10"),
                new Exercise("Underhand flys", "4x12"),
                new Exercise("Reverse flys", "4x12"),
                new Exercise("Angel Wings with Overhead Tricep Extension", "4x12")
        ));
        plan1Day4.getExercises().addAll(Arrays.asList(
                new Exercise("Lunges (each side)", "4x10"),
                new Exercise("Seated Calf Raise (per leg)", "4x12"),
                new Exercise("Lying alternating leg curls (per leg)", "4x12"),
                new Exercise("Biceps curls (per arm)", "4x12"),
                new Exercise("Squats", "4x12")
        ));
        plan1.getDays().add(plan1Day1);
        plan1.getDays().add(plan1Day2);
        plan1.getDays().add(plan1Day3);
        plan1.getDays().add(plan1Day4);

        // Plan 2 - Cardio
        WorkoutPlan plan2 = new WorkoutPlan(userId, "Marathon Prep");
        WorkoutDay plan2Day1 = new WorkoutDay("Wednesday - Running");
        plan2Day1.getExercises().add(new Exercise("Interval Running", "5x400m"));
        plan2.getDays().add(plan2Day1);

        return new ArrayList<>(Arrays.asList(plan1, plan2));
    }
}
