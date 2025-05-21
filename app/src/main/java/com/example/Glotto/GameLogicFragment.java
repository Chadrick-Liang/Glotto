package com.example.Glotto;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class GameLogicFragment extends Fragment {

    public GameLogicFragment() { }

    private ImageButton choice1Button, choice2Button, choice3Button;
    private TextView questionText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_logic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize your views using the inflated view
        choice1Button = view.findViewById(R.id.choice1);
        choice2Button = view.findViewById(R.id.choice2);
        choice3Button = view.findViewById(R.id.choice3);
        questionText = view.findViewById(R.id.questionText); // if needed for displaying the question

        // Set click listeners for each button
        choice1Button.setOnClickListener(v -> {
            Log.d("GameLogicFragment", "Choice 1 clicked - Fail");
            showFailDialog();
        });

        choice2Button.setOnClickListener(v -> {
            Log.d("GameLogicFragment", "Choice 2 clicked - Success");
            showSuccessDialog();
        });

        choice3Button.setOnClickListener(v -> {
            Log.d("GameLogicFragment", "Choice 3 clicked - Fail");
            showFailDialog();
        });
    }

    private void showFailDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Quiz Failed")
                .setMessage("Sorry, you've failed the quiz.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate to home (replace GameFragment with your actual home fragment)
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new GameFragment())
                            .commit();
                })
                .setCancelable(false)
                .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Quiz Passed")
                .setMessage("Congratulations! You passed the quiz.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back to home (replace GameFragment with your intended fragment)
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new GameFragment())
                            .commit();
                })
                .setCancelable(false)
                .show();
    }
}
