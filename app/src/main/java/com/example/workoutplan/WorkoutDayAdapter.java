package com.example.workoutplan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkoutDayAdapter extends RecyclerView.Adapter<WorkoutDayAdapter.DayViewHolder> {
    private ArrayList<WorkoutDay> days;
    private Context context;

    public WorkoutDayAdapter(ArrayList<WorkoutDay> days, Context context) {
        this.days = days;
        this.context = context;
    }

    public interface OnDayActionListener {
        void onDayDelete(int position);
        void onDayEdit(int position, WorkoutDay day);
    }

    private OnDayActionListener dayActionListener;

    public void setOnDayActionListener(OnDayActionListener listener) {
        this.dayActionListener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        WorkoutDay day = days.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDayName;
        private RecyclerView exercisesRecyclerView;
        private ExerciseAdapter exerciseAdapter;
        private Button btnAddExercise;
        private Button btnDeleteDay;
        private Button btnEditDay;
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            exercisesRecyclerView = itemView.findViewById(R.id.exercises_recycler);

            exerciseAdapter = new ExerciseAdapter(new ArrayList<>());
            exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            exercisesRecyclerView.setAdapter(exerciseAdapter);
            btnAddExercise = itemView.findViewById(R.id.btn_add_exercise);
            btnEditDay = itemView.findViewById(R.id.btn_edit_day);
            btnDeleteDay = itemView.findViewById(R.id.btn_delete_day);
            btnAddExercise.setOnClickListener(v -> {
                showAddExerciseDialog();
            });
            btnDeleteDay.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && dayActionListener != null) {
                    dayActionListener.onDayDelete(position);
                }
            });

            btnEditDay.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && dayActionListener != null) {
                    dayActionListener.onDayEdit(position, days.get(position));
                }
            });
        }

        void bind(WorkoutDay day) {
            tvDayName.setText(day.getDayName());

            exerciseAdapter.updateExercises(day.getExercises());
            exerciseAdapter.setOnExerciseClickListener(new ExerciseAdapter.OnExerciseClickListener() {
                @Override
                public void onExerciseClick(int position) {
                    showEditExerciseDialog(day, position);
                }

                @Override
                public void onDeleteClick(int position) {
                    day.getExercises().remove(position);
                    exerciseAdapter.notifyItemRemoved(position);
                }
            });

            exercisesRecyclerView.setAdapter(exerciseAdapter);
        }

        private void showAddExerciseDialog() {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_exercise, null);
            EditText etName = dialogView.findViewById(R.id.et_exercise_name);
            EditText etSetsReps = dialogView.findViewById(R.id.et_sets_reps);

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setTitle("New exercise")
                    .setPositiveButton("Add", (d, which) -> {

                        hideKeyboardFrom(dialogView);

                        String name = etName.getText().toString();
                        String setsReps = etSetsReps.getText().toString();

                        if (!name.isEmpty() && !setsReps.isEmpty()) {
                            int dayPosition = getAdapterPosition();
                            if (dayPosition != RecyclerView.NO_POSITION) {
                                days.get(dayPosition).getExercises().add(new Exercise(name, setsReps));
                                notifyItemChanged(dayPosition);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", (d, which) -> hideKeyboardFrom(dialogView))
                    .create();

            dialog.setOnShowListener(d -> {
                // Dialógus ablak háttérszíne
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.light_gray)));

                // Dialógus tartalom háttérszíne
                dialogView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray));

                etName.postDelayed(() -> {
                    etName.requestFocus();
                    showKeyboardFor(etName);
                }, 50);
            });
            dialog.show();
        }

        private void showEditExerciseDialog(WorkoutDay day, int exercisePos) {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_exercise, null);
            EditText etName = dialogView.findViewById(R.id.et_exercise_name);
            EditText etSetsReps = dialogView.findViewById(R.id.et_sets_reps);

            etName.setText(day.getExercises().get(exercisePos).getName());
            etSetsReps.setText(day.getExercises().get(exercisePos).getSetsReps());

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setTitle("Edit exercise")
                    .setPositiveButton("Save", (d, which) -> {
                        hideKeyboardFrom(dialogView);
                        day.getExercises().get(exercisePos).setName(etName.getText().toString());
                        day.getExercises().get(exercisePos).setSetsReps(etSetsReps.getText().toString());
                        exerciseAdapter.notifyItemChanged(exercisePos);
                    })
                    .setNegativeButton("Cancel", (d, which) -> hideKeyboardFrom(dialogView))
                    .create();

            dialog.setOnShowListener(d -> {
                // Dialógus ablak háttérszíne
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.light_gray)));

                // Dialógus tartalom háttérszíne
                dialogView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray));

                etName.postDelayed(() -> {
                    etName.requestFocus();
                    showKeyboardFor(etName);
                }, 50);
            });
            dialog.show();
        }

        private void showKeyboardFor(View view) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        private void hideKeyboardFrom(View view) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

    }
}
