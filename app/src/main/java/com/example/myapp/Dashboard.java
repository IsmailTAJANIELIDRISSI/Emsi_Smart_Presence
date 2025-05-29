package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dashboard extends AppCompatActivity implements View.OnClickListener {
    private CardView planningCard, calendarCard, absenceCard, mapsCard, assistanceCard;
    private TextView tvWelcome, tvUserName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tv_welcome);
        tvUserName = findViewById(R.id.tv_user_name);

        loadUserData();
        // Initialize cards
        planningCard = findViewById(R.id.card_planning);
        calendarCard = findViewById(R.id.card_calendar);
        absenceCard = findViewById(R.id.card_absence);
        mapsCard = findViewById(R.id.card_maps);
        assistanceCard = findViewById(R.id.card_assistance);

        // Set click listeners
        planningCard.setOnClickListener(this);
        calendarCard.setOnClickListener(this);
        absenceCard.setOnClickListener(this);
        mapsCard.setOnClickListener(this);
        assistanceCard.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.card_planning) {
            // Launch Planning Activity
            Toast.makeText(this, "Planning selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PlanningActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.card_calendar) {
            // Launch Calendar Activity
            Toast.makeText(this, "Calendar selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.card_absence) {
            // Launch Absence Activity
            Toast.makeText(this, "Absence selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AttendanceActivity.class);
            startActivity(intent);
        } else if (id == R.id.card_maps) {
            // Launch Maps Activity
            Toast.makeText(this, "Maps selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Maps.class);
            startActivity(intent);
        } else if (id == R.id.card_assistance) {
            // Launch Assistance Activity
            Toast.makeText(this, "Assistance selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AssistantActivity.class);
            startActivity(intent);
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Récupérer le nom complet et l'afficher
                        String fullName = document.getString("fullName");
                        tvUserName.setText(fullName);
                    } else {
                        tvUserName.setText("Utilisateur"); // Valeur par défaut
                    }
                } else {
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
