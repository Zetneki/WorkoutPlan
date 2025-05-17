package com.example.workoutplan;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkoutRepository {
    private static WorkoutRepository instance;
    private List<WorkoutPlan> localData;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId;

    public void loadWorkoutPlans(OnPlansLoadedListener listener) {
        db.collection("Workouts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WorkoutPlan> plans = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        WorkoutPlan plan = document.toObject(WorkoutPlan.class);
                        plan.setPlanId(document.getId());

                        if (plan.getDays() == null) {
                            plan.setDays(new ArrayList<>());
                        }

                        for (WorkoutDay day : plan.getDays()) {
                            if (day.getExercises() == null) {
                                day.setExercises(new ArrayList<>());
                            }
                        }

                        plans.add(plan);
                    }

                    localData.clear();
                    localData.addAll(plans);

                    listener.onPlansLoaded(plans);
                })
                .addOnFailureListener(listener::onError);
    }

    public interface OnPlansLoadedListener {
        void onPlansLoaded(List<WorkoutPlan> plans);
        void onError(Exception e);
    }

    public interface OnPlanSavedListener {
        void onSuccess();
        void onError(Exception e);
    }

    public WorkoutRepository(String userId) {
        this.userId = userId;
        this.localData = new ArrayList<>();
    }

    public static synchronized WorkoutRepository getInstance(String userId) {
        if (instance == null || !instance.userId.equals(userId)) {
            instance = new WorkoutRepository(userId);
        }
        return instance;
    }

    public ArrayList<WorkoutPlan> getAllPlans() {
        return new ArrayList<>(localData);
    }

    public void addPlan(WorkoutPlan plan, OnPlanSavedListener listener) {
        if (plan.getPlanId() == null) {
            plan.setPlanId(db.collection("Workouts").document().getId());
        }
        plan.setUserId(userId);

        db.collection("Workouts")
                .document(plan.getPlanId())
                .set(plan)
                .addOnSuccessListener(e -> {
                    localData.add(0, plan);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onError(e));
    }

    public void deletePlan(String planId) {
        db.collection("Workouts")
                .document(planId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    localData.removeIf(plan -> plan.getPlanId().equals(planId));
                });
    }

    public void getPlanByIdAsync(String planId, OnPlanLoadedListener listener) {
        db.collection("Workouts")
                .document(planId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        WorkoutPlan plan = documentSnapshot.toObject(WorkoutPlan.class);
                        plan.setPlanId(documentSnapshot.getId());

                        if (plan.getDays() == null) {
                            plan.setDays(new ArrayList<>());
                        }

                        for (WorkoutDay day : plan.getDays()) {
                            if (day.getExercises() == null) {
                                day.setExercises(new ArrayList<>());
                            }
                        }

                        listener.onPlanLoaded(plan);
                    } else {
                        listener.onError(new Exception("Plan not found"));
                    }
                })
                .addOnFailureListener(e -> listener.onError(e));
    }

    public interface OnPlanLoadedListener {
        void onPlanLoaded(WorkoutPlan plan);
        void onError(Exception e);
    }

    public WorkoutPlan getPlanById(String planId) {
        for (WorkoutPlan plan : localData) {
            if (plan.getPlanId().equals(planId)) {
                return plan;
            }
        }
        Log.w(TAG, "Plan not found in local cache. You should use getPlanByIdAsync instead.");
        return null;
    }

    public void updatePlan(WorkoutPlan updatedPlan) {
        db.collection("Workouts")
                .document(updatedPlan.getPlanId())
                .set(updatedPlan)
                .addOnSuccessListener(aVoid -> {
                    for (int i = 0; i < localData.size(); i++) {
                        if (localData.get(i).getPlanId().equals(updatedPlan.getPlanId())) {
                            localData.set(i, updatedPlan);
                            break;
                        }
                    }
                });
    }
}