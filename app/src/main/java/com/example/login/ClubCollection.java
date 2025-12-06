package com.example.login;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClubCollection extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // 동아리 데이터 추가
        addAllClubs();
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
            put("description", "독서연구회는 2003년에 창립된 학술 독서 기반 동아리로, 동아리방은 학생회관 312호에 있습니다.\n\n" +
                    "다양한 분야의 책을 선정해 함께 읽고 토론하며 사고의 깊이를 넓히는 활동을 주로 진행합니다.\n" +
                    "독서 모임, 주제별 토론회, 도서 리뷰 작성을 비롯해 소규모 스터디팀 운영 등 지적 교류를 중심으로 한 활동이 활발합니다.\n\n" +
                    "책을 통해 새로운 관점을 배우고 싶은 학생들이 꾸준히 참여하는 학술 동아리입니다.");
        }});

        // 2. 연대사업분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "연대사업분과");
            put("name", "봉사나눔회");
            put("president", "이지은");
            put("founded", "2019-09-15");
            put("activities", "지역사회 연대활동 및 자원봉사 주관");
            put("description", "봉사나눔회는 2019년에 창립되어 지역사회 연대활동과 자원봉사를 중심으로 활동하는 동아리입니다.\n\n" +
                    "동아리방은 학생회관 243호에 위치해 있으며, 도움이 필요한 지역 기관과 연계한 봉사 프로그램, 환경·노인·아동복지 관련 프로젝트 등을 꾸준히 진행하고 있습니다.\n" +
                    "정기 봉사 외에도 자체 기획 행사와 캠페인을 운영하여 회원들이 사회문제에 관심을 가지고 직접 실천하는 경험을 쌓을 수 있도록 돕는 공동체 중심 동아리입니다.");
        }});

        // 3. 연행예술분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "연행예술분과");
            put("name", "공연예술회");
            put("president", "박서준");
            put("founded", "2017-05-10");
            put("activities", "공연기획, 연극 및 뮤지컬 제작 참여");
            put("description", "공연예술회는 2011년에 창립된 공연 중심 동아리이며 동아리방은 학생회관 203호에 자리하고 있습니다.\n\n" +
                    "음악, 연극, 무용 등 다양한 공연 콘텐츠를 기획하고 무대에 올리는 활동이 핵심이며, 정기 공연과 학교 행사 참여를 통해 공연 경험을 쌓을 수 있습니다.\n" +
                    "팀별 연습, 무대 기획, 퍼포먼스 개발 등 공연 제작의 전 과정을 스스로 운영함으로써 협업 능력과 표현력을 함께 키울 수 있는 동아리입니다.");
        }});

        // 4. 종교분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "종교분과");
            put("name", "한빛선교회");
            put("president", "정하늘");
            put("founded", "2016-10-01");
            put("activities", "주 1회 예배, 기도모임, 봉사활동");
            put("description", "한빛선교회는 2005년에 창립된 신앙 기반 공동체 동아리입니다.\n\n" +
                    "동아리방은 학생회관 221호에 위치해 있으며, 함께 예배하고 성경을 공부하는 정기 모임을 중심으로 활동합니다.\n" +
                    "말씀 나눔, 기도 모임, 소규모 스터디, 친교 활동 등을 통해 공동체적 유대감을 형성하고 있습니다. 또한 봉사 및 아웃리치 활동 등을 통해 신앙적 가치를 실천하는 프로그램도 함께 운영합니다.\n");
        }});

        // 5. 창작전시분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "창작전시분과");
            put("name", "예술창작회");
            put("president", "최다은");
            put("founded", "2018-09-05");
            put("activities", "개인 작품 전시, 공동 프로젝트 기획");
            put("description", "예술창작회는 2008년에 창립되었으며, 동아리방은 학생회관 108호에 위치해 있습니다.\n\n" +
                    "회화·조형·디지털아트 등 다양한 창작 활동을 진행하며, 정기 창작 모임을 통해 개인 작품을 제작하고 전시회나 온라인 갤러리에서 결과물을 공개합니다.\n" +
                    "회원들은 서로의 작품을 감상하며 피드백을 나누고, 예술적 표현을 발전시키기 위한 워크숍과 공동 프로젝트도 주기적으로 진행합니다.\n예술적 창작을 즐기는 학생들을 기다립니다!");
        }});

        // 6. 체육분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "체육분과");
            put("name", "축구사랑회");
            put("president", "박지훈");
            put("founded", "2020-04-20");
            put("activities", "교내 리그 운영, 주 2회 훈련 및 친선 경기");
            put("description", "축구사랑회는 1998년에 창립된 스포츠 교류 동아리로, 동아리방은 학생회관 105호에 있습니다.\n\n" +
                    "정기 연습, 팀 훈련, 친선 경기, 교내·교외 리그 참여 등 축구 중심의 활동을 꾸준히 진행합니다.\n" +
                    "실력에 관계없이 함께 뛰며 팀워크를 쌓는 것을 중요하게 생각하고, 체력 강화와 친목 도모를 동시에 이룰 수 있는 활기찬 분위기의 스포츠 동아리입니다.");
        }});

        // 7. 학술분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "학술분과");
            put("name", "AI연구회");
            put("president", "오세훈");
            put("founded", "2021-07-01");
            put("activities", "AI 세미나, 프로젝트 및 논문 스터디 진행");
            put("description", "AI연구회는 2021년에 창립된 기술 연구 동아리로, 동아리방은 학생회관 217호에 있습니다.\n\n" +
                    "머신러닝, 딥러닝, 데이터 분석 등 인공지능 기술 전반을 학습하며, 스터디와 실습 중심의 활동을 진행합니다.\n" +
                    "팀·개인 프로젝트를 통해 실제 모델을 개발해 보고, 세미나나 발표회를 통해 학습한 내용을 공유합니다.\n\n" +
                    "AI를 배우고 연구하고 싶은 학생들이 함께 성장할 수 있는 기술 중심 동아리입니다.");
        }});

        // Firestore에 모두 추가
//        for (Map<String, Object> club : clubs) {
//            db.collection("clubs")
//                    .add(club)
//                    .addOnSuccessListener(documentReference -> {
//                        Log.d("FirestoreAdd", "성공적으로 추가됨: " + club.get("name") +
//                                " (문서 ID: " + documentReference.getId() + ")");
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.w("FirestoreAdd", "추가 실패: " + club.get("name"), e);
//                    });
//        }

        for (Map<String, Object> club : clubs) {

            String name = club.get("name").toString();

            db.collection("clubs")
                    .whereEqualTo("name", name)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            // 이미 기존 문서가 존재 → 문서ID 가져와서 업데이트
                            String docId = query.getDocuments().get(0).getId();

                            db.collection("clubs")
                                    .document(docId)
                                    .set(club, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", name + " 업데이트 완료");
                                    });

                        } else {
                            // 문서가 없음 → 새로 생성
                            db.collection("clubs")
                                    .add(club)
                                    .addOnSuccessListener(ref -> {
                                        Log.d("Firestore", name + " 새로 생성됨");
                                    });
                        }
                    });
        }

    }
}