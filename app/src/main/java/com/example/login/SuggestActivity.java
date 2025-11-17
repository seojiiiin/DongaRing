package com.example.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.login.databinding.ActivitySuggestBinding;

import java.util.ArrayList;

public class SuggestActivity extends AppCompatActivity {
    ActivitySuggestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuggestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<String> selectedItems = getIntent().getStringArrayListExtra("selected");

        binding.volunteer.setVisibility(View.GONE);
        binding.study.setVisibility(View.GONE);
        binding.music.setVisibility(View.GONE);
        binding.liberal.setVisibility(View.GONE);
        binding.sport.setVisibility(View.GONE);
        binding.art.setVisibility(View.GONE);

        if (selectedItems  != null) {
            for (String item : selectedItems) {
                switch (item) {
                    case "volunteer" -> binding.volunteer.setVisibility(View.VISIBLE);
                    case "study" -> binding.study.setVisibility(View.VISIBLE);
                    case "music" -> binding.music.setVisibility(View.VISIBLE);
                    case "liberal" -> binding.liberal.setVisibility(View.VISIBLE);
                    case "sport" -> binding.sport.setVisibility(View.VISIBLE);
                    case "art" -> binding.art.setVisibility(View.VISIBLE);
                }
            }
        }

        binding.startButton.setOnClickListener(v ->
                startActivity(new Intent(SuggestActivity.this, MyPageActivity.class)));

    }
}