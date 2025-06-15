package com.example.apel_ppb;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CareTipsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_tips);

        findViewById(R.id.button_back).setOnClickListener(v -> {
            finish(); // Kembali ke activity sebelumnya
        });
    }
}