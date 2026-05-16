package com.heshan.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ValueEventListener profileListener;

    private EditText inputName;
    private EditText inputLocation;
    private EditText inputEmail;
    private EditText inputPhone;
    private ImageButton btnEditProfile;
    private ImageButton btnBackProfile;
    private MaterialButton btnSaveProfile;

    private boolean editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        inputName = findViewById(R.id.inputName);
        inputLocation = findViewById(R.id.inputLocation);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        inputEmail.setEnabled(false);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);


        mAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null) {
                detachProfileListener();
                startActivity(new Intent(profile.this, login.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            } else {
                attachProfileListener();
            }
        };

        btnEditProfile.setOnClickListener(v -> enableProfileEditing());
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
        detachProfileListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FooterBarHelper.setupFooter(this);
        applyEmailFromAuth();
    }

    private void applyEmailFromAuth() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            inputEmail.setText(user.getEmail());
        }
    }

    private void attachProfileListener() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        if (profileListener != null) {
            return;
        }
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ensureDefaultProfileDocument(user);
                    return;
                }
                if (editing) {
                    return;
                }
                String name = snapshot.child("name").getValue(String.class);
                String location = snapshot.child("location").getValue(String.class);
                String storedEmail = snapshot.child("email").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);

                inputName.setText(valueOrEmpty(name));
                inputLocation.setText(valueOrEmpty(location));
                if (!TextUtils.isEmpty(storedEmail)) {
                    inputEmail.setText(storedEmail);
                } else {
                    applyEmailFromAuth();
                }
                inputPhone.setText(valueOrEmpty(phone));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(profile.this, "Profile load error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).addValueEventListener(profileListener);
    }

    private void ensureDefaultProfileDocument(FirebaseUser user) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail() != null ? user.getEmail() : "");
        data.put("name", "");
        data.put("location", "");
        data.put("phone", "");
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .setValue(data);
    }

    private static String valueOrEmpty(String s) {
        return s != null ? s : "";
    }

    private void detachProfileListener() {
        if (profileListener != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).removeEventListener(profileListener);
            }
            profileListener = null;
        }
    }

    private void enableProfileEditing() {
        editing = true;
        inputName.setEnabled(true);
        inputLocation.setEnabled(true);
        inputPhone.setEnabled(true);
        btnSaveProfile.setVisibility(android.view.View.VISIBLE);
        btnEditProfile.setVisibility(android.view.View.GONE);
        Toast.makeText(this, "Edit your details, then tap Save", Toast.LENGTH_SHORT).show();
    }

    private void saveProfileChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        String name = inputName.getText() != null ? inputName.getText().toString().trim() : "";
        String location = inputLocation.getText() != null ? inputLocation.getText().toString().trim() : "";
        String phone = inputPhone.getText() != null ? inputPhone.getText().toString().trim() : "";
        String email = user.getEmail() != null ? user.getEmail() : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("location", location);
        updates.put("phone", phone);
        updates.put("email", email);

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .setValue(updates, (error, ref) -> {
                    if (error == null) {
                        editing = false;
                        inputName.setEnabled(false);
                        inputLocation.setEnabled(false);
                        inputPhone.setEnabled(false);
                        btnSaveProfile.setVisibility(android.view.View.GONE);
                        btnEditProfile.setVisibility(android.view.View.VISIBLE);
                        Toast.makeText(profile.this, "Profile saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(profile.this, "Save failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
