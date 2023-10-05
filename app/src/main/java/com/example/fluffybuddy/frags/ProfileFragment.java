package com.example.fluffybuddy.frags;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.fluffybuddy.R;
import com.example.fluffybuddy.SignIn;
import com.example.fluffybuddy.SignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private StorageReference storageRef;

    private ImageView profileIcon, addPhotoIcon;
    private EditText displayNameEditText, emailEditText, addressEditText, mobNumEditText;
    private Button saveProfileButton, deleteProfileButton;
    private ImageButton imgBtnLogout;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private boolean isProfileImageSet = false;
    private Uri pickedImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        imgBtnLogout = view.findViewById(R.id.btnLogout);

        profileIcon = view.findViewById(R.id.profileIcon);
        addPhotoIcon = view.findViewById(R.id.addPhotoIcon);
        displayNameEditText = view.findViewById(R.id.etDisplayName);
        emailEditText = view.findViewById(R.id.etEmail);
        addressEditText = view.findViewById(R.id.etAddress);
        mobNumEditText = view.findViewById(R.id.etMobNum);
        saveProfileButton = view.findViewById(R.id.btnSaveProfile);
        deleteProfileButton = view.findViewById(R.id.btnDeleteProfile);

        imgBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutConfirmationDialog();
            }
        });

        displayNameEditText.setEnabled(false);
        // Disable email EditText field from being edited
        emailEditText.setEnabled(false);

        // Get the current user from FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, you can access their data
            String uid = currentUser.getUid();
            String email = currentUser.getEmail();

            // Set the email to the corresponding EditText field
            emailEditText.setText(email);

            // Retrieve user data from the Realtime Database
            mDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Retrieve user data from the snapshot
                    String displayName = snapshot.child("name").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String mobileNumber = snapshot.child("mobileNumber").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);


                    // Set the retrieved data to the corresponding EditText fields
                    displayNameEditText.setText(displayName);
                    addressEditText.setText(address);
                    mobNumEditText.setText(mobileNumber);

                    // Load profile image using Glide and apply circular transformation
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(ProfileFragment.this)
                                .load(profileImageUrl)
                                .apply(RequestOptions.circleCropTransform()) // Apply circular transformation
                                .into(profileIcon);
                        isProfileImageSet = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error if retrieval is canceled
                    Toast.makeText(getActivity(), "Failed to retrieve user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Set OnClickListener for the Add Photo icon
            addPhotoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCamera();
                }
            });

            // Set OnClickListener for the Save Profile button
            saveProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Save the updated profile data to the Realtime Database
                    String displayName = displayNameEditText.getText().toString().trim();
                    String address = addressEditText.getText().toString().trim();
                    String mobileNumber = mobNumEditText.getText().toString().trim();

                    // Update the user's data in the Realtime Database
                    mDatabaseRef.child(uid).child("name").setValue(displayName);
                    mDatabaseRef.child(uid).child("address").setValue(address);
                    mDatabaseRef.child(uid).child("mobileNumber").setValue(mobileNumber);

                    if (pickedImageUri != null) {
                        // Profile image is set, upload the image to Firebase Storage
                        uploadImageToFirebase(uid, displayName, address, mobileNumber);
                    } else {
                        // No profile image selected, just update the other fields in the Realtime Database
                        mDatabaseRef.child(uid).child("name").setValue(displayName);
                        mDatabaseRef.child(uid).child("address").setValue(address);
                        mDatabaseRef.child(uid).child("mobileNumber").setValue(mobileNumber);
                        Toast.makeText(getActivity(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Set OnClickListener for the Delete Profile button
            deleteProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog();
                    // Delete the user's data from the Realtime Database
                }
            });
        } else {
            // User is not signed in, handle this case appropriately
            // For example, redirect the user to the sign-in activity
        }

        return view;
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call the logout function to sign out the user
                performLogout();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog if "No" is clicked
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void performLogout() {
        // Implement your logout functionality here
        // For example, if you are using Firebase Authentication, you can call:
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), SignIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // After signing out the user, you may navigate to the login screen or other appropriate actions.
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Profile");
        builder.setMessage("Are you sure you want to delete your profile? This action cannot be undone.");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User confirmed, delete profile and sign out
            deleteUserAccountAndSignOut();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // User canceled, dismiss the dialog
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void deleteUserAccountAndSignOut() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Delete the user's data from the Realtime Database
            mDatabaseRef.child(uid).removeValue();

            // Delete the user's account from FirebaseAuth
            currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Profile Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        // User account deleted, sign out and go back to the sign-up activity
                        mAuth.signOut();
                        startActivity(new Intent(getActivity(), SignUp.class));
                        requireActivity().finish(); // Finish the current activity (ProfileFragment)
                    } else {
                        Toast.makeText(getActivity(), "Failed to delete profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void uploadImageToFirebase(String uid, String displayName, String address, String mobileNumber) {
        // Create a reference to the location where you want to save the image in Firebase Storage
        StorageReference imageRef = storageRef.child("profile_images/" + uid + ".jpg");

        // Upload the image to Firebase Storage
        UploadTask uploadTask = imageRef.putFile(pickedImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image upload success, get the download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Save the download URL to the Realtime Database
                String profileImageUrl = uri.toString();
                mDatabaseRef.child(uid).child("profileImageUrl").setValue(profileImageUrl);

                // Update other fields in the Realtime Database
                mDatabaseRef.child(uid).child("name").setValue(displayName);
                mDatabaseRef.child(uid).child("address").setValue(address);
                mDatabaseRef.child(uid).child("mobileNumber").setValue(mobileNumber);

                Toast.makeText(getActivity(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Handle any errors while getting the download URL
                Toast.makeText(getActivity(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Handle any errors while uploading the image
            Toast.makeText(getActivity(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                profileIcon.setImageBitmap(imageBitmap);
                isProfileImageSet = true; // Set the flag to true when an image is set
            }
        }
    }
}
