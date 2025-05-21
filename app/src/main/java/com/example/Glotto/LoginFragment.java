package com.example.Glotto;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    // Required empty public constructor
    public LoginFragment() {
    }

    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginButton= view.findViewById(R.id.login);
        TextInputLayout emailTextField = view.findViewById(R.id.email);
        TextInputLayout passwordTextField = view.findViewById(R.id.password);

        loginButton.setOnClickListener(v -> {
            // Obtain the NavController from the current view
            Log.d("login", "I am clicked.");
            String email = emailTextField.getEditText().getText().toString().trim();
            String password = passwordTextField.getEditText().getText().toString().trim();
            Log.d("login", "email is " + email);
            Log.d("login", "password is " + password);

            if (email.isEmpty()) {
                emailTextField.setError("Email is required");
                return;
            } else {
                emailTextField.setError(null); // Clear previous error if any
            }

            if (password.isEmpty()) {
                passwordTextField.setError("Password is required");
                return;
            } else {
                passwordTextField.setError(null); // Clear previous error if any
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (!task.isSuccessful()) {
                            Log.d("login", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            return;
                        }

                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("login", "signInWithEmail:success, uid=" + user.getUid());

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("config")
                                .document("keys")
                                .get()
                                .addOnSuccessListener(document -> {
                                    Map<String,String> map = new HashMap<>();
                                    map.put("azure",  document.getString("azure"));
                                    map.put("openai", document.getString("openai"));
                                    ApiKeyManager.get().setKeys(map);
                                    updateUI(user);
                                })
                                .addOnFailureListener(err -> {
                                    Log.e("login", "Failed to load API keys", err);
                                    Toast.makeText(getActivity(),
                                            "Could not load configuration; please try again.",
                                            Toast.LENGTH_LONG).show();
                                    updateUI(null);
                                });
                    });

        });

    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ImageRecogFragment())
                    .commit();
        } else {

        }
    }
}