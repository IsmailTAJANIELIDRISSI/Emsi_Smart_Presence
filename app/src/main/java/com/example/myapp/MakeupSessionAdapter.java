package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MakeupSessionAdapter extends RecyclerView.Adapter<MakeupSessionAdapter.MakeupSessionViewHolder> {

    private Context context;
    private List<MakeupSession> makeupSessions;
    private List<MakeupSession> originalList;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat dayFormat;

    public MakeupSessionAdapter(Context context, List<MakeupSession> makeupSessions) {
        this.context = context;
        this.makeupSessions = new ArrayList<>(makeupSessions);
        this.originalList = new ArrayList<>(makeupSessions);
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH);
        this.displayDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
        this.dayFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH);
    }

    @NonNull
    @Override
    public MakeupSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_makeup_session, parent, false);
        return new MakeupSessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MakeupSessionViewHolder holder, int position) {
        MakeupSession session = makeupSessions.get(position);

        // Set animation
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right));

        // Bind data
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return makeupSessions.size();
    }

    public void updateList(List<MakeupSession> newList) {
        this.makeupSessions.clear();
        this.makeupSessions.addAll(newList);
        notifyDataSetChanged();
    }

    public void sortByDate() {
        makeupSessions.sort((s1, s2) -> {
            try {
                Date date1 = inputDateFormat.parse(s1.getDate());
                Date date2 = inputDateFormat.parse(s2.getDate());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                return 0;
            }
        });
        notifyDataSetChanged();
    }

    public void sortByGroup() {
        makeupSessions.sort((s1, s2) -> s1.getGroupId().compareToIgnoreCase(s2.getGroupId()));
        notifyDataSetChanged();
    }

    public void resetSort() {
        makeupSessions.clear();
        makeupSessions.addAll(originalList);
        notifyDataSetChanged();
    }

    private boolean isWithinNextWeek(String dateString) {
        try {
            Date sessionDate = inputDateFormat.parse(dateString);
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, 7);
            Date nextWeek = cal.getTime();

            return sessionDate.after(today) && sessionDate.before(nextWeek);
        } catch (ParseException e) {
            return false;
        }
    }

    public class MakeupSessionViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvDayName, tvDayDate, tvClassCount;
        private TextView tvTimeSlot, tvDuration, tvSubjectName, tvClassroom, tvTeacher;
        private LinearLayout layoutScheduleItems;
        private MaterialButton btnMarkAttendance;
        private boolean isExpanded = false;

        public MakeupSessionViewHolder(@NonNull View itemView) {
            super(itemView);

            // Match IDs to your item_makeup_session.xml
            cardView = (CardView) itemView;
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDayDate = itemView.findViewById(R.id.tv_day_date);
            tvClassCount = itemView.findViewById(R.id.tv_class_count);
            tvTimeSlot = itemView.findViewById(R.id.tv_time_slot);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
            tvClassroom = itemView.findViewById(R.id.tv_classroom);
            tvTeacher = itemView.findViewById(R.id.tv_teacher);
            layoutScheduleItems = itemView.findViewById(R.id.layout_schedule_items);

            // You'll need to add this button to your XML if you want attendance marking
//            btnMarkAttendance = itemView.findViewById(R.id.btn_mark_attendance);

            cardView.setOnClickListener(v -> toggleExpansion());
            if (btnMarkAttendance != null) {
                btnMarkAttendance.setOnClickListener(v -> openAttendance());
            }
        }

        public void bind(MakeupSession session) {
            // Set day and date
            try {
                Date date = inputDateFormat.parse(session.getDate());
                tvDayName.setText(dayFormat.format(date).split(" ")[0]); // Just the day name
                tvDayDate.setText(displayDateFormat.format(date));
            } catch (ParseException e) {
                tvDayName.setText("");
                tvDayDate.setText(session.getDate());
            }

            // Set time slot and duration
            String endTime = calculateEndTime(session.getTime());
            tvTimeSlot.setText(String.format("%s - %s", session.getTime(), endTime));
            tvDuration.setText(calculateDuration(session.getTime(), endTime));

            // Set course details
            tvSubjectName.setText(session.getCourseTitle());
            tvClassroom.setText(session.getRoom());
            tvTeacher.setText(String.format("Prof. %s", session.getProfessorId())); // Adjust as needed

            // Update class count if needed
            tvClassCount.setText("1 cours"); // Or calculate dynamically
        }

        private void toggleExpansion() {
            isExpanded = !isExpanded;
            // Implement expansion logic if needed
        }

        private void openAttendance() {
            MakeupSession session = makeupSessions.get(getAdapterPosition());
            Intent intent = new Intent(context, AttendanceActivity.class);
            intent.putExtra("groupId", session.getGroupId());
            intent.putExtra("siteId", session.getSiteId());
            intent.putExtra("courseId", session.getCourseId());
            intent.putExtra("sessionType", "makeup");
            intent.putExtra("sessionId", session.getSessionId());
            context.startActivity(intent);
        }

        private String calculateEndTime(String startTime) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRENCH);
                Date time = timeFormat.parse(startTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(time);
                cal.add(Calendar.HOUR_OF_DAY, 2); // Assuming 2-hour sessions
                return timeFormat.format(cal.getTime());
            } catch (ParseException e) {
                return startTime;
            }
        }

        private String calculateDuration(String startTime, String endTime) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRENCH);
                Date start = timeFormat.parse(startTime);
                Date end = timeFormat.parse(endTime);
                long diff = end.getTime() - start.getTime();
                long hours = diff / (60 * 60 * 1000);
                return String.format(Locale.FRENCH, "%dh", hours);
            } catch (ParseException e) {
                return "2h";
            }
        }
    }
}