package com.example.workoutplan;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WorkoutPlan {
    private String planId;
    private String userId;
    private String name;
    private int currentProgress = 0;
    private List<WorkoutDay> days;
    private Date createdAt;

    public WorkoutPlan() {
        days = new ArrayList<>();
    }

    public WorkoutPlan(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.createdAt = new Date();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WorkoutDay> getDays() {
        return days;
    }

    public void setDays(List<WorkoutDay> days) {
        this.days = days;
    }

    public int getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}