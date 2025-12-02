package com.example.login;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EventCollection extends AppCompatActivity {

    // Firestore 인스턴스 가져오기
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_collection);

        db = FirebaseFirestore.getInstance();

        // addAllEvents();
    }

    private void addAllEvents() {
        // 부모 문서 참조 (clubs의 예술창작회 문서 id)
        DocumentReference parentDocRef = db.collection("clubs").document("PzRpqeEHAcxtHwSFltKE");
        // 하위 컬렉션 참조 (예: clubs/각동아리문서/events)
        CollectionReference subcollectionRef = parentDocRef.collection("events");


        Map<String, Object> event1 = new HashMap<>();
        event1.put("title", "한강 출사");
        event1.put("startDate", "2025-12-15");
        event1.put("endDate", "2025-12-20");
        event1.put("content", "\uD83D\uDCC5 일정 안내\n" +
                "일시: 2025년 11월 9일(토) 오후 2시 ~ 저녁 7시\n" +
                "집결 장소: 여의도 한강공원 (2호선 여의나루역 2번 출구)\n" +
                "주제: 노을 속의 도시 풍경\n" +
                "\uD83D\uDCF7 활동 내용\n" +
                "팀별 또는 개인별 자유 촬영 진행\n" +
                "노을, 도시 풍경, 인물 등 다양한 주제 실습\n" +
                "단체 촬영 및 사진 리뷰 예정\n" +
                "촬영 종료 후 간단한 뒤풀이 진행 (참석 여부 별도 조사)\n" +
                "\uD83E\uDDF3 준비물\n" +
                "카메라 또는 휴대전화(촬영 가능 기기)\n" +
                "보조배터리, 삼각대(선택 사항)\n" +
                "개인 음료 및 방한복");
        event1.put("visivility", "clubOnly");

        // 하위 컬렉션에 문서 추가
        subcollectionRef.add(event1);
    }
}

