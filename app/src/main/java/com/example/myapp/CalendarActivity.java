package com.example.myapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView rvMakeupSessions;
    private MakeupSessionAdapter adapter;
    private LinearLayout layoutEmptyState;
    private CircularProgressIndicator progressLoading;
    private TextView tvTotalSessions, tvUpcomingSessions;
    private CardView cardSortStatus;
    private TextView tvSortStatus;

    // Firebase
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String currentProfessorId;

    // Data
    private final List<MakeupSession> makeupSessionsList = new ArrayList<>();
    private String currentSortType = "";
    private boolean isDataLoaded = false;

    // Constants
    private static final String SORT_DATE = "date";
    private static final String SORT_GROUP = "group";
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Check authentication
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        currentProfessorId = auth.getCurrentUser().getUid();

        initViews();
        setupRecyclerView();

        // Add this block to create 10 makeup sessions
        String currentProfessorId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "qCjd5nWpqt7aETEJ99UFffnA2"; // Use actual UID
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String[] dates = {
                "2025-05-26", "2025-05-27", "2025-05-28", "2025-05-29", "2025-05-30",
                "2025-06-01", "2025-06-02", "2025-06-03", "2025-06-04", "2025-06-05"
        };
        String[] courseIds = {"MATH101", "PHYS201", "INFO301", "MATH101", "CHEM202",
                "INFO301", "PHYS201", "CHEM202", "MATH101", "INFO301"};
        String[] courseTitles = {"Mathématiques Avancées", "Physique Appliquée", "Algorithmique",
                "Mathématiques Avancées", "Chimie Organique", "Algorithmique",
                "Physique Appliquée", "Chimie Organique", "Mathématiques Avancées", "Algorithmique"};
        String[] groupIds = {"4IIR_G3", "4IIR_G2", "4IIR_G1", "4IIR_G3", "4IIR_G2",
                "4IIR_G1", "4IIR_G2", "4IIR_G2", "4IIR_G3", "4IIR_G1"};
        String[] siteIds = {"Centre", "Maarif", "Centre", "Centre", "Maarif",
                "Centre", "Maarif", "Maarif", "Centre", "Centre"};
        String[] times = {"14:00", "09:30", "15:00", "10:00", "13:00",
                "11:00", "14:30", "09:00", "16:00", "12:00"};
        String[] rooms = {"405", "301", "407", "405", "302",
                "407", "301", "302", "405", "407"};
        String[] reasons = {"Annulation due à une conférence", "Maladie du professeur", "Fête nationale",
                "Retard dans le programme", "Examen reporté", "Vacances scolaires",
                "Mauvais temps", "Grève étudiante", "Projet urgent", "Séminaire"};

        for (int i = 0; i < 10; i++) {
            MakeupSession session = new MakeupSession(
                    "makeup_session_" + (i + 1),
                    courseIds[i],
                    courseTitles[i],
                    groupIds[i],
                    siteIds[i],
                    dates[i],
                    times[i],
                    rooms[i],
                    currentProfessorId,
                    reasons[i]
            );

            db.collection("makeup_sessions")
                    .document("makeup_session_" + (i + 1))
                    .set(session)
                    .addOnSuccessListener(aVoid -> Log.d("Courses nchouf", "Makeup session " + (i + 1) + " added"))
                    .addOnFailureListener(e -> Log.e("Courses nchouf", "Error adding makeup session " + (i + 1) + ": " + e.getMessage()));
        }
//        setupClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isDataLoaded) {
            loadMakeupSessions();
        }
    }

    private void initViews() {
        rvMakeupSessions = findViewById(R.id.rv_makeup_sessions);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        progressLoading = findViewById(R.id.progress_loading);
        tvTotalSessions = findViewById(R.id.tv_total_sessions);
        tvUpcomingSessions = findViewById(R.id.tv_upcoming_sessions);
        cardSortStatus = findViewById(R.id.card_sort_status);
        tvSortStatus = findViewById(R.id.tv_sort_status);

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnSort = findViewById(R.id.btn_sort);
        ImageButton btnClearSort = findViewById(R.id.btn_clear_sort);

        btnBack.setOnClickListener(v -> supportFinishAfterTransition());
        btnSort.setOnClickListener(v -> showSortOptions());
        btnClearSort.setOnClickListener(v -> clearSort());
    }

    private void setupRecyclerView() {
        adapter = new MakeupSessionAdapter(this, makeupSessionsList);
        rvMakeupSessions.setLayoutManager(new LinearLayoutManager(this));
        rvMakeupSessions.setAdapter(adapter);
        rvMakeupSessions.setItemAnimator(null); // Disable default animations for better performance
    }

    private void loadMakeupSessions() {
        showLoading(true);
        isDataLoaded = false;

        db.collection("makeup_sessions")
                .whereEqualTo("professorId", currentProfessorId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        makeupSessionsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MakeupSession session = document.toObject(MakeupSession.class);
                            session.setSessionId(document.getId());
                            makeupSessionsList.add(session);
                        }
                        isDataLoaded = true;
                        updateUI();
                    } else {
                        showError(getString(R.string.error_loading_makeup_sessions));
                    }
                });
    }

    private void updateUI() {
        if (makeupSessionsList.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            adapter.updateList(new ArrayList<>(makeupSessionsList)); // Pass copy to adapter
            rvMakeupSessions.scheduleLayoutAnimation(); // Trigger layout animation
        }
        updateSummaryCards();
    }

    private void updateSummaryCards() {
        int total = makeupSessionsList.size();
        int upcoming = countUpcomingSessions();

        tvTotalSessions.setText(String.valueOf(total));
        tvUpcomingSessions.setText(String.valueOf(upcoming));

        // Only animate when values change
        if (total > 0) {
            tvTotalSessions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
        }
        if (upcoming > 0) {
            tvUpcomingSessions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
        }
    }

    private int countUpcomingSessions() {
        int count = 0;
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date nextWeek = cal.getTime();

        for (MakeupSession session : makeupSessionsList) {
            try {
                Date sessionDate = DATE_FORMAT.parse(session.getDate());
                if (sessionDate != null && sessionDate.after(today) && sessionDate.before(nextWeek)) {
                    count++;
                }
            } catch (ParseException ignored) {
                // Skip invalid dates
            }
        }
        return count;
    }

    private void showSortOptions() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sort_by)
                .setItems(R.array.sort_options, (dialog, which) -> {
                    switch (which) {
                        case 0: sortByDate(); break;
                        case 1: sortByGroup(); break;
                    }
                })
                .show();
    }

    private void sortByDate() {
        adapter.sortByDate();
        currentSortType = SORT_DATE;
        showSortStatus(getString(R.string.sorted_by_date));
    }

    private void sortByGroup() {
        adapter.sortByGroup();
        currentSortType = SORT_GROUP;
        showSortStatus(getString(R.string.sorted_by_group));
    }

    private void clearSort() {
        adapter.resetSort();
        currentSortType = "";
        hideSortStatus();
    }

    private void showSortStatus(String status) {
        tvSortStatus.setText(status);
        if (cardSortStatus.getVisibility() != View.VISIBLE) {
            cardSortStatus.setVisibility(View.VISIBLE);
            cardSortStatus.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down));
        }
    }

    private void hideSortStatus() {
        if (cardSortStatus.getVisibility() == View.VISIBLE) {
            cardSortStatus.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
            cardSortStatus.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        rvMakeupSessions.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        if (show) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            layoutEmptyState.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            rvMakeupSessions.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvMakeupSessions.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Snackbar.make(rvMakeupSessions, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error_color))
                .setTextColor(getColor(R.color.white))
                .show();
    }
}