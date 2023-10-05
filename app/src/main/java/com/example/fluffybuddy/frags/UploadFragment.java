package com.example.fluffybuddy.frags;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fluffybuddy.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 1;

    private EditText animalNameEditText;
    private EditText animalDescriptionEditText;
    private EditText animalLocationEditText;
    private ImageView animalImageView;
    private Button uploadButton;

    private Uri imageUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        animalNameEditText = view.findViewById(R.id.uploadTopic);
        animalDescriptionEditText = view.findViewById(R.id.uploadDescription);
        animalLocationEditText = view.findViewById(R.id.enterLocation);
        animalImageView = view.findViewById(R.id.uploadImage);
        uploadButton = view.findViewById(R.id.uploadButton);

        animalLocationEditText.setEnabled(false);
        retrieveAddressFromFirebase();

        animalImageView.setOnClickListener(v -> openImagePicker());
        uploadButton.setOnClickListener(v -> uploadImageToFirebaseStorage());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);


        return view;
    }

    private void retrieveAddressFromFirebase() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("address");

        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String address = snapshot.getValue(String.class);
                    animalLocationEditText.setText(address);
                } else {
                    // Handle the case when the address doesn't exist in the database
                    // For example, you could set a default value or show an error message.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error case if the data retrieval is canceled
                // For example, you could show an error message to the user.
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            animalImageView.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage() {
        if (imageUri == null) {
            Toast.makeText(getActivity(), "Please select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveDataToFirestore(imageUrl);
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDataToFirestore(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference uploadsRef = mFirestore.collection("uploads");
        Map<String, Object> uploadData = new HashMap<>();
        uploadData.put("name", animalNameEditText.getText().toString().trim());
        uploadData.put("description", animalDescriptionEditText.getText().toString().trim());
        uploadData.put("location", animalLocationEditText.getText().toString().trim());
        uploadData.put("imageUrl", imageUrl);
        uploadData.put("userId", userId);

        uploadsRef.add(uploadData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Upload successful!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
