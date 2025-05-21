package com.example.Glotto;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Comparator;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class FlashcardsFragment extends Fragment {

    private String uid;
    private FirebaseFirestore db;

    // UI components
    private View cardFront;
    private View cardBack;
    private Button answerButton;
    private Button audioChecker;
    private Button audioPlay;
    private Button difficultButton;
    private Button okayButton;
    private Button easyButton;
    private TextView frontForeignWord;
    // Back side text views
    private TextView backForeignWord;
    private TextView englishMeaning;
    private TextView foreignSentence;
    private TextView englishSentence;
    private PriorityQueue<Flashcard> queue = new PriorityQueue<Flashcard>(Comparator.comparingDouble(Flashcard::getRetentionScore));
    private Flashcard currentCard;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flashcards, container, false);
    }

    //set up listeners
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind the two card faces
        cardFront = view.findViewById(R.id.card_front);
        cardBack = view.findViewById(R.id.card_back);

        // Front side UI
        answerButton = view.findViewById(R.id.answer_button);
        frontForeignWord = view.findViewById(R.id.front_foreign_word);

        // Back side UI
        backForeignWord = view.findViewById(R.id.back_foreign_word);
        englishMeaning = view.findViewById(R.id.english_meaning);
        foreignSentence = view.findViewById(R.id.foreign_sentence);
        englishSentence = view.findViewById(R.id.english_sentence);
        difficultButton = view.findViewById(R.id.difficult_button);
        okayButton = view.findViewById(R.id.okay_button);
        easyButton = view.findViewById(R.id.easy_button);
        audioPlay = view.findViewById(R.id.audio_button);
        audioChecker = view.findViewById(R.id.audio_checker);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("retrieve", "Got the id " + uid);
        db = FirebaseFirestore.getInstance();
        db.collection("userdeck")
                .document(uid)
                .collection("cards")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("retrieve", document.getId() + " => " + document.getData());
                                queue.add(new Flashcard(
                                        document.getId(),
                                        document.getString("english_meaning"),
                                        document.getString("english_sentence"),
                                        document.getString("foreign_sentence"),
                                        document.getString("audio"),
                                        document.getDouble("retention_score")
                                ));
                            }
                            // Now that the flashcards are loaded, update the UI if the list is not empty.
                            if (!queue.isEmpty()) {
                                currentCard = queue.peek();
                                showFront();
                                displayCurrentFlashcardFront();
                            } else {
                                Log.d("retrieve", "No flashcards available.");
                            }
                        } else {
                            Log.d("retrieve", "Error getting documents: ", task.getException());
                        }
                    }
                });


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        }


        // Set listener for Answer button
        answerButton.setOnClickListener(v -> showBack());

        audioChecker.setOnClickListener(v -> {
            // Create a new instance of the AudioRecordingDialogFragment
            String word = currentCard.getForeignWord();
            PopupRecordingFragment dialogFragment = new PopupRecordingFragment();//special fragment to host dialog small window
            Bundle args = new Bundle();
            args.putString("Word", word);
            dialogFragment.setArguments(args);
            Log.d("REST_API", "word sent: " + word);
            dialogFragment.show(getChildFragmentManager(), "AudioRecordingDialog"); //arbitary tag for identificiation purposes
        });

        FirebaseStorage storage = FirebaseStorage.getInstance();

        audioPlay.setOnClickListener(v -> {
            String path = currentCard.getAudioPath();
            StorageReference audioRef = storage.getReference().child(path);
            try {
                // Create a temporary file
                final File localFile = File.createTempFile("audioFile", "mp3");
                audioRef.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Successfully downloaded. Use the file to play via MediaPlayer.
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(localFile.getAbsolutePath());
                                    mediaPlayer.prepare();
                                    mediaPlayer.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                Log.e("Audio", "Failed to download file", exception);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        difficultButton.setOnClickListener(makeFeedbackListener(-1));
        okayButton.setOnClickListener(makeFeedbackListener(1));
        easyButton.setOnClickListener(makeFeedbackListener(2));
    }

    private void displayCurrentFlashcardFront() {
        if (currentCard != null) {
            frontForeignWord.setText(currentCard.getForeignWord());
        } else {
            frontForeignWord.setText("No cards for now");
        }
    }

    private void displayCurrentFlashcardBack() {
        if (currentCard != null) {
            backForeignWord .setText(currentCard.getForeignWord());
            englishMeaning .setText(currentCard.getEnglishMeaning());
            foreignSentence.setText(currentCard.getExampleForeign());
            englishSentence.setText(currentCard.getExampleEnglish());
        } else {
            backForeignWord .setText("No data for now");
            englishMeaning .setText("No data for now");
            foreignSentence.setText("No data for now");
            englishSentence.setText("No data for now");
        }
    }

    private void showFront() {
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setVisibility(View.GONE);
        if (currentCard == null) {
            answerButton.setVisibility(View.GONE);
        }
    }

    private void showBack() {
        displayCurrentFlashcardBack();
        cardFront.setVisibility(View.GONE);
        cardBack.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener makeFeedbackListener(double adjustment) {
        return v -> {
            Flashcard card = queue.poll();
            card.updateRetentionScore(adjustment);
            db.collection("userdeck")
                    .document(uid)
                    .collection("cards")
                    .document(card.getId())
                    .update("retention_score", card.getRetentionScore());
            currentCard = queue.peek();
            displayCurrentFlashcardFront();
            showFront();
        };
    }
}
