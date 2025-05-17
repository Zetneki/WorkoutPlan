package com.example.workoutplan;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDay {
    private String dayName;
    private ArrayList<Exercise> exercises = new ArrayList<>();;

    public WorkoutDay() {}

    public WorkoutDay(String dayName) {
        this.dayName = dayName;
        this.exercises = new ArrayList<>();
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises != null ? exercises : new ArrayList<>();
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

}
