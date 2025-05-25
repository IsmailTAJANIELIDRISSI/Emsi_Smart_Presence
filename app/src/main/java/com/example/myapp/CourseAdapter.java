package com.example.myapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courses;
    private OnCourseClickListener onCourseClickListener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseAdapter(List<Course> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.onCourseClickListener = listener;
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseTitle, tvCourseTime, tvCourseRoom;

        CourseViewHolder(View itemView) {
            super(itemView);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
//            tvCourseTime = itemView.findViewById(R.id.tv_course_time);
//            tvCourseRoom = itemView.findViewById(R.id.tv_course_room);
        }

        void bind(Course course) {
            tvCourseTitle.setText(course.getTitle());
            tvCourseTime.setText(course.getStartTime() + " - " + course.getEndTime());
            tvCourseRoom.setText("Salle: " + course.getRoom());
            itemView.setOnClickListener(v -> onCourseClickListener.onCourseClick(course));
        }
    }
}