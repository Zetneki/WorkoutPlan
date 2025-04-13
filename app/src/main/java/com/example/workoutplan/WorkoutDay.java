package com.example.workoutplan;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDay {
    private String dayName;
    private ArrayList<Exercise> exercises = new ArrayList<>();
    private Boolean completed;

    public WorkoutDay(String dayName) {
        this.dayName = dayName;
        this.completed = false;
    }

    // Getters and setters

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
