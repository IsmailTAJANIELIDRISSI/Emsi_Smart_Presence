package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;


public class SignIn extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private final String validEmail = "user@gmail.com";
    private final String validPassword = "Tenda2020";
    private FirebaseAuth mAuth;
    private TextView tvRegister, tvForgotPassword;




    private void authenticateUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        /** if (email.equals(validEmail) && password.equals(validPassword)) {
            Toast.makeText(this, "Authentification réussie", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
        } **/

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignIn.this, Dashboard.class);
                startActivity(intent);
                finish(); // Close login activity
            } else {
                Toast.makeText(this, "Erreur" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        // Récupération des éléments
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateUser();
            }
        });
        // Register text click listener
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, Register.class);
                intent.putExtra("test","hello");
                startActivity(intent);
            }
        });

        // Forgot password text click listener
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle forgot password functionality
                Toast.makeText(SignIn.this, "Fonction de récupération de mot de passe", Toast.LENGTH_SHORT).show();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}