package com.example.Glotto;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.github.squti.androidwaverecorder.WaveRecorder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


public class PopupRecordingFragment extends DialogFragment {

    // UI components definition.
    private ProgressBar audioBar;
    private Button stopRecord;
    private Button playRecord;
    private ImageButton closeRecord;
    private TextView pronounceResult;

    // Handler for UI updates.
    private Handler handler = new Handler();
    private boolean isRecording = false;
    private String word;

    // MediaRecorder for recording audio to a file.
    private String audioFilePath;
    private WaveRecorder waveRecorder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the popup layout with Views
        View view = inflater.inflate(R.layout.popup_recording, container, false);

        if (getArguments() != null) {
            word = getArguments().getString("Word");
            Log.d("REST_API", "word received: " + word);
        }

        // Bind UI components
        audioBar = view.findViewById(R.id.audioBar);
        stopRecord = view.findViewById(R.id.stopRecord);
        playRecord = view.findViewById(R.id.playRecord);
        closeRecord = view.findViewById(R.id.closeRecord);
        pronounceResult = view.findViewById(R.id.pronounceResult);

        audioFilePath = requireContext().getCacheDir().getAbsolutePath() + "/recorded_audio.wav";
        waveRecorder = new WaveRecorder(audioFilePath);

        // Set up listeners
        stopRecord.setOnClickListener(v -> {
            stopRecording();
            Log.d("Recorded file", requireContext().getCacheDir().getAbsolutePath() + "/recorded_audio.wav");
            // Call the REST API to submit the recorded file for assessment.
            queryRestApiAssessment();
        });
        playRecord.setOnClickListener(v -> playRecording());
        closeRecord.setOnClickListener(v -> dismiss());

        // Start recording when the popup is shown.
        isRecording = true;
        waveRecorder.startRecording();
        updateAudioBar();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    // Update the audio bar with a random values to indicate recording activity.
    private void updateAudioBar() {
        if (!isRecording) return;
        int progress = (int) (Math.random() * 100);
        audioBar.setProgress(progress);
        handler.postDelayed(this::updateAudioBar, 200);
    }

    // Stop recording and release MediaRecorder resources.
    private void stopRecording() {
        isRecording = false;
        handler.removeCallbacksAndMessages(null);
        waveRecorder.stopRecording();

        // Update UI: show playback button, hide stop button and audio bar.
        playRecord.setVisibility(View.VISIBLE);
        stopRecord.setVisibility(View.GONE);
        audioBar.setVisibility(View.GONE);
    }

    // Play the recorded file using MediaPlayer.
    private void playRecording() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(requireContext().getCacheDir().getAbsolutePath() + "/recorded_audio.wav");
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
        } catch (IOException e) {
            e.printStackTrace();
            pronounceResult.setText("Playback error: " + e.getMessage());
        }
    }

    private static final String REST_API_SUBSCRIPTION_KEY = ApiKeyManager.get().getKey("azure");
    private final OkHttpClient azureClient = new OkHttpClient();
    private final String test = ApiKeyManager.get().getKey("hello");

    private void queryRestApiAssessment() {
        new Thread(() -> {
            try {
                // This JSON string contains the parameters
                String pronAssessmentParamsJson = "{\"ReferenceText\":\"" + word + "\",\"GradingSystem\":\"HundredMark\",\"Granularity\":\"Word\",\"Dimension\":\"Basic\",\"EnableProsodyAssessment\":\"True\"}";
                // Convert to UTF-8 bytes.
                byte[] pronAssessmentParamsBytes = pronAssessmentParamsJson.getBytes("UTF-8");
                // Convert bytes to a Base64-encoded string
                String pronAssessmentHeader = Base64.encodeToString(pronAssessmentParamsBytes, Base64.NO_WRAP);

                // Build the endpoint URL with query parameters
                String url = "https://southeastasia.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=ja-JP&format=detailed";

                // Create a RequestBody from the recorded file (make sure it is a WAV file)
                File audioFile = new File(requireContext().getCacheDir().getAbsolutePath() + "/recorded_audio.wav");
                RequestBody requestBody = RequestBody.create(audioFile, MediaType.parse("audio/wav; codecs=audio/pcm; samplerate=16000"));

                // Build the HTTP POST request with the required components.
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        //.addHeader("Accept", "application/json;text/xml")
                        .addHeader("Content-Type", "audio/wav; codecs=audio/pcm; samplerate=16000")
                        .addHeader("Ocp-Apim-Subscription-Key", REST_API_SUBSCRIPTION_KEY)
                        .addHeader("Pronunciation-Assessment", pronAssessmentHeader)
                        .build();

                Response response = azureClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Log.i("REST_API", "Response: " + response);
                    Log.i("REST_API", "JSONResponse: " + jsonResponse);
                    Log.i("REST_API", "HTTP Status Code: " + response.code());
                    Log.i("REST_API", "Headers: " + response.headers().toString());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonResponse);
                                JSONArray nBestArray = jsonObject.getJSONArray("NBest");
                                JSONObject nestedObj = nBestArray.getJSONObject(0);
                                double accuracy = nestedObj.getDouble("AccuracyScore");
                                pronounceResult.setText("Assessment: " + accuracy + "/100.0");
                            } catch (JSONException e) {
                                pronounceResult.setText("Error: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                pronounceResult.setText("Upload failed: HTTP " + response.code()));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            pronounceResult.setText("Error: " + ex.getMessage()));
                }
            }
        }).start();
    }

}
