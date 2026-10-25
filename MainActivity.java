
package com.personalassistant.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    LinearLayout layout, messages;
    EditText input;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
            }
        });

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setBackgroundColor(Color.rgb(15, 15, 25));

        TextView title = new TextView(this);
        title.setText("Personal Assistant");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your personal AI companion");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, 20, 0, 20);
        scroll.addView(messages);

        layout.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1));

        addMessage("Assistant",
                "Hello! Ask me a question, use the microphone, or say Open YouTube.");

        input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Type your message...");
        input.setHintTextColor(Color.GRAY);
        input.setTextColor(Color.WHITE);
        layout.addView(input);

        Button send = new Button(this);
        send.setText("Send");
        layout.addView(send);

        Button voice = new Button(this);
        voice.setText("🎤 Speak");
        layout.addView(voice);

        Button speak = new Button(this);
        speak.setText("🔊 Speak welcome message");
        layout.addView(speak);

        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                input.setText("");
                handleCommand(text);
            }
        });

        voice.setOnClickListener(v -> startVoiceInput());

        speak.setOnClickListener(v ->
                tts.speak("Hello! Your personal assistant is ready.",
                        TextToSpeech.QUEUE_FLUSH, null, "welcome"));

        setContentView(layout);
    }

    void addMessage(String who, String text) {
        TextView message = new TextView(this);
        message.setText(who + ": " + text);
        message.setTextColor(Color.WHITE);
        message.setTextSize(16);
        message.setPadding(8, 12, 8, 12);
        messages.addView(message);
    }

    void handleCommand(String text) {
        addMessage("You", text);
        String command = text.toLowerCase(Locale.ROOT);

        if (command.contains("open youtube")) {
            openApp("com.google.android.youtube", "YouTube");
        } else if (command.contains("open settings")) {
            try {
                startActivity(new Intent(
                        android.provider.Settings.ACTION_SETTINGS));
                addMessage("Assistant", "Opening Settings.");
            } catch (Exception e) {
                addMessage("Assistant", "Could not open Settings.");
            }
        } else if (command.contains("stop speaking")) {
            if (tts != null) tts.stop();
            addMessage("Assistant", "Stopped speaking.");
        } else {
            String reply =
                    "I received your question. AI answers need to be connected to a secure AI service in the next step.";
            addMessage("Assistant", reply);
            if (tts != null) {
                tts.speak(reply, TextToSpeech.QUEUE_FLUSH,
                        null, "reply");
            }
        }
    }

    void openApp(String packageName, String appName) {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(packageName);

        if (intent != null) {
            startActivity(intent);
        } else {
            addMessage("Assistant",
                    appName + " is not installed or cannot be opened.");
        }
    }

    void startVoiceInput() {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        try {
            startActivityForResult(intent, 100);
        } catch (Exception e) {
            addMessage("Assistant",
                    "Voice recognition is unavailable on this phone.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
            int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK
                && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                handleCommand(results.get(0));
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}