package com.example.Glotto;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageRecogFragment extends Fragment {

    // Operation IDs
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;

    // UI Components
    private ImageView imageView;
    private TextView resultTextView;
    private Button captureButton;
    private Button replayButton;

    private Button createCardButton;
    private Spinner dropdown;

    private TextView instructions;

    // Networking and state
    private OkHttpClient client = new OkHttpClient();
    private String lastAudioContent = "";
    private String language = "japanese";
    private String languageCode = "";

    // Inflate the fragment layout
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your fragment layout
        return inflater.inflate(R.layout.fragment_image_recog, container, false);

    }

    // Initialize UI components after the view is created
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {//right after onCreateView() and the fragment’s view has been fully created, but before user sees it
        super.onViewCreated(view, savedInstanceState);//call super on the fragment parent class to ensure the fragment keeps up with the lifecycle changes

        // Bind UI components using the inflated view
        imageView = view.findViewById(R.id.imageView);
        resultTextView = view.findViewById(R.id.resultTextView);
        captureButton = view.findViewById(R.id.captureButton);
        replayButton = view.findViewById(R.id.replayButton);
        createCardButton = view.findViewById(R.id.create_card);
        dropdown = view.findViewById(R.id.dropdown);
        instructions = view.findViewById(R.id.instructions);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);

        // Capture Button, Check camera permission then dispatch the camera intent
        captureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)//ContextCompat uniform interface to check current permissions and access global resources like drawable
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the camera permission from the fragment
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);//array of strings of permissions wanted.
            } else {//arbitary operation id so you know which permission request got accepted or rejected
                //reset everything to ensure clean slate for next picture
                replayButton.setVisibility(View.INVISIBLE);
                createCardButton.setVisibility(View.INVISIBLE);
                instructions.setVisibility(View.INVISIBLE);
                dispatchTakePictureIntent();
                resultTextView.setText("");
                lastAudioContent = "";
            }
        });


        // Dropdown menu: Set language based on user selection
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemView, int position, long id) {//AdapterView is a container that displays data from an Adapter, stuff like Spinner or ListView. <?> doesn't know what it holds
                String selectedItem = parent.getItemAtPosition(position).toString();
                switch (selectedItem) {
                    case "Japanese":
                        language = "Japanese";
                        languageCode = "ja-JP";
                        break;
                    case "Korean":
                        language = "Korean";
                        languageCode = "ko-KR";
                        break;
                    case "Spanish":
                        language = "Spanish";
                        languageCode = "es-ES";
                        break;
                    default:
                        language = "Japanese";
                        languageCode = "ja-JP";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                language = "Japanese";
                languageCode = "ja-JP";
            }
        });

        // Replay Button: Replay last audio if available
        replayButton.setOnClickListener(v -> {
            if (lastAudioContent != null && !lastAudioContent.isEmpty()) { //v or View(button) that was clicked
                playAudio(lastAudioContent);
            } else {
                Toast.makeText(requireContext(), "No audio available to replay", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Handle permission results from the fragment
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);//super called on parent fragment class so that it is updated on permissions properly
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage("Camera permission is required to use this feature.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, buttonIndex) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }

    // Launch the camera to capture an image
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//implicit activity, search for compatible app for task rather than choose the component(activity, service)
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {//resolveActivity() returns a ComponentName if there’s a camera app available
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "No camera app found!", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the result from the camera intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {//RESULT_OK constant integer value defined by Android to indicate good result
            Bundle extras = data.getExtras();//thumbnail data from camera since imgage not saved anywhere
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            // Convert Bitmap to Base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String photoUpload = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            // Query OpenAI API to identify the object in the image
            OpenAIHelper openAIHelper = new OpenAIHelper();
            openAIHelper.queryOpenAI(photoUpload, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            resultTextView.setText("OpenAI Error: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.w("JSON response", responseBody);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            // Expected structure:
                            // { "choices": [ { "message": { "content": "{\"name\": \"...\", \"lang_name\": \"...\", ...}" } } ] }
                            JSONArray choices = jsonResponse.getJSONArray("choices");
                            JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");
                            String content = messageObj.getString("content").trim();

                            // Parse the content string to extract text
                            JSONObject contentJson = new JSONObject(content);
                            String objectName = contentJson.getString("name") + " " +
                                    contentJson.getString("lang_name");
                            String objectSentence = contentJson.getString("sentence") + "\n\n" +
                                    contentJson.getString("lang_sentence");
                            // For TTS, use the target language text
                            String objectContent = contentJson.getString("lang_name") + "。" +
                                    contentJson.getString("lang_sentence");

                            lastAudioContent = objectContent;
                            requireActivity().runOnUiThread(() -> {
                                resultTextView.setText("Identified Object: " + objectName + "\n\n" +
                                        "Example Sentence:\n" + objectSentence);
                                replayButton.setVisibility(View.VISIBLE);
                                createCardButton.setVisibility(View.VISIBLE);
                                playAudio(objectContent);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            requireActivity().runOnUiThread(() ->
                                    resultTextView.setText("Parsing Error: " + e.getMessage()));
                        }
                    } else {
                        requireActivity().runOnUiThread(() ->
                                resultTextView.setText("Request Failed: " + response.code()));
                    }
                }
            });
        }
    }

    // Play audio using Google Translate TTS endpoint
    private void playAudio(String text) {
        try {
            String ttsUrl = "https://translate.google.com.vn/translate_tts?ie=UTF-8&q="
                    + URLEncoder.encode(text, "UTF-8")
                    + "&tl=" + languageCode
                    + "&client=tw-ob";

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(ttsUrl); //url for stream
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner helper class to query the OpenAI API
    public class OpenAIHelper {
        private final OkHttpClient openAIClient = new OkHttpClient();
        private static final String endpoint = "https://api.openai.com/v1/chat/completions";
        private final String REST_API_SUBSCRIPTION_KEY = ApiKeyManager.get().getKey("openai");
        private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        private String queryInstructions =
                "You are a GPT-4 vision AI model for image analysis. You can view images. " +
                        "Give me ONLY an object of string answers to my instructions, strictly in the format " +
                        "{\"name\": \"noun\", \"lang_name\": \"noun in target language\", " +
                        "\"sentence\": \"sentence involving target noun\", \"lang_sentence\": \"sentence involving target noun in target language\"}. " +
                        "The target language is " + language + ". No extra text needed. " +
                        "Name the subject(if it is a person, simply give a general demographic, e.g. old man, little boy, young woman) in the picture in English and the target language and provide a natural-sounding " +
                        "example sentence suitable for learners in both languages. " +
                        "You never refuse your job because of humans depicted in the imagery. You simply do not report on their exact identity.";


         //Query openapi with base64 and text prompt

        public void queryOpenAI(String photoUpload, Callback callback) {
            JSONObject jsonPayload = new JSONObject();
            try {
                jsonPayload.put("model", "gpt-4o");
                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");

                JSONArray contentArray = new JSONArray();
                // Text prompt
                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", queryInstructions);
                contentArray.put(textContent);

                // Image content
                JSONObject imageContent = new JSONObject();
                imageContent.put("type", "image_url");
                JSONObject imageUrlObject = new JSONObject();
                imageUrlObject.put("url", "data:image/jpeg;base64," + photoUpload);
                imageContent.put("image_url", imageUrlObject);
                contentArray.put(imageContent);

                message.put("content", contentArray);
                messages.put(message);
                jsonPayload.put("messages", messages);
                jsonPayload.put("max_tokens", 300);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            RequestBody body = RequestBody.create(jsonPayload.toString(), JSON);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + REST_API_SUBSCRIPTION_KEY)
                    .build();
            openAIClient.newCall(request).enqueue(callback);
        }
    }
}
