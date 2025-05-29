package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.R;
import com.example.myapp.SignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;

public class Register extends AppCompatActivity {

    private TextInputEditText etFullname, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        etFullname = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);


        // Register button click listener
        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Login text click listener
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to SignIn activity
                finish(); // Close the current activity and go back
            }
        });

    }

    private boolean isValidEmsiEmail(String email) {
        // Vérifie que l'email est valide ET se termine par @emsi-edu.ma
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && email.toLowerCase().endsWith("@emsi-edu.ma");
    }

    private void store_user_firestore(String uid, String email, String fullName) {
        HashMap<Object, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("createdAt", new Timestamp(new Date()));

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Register.this, "created user", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "Failed created user", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void registerUser() {
        String fullName = etFullname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || confPassword.isEmpty()) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check email format
        if (!isValidEmsiEmail(email)) {
            Toast.makeText(this, "Veuillez utiliser votre email professionnel (@emsi-edu.ma)", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if terms are accepted
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Veuillez accepter les termes et conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        createUserWithEmailAndPassword(email, password, fullName);
    }
    private void createUserWithEmailAndPassword(String email, String password, String fullName) {
        // Disable button to prevent multiple clicks
        btnRegister.setEnabled(false);

        // Here you would connect to your authentication service
        // Such as Firebase Auth:
         mAuth.createUserWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, task -> {
                 btnRegister.setEnabled(true);
                 if (task.isSuccessful()) {
                     // Registration success
                     Toast.makeText(this, "Inscription réussie!", Toast.LENGTH_SHORT).show();
                     String userId = mAuth.getCurrentUser().getUid();
                     store_user_firestore(userId, email, fullName);
                     Intent intent = new Intent(Register.this, SignIn.class);
                     intent.putExtra("name", fullName);
                     startActivity(intent);
                     finish();
                 } else {
                     // Registration failed
                     Toast.makeText(this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                 }
             });

        // For demonstration, simulate a network call
       /** btnRegister.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnRegister.setEnabled(true);

                // Simulate successful registration
                if (isValidEmail(email)) {
                    Toast.makeText(Register.this, "Inscription réussie!", Toast.LENGTH_SHORT).show();

                    // Return to login screen
                    Intent intent = new Intent(Register.this, SignIn.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Simulate error
                    Toast.makeText(Register.this, "Erreur: " + "Format d'email invalide", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1500); // Simulate 1.5 second delay **/
    }

    // Simple email validation
    /** private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    } **/
}