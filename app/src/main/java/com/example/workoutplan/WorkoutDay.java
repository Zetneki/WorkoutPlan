package com.example.workoutplan;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDay {
    private String dayName;
    private List<Exercise> exercises = new ArrayList<>();

    public WorkoutDay(String dayName) {
        this.dayName = dayName;
    }

    // Getters and setters

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
}
