package com.example.Glotto;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GameFragment extends Fragment {

    // Required empty public constructor
    public GameFragment() { }

    private ImageButton homeButton;
    private ImageButton stage2Button;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,//Instantiates a layout XML file into its corresponding View objects
                             ViewGroup container,//Container containing all the UI elements(Views),e.g. linearlayout
                             Bundle savedInstanceState) {//saved fragment state data when fragment is destroyed, ie leaving the app
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game, container, false);//attach to root is to attach the UI elements created to the container, android does it automatically so don't or will crash
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeButton= view.findViewById(R.id.home);
        stage2Button = view.findViewById(R.id.stage2);

        homeButton.setOnClickListener(v -> {
            // Obtain the NavController from the current view
            Log.d("home", "I am clicked.");
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ImageRecogFragment())
                    .commit();
        });

        stage2Button.setOnClickListener(v -> {
            // Obtain the NavController from the current view
            Log.d("stage 2", "I am clicked.");
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new GameLogicFragment())
                    .commit();
        });

    }

}
