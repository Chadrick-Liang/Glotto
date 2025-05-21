package com.example.Glotto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // Load the default fragment Home
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }

        // Set up the bottom navigation to switch fragments
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null; //temp placeholder for current fragment
            int itemId = item.getItemId();
            if (itemId == R.id.nav_image) {
                selectedFragment = new ImageRecogFragment();
            } else if (itemId == R.id.nav_flashcards) {
                selectedFragment = new FlashcardsFragment();
            } else if (itemId == R.id.nav_game) {
                selectedFragment = new GameFragment();
            }            // Replace the container with the selected fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        });
    }


}
