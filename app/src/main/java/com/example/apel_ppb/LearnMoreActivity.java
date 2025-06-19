package com.example.apel_ppb;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class LearnMoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);
        ImageButton backButton = findViewById(R.id.back_button);

        WebView webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        // Ambil URL dari Intent
        String url = getIntent().getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            // Fallback jika URL tidak valid
            webView.loadUrl("https://example.com/articles/apples");
        }

        backButton.setOnClickListener(v -> finish()); // Navigate back to previous activity
    }
}