package com.example.fluffybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fluffybuddy.model.PasswordHash;
import com.example.fluffybuddy.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    private TextView signInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.button_sup);
        signInTextView = findViewById(R.id.bar_signIN);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this, SignIn.class));
                finish();
            }
        });
    }

    private void signUp() {
        final String name = nameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Creating user account...")
                .setCancelable(false)
                .show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendVerificationEmail(name, email, password, dialog);
            } else {
                dialog.dismiss();
                Toast.makeText(SignUp.this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationEmail(final String name, final String email, final String password, final AlertDialog dialog) {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storeUserData(name, email, password, dialog);
            } else {
                dialog.dismiss();
                Toast.makeText(SignUp.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeUserData(String name, String email, String password, AlertDialog dialog) {
        String id = mAuth.getCurrentUser().getUid();
        Users user = new Users(name, email, password); // Make sure the Users class includes a field for password.
        databaseReference.child(id).setValue(user).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(SignUp.this, "User created successfully. Please verify your email.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                startActivity(new Intent(SignUp.this, SignIn.class));
                finish();
            } else {
                Toast.makeText(SignUp.this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}