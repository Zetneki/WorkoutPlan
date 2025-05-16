package com.example.workoutplan;

public class Exercise {
    private String name;
    private String setsReps; // e.g. "4x12"

    public Exercise() {}

    public Exercise(String name, String setsReps) {
        this.name = name;
        this.setsReps = setsReps;
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
}
