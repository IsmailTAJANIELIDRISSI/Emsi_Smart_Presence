package com.example.myapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssistantActivity extends AppCompatActivity {
    private RecyclerView rvMessages;
    private TextInputEditText etMessage;
    private MaterialButton btnSend;
    private LinearProgressIndicator progressBar;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        // Initialize views
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        adapter = new MessageAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        // Send button click
        btnSend.setOnClickListener(v -> sendMessage());

        // Keyboard send action
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Add welcome message
        addBotMessage(getString(R.string.welcome_message));
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Add user message
            addUserMessage(messageText);
            etMessage.setText("");

            // Show typing indicator
            progressBar.setVisibility(View.VISIBLE);

            // Process with LLM (Gemini/Llama)
            processWithLLM(messageText);
        }
    }



    private void processWithLLM(String userInput) {
        String apiKey = "AIzaSyDaRnUNlu5a3ju6K7agPGN4QiexNixVePQ"; // Replace with your valid API key

        GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("You are a helpful professor's assistant. Answer concisely. Question: " + userInput)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String botResponse = result.getText();
                runOnUiThread(() -> {
                    addBotMessage(botResponse);
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    addBotMessage("Erreur : " + t.getMessage()); // Display the error
                    progressBar.setVisibility(View.GONE);
                });
                t.printStackTrace(); // Log to Android Studio's Logcat
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private String generateMockResponse(String input) {
        // TODO: Replace with actual LLM call
        if (input.toLowerCase().contains("attendance")) {
            return "You can mark attendance by going to the 'Attendance' tab and selecting the class.";
        } else if (input.toLowerCase().contains("schedule")) {
            return "Your weekly schedule is available in the 'Planning' section.";
        } else {
            return "I'm your virtual assistant. How can I help you with the app today?";
        }
    }

    private void addUserMessage(String text) {
        Message message = new Message(text, "user", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        rvMessages.smoothScrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        Message message = new Message(text, "bot", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        rvMessages.smoothScrollToPosition(messages.size() - 1);
    }

}