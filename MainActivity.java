package com.personalassistant.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(8, 9, 13);
    private final int panel = Color.rgb(22, 23, 31);
    private final int purple = Color.rgb(190, 164, 255);
    private LinearLayout messages;
    private ScrollView scroll;
    private EditText input;
    private Button send;
    private boolean busy = false;

    // Set this to your own HTTPS backend URL to enable real AI replies.
    // Never put a private AI provider API key inside this Android app.
    private static final String BACKEND_URL = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
        addMessage("Personal Assistant", "Hello! I'm your Personal Assistant. Ask me a question to get started.", false);
        addSuggestions();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        root.setPadding(dp(18), dp(16), dp(18), dp(12));
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, dp(4), 0, dp(18));
        TextView logo = new TextView(this);
        logo.setText("✦");
        logo.setTextColor(purple);
        logo.setTextSize(28);
        logo.setGravity(Gravity.CENTER);
        GradientDrawable iconBg = rounded(Color.rgb(35, 30, 49), 16);
        logo.setBackground(iconBg);
        LinearLayout.LayoutParams lpLogo = new LinearLayout.LayoutParams(dp(48), dp(48));
        lpLogo.setMargins(0, 0, dp(12), 0);
        header.addView(logo, lpLogo);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("Personal Assistant");
        title.setTextColor(Color.WHITE);
        title.setTextSize(19);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView subtitle = new TextView(this);
        subtitle.setText("YOUR AI • YOUR CONTROL");
        subtitle.setTextColor(Color.rgb(153, 150, 168));
        subtitle.setTextSize(10);
        subtitle.setLetterSpacing(0.08f);
        titles.addView(title);
        titles.addView(subtitle);
        header.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        TextView clear = new TextView(this);
        clear.setText("↻");
        clear.setTextColor(purple);
        clear.setTextSize(26);
        clear.setGravity(Gravity.CENTER);
        clear.setOnClickListener(v -> {
            messages.removeAllViews();
            addMessage("Personal Assistant", "New conversation. What can I help you with?", false);
        });
        header.addView(clear, new LinearLayout.LayoutParams(dp(42), dp(48)));
        root.addView(header);

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(39, 39, 49));
        root.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, dp(18), 0, dp(12));
        scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView foot = new TextView(this);
        foot.setText("AI responses require a connected backend");
        foot.setTextColor(Color.rgb(111, 111, 128));
        foot.setTextSize(11);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, dp(6), 0, dp(10));
        root.addView(foot);

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.CENTER_VERTICAL);
        composer.setPadding(dp(12), dp(5), dp(5), dp(5));
        composer.setBackground(rounded(panel, 18));
        input = new EditText(this);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.rgb(135, 134, 150));
        input.setHint("Message your assistant…");
        input.setTextSize(15);
        input.setSingleLine(false);
        input.setMaxLines(4);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setPadding(dp(8), dp(8), dp(6), dp(8));
        composer.addView(input, new LinearLayout.LayoutParams(0, -2, 1));

        send = new Button(this);
        send.setText("↑");
        send.setTextSize(23);
        send.setTextColor(Color.rgb(20, 16, 29));
        send.setAllCaps(false);
        send.setPadding(0, 0, 0, dp(3));
        send.setMinWidth(0);
        send.setMinimumWidth(0);
        send.setBackground(rounded(purple, 14));
        composer.addView(send, new LinearLayout.LayoutParams(dp(48), dp(48)));
        send.setOnClickListener(v -> sendMessage());
        root.addView(composer);
    }

    private void addSuggestions() {
        TextView hint = new TextView(this);
        hint.setText("TRY ASKING");
        hint.setTextColor(Color.rgb(132, 128, 151));
        hint.setTextSize(10);
        hint.setLetterSpacing(0.12f);
        hint.setPadding(0, dp(18), 0, dp(8));
        messages.addView(hint);
        String[] suggestions = {"Explain a topic", "Help me write", "Solve a problem"};
        for (String s : suggestions) {
            TextView chip = new TextView(this);
            chip.setText("✧  " + s);
            chip.setTextColor(purple);
            chip.setTextSize(13);
            chip.setPadding(dp(14), dp(10), dp(14), dp(10));
            chip.setBackground(rounded(Color.rgb(29, 26, 40), 14));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
            p.setMargins(0, 0, 0, dp(8));
            messages.addView(chip, p);
            chip.setOnClickListener(v -> {
                input.setText(s);
                input.setSelection(input.getText().length());
                input.requestFocus();
            });
        }
    }

    private void sendMessage() {
        if (busy) return;
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        input.setText("");
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(input.getWindowToken(), 0);
        addMessage("You", text, true);
        if (BACKEND_URL.trim().isEmpty()) {
            addMessage("Personal Assistant",
                "Your app is working, but real AI isn't connected yet. Add your own HTTPS backend URL in MainActivity.java (BACKEND_URL) to enable AI replies. Keep your AI provider API key on that backend, never in this APK.", false);
            return;
        }
        busy = true;
        send.setEnabled(false);
        TextView loading = addMessage("Personal Assistant", "Thinking…", false);
        new Thread(() -> {
            String answer;
            try { answer = requestBackend(text); }
            catch (Exception e) { answer = "I couldn't connect to the AI service. Check your internet connection and backend settings."; }
            final String result = answer;
            runOnUiThread(() -> {
                messages.removeView(loading.getParent() instanceof View ? (View) loading.getParent() : loading);
                addMessage("Personal Assistant", result, false);
                busy = false;
                send.setEnabled(true);
            });
        }).start();
    }

    private String requestBackend(String prompt) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(BACKEND_URL).openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        JSONObject body = new JSONObject();
        body.put("message", prompt);
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = c.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? c.getInputStream() : c.getErrorStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = stream.read(buffer)) > 0) out.write(buffer, 0, n);
        c.disconnect();
        JSONObject response = new JSONObject(out.toString("UTF-8"));
        if (status < 200 || status >= 300) throw new IOException("HTTP " + status);
        return response.optString("reply", "The AI service returned no reply.");
    }

    private TextView addMessage(String who, String body, boolean user) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setGravity(user ? Gravity.END : Gravity.START);
        group.setPadding(0, 0, 0, dp(15));
        TextView label = new TextView(this);
        label.setText(who.toUpperCase());
        label.setTextColor(user ? purple : Color.rgb(155, 151, 173));
        label.setTextSize(10);
        label.setLetterSpacing(0.08f);
        label.setPadding(dp(3), 0, dp(3), dp(5));
        group.addView(label);

        TextView message = new TextView(this);
        message.setText(body);
        message.setTextColor(Color.rgb(235, 233, 243));
        message.setTextSize(15);
        message.setLineSpacing(dp(3), 1f);
        message.setPadding(dp(14), dp(12), dp(14), dp(12));
        message.setBackground(rounded(user ? Color.rgb(43, 35, 61) : panel, 16));
        message.setMaxWidth((int)(getResources().getDisplayMetrics().widthPixels * 0.82));
        group.addView(message);
        messages.addView(group);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        return message;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
}
