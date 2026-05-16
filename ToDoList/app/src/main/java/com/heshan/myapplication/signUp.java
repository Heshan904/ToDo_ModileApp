package com.heshan.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class signUp extends AppCompatActivity {

    TextView loginLink;
    TextInputEditText etEmail;
    TextInputEditText etPassword;
    TextInputEditText etConfirmPassword;
    MaterialButton btnRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        loginLink = findViewById(R.id.LoginLink);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(signUp.this, login.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> profile = new HashMap<>();
                            profile.put("email", email);
                            profile.put("name", "");
                            profile.put("location", "");
                            profile.put("phone", "");
                            FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(user.getUid())
                                    .setValue(profile, (error, ref) -> {
                                        if (error != null) {
                                            Toast.makeText(signUp.this, "Profile save failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                        Toast.makeText(signUp.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(signUp.this, login.class));
                        finish();
                    } else {
                        String message = "Registration failed. Please try again.";
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            message = "This email is already registered.";
                        }
                        Toast.makeText(signUp.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
