package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
public class WeeklyScheduleAdapter extends RecyclerView.Adapter<WeeklyScheduleAdapter.DayViewHolder> {

    private List<DaySchedule> weeklySchedule;
    private OnCourseClickListener courseClickListener;
    private LayoutInflater inflater;

    public interface OnCourseClickListener {
        void onCourseAttendanceClick(Course course);
    }

    public WeeklyScheduleAdapter(List<DaySchedule> weeklySchedule, OnCourseClickListener listener) {
        this.weeklySchedule = weeklySchedule;
        this.courseClickListener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.getContext());
        }
        View view = inflater.inflate(R.layout.item_day_schedule, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DaySchedule daySchedule = weeklySchedule.get(position);
        holder.bind(daySchedule);
    }

    @Override
    public int getItemCount() {
        return weeklySchedule.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDayName, tvDayDate, tvNoClasses;
        private LinearLayout layoutDayHeader, layoutCoursesContainer;
        private View viewCurrentDayIndicator;
        private SimpleDateFormat dateFormat;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDayDate = itemView.findViewById(R.id.tv_day_date);
            tvNoClasses = itemView.findViewById(R.id.tv_no_classes);
            layoutDayHeader = itemView.findViewById(R.id.layout_day_header);
            layoutCoursesContainer = itemView.findViewById(R.id.layout_courses_container);
            viewCurrentDayIndicator = itemView.findViewById(R.id.view_current_day_indicator);

            dateFormat = new SimpleDateFormat("dd MMMM", new Locale("fr", "FR"));
        }

        public void bind(DaySchedule daySchedule) {
            // Set day name and date
            tvDayName.setText(daySchedule.dayName);
            tvDayDate.setText(dateFormat.format(daySchedule.date));

            // Show current day indicator
            viewCurrentDayIndicator.setVisibility(daySchedule.isToday ? View.VISIBLE : View.GONE);

            // Highlight current day header
            if (daySchedule.isToday) {
                layoutDayHeader.setBackgroundColor(itemView.getContext().getColor(R.color.present_green));
            } else {
                layoutDayHeader.setBackgroundColor(itemView.getContext().getColor(R.color.university_blue));
            }

            // Clear previous course views
            layoutCoursesContainer.removeAllViews();

            if (daySchedule.courses.isEmpty()) {
                // Show no classes message
                tvNoClasses.setVisibility(View.VISIBLE);
                layoutCoursesContainer.addView(tvNoClasses);
            } else {
                tvNoClasses.setVisibility(View.GONE);

                // Add course views
                for (Course course : daySchedule.courses) {
                    View courseView = createCourseView(course);
                    layoutCoursesContainer.addView(courseView);
                }
            }
        }

        private View createCourseView(Course course) {
            View courseView = inflater.inflate(R.layout.item_course, layoutCoursesContainer, false);

            TextView tvStartTime = courseView.findViewById(R.id.tv_start_time);
            TextView tvEndTime = courseView.findViewById(R.id.tv_end_time);
            TextView tvCourseTitle = courseView.findViewById(R.id.tv_course_title);
            TextView tvGroup = courseView.findViewById(R.id.tv_group);
            TextView tvLocation = courseView.findViewById(R.id.tv_location);
            View fabMarkAttendance = courseView.findViewById(R.id.fab_mark_attendance);

            // Set course data
            tvStartTime.setText(course.startTime);
            tvEndTime.setText(course.endTime);
            tvCourseTitle.setText(course.title);
            tvGroup.setText(course.groupId);

            // Combine site and room for location
            String location = course.siteId + " - Salle " + course.room;
            tvLocation.setText(location);

            // Set click listener for attendance button
            fabMarkAttendance.setOnClickListener(v -> {
                if (courseClickListener != null) {
                    courseClickListener.onCourseAttendanceClick(course);
                }
            });

            return courseView;
        }
    }
}