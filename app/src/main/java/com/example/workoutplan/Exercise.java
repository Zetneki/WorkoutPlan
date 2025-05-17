package com.example.workoutplan;

public class Exercise {
    private String name;
    private String setsReps;

    public Exercise() {}

    public Exercise(String name, String setsReps) {
        this.name = name;
        this.setsReps = setsReps;
    }

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
