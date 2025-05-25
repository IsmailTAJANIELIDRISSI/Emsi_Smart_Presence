package com.example.myapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<Student> studentList;
    private final OnStudentInteractionListener listener;

    public interface OnStudentInteractionListener {
        void onAttendanceToggled(Student student, int position, boolean isPresent);
        void onDepartureTimeRequested(Student student, int position);
    }

    public StudentAdapter(List<Student> studentList, OnStudentInteractionListener listener) {
        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.bind(student, position);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView studentNumber;
        private final TextView studentName;
        private final TextView studentId;
        private final CheckBox attendanceCheckbox;
        private final MaterialButton setDepartureTimeButton;
        private final MaterialCardView studentCard;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNumber = itemView.findViewById(R.id.student_number);
            studentName = itemView.findViewById(R.id.student_name);
            studentId = itemView.findViewById(R.id.student_id);
            attendanceCheckbox = itemView.findViewById(R.id.student_attendance_checkbox);
            setDepartureTimeButton = itemView.findViewById(R.id.set_departure_time_button);
            studentCard = itemView.findViewById(R.id.student_card);
        }

        public void bind(final Student student, final int position) {
            // Set student information
            studentNumber.setText(String.valueOf(position + 1));
            studentName.setText(student.getName());
            studentId.setText("ID: " + student.getId());

            // Set attendance status
            attendanceCheckbox.setChecked(student.isPresent());

            // Update card background based on status
            updateCardAppearance(student);

            // Update departure time button text if time is set
            updateDepartureButtonText(student);

            // Set click listeners
            attendanceCheckbox.setOnClickListener(v -> {
                boolean isChecked = attendanceCheckbox.isChecked();
                listener.onAttendanceToggled(student, position, isChecked);
                student.setPresent(isChecked);
                updateCardAppearance(student);
                updateDepartureButtonText(student);
            });

            setDepartureTimeButton.setOnClickListener(v ->
                    listener.onDepartureTimeRequested(student, position));
        }

        private void updateCardAppearance(Student student) {
            int cardColor = ContextCompat.getColor(itemView.getContext(), R.color.white);

            if (!student.isPresent()) {
                // Absent student - red tint
                cardColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_light);
                studentCard.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.absent_red));
                studentCard.setStrokeWidth(2);
            } else if (student.getDepartureTime() != null) {
                // Early departure - orange tint
                cardColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
                studentCard.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.early_departure_orange));
                studentCard.setStrokeWidth(2);
            } else {
                // Present - green tint
                cardColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
                studentCard.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.present_green));
                studentCard.setStrokeWidth(2);
            }

            studentCard.setCardBackgroundColor(cardColor);
        }

        private void updateDepartureButtonText(Student student) {
            if (student.getDepartureTime() != null) {
                setDepartureTimeButton.setText(student.getDepartureTime());
                setDepartureTimeButton.setIconTint(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.early_departure_orange));
            } else {
                setDepartureTimeButton.setText("Set Time");
                setDepartureTimeButton.setIconTint(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.text_secondary));
            }

            // Show/hide departure time button based on attendance
            setDepartureTimeButton.setEnabled(student.isPresent());
            setDepartureTimeButton.setAlpha(student.isPresent() ? 1.0f : 0.5f);
        }
    }
}