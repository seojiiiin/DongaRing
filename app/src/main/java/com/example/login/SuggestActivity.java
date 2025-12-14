package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login.databinding.ActivitySuggestBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class SuggestActivity extends AppCompatActivity {
    ActivitySuggestBinding binding;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuggestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<String> selectedItems = getIntent().getStringArrayListExtra("selected");

        db = FirebaseFirestore.getInstance();

        binding.volunteer.setVisibility(View.GONE);
        binding.study.setVisibility(View.GONE);
        binding.music.setVisibility(View.GONE);
        binding.liberal.setVisibility(View.GONE);
        binding.sport.setVisibility(View.GONE);
        binding.art.setVisibility(View.GONE);

        if (selectedItems != null) {
            for (String item : selectedItems) {
                switch (item) {
                    case "liberal" -> {
                        binding.liberal.setVisibility(View.VISIBLE);
                        loadClubNames("교양분과", binding.liberal);
                    }
                    case "volunteer" -> {
                        binding.volunteer.setVisibility(View.VISIBLE);
                        loadClubNames("연대사업분과", binding.volunteer);
                    }
                    case "music" -> {
                        binding.music.setVisibility(View.VISIBLE);
                        loadClubNames("연행예술분과", binding.music);
                    }
                    case "art" -> {
                        binding.art.setVisibility(View.VISIBLE);
                        loadClubNames("창작전시분과", binding.art);
                    }
                    case "sport" -> {
                        binding.sport.setVisibility(View.VISIBLE);
                        loadClubNames("체육분과", binding.sport);
                    }
                    case "study" -> {
                        binding.study.setVisibility(View.VISIBLE);
                        loadClubNames("학술분과", binding.study);
                    }
                }
            }
        }

        binding.startButton.setOnClickListener(v -> startActivity(new Intent(SuggestActivity.this, MyPageActivity.class)));
    }

    private void loadClubNames(String type, View textView) {
        Log.d("동아링", "loadClubNames 호출됨: type=" + type);

        db.collection("clubs")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(q -> {
                    Log.d("동아링", "성공! 문서 개수: " + q.size());
                    StringBuilder builder = new StringBuilder();
                    for (QueryDocumentSnapshot doc : q) {
                        String name = doc.getString("name");
                        builder.append("• ").append(name).append("\n");
                    }

                    if (builder.length() == 0) {
                        ((TextView) textView).setText("해당 분과의 동아리가 없습니다.");
                    } else {
                        ((TextView) textView).setText(builder.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("동아링", "에러 발생:", e);
                    ((TextView) textView).setText("데이터 불러오기 실패");
                });

    }
}