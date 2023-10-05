package com.example.fluffybuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.fluffybuddy.frags.AnimalListFragment;
import com.example.fluffybuddy.frags.ProfileFragment;
import com.example.fluffybuddy.frags.UploadFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navigationView;
    private FragmentManager fragmentManager;


    private BottomNavigationView.OnNavigationItemSelectedListener navItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    Fragment selectedFragment = null;
                    if (item.getItemId() == R.id.list) {
                        selectedFragment = new AnimalListFragment();
                    }else if (item.getItemId() == R.id.user_upload) {
                        selectedFragment = new UploadFragment();
                    }else if (item.getItemId() == R.id.user_profile) {
                        selectedFragment = new ProfileFragment();
                    }

                    if (selectedFragment != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.container, selectedFragment);
                        transaction.commit();
                        return true;
                    }

                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setOnNavigationItemSelectedListener(navItemSelectedListener);

        // Set the default fragment
        navigationView.setSelectedItemId(R.id.user_upload);
    }

        @Override
        public void onBackPressed () {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
            materialAlertDialogBuilder.setTitle(R.string.app_name);
            materialAlertDialogBuilder.setMessage("Are you sure you want to log out?");
            materialAlertDialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            materialAlertDialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Redirect to login UI
                    Intent intent = new Intent(MainActivity.this, SignIn.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            materialAlertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    // Prevent the dialog from being dismissed by pressing the back button
                }
            });
            materialAlertDialogBuilder.show();
        }
    }