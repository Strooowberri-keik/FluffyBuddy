package com.example.fluffybuddy.frags;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.fluffybuddy.R;
import com.example.fluffybuddy.model.itemModel;
import com.example.fluffybuddy.screen.ViewDetailsScreen;
import com.example.fluffybuddy.viewModel.ItemAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnimalListFragment extends Fragment implements ItemAdapter.OnItemClickListener {
    private FirebaseFirestore mFirestore;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animal_list, container, false);


        adapter = new ItemAdapter(new ArrayList<>());

        mFirestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);

        // Set the RecyclerView properties
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        // Initialize the adapter with an empty list (to avoid null issues)
        adapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        // Retrieve data from Firestore and display it in the RecyclerView
        retrieveDataFromFirestore();

        return view;
    }

    private void retrieveDataFromFirestore() {
        CollectionReference uploadsRef = mFirestore.collection("uploads");

        uploadsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<itemModel> uploadList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Convert each document to a custom UploadDataModel object
                        itemModel upload = document.toObject(itemModel.class);
                        uploadList.add(upload);
                    }

                    // Update the adapter with the new data
                    adapter.setItems(uploadList);
                } else {
                    // Handle errors and display a toast message
                    Toast.makeText(getActivity(), "Error retrieving data: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(itemModel item) {
        Intent intent = new Intent(getActivity(), ViewDetailsScreen.class);
        intent.putExtra("itemName", item.getName()); // Replace "getItemName()" with the appropriate method to get the item name
        intent.putExtra("itemDescription", item.getDescription());
        intent.putExtra("itemLocation", item.getLocation());
        intent.putExtra("itemImageUrl", item.getImageUrl());// Replace "getItemDescription()" with the appropriate method to get the item description
        // Add any other relevant data you want to pass to the ViewDetailsScreen activity
        startActivity(intent);

    }

}