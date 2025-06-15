package com.example.apel_ppb;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LearnMoreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        findViewById(R.id.button_varieties).setOnClickListener(v -> {
            Intent intent = new Intent(LearnMoreActivity.this, VarietiesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.button_diseases).setOnClickListener(v -> {
            Intent intent = new Intent(LearnMoreActivity.this, DiseasesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.button_care_tips).setOnClickListener(v -> {
            Intent intent = new Intent(LearnMoreActivity.this, CareTipsActivity.class);
            startActivity(intent);
        });
    }
}