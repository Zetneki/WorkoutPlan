package com.example.workoutplan;

public class Exercise {
    private String name;
    private String setsReps; // e.g. "4x12"
    private boolean completed;

    public Exercise(String name, String setsReps) {
        this.name = name;
        this.setsReps = setsReps;
        this.completed = false;
    }

    // Getters and setters


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSetsReps() {
        return setsReps;
    }

    public void setSetsReps(String setsReps) {
        this.setsReps = setsReps;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
