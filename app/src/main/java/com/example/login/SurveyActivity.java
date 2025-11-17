package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.login.databinding.ActivitySurveyBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class SurveyActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    ActivitySurveyBinding binding;
    ArrayList<String> selected = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySurveyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(SurveyActivity.this, MainActivity.class));
            finish();
            return;
        }

        binding.toVolunteer.setOnCheckedChangeListener(this);
        binding.toStudy.setOnCheckedChangeListener(this);
        binding.toMusic.setOnCheckedChangeListener(this);
        binding.toLiberal.setOnCheckedChangeListener(this);
        binding.toSport.setOnCheckedChangeListener(this);
        binding.toArt.setOnCheckedChangeListener(this);

        binding.nextButton.setOnClickListener(v -> {
            startActivity(new Intent(SurveyActivity.this, SuggestActivity.class)
                    .putStringArrayListExtra("selected", selected));
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String category = null;

        if (buttonView == binding.toVolunteer) category = "volunteer";
        else if (buttonView == binding.toStudy) category = "study";
        else if (buttonView == binding.toMusic) category = "music";
        else if (buttonView == binding.toLiberal) category = "liberal";
        else if (buttonView == binding.toSport) category = "sport";
        else if (buttonView == binding.toArt) category = "art";


        if (category != null) {
            Log.d("LSJ", category + " " + isChecked);
            if (isChecked) selected.add(category);
            else selected.remove(category);
        }
    }
}
