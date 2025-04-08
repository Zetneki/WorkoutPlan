package com.example.workoutplan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.PlanViewHolder> {

    private List<WorkoutPlan> plans;

    public WorkoutPlanAdapter(List<WorkoutPlan> plans) {
        this.plans = plans;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        WorkoutPlan plan = plans.get(position);
        holder.planName.setText(plan.getName());

        // Example: Show first day's exercises
        if (!plan.getDays().isEmpty()) {
            StringBuilder exercises = new StringBuilder();
            for (Exercise ex : plan.getDays().get(0).getExercises()) {
                exercises.append(ex.getName()).append(" (").append(ex.getSetsReps()).append(")\n");
            }
            holder.dayExercises.setText(exercises.toString());
        }
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView planName;
        TextView dayExercises;
        CheckBox completedCheckbox;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            planName = itemView.findViewById(R.id.plan_name);
            dayExercises = itemView.findViewById(R.id.exercises_list);
            completedCheckbox = itemView.findViewById(R.id.completed_checkbox);
        }
    }
}
