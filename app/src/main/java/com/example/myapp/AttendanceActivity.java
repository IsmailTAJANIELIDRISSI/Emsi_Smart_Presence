package com.example.myapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AttendanceActivity extends AppCompatActivity implements StudentAdapter.OnStudentInteractionListener {

    private AutoCompleteTextView groupSpinner, siteSpinner;
    private RecyclerView studentList;
    private MaterialButton saveButton, exportButton;
    private TextInputEditText signatureField, remarksField;
    private TextView exceptionNotes, sessionDate;
    private TextView presentCount, absentCount, earlyDepartureCount;
    private MaterialCardView exceptionsCard;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private CollectionReference attendanceRef;
    private StudentAdapter adapter;
    private List<Student> studentListData = new ArrayList<>();
    private String selectedGroup = "", selectedSite = "", sessionId;
    private FirebaseAuth mAuth;
    private int classEndHour = 17; // Default class end time - 5:00 PM
    private int classEndMinute = 0;
    private static final String TAG = "AttendanceActivity";
    private boolean isDataSaved = false; // Track if data is saved
    private static final String PREFS_NAME = "AttendancePrefs"; // SharedPreferences file name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, SignIn.class));
            finish();
            return;
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        attendanceRef = db.collection("attendance");

        // Initialize UI components
        initializeViews();

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // Set session date and generate sessionId
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        sessionDate.setText(getString(R.string.session_date) + currentDate);
        sessionId = UUID.randomUUID().toString(); // Generate unique session ID

        // Set up RecyclerView
        setupRecyclerView();

        // Populate spinners
        setupSiteSpinner();
        setupGroupSpinner();

        // Button click listeners
        setupClickListeners(currentUser.getUid(), currentDate);

        // Get class end time from preferences or use default (5:00 PM)
        retrieveClassEndTime();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        groupSpinner = findViewById(R.id.group_spinner);
        siteSpinner = findViewById(R.id.site_spinner);
        studentList = findViewById(R.id.student_list);
        saveButton = findViewById(R.id.save_button);
        exportButton = findViewById(R.id.export_button);
        signatureField = findViewById(R.id.signature_field);
        remarksField = findViewById(R.id.remarks_field);
        exceptionNotes = findViewById(R.id.exception_notes);
        sessionDate = findViewById(R.id.session_date);
        presentCount = findViewById(R.id.present_count);
        absentCount = findViewById(R.id.absent_count);
        earlyDepartureCount = findViewById(R.id.early_departure_count);
        exceptionsCard = findViewById(R.id.exceptions_card);
    }

    private void setupRecyclerView() {
        studentList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(studentListData, this);
        studentList.setAdapter(adapter);
    }

    private void setupClickListeners(String professorId, String currentDate) {
        saveButton.setOnClickListener(v -> saveAttendance(currentDate, professorId));
        exportButton.setOnClickListener(v -> exportAttendance());
    }

    private void retrieveClassEndTime() {
        // This could be expanded to fetch from shared preferences or settings
        // For now we're using the default values set in the class variables
        // classEndHour = 17; (5 PM)
        // classEndMinute = 0;
    }

    private void setupSiteSpinner() {
        List<String> sites = new ArrayList<>();
        sites.add(getString(R.string.select_site)); // Add placeholder

        db.collection("sites").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String siteId = document.getString("siteId");
                    if (siteId != null && !siteId.isEmpty()) {
                        sites.add(siteId);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_dropdown_item_1line, sites);
                siteSpinner.setAdapter(adapter);
                siteSpinner.setOnItemClickListener((parent, view, position, id) -> {
                    if (position > 0) { // Skip placeholder
                        selectedSite = sites.get(position);
                        loadStudents();
                    } else {
                        selectedSite = "";
                        studentListData.clear();
                        adapter.notifyDataSetChanged();
                        updateCounters();
                    }
                });
            } else {
                Log.e(TAG, "Error getting sites: ", task.getException());
                showSnackbar(getString(R.string.error_failed_load_sites));
            }
        });
    }

    private void setupGroupSpinner() {
        List<String> groups = new ArrayList<>();
        groups.add(getString(R.string.select_group)); // Add placeholder

        db.collection("groups").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String groupId = document.getString("groupId");
                    if (groupId != null && !groupId.isEmpty()) {
                        groups.add(groupId);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_dropdown_item_1line, groups);
                groupSpinner.setAdapter(adapter);
                groupSpinner.setOnItemClickListener((parent, view, position, id) -> {
                    if (position > 0) { // Skip placeholder
                        selectedGroup = groups.get(position);
                        loadStudents();
                    } else {
                        selectedGroup = "";
                        studentListData.clear();
                        adapter.notifyDataSetChanged();
                        updateCounters();
                    }
                });
            } else {
                Log.e(TAG, "Error getting groups: ", task.getException());
                showSnackbar(getString(R.string.error_failed_load_groups));
            }
        });
    }

    private void loadStudents() {
        if (selectedGroup.isEmpty() || selectedSite.isEmpty()) {
            return;
        }

        studentListData.clear();

        db.collection("students")
                .whereEqualTo("groupId", selectedGroup)
                .whereEqualTo("siteId", selectedSite)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student student = document.toObject(Student.class);

                            // Set defaults for all students
                            student.setPresent(true); // All students present by default
                            student.setDepartureTime(null); // No departure time by default

                            studentListData.add(student);
                        }

                        adapter.notifyDataSetChanged();
                        updateCounters();

                        // If no students found
                        if (studentListData.isEmpty()) {
                            showSnackbar(getString(R.string.no_students_found));
                        }
                    } else {
                        Log.e(TAG, "Error getting students: ", task.getException());
                        showSnackbar(getString(R.string.error_failed_load_students));
                    }
                });
    }

    @Override
    public void onAttendanceToggled(Student student, int position, boolean isPresent) {
        student.setPresent(isPresent);

        // If marked absent, set current time as departure time
//        if (!isPresent && student.getDepartureTime() == null) {
//            student.setDepartureTime(getCurrentTime());
//        }

        // If marked present again, clear departure time
        if (isPresent) {
            student.setDepartureTime(null);
        }

        adapter.notifyItemChanged(position);
        checkExceptions();
        checkAttendancePatterns();
        updateCounters();
    }

    @Override
    public void onDepartureTimeRequested(Student student, int position) {
        // Only allow setting departure time for present students
        if (student.isPresent()) {
            showTimePickerDialog(student, position);
        }
    }

    private void showTimePickerDialog(Student student, int position) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    student.setDepartureTime(time);
                    adapter.notifyItemChanged(position);
                    checkExceptions();
                    updateCounters();
                },
                currentHour,
                currentMinute,
                true); // 24-hour format

        timePickerDialog.setTitle("Définir l'heure de départ");
        timePickerDialog.show();
    }

    private void checkExceptions() {
        StringBuilder notes = new StringBuilder();
        int earlyDepartureCount = 0;

        for (Student student : studentListData) {
            if (student.isPresent() && student.getDepartureTime() != null &&
                    isEarlyDeparture(student.getDepartureTime())) {
                notes.append("• ").append(student.getName())
                        .append(" ").append(" est absent(e) (départ: ").append(" ").append(student.getDepartureTime())
                        .append("\n");
                earlyDepartureCount++;
            }
//            else if (!student.isPresent()) {
//                notes.append("• ").append(student.getName())
//                        .append(" is absent (departure: ")
//                        .append(student.getDepartureTime() != null ? student.getDepartureTime() : "N/A")
//                        .append(")\n");
//            }
        }

        if (notes.length() > 0) {
            exceptionNotes.setText(notes.toString());
            exceptionsCard.setVisibility(View.VISIBLE);
        } else {
            exceptionsCard.setVisibility(View.GONE);
        }

        this.earlyDepartureCount.setText("Départs anticipés: " + earlyDepartureCount);
    }

    private boolean isEarlyDeparture(String departureTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date departure = sdf.parse(departureTime);
            Date classEnd = getClassEndTime();

            if (departure != null && classEnd != null) {
                long diffInMinutes = (classEnd.getTime() - departure.getTime()) / (1000 * 60);
                return diffInMinutes >= 10; // 10 minutes or more before class end
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time", e);
        }
        return false;
    }

    private Date getClassEndTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String endTimeStr = String.format(Locale.getDefault(), "%02d:%02d", classEndHour, classEndMinute);
            return sdf.parse(endTimeStr);
        } catch (Exception e) {
            Log.e(TAG, "Error creating class end time", e);
            return null;
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void checkAttendancePatterns() {
        int presentCount = 0;
        int absentCount = 0;

        for (Student student : studentListData) {
            if (student.isPresent()) {
                presentCount++;
            } else {
                absentCount++;
            }
        }

        // Mise à jour des compteurs
        this.presentCount.setText("Présents: " + presentCount);
        this.absentCount.setText("Absents: " + absentCount);

        // Alert if more than 50% absent
        if (studentListData.size() > 0 && absentCount > studentListData.size() / 2) {
            showAlertDialog("Alerte de Présence",
                    "Plus de 50% des étudiants sont absents ! Veuillez vérifier s'il y a un conflit d'horaire.");
        }
    }


    private void updateCounters() {
        int presentCount = 0;
        int absentCount = 0;
        int earlyDepartureCount = 0;

        for (Student student : studentListData) {
            if (student.isPresent()) {
                presentCount++;

                if (student.getDepartureTime() != null && isEarlyDeparture(student.getDepartureTime())) {
                    earlyDepartureCount++;
                }
            } else {
                absentCount++;
            }
        }

        this.presentCount.setText("Présents: " + presentCount);
        this.absentCount.setText("Absents: " + absentCount);
        this.earlyDepartureCount.setText("Départs anticipés: " + earlyDepartureCount);
    }

    private void saveAttendance(String date, String professorId) {
        String signature = signatureField.getText().toString().trim();
        String remarks = remarksField.getText().toString().trim();

        if (selectedSite.isEmpty() || selectedGroup.isEmpty()) {
            showSnackbar("Veuillez sélectionner un site et un groupe");
            return;
        }

        if (studentListData.isEmpty()) {
            showSnackbar("Aucun étudiant pour enregistrer la présence");
            return;
        }

        if (signature.isEmpty()) {
            showSnackbar("Veuillez entrer votre signature");
            signatureField.requestFocus();
            return;
        }

        // Affichage de l'indicateur de progression
        saveButton.setEnabled(false);
        saveButton.setText("Enregistrement...");

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("sessionId", sessionId);
        sessionData.put("groupId", selectedGroup);
        sessionData.put("siteId", selectedSite);
        sessionData.put("date", date);
        sessionData.put("professorId", professorId);
        sessionData.put("signature", signature);
        sessionData.put("remarks", remarks);
        sessionData.put("timestamp", new Date());

        // Enregistrement des métadonnées de la session
        DocumentReference sessionDoc = attendanceRef.document(sessionId);
        sessionDoc.set(sessionData)
                .addOnSuccessListener(aVoid -> {
                    // Enregistrement des présences des étudiants par lot
                    CollectionReference recordsRef = sessionDoc.collection("records");

                    int totalStudents = studentListData.size();
                    int savedCount = 0;

                    for (Student student : studentListData) {
                        Map<String, Object> record = new HashMap<>();
                        record.put("studentId", student.getId());
                        record.put("present", student.isPresent());
                        if (student.isPresent()) {
                            record.put("departureTime", student.getDepartureTime());
                        } else {
                            record.put("departureTime", null);
                        }

                        int finalSavedCount = savedCount + 1;

                        recordsRef.document(student.getId()).set(record)
                                .addOnSuccessListener(aVoid1 -> {
                                    if (finalSavedCount == totalStudents) {
                                        // Tous les enregistrements sont sauvegardés
                                        saveButton.setEnabled(true);
                                        saveButton.setText("Enregistrer");
                                        showSnackbar("Présence enregistrée avec succès");
                                        isDataSaved = true;
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erreur lors de l'enregistrement de l'étudiant", e);
                                    saveButton.setEnabled(true);
                                    saveButton.setText("Enregistrer");
                                    showSnackbar("Erreur lors de l'enregistrement de certaines présences");
                                });

                        savedCount++;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de l'enregistrement de la session", e);
                    saveButton.setEnabled(true);
                    saveButton.setText("Enregistrer");
                    showSnackbar("Échec de l'enregistrement de la présence. Veuillez réessayer.");
                });
    }
    private void exportAttendance() {
        if (studentListData.isEmpty()) {
            showSnackbar("Aucune donnée de présence à exporter");
            return;
        }

        try {
            // Création du chemin du dossier
            File directory = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "EMSI_Attendance"
            );

            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Création du nom de fichier avec horodatage
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "presence_" + selectedGroup + "_" + timestamp + ".csv";
            File file = new File(directory, fileName);

            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {

                // Écriture du BOM pour la compatibilité UTF-8 avec Excel
                writer.write('\ufeff');

                // Écriture de l'en-tête avec des points-virgules comme séparateurs
                writer.write("ID Étudiant;Nom;Présent;Heure de Départ\n");

                // Écriture des données des étudiants
                for (Student student : studentListData) {
                    writer.write(escapeCsv(student.getId()) + ";" +
                            escapeCsv(student.getName()) + ";" +
                            (student.isPresent() ? "oui" : "non") + ";" +
                            (student.isPresent() && student.getDepartureTime() != null ?
                                    escapeCsv(student.getDepartureTime()) : "") + "\n");
                }

                // Ajout d'une ligne vide
                writer.write("\n");

                // Écriture de l'en-tête des détails de la session
                writer.write("DÉTAILS DE LA SESSION\n");

                // Écriture des détails de la session
                writer.write("Groupe;" + escapeCsv(selectedGroup) + "\n");
                writer.write("Site;" + escapeCsv(selectedSite) + "\n");
                writer.write("Date;" + escapeCsv(sessionDate.getText().toString().replace("Date de la session: ", "")) + "\n");
                writer.write("Signature Professeur;" + escapeCsv(signatureField.getText().toString()) + "\n");
                writer.write("Remarques;" + escapeCsv(remarksField.getText().toString()) + "\n");
                writer.write("Nombre de Présents;" + getStudentCount(true) + "\n");
                writer.write("Nombre d'Absents;" + getStudentCount(false) + "\n");

                writer.flush();
            }

            // Affichage du message de succès
            Snackbar.make(findViewById(android.R.id.content),
                            "Exporté vers: Téléchargements/EMSI_Attendance/" + fileName, Snackbar.LENGTH_LONG)
                    .setAction("Ouvrir", v -> openExportedFile(file))
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Échec de l'exportation", e);
            showSnackbar("Échec de l'exportation: " + e.getMessage());
        }
    }
    private int getStudentCount(boolean present) {
        int count = 0;
        for (Student student : studentListData) {
            if (student.isPresent() == present) {
                count++;
            }
        }
        return count;
    }

    private void openExportedFile(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );
            intent.setDataAndType(uri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening file", e);
            showSnackbar(getString(R.string.error_open_file));
        }
    }

    // Helper method for proper CSV escaping
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data if needed
        if (!selectedSite.isEmpty() && !selectedGroup.isEmpty()) {
            loadStudents();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save any temporary state if needed
        if (!isDataSaved && !studentListData.isEmpty()) {
            // Optionally, save draft to SharedPreferences
            saveDraftAttendance();
        }
    }

    private void saveDraftAttendance() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save basic session data
        editor.putString("draft_site", selectedSite);
        editor.putString("draft_group", selectedGroup);
        editor.putString("draft_session_id", sessionId);
        editor.putString("draft_remarks", remarksField.getText().toString());
        editor.putString("draft_signature", signatureField.getText().toString());

        // We could save student data too, but that would be complex
        // Just note that we have a draft
        editor.putBoolean("has_draft", true);

        editor.apply();
    }

    private void loadDraftAttendance() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasDraft = prefs.getBoolean("has_draft", false);

        if (hasDraft) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.load_draft_title)
                    .setMessage(R.string.load_draft_message)
                    .setPositiveButton(R.string.load_draft_positive, (dialog, which) -> {
                        // Restore draft data
                        selectedSite = prefs.getString("draft_site", "");
                        selectedGroup = prefs.getString("draft_group", "");
                        sessionId = prefs.getString("draft_session_id", UUID.randomUUID().toString());
                        remarksField.setText(prefs.getString("draft_remarks", ""));
                        signatureField.setText(prefs.getString("draft_signature", ""));

                        // Set spinners
                        siteSpinner.setText(selectedSite, false);
                        groupSpinner.setText(selectedGroup, false);

                        // Load students if we have site and group
                        if (!selectedSite.isEmpty() && !selectedGroup.isEmpty()) {
                            loadStudents();
                        }
                    })
                    .setNegativeButton(R.string.load_draft_negative, (dialog, which) -> {
                        // Clear the draft
                        clearDraft();
                    })
                    .show();
        }
    }

    private void clearDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("has_draft", false);
        editor.apply();
    }

    private void handleAttendanceAnalytics() {
        // Calculate attendance rates
        int totalStudents = studentListData.size();
        if (totalStudents == 0) return;

        int presentCount = 0;
        int absentCount = 0;
        int earlyDepartureCount = 0;

        for (Student student : studentListData) {
            if (student.isPresent()) {
                presentCount++;
                if (student.getDepartureTime() != null && isEarlyDeparture(student.getDepartureTime())) {
                    earlyDepartureCount++;
                }
            } else {
                absentCount++;
            }
        }

        // Determine if we should alert for unusual patterns
        double absentRate = (double) absentCount / totalStudents;

        if (absentRate > 0.3) { // More than 30% absent
            showAlertDialog(getString(R.string.high_absence_rate_title),
                    String.format(getString(R.string.high_absence_rate_message), (int) (absentRate * 100)));
        }

        if (earlyDepartureCount > totalStudents * 0.25) { // More than 25% early departures
            showAlertDialog(getString(R.string.high_early_departure_title),
                    String.format(getString(R.string.high_early_departure_message), earlyDepartureCount));
        }

        // Save analytics to Firebase for later reporting
        if (isDataSaved) {
            saveAttendanceAnalytics(presentCount, absentCount, earlyDepartureCount, totalStudents);
        }
    }

    private void saveAttendanceAnalytics(int presentCount, int absentCount, int earlyDepartureCount, int totalStudents) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("sessionId", sessionId);
        analytics.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        analytics.put("groupId", selectedGroup);
        analytics.put("siteId", selectedSite);
        analytics.put("professorId", currentUser.getUid());
        analytics.put("totalStudents", totalStudents);
        analytics.put("presentCount", presentCount);
        analytics.put("absentCount", absentCount);
        analytics.put("earlyDepartureCount", earlyDepartureCount);
        analytics.put("presentRate", (double) presentCount / totalStudents);
        analytics.put("absentRate", (double) absentCount / totalStudents);
        analytics.put("earlyDepartureRate", (double) earlyDepartureCount / totalStudents);

        db.collection("attendance_analytics").document(sessionId)
                .set(analytics)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Analytics saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving analytics", e));
    }

    // Method to handle student search functionality
    private void setupSearchFunctionality() {
        // Implementation would go here if search functionality were added
        // This would filter the student list based on search input
    }

    // Method to handle bulk actions
    private void setupBulkActionButtons() {
        // If you added bulk mark present/absent functionality
    }

    // Utility method to validate attendance data before saving
    private boolean validateAttendanceData() {
        if (selectedSite.isEmpty() || selectedGroup.isEmpty()) {
            showSnackbar(getString(R.string.error_select_site_group));
            return false;
        }

        if (studentListData.isEmpty()) {
            showSnackbar(getString(R.string.error_no_students));
            return false;
        }

        String signature = signatureField.getText().toString().trim();
        if (signature.isEmpty()) {
            showSnackbar(getString(R.string.error_no_signature));
            signatureField.requestFocus();
            return false;
        }

        return true;
    }

    // Handle deep linking if applicable
    private void handleDeepLink(Intent intent) {
        if (intent != null && intent.getData() != null) {
            // Handle any deep link data like pre-selecting a course or date
            Uri data = intent.getData();
            String group = data.getQueryParameter("group");
            String site = data.getQueryParameter("site");

            if (group != null && !group.isEmpty()) {
                selectedGroup = group;
                groupSpinner.setText(group, false);
            }

            if (site != null && !site.isEmpty()) {
                selectedSite = site;
                siteSpinner.setText(site, false);
            }

            if (!selectedGroup.isEmpty() && !selectedSite.isEmpty()) {
                loadStudents();
            }
        }
    }

    // Method to add any additional permissions checks
    private void checkRequiredPermissions() {
        // Add any permission checks needed for the app
        // e.g., storage permissions for export functionality
    }

    // Add method to sync offline attendance data if implemented
    private void syncOfflineData() {
        // Implementation would go here to sync any attendance data
        // that was collected while offline
    }

    private String escapeCsv(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains semicolons or newlines
        String escaped = content.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showAlertDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
