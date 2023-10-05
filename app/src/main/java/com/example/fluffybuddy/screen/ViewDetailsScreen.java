package com.example.fluffybuddy.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.fluffybuddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewDetailsScreen extends AppCompatActivity {

    private ImageView detailedImg;
    private TextView detailedName, detailedDescription, detailedAddress, detailedPostBy, detailedContact;
    private FirebaseFirestore firestore;
    private String itemId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_details_screen);

        ImageView deleteImageView = findViewById(R.id.trashDetails);
        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        detailedImg = findViewById(R.id.detailed_img);
        detailedName = findViewById(R.id.detailed_name);
        detailedDescription = findViewById(R.id.detailed_description);
        detailedPostBy = findViewById(R.id.detailed_postBy);
        detailedContact = findViewById(R.id.detailed_mobileNum);
        detailedAddress = findViewById(R.id.detailed_address);

        // Retrieve image URL from Firestore
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String itemName = extras.getString("itemName");
            String itemDescription = extras.getString("itemDescription");
            String itemLocation = extras.getString("itemLocation");
            String itemPostBy= extras.getString("itemPostBy");
            String itemContact = extras.getString("itemContact");
            String itemImageUrl = extras.getString("itemImageUrl");
            itemId = extras.getString("itemId"); // Replace "itemId" with the key used to pass the document ID.

            // Set the data to the corresponding TextViews
            detailedName.setText(itemName);
            detailedDescription.setText(itemDescription);
            detailedAddress.setText(itemLocation);


            // Load the image using Glide
            Glide.with(this)
                    .load(itemImageUrl)
                    .placeholder(R.drawable.ic_image) // Placeholder image while loading
                    .error(R.drawable.ic_error_image) // Image to display on error
                    .into(detailedImg);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference itemDataRef = databaseReference.child("Users");
            itemDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String mobileNumber = dataSnapshot.child("mobileNumber").getValue(String.class);

                        // Set the additional data to the TextViews
                        detailedPostBy.setText(itemPostBy);
                        detailedContact.setText(itemContact);
                    } else {
                        // Handle the case where the data does not exist
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error if data retrieval is canceled
                }
            });

            // Initialize Firestore reference
            firestore = FirebaseFirestore.getInstance();

            // Fetch additional data from Firestore for the selected item
            if (itemId != null) {
                DocumentReference itemRef = firestore.collection("uploads").document(itemId);
                itemRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve the additional data from the document
                                // Set the additional data to the TextView
                            } else {
                                // Document does not exist or is empty
                            }
                        } else {
                            // Handle error if data retrieval fails
                        }
                    }
                });
            }

        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Details");
        builder.setMessage("Are you sure you want to delete all details? This action cannot be undone.");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call the method to delete the details here
                deleteDetails();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void deleteDetails() {
        // Perform the deletion of details here
        // For example, set the TextViews to empty strings or remove data from the database
        TextView detailedNameTextView = findViewById(R.id.detailed_name);
        TextView detailedDescriptionTextView = findViewById(R.id.detailed_description);
        TextView detailedPostByTextView = findViewById(R.id.detailed_postBy);
        TextView detailedMobileNumTextView = findViewById(R.id.detailed_mobileNum);
        TextView detailedAddressTextView = findViewById(R.id.detailed_address);

        detailedNameTextView.setText("");
        detailedDescriptionTextView.setText("");
        detailedPostByTextView.setText("");
        detailedMobileNumTextView.setText("");
        detailedAddressTextView.setText("");
    }
}
