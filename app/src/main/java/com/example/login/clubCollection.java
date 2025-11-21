package com.example.login;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class clubCollection extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // 동아리 데이터 추가
        //addAllClubs();
    }

    private void addAllClubs() {
        //CollectionReference clubs = db.collection("clubs");
        List<Map<String, Object>> clubs = new ArrayList<>();

        // 1. 교양분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "교양분과");
            put("name", "독서연구회");
            put("president", "김민준");
            put("founded", "2018-03-01");
            put("activities", "매달 독서토론 및 서평 교류");
        }});

        // 2. 연대사업분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "연대사업분과");
            put("name", "봉사나눔회");
            put("president", "이지은");
            put("founded", "2019-09-15");
            put("activities", "지역사회 연대활동 및 자원봉사 주관");
        }});

        // 3. 연행예술분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "연행예술분과");
            put("name", "공연예술회");
            put("president", "박서준");
            put("founded", "2017-05-10");
            put("activities", "공연기획, 연극 및 뮤지컬 제작 참여");
        }});

        // 4. 종교분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "종교분과");
            put("name", "한빛선교회");
            put("president", "정하늘");
            put("founded", "2016-10-01");
            put("activities", "주 1회 예배, 기도모임, 봉사활동");
        }});

        // 5. 창작전시분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "창작전시분과");
            put("name", "예술창작회");
            put("president", "최다은");
            put("founded", "2018-09-05");
            put("activities", "개인 작품 전시, 공동 프로젝트 기획");
        }});

        // 6. 체육분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "체육분과");
            put("name", "축구사랑회");
            put("president", "박지훈");
            put("founded", "2020-04-20");
            put("activities", "교내 리그 운영, 주 2회 훈련 및 친선 경기");
        }});

        // 7. 학술분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "학술분과");
            put("name", "AI연구회");
            put("president", "오세훈");
            put("founded", "2021-07-01");
            put("activities", "AI 세미나, 프로젝트 및 논문 스터디 진행");
        }});

        // Firestore에 모두 추가
        for (Map<String, Object> club : clubs) {
            db.collection("clubs")
                    .add(club)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("FirestoreAdd", "성공적으로 추가됨: " + club.get("name") +
                                " (문서 ID: " + documentReference.getId() + ")");
                    })
                    .addOnFailureListener(e -> {
                        Log.w("FirestoreAdd", "추가 실패: " + club.get("name"), e);
                    });
        }
    }
}