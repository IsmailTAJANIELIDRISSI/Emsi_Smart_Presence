package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlanningActivity extends AppCompatActivity {

    private ImageButton btnBack, btnFilter, btnPreviousWeek, btnNextWeek, btnClearFilter;
    private TextView tvWeekRange, tvCurrentWeek, tvFilterStatus;
    private RecyclerView rvWeeklySchedule;
    private LinearLayout layoutEmptyState;
    private CardView cardFilterStatus;
    private CircularProgressIndicator progressLoading;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private WeeklyScheduleAdapter adapter;
    private Calendar currentWeekStart;
    private List<DaySchedule> weeklySchedule;

    private String currentSiteFilter = null;
    private String currentGroupFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        initializeViews();
        initializeFirebase();
        setupRecyclerView();
        setupClickListeners();
        initializeWeek();
        loadWeeklySchedule();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        btnFilter = findViewById(R.id.btn_filter);
        btnPreviousWeek = findViewById(R.id.btn_previous_week);
        btnNextWeek = findViewById(R.id.btn_next_week);
        btnClearFilter = findViewById(R.id.btn_clear_filter);
        tvWeekRange = findViewById(R.id.tv_week_range);
        tvCurrentWeek = findViewById(R.id.tv_current_week);
        tvFilterStatus = findViewById(R.id.tv_filter_status);
        rvWeeklySchedule = findViewById(R.id.rv_weekly_schedule);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        cardFilterStatus = findViewById(R.id.card_filter_status);
        progressLoading = findViewById(R.id.progress_loading);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        weeklySchedule = new ArrayList<>();
        adapter = new WeeklyScheduleAdapter(weeklySchedule, this::onCourseAttendanceClick);
        rvWeeklySchedule.setLayoutManager(new LinearLayoutManager(this));
        rvWeeklySchedule.setAdapter(adapter);
        rvWeeklySchedule.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFilter.setOnClickListener(v -> showFilterDialog());

        btnPreviousWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekDisplay();
            loadWeeklySchedule();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekDisplay();
            loadWeeklySchedule();
        });

        btnClearFilter.setOnClickListener(v -> {
            currentSiteFilter = null;
            currentGroupFilter = null;
            updateFilterStatus();
            loadWeeklySchedule();
        });
    }

    private void initializeWeek() {
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);

        updateWeekDisplay();
    }

    private void updateWeekDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", new Locale("fr", "FR"));
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);

        String weekRange = dateFormat.format(currentWeekStart.getTime()) + " - " +
                dateFormat.format(weekEnd.getTime()) + " " +
                currentWeekStart.get(Calendar.YEAR);
        tvWeekRange.setText(weekRange);

        // Check if current week
        Calendar today = Calendar.getInstance();
        Calendar thisWeekStart = (Calendar) today.clone();
        thisWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        boolean isCurrentWeek = isSameWeek(currentWeekStart, thisWeekStart);
        tvCurrentWeek.setVisibility(isCurrentWeek ? View.VISIBLE : View.GONE);
    }

    private boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private void loadWeeklySchedule() {
        if (auth.getCurrentUser() == null) {
            Log.d("Courses nchouf", "No user logged in, exiting loadWeeklySchedule");
            return;
        }

        showLoading(true);
        weeklySchedule.clear();
        Log.d("Courses nchouf", "Weekly schedule cleared");

        // Initialize days of the week
        String[] dayNames = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (int i = 0; i < 7; i++) {
            Calendar dayCalendar = (Calendar) currentWeekStart.clone();
            dayCalendar.add(Calendar.DAY_OF_YEAR, i);

            DaySchedule daySchedule = new DaySchedule();
            daySchedule.dayName = dayNames[i];
            daySchedule.date = dayCalendar.getTime();
            daySchedule.courses = new ArrayList<>();
            daySchedule.isToday = isToday(dayCalendar);
            weeklySchedule.add(daySchedule);

            Log.d("Courses nchouf", "Initialized day: " + daySchedule.dayName + ", Date: " + daySchedule.date + ", isToday: " + daySchedule.isToday);
        }
        Log.d("Courses nchouf", "Initialized " + weeklySchedule.size() + " days in weekly schedule");

        // Build query
        Query query = db.collection("courses")
                .whereEqualTo("professorId", auth.getCurrentUser().getUid());
        Log.d("Courses nchouf", "Query built with professorId: " + auth.getCurrentUser().getUid());

        if (currentSiteFilter != null) {
            query = query.whereEqualTo("siteId", currentSiteFilter);
            Log.d("Courses nchouf", "Added site filter: " + currentSiteFilter);
        }

        if (currentGroupFilter != null) {
            query = query.whereEqualTo("groupId", currentGroupFilter);
            Log.d("Courses nchouf", "Added group filter: " + currentGroupFilter);
        }

        query.get().addOnCompleteListener(task -> {
            showLoading(false);
            Log.d("Courses nchouf", "Query completed, checking success");

            if (task.isSuccessful()) {
                Log.d("Courses nchouf", "Query successful. Result size: " + task.getResult().size());
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Course course = document.toObject(Course.class);
                    course.courseId = document.getId();
                    Log.d("Courses nchouf", "Fetched course: courseId=" + course.courseId + ", dayOfWeek=" + course.dayOfWeek +
                            ", startTime=" + course.startTime + ", siteId=" + course.siteId + ", groupId=" + course.groupId);

                    // Find the corresponding day and add the course
                    String dayOfWeek = course.dayOfWeek;
                    for (DaySchedule daySchedule : weeklySchedule) {
                        if (daySchedule.dayName.equals(dayOfWeek)) {
                            daySchedule.courses.add(course);
                            Log.d("Courses nchouf", "Added course to " + daySchedule.dayName + ": " + course.courseId);
                            break;
                        }
                    }
                }

                // Sort courses by start time for each day
                for (DaySchedule daySchedule : weeklySchedule) {
                    daySchedule.courses.sort((c1, c2) -> c1.startTime.compareTo(c2.startTime));
                    Log.d("Courses nchouf", "Sorted courses for " + daySchedule.dayName + ". Course count: " + daySchedule.courses.size());
                }

                adapter.notifyDataSetChanged();
                Log.d("Courses nchouf", "Adapter notified of data change");
                updateEmptyState();
                Log.d("Courses nchouf", "Empty state updated");
            } else {
                Log.e("Courses nchouf", "Query failed: " + task.getException());
                showError(getString(R.string.error_loading_schedule));
            }
        });
    }

    private boolean isToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    private void updateEmptyState() {
        boolean isEmpty = true;
        for (DaySchedule daySchedule : weeklySchedule) {
            if (!daySchedule.courses.isEmpty()) {
                isEmpty = false;
                break;
            }
        }

        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvWeeklySchedule.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

//    private void showFilterDialog() {
//        // TODO: Implement filter dialog with site and group options
//        // For now, show a simple toast
//        Toast.makeText(this, "Filtre à implémenter", Toast.LENGTH_SHORT).show();
//    }
private void showFilterDialog() {
    // Create dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_schedule_filter, null);
    builder.setView(dialogView);

    // Initialize dialog views
    Spinner spinnerSite = dialogView.findViewById(R.id.spinner_site);
    Spinner spinnerGroup = dialogView.findViewById(R.id.spinner_group);
    Button btnApplyFilter = dialogView.findViewById(R.id.btn_apply_filter);
    Button btnCancelFilter = dialogView.findViewById(R.id.btn_cancel_filter);
    ProgressBar progressDialog = dialogView.findViewById(R.id.progress_dialog);
    LinearLayout layoutFilters = dialogView.findViewById(R.id.layout_filters);

    AlertDialog dialog = builder.create();

    // Show loading initially
    progressDialog.setVisibility(View.VISIBLE);
    layoutFilters.setVisibility(View.GONE);

    // Load filter options
    loadFilterOptions(spinnerSite, spinnerGroup, progressDialog, layoutFilters);

    // Set up click listeners
    btnApplyFilter.setOnClickListener(v -> {
        Site selectedSite = (Site) spinnerSite.getSelectedItem();
        Group selectedGroup = (Group) spinnerGroup.getSelectedItem();

        // Update filters (null check for "All" options)
        currentSiteFilter = (selectedSite != null && selectedSite.siteId != null) ? selectedSite.siteId : null;
        currentGroupFilter = (selectedGroup != null && selectedGroup.groupId != null) ? selectedGroup.groupId : null;

        updateFilterStatus();
        loadWeeklySchedule();
        dialog.dismiss();
    });

    btnCancelFilter.setOnClickListener(v -> dialog.dismiss());

    dialog.show();
}

    private void loadFilterOptions(Spinner spinnerSite, Spinner spinnerGroup,
                                   ProgressBar progressDialog, LinearLayout layoutFilters) {
        if (auth.getCurrentUser() == null) return;

        // Lists to store options
        List<Site> sitesList = new ArrayList<>();
        List<Group> groupsList = new ArrayList<>();

        // Add "All" options at the beginning
        Site allSites = new Site();
        allSites.siteId = "Tous les sites";
//        allSites.siteId = null;
        sitesList.add(allSites);

        Group allGroups = new Group();
        allGroups.groupId = "Tous les groupes";
//        allGroups.groupId = null;
        groupsList.add(allGroups);

        // Counter to track completed requests
        final int[] completedRequests = {0};
        final int totalRequests = 2;

        // Load sites
        db.collection("sites")
                .orderBy("siteId")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Sites collection", "Sites query successful. Result size: " + task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Site site = document.toObject(Site.class);
                            site.siteId = document.getString("siteId");
                            sitesList.add(site);
                        }
                    }

                    completedRequests[0]++;
                    if (completedRequests[0] == totalRequests) {
                        setupSpinners(spinnerSite, spinnerGroup, sitesList, groupsList,
                                progressDialog, layoutFilters);
                    }
                })
                .addOnFailureListener(e -> {
                    completedRequests[0]++;
                    if (completedRequests[0] == totalRequests) {
                        setupSpinners(spinnerSite, spinnerGroup, sitesList, groupsList,
                                progressDialog, layoutFilters);
                    }
                });

        // Load groups
        db.collection("groups")
                .orderBy("groupId")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Groups collection", "Groups query successful. Result size: " + task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Group group = document.toObject(Group.class);
                            group.groupId = document.getString("groupId");
                            groupsList.add(group);
                        }
                    }

                    completedRequests[0]++;
                    if (completedRequests[0] == totalRequests) {
                        setupSpinners(spinnerSite, spinnerGroup, sitesList, groupsList,
                                progressDialog, layoutFilters);
                    }
                })
                .addOnFailureListener(e -> {
                    completedRequests[0]++;
                    if (completedRequests[0] == totalRequests) {
                        setupSpinners(spinnerSite, spinnerGroup, sitesList, groupsList,
                                progressDialog, layoutFilters);
                    }
                });
    }

    private void setupSpinners(Spinner spinnerSite, Spinner spinnerGroup,
                               List<Site> sitesList, List<Group> groupsList,
                               ProgressBar progressDialog, LinearLayout layoutFilters) {

        // Set up site spinner
        SiteSpinnerAdapter siteAdapter = new SiteSpinnerAdapter(this, sitesList);
        spinnerSite.setAdapter(siteAdapter);

        // Set current selection for site
        if (currentSiteFilter != null) {
            for (int i = 0; i < sitesList.size(); i++) {
                if (sitesList.get(i).siteId != null &&
                        sitesList.get(i).siteId.equals(currentSiteFilter)) {
                    spinnerSite.setSelection(i);
                    break;
                }
            }
        }

        // Set up group spinner
        GroupSpinnerAdapter groupAdapter = new GroupSpinnerAdapter(this, groupsList);
        spinnerGroup.setAdapter(groupAdapter);

        // Set current selection for group
        if (currentGroupFilter != null) {
            for (int i = 0; i < groupsList.size(); i++) {
                if (groupsList.get(i).groupId != null &&
                        groupsList.get(i).groupId.equals(currentGroupFilter)) {
                    spinnerGroup.setSelection(i);
                    break;
                }
            }
        }

        // Hide loading and show content
        progressDialog.setVisibility(View.GONE);
        layoutFilters.setVisibility(View.VISIBLE);
    }
    private void updateFilterStatus() {
        boolean hasFilter = currentSiteFilter != null || currentGroupFilter != null;
        cardFilterStatus.setVisibility(hasFilter ? View.VISIBLE : View.GONE);

        if (hasFilter) {
            StringBuilder filterText = new StringBuilder("Filtres actifs: ");
            if (currentSiteFilter != null) {
                filterText.append("Site: ").append(currentSiteFilter);
            }
            if (currentGroupFilter != null) {
                if (currentSiteFilter != null) filterText.append(", ");
                filterText.append("Groupe: ").append(currentGroupFilter);
            }
            tvFilterStatus.setText(filterText.toString());
        }
    }

    private void onCourseAttendanceClick(Course course) {
        Intent intent = new Intent(this, AttendanceActivity.class);
        intent.putExtra("groupId", course.groupId);
        intent.putExtra("siteId", course.siteId);
        intent.putExtra("courseId", course.courseId);
        intent.putExtra("courseTitle", course.title);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        rvWeeklySchedule.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}