package com.example.workoutplan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.PlanViewHolder> {
    private ArrayList<WorkoutPlan> plans;
    private OnPlanDeleteListener deleteListener;
    private OnProgressChangeListener progressChangeListener;

    public WorkoutPlanAdapter(ArrayList<WorkoutPlan> plans, OnPlanDeleteListener deleteListener) {
        this.plans = plans;
        this.deleteListener = deleteListener;
    }

    public interface OnPlanDeleteListener {
        void onPlanDelete(int position);
    }

    public interface OnProgressChangeListener {
        void onProgressChanged(String planId, int progress);
        void onWorkoutCompleted(String planId);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        this.progressChangeListener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_plan, parent, false);
        return new PlanViewHolder(view, deleteListener, progressChangeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        WorkoutPlan currentPlan = plans.get(position);
        holder.planName.setText(currentPlan.getName());

        int totalDays = currentPlan.getDays().size();
        int completedDays = (int) currentPlan.getDays().stream().filter(WorkoutDay::isCompleted).count();

        holder.progressSeekBar.setMax(Math.max(totalDays, 1));
        holder.progressSeekBar.setProgress(completedDays);

        holder.bindPlanId(currentPlan.getPlanId());

        StringBuilder allDaysExercises = new StringBuilder();
        for (WorkoutDay day : currentPlan.getDays()) {
            if (day.isCompleted()) {
                completedDays++;
            }

            allDaysExercises.append(day.getDayName()).append(":\n");

            for (Exercise ex : day.getExercises()) {
                allDaysExercises.append("• ")
                        .append(ex.getName())
                        .append(" (")
                        .append(ex.getSetsReps())
                        .append(")\n");
            }

            allDaysExercises.append("\n");
        }

        boolean isCompleted = totalDays > 0 && completedDays == totalDays;
        holder.progressSeekBar.setActivated(isCompleted);

        holder.daysAndExercises.setText(allDaysExercises.toString());

        /*
        if (!currentPlan.getDays().isEmpty()) {
            holder.completedCheckbox.setChecked(currentPlan.getDays().get(0).getDoneForTheDay());
        }*/

    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<WorkoutPlan> newPlans) {
        this.plans = newPlans; // Frissíti a referenciát
        notifyDataSetChanged();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        private TextView planName;
        private TextView daysAndExercises;
        private SeekBar progressSeekBar;
        private String currentPlanId;
        private ImageButton btnDelete;
        private boolean wasAtMax = false;

        public PlanViewHolder(@NonNull View itemView, OnPlanDeleteListener deleteListener, OnProgressChangeListener progressChangeListener) {
            super(itemView);
            planName = itemView.findViewById(R.id.plan_name);
            daysAndExercises = itemView.findViewById(R.id.days_exercises_list);
            progressSeekBar = itemView.findViewById(R.id.seekBar);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && progressChangeListener != null) {
                        boolean isAtMax = (progress == seekBar.getMax());

                        // Küldjük el a progress változást
                        progressChangeListener.onProgressChanged(currentPlanId, progress);

                        // Ha elértük a maximumot és korábban nem voltunk ott
                        if (isAtMax && !wasAtMax) {
                            progressChangeListener.onWorkoutCompleted(currentPlanId);
                            triggerCompletionAnimation(seekBar);
                        }
                        wasAtMax = isAtMax;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            itemView.findViewById(R.id.btn_edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Activity", "Edit plan button clicked!");
                    // Új intent létrehozása az AddWorkoutPlan Activity-hez
                    Intent intent = new Intent(itemView.getContext(), AddWorkoutPlan.class);
                    // Átadjuk a planId-t szerkesztéshez
                    intent.putExtra("PLAN_ID", currentPlanId);
                    intent.putExtra("EDIT_MODE", true);
                    itemView.getContext().startActivity(intent);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                        deleteListener.onPlanDelete(position);
                    }
                }
            });
        }

        private void triggerCompletionAnimation(SeekBar seekBar) {
            seekBar.setScaleX(1f);
            seekBar.setScaleY(1f);

            seekBar.post(() -> {
                seekBar.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(150)
                        .withEndAction(() -> {
                            seekBar.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .start();
                        })
                        .start();
            });
        }

        public void bindPlanId(String planId) {
            this.currentPlanId = planId;
        }
    }
}

