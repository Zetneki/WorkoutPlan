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
                        // Alapvető adatok betöltése
                        WorkoutPlan plan = document.toObject(WorkoutPlan.class);
                        plan.setPlanId(document.getId());

                        // Napok és gyakorlatok kezelése
                        if (plan.getDays() == null) {
                            plan.setDays(new ArrayList<>());
                        }

                        // Minden nap gyakorlatainak ellenőrzése
                        for (WorkoutDay day : plan.getDays()) {
                            if (day.getExercises() == null) {
                                day.setExercises(new ArrayList<>());
                            }
                        }

                        plans.add(plan);
                    }

                    // Helyi adatok frissítése
                    localData.clear();
                    localData.addAll(plans);

                    listener.onPlansLoaded(plans);
                })
                .addOnFailureListener(listener::onError);
    }

    // Interface-ek
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

    // CRUD Operations
    public ArrayList<WorkoutPlan> getAllPlans() {
        return new ArrayList<>(localData); // Return defensive copy
    }

    public void addPlan(WorkoutPlan plan, OnPlanSavedListener listener) {
        // Ha nincs ID, generálunk egyet
        if (plan.getPlanId() == null) {
            plan.setPlanId(db.collection("Workouts").document().getId());
        }
        plan.setUserId(userId); // Set the userId

        // Mentés a Firestore-ba
        db.collection("Workouts")
                .document(plan.getPlanId())
                .set(plan)
                .addOnSuccessListener(e -> {
                    // Ha sikeres a mentés, frissítsük a helyi adatokat is
                    localData.add(0, plan);  // Hozzáadjuk a helyi listához
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onError(e));
    }

    public void deletePlan(String planId) {
        db.collection("Workouts")  // Add Firestore deletion
                .document(planId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local data after successful Firestore deletion
                    localData.removeIf(plan -> plan.getPlanId().equals(planId));
                });
    }

    // Find plan by ID - Updated to load from Firestore
    public void getPlanByIdAsync(String planId, OnPlanLoadedListener listener) {
        db.collection("Workouts")
                .document(planId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        WorkoutPlan plan = documentSnapshot.toObject(WorkoutPlan.class);
                        plan.setPlanId(documentSnapshot.getId());

                        // Ensure days and exercises are initialized
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

    // Interface for single plan loading
    public interface OnPlanLoadedListener {
        void onPlanLoaded(WorkoutPlan plan);
        void onError(Exception e);
    }

    // Optional: Find plan by ID
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
        // Update in Firestore
        db.collection("Workouts")
                .document(updatedPlan.getPlanId())
                .set(updatedPlan)
                .addOnSuccessListener(aVoid -> {
                    // Update local data after successful Firestore update
                    for (int i = 0; i < localData.size(); i++) {
                        if (localData.get(i).getPlanId().equals(updatedPlan.getPlanId())) {
                            localData.set(i, updatedPlan);
                            break;
                        }
                    }
                });
    }
}