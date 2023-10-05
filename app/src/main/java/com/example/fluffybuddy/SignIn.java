package com.example.fluffybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fluffybuddy.model.Users;
import com.example.fluffybuddy.screen.OnBoardingScreen;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private TextView signUpTextView, forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signInButton = findViewById(R.id.button_sin);
        signUpTextView = findViewById(R.id.bar_signUp);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignIn.this, SignUp.class));
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
    }

    private void showForgotPasswordDialog() {
        // Create a custom AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.forgot_password, null);

        // Set the custom layout as the view for the dialog
        builder.setView(view);

        // Find the EditText inside the custom layout
        EditText emailEditText = view.findViewById(R.id.emailEditText);

        // Set the positive button and its click listener
        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = emailEditText.getText().toString().trim();
            // Implement the logic to reset password using the provided email address
            // You can use FirebaseAuth.getInstance().sendPasswordResetEmail() here
            resetPassword(email);
        });

        // Set the negative button and its click listener
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // User canceled, dismiss the dialog
            dialog.dismiss();
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        dialog.show();

        // Customize the dialog's appearance
        // Set the background color and text color for the dialog's buttons
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }


    private void resetPassword(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Send a password reset email to the user's email address
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Toast.makeText(SignIn.this, "Password reset email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to send password reset email
                            Toast.makeText(SignIn.this, "Failed to send password reset email. Please check the email address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // Proceed with sign-in, user is authenticated and email is verified
                    // Fetch user data from the database and go to HomeActivity
                    fetchUserDataAndGoToHomeActivity(user.getUid());
                } else {
                    mAuth.signOut();
                    Toast.makeText(SignIn.this, "Please verify your email before signing in.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignIn.this, "Failed to sign in: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDataAndGoToHomeActivity(String userId) {
        Query userQuery = databaseReference.child(userId);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User data exists in the database
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        // Store user data in the global application state (optional)
                        // AppData.getInstance().setUser(user);

                        // Proceed to HomeActivity
                        startActivity(new Intent(SignIn.this, OnBoardingScreen.class));
                        finish();
                    }
                } else {
                    // User data does not exist in the database
                    Toast.makeText(SignIn.this, "User data not found in the database.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SignIn.this, "Failed to fetch user data from the database.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}