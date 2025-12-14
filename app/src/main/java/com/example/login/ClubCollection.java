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
            put("name", "SRC");
            put("president", "채하늘");
            put("founded", "2011년");
            put("activities", "숭실대학교 유일 중앙 와인 동아리");
            put("description", "와인에 관심있는 사람들이 모여 다양한 와인을 테이스팅하고, 테이스팅 노트를 작성하며 와인에 대한 지식을 넓힐 수 있는 동아리입니다.\n" +
                    "\n" +
                    "와인을 잘 모르시는 분들도 함께 와인에 대해 알아가며 서로 친목을 다지고 더불어 와인과도 친해질 수 있는 동아리입니다.\n" +
                    "\n" +
                    "와인으로 삶에 매력을 더하는 동아리라고 생각합니다 ◡̈ ⋆\n" +
                    "\n" +
                    "추가적으로 동아리방에는 와인에 대한 서적 외에도 보드게임 및 빔 프로젝터 등 다양한 컨텐츠가 준비되어 있어 편하게 즐기실 수 있습니다.\n" +
                    "\n" +
                    "\uD83D\uDCE3주요 활동\uD83D\uDCE3\n" +
                    "\n" +
                    "1. 매주 정기 활동에서 와인을 테이스팅하고 와인의 향, 맛, 여운 등의 특징과 매력에 대해 토의하며 테이스팅 노트를 작성하며 와인에 대한 지식과 경험을 쌓습니다.\n" +
                    "2. 종강 이후에 외부 장소를 대여해 서로의 와인을 나눠 마시는 와인 파티를 진행합니다.\n" +
                    "3. 다양한 주류 관련 행사와 시음회에 참석하며, 와인 산업과 문화를 직접 경험합니다.\n" +
                    "4. 비정기적인 와인 스터디를 통해서 와인에 대한 더 심도 있고 전문적인 지식을 함께 쌓아갑니다.\n" +
                    "5. 인스타그램(@srcsoongsil) 활동을 통해 테이스팅 기록을 남깁니다.\n" +
                    "6. MT, 한강 나들이 등 다양한 친목 활동도 진행하고 있습니다.\n");
        }});

        // 2. 연대사업분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "연대사업분과");
            put("name", "소리보임");
            put("president", "박지민");
            put("founded", "1999년");
            put("activities", "숭실대학교 중앙 수어봉사 동아리");
            put("description", "✅ 동아리소개\n" +
                    "숭실대학교 수어 봉사 동아리 소리보임은 수어를 학습하고, 이를 통해 봉사를 실천하는 동아리입니다.\n" +
                    "소리보임은 단순한 수어의 학습을 넘어, 수어를 매개로 한 사회적 가치 실현을 목표로 합니다.\n" +
                    "수어는 단순히 농인(청각장애인)의 것이 아닌, 당당한 한 언어의 종류이자, 농인과 청인이 서로 이해하고 공감할 수 있도록 돕는 중요한 연결고리 입니다.\n" +
                    "우리 소리보임은 수어 교육을 바탕으로 실질적 봉사 활동을 실천하여, 농인과의 상호 교류를 통해 포용적 사회를 만들어가는 데 기여하고자 합니다.\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "✅ 활동소개\n" +
                    "수어 학습 및 실습: 정기적인 스터디 및 교육 프로그램을 통한 체계적 수어 능력 향상\n" +
                    "봉사 활동: 삼성 소리샘 복지관과의 연계를 통한 자원봉사\n" +
                    "수어 동아리 연합회: 삼성 소리샘 복지관을 중심으로, 12개 대학의 수어동아리 연합 활동\n" +
                    "\n");
        }});

        // 3. 연행예술분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "연행예술분과");
            put("name", "블랙세인트");
            put("president", "남찬희");
            put("founded", "1980년");
            put("activities", "중앙 락밴드 동아리");
            put("description", "✅ 동아리소개\n" +
                    "뜨거운 것을 합니다.\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "✅ 활동소개\n" +
                    "합주\n" +
                    "학기 별 정기 공연\n" +
                    "학교축제 공연\n" +
                    "MT\n" +
                    "\n블랙세인트는 1980년 창단 이후 45년을 이어온 숭실대 최장수 밴드 동아리입니다!\n" +
                    "\n" +
                    "선발 후 팀 단위로 편성되어 활동하게 되며, 교내 축제, 정기공연, 새터 공연, 연합공연 등의 행사에 참여하게 됩니다! 때문에 최소 1년 이상 활동하는 것을 권장합니다.\n" +
                    "\n" +
                    "밴드 활동 외적으로는 신입생 환영회, 종강총회, MT 등의 활동이 있습니다.");
        }});

        // 4. 종교분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "종교분과");
            put("name", "가톨릭학생회");
            put("president", "현승민 요셉피나");
            put("founded", "1974년");
            put("activities", "숭실대학교 가톨릭동아리 프란치스코");
            put("description", "✅ 동아리소개\n" +
                    "각 본당에서 여러 봉사에 참여해온 대학생들이\n" +
                    "우리나라의 광복 이후 교회 안에 머무를 것이 아니라 세상으로 나아가\n" +
                    "세상을 바꿔나가야 한다는 믿음으로 \"대한 가톨릭 학생회\"를 창립하였고,\n" +
                    "1954년 교황 비오 10세께서 이끄신 세계 가톨릭 대학생 모임인 PAX ROMANA에 가입하였습니다.\n" +
                    "\n" +
                    "가톨릭학생회 프란치스코는 숭실대 동아리연합회 소속 동아리 중\n" +
                    "유일한 가톨릭 동아리이자 어느 학과나 쉽게 들어올 수 있는 중앙동아리 입니다.\n" +
                    "또한 서울대교구 가톨릭 대학생 연합회 소속인 연합동아리 입니다!\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "✅ 활동소개\n" +
                    "프란치스코의 주된 활동은 1주일에 한 번씩 진행하는 주모임,\n" +
                    "개강과 종강 때 진행하는 개강미사와 종강미사,\n" +
                    "과거 학생운동에 앞장 서시다 세상을 떠나신 신건수 분도님의 추모미사와,\n" +
                    "그리고 동아리 창립을 기념하는 창립제가 있습니다!\n" +
                    "\n" +
                    "종교적인 활동 뿐 아니라 여름, 겨울방학때 떠나는 MT와\n" +
                    "한 학기에 두 세 번 정도 동방에서의 밤샘파티를 진행하고 있습니다!\n" +
                    "\n" +
                    "또한 숭실대를 포함한 서울 내 35개 대학이 소속되어 있는\n" +
                    "서울대교구 가톨릭 대학생 연합회에서 진행하는 다양한 프로그램에 참여하실 수 있습니다!\n" +
                    "서가대연 행사에 참여하셔서 다양한 대학교 학생들과 교류해 보세요!\n");
        }});

        // 5. 창작전시분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "창작전시분과");
            put("name", "빛누리");
            put("president", "윤세혁");
            put("founded", "1971년");
            put("activities", "숭실대학교 필름 순수사진 동아리");
            put("description", "| 빛누리\n" +
                    "1971년 창립된 중앙사진동아리로, 필름 카메라부터 디지털 카메라까지 모두 다루고 있습니다.\n" +
                    "학생회관 310호에 위치해 있으며 유일무이하게 흑백 필름 현상 및 확대인화가 가능한 암실을 보유하고 있습니다!\n" +
                    "\n" +
                    "| 빛누리에서는✨\n" +
                    "1\uFE0F⃣ 고정 정기 출사 및 자유로운 번개 출사\n" +
                    "2\uFE0F⃣ 신인전, 미니 정기전과 외부 정기전\n" +
                    "3\uFE0F⃣ 현상인화의 날\n" +
                    "(필름 현상과 인화를 배우고 직접 해볼 수 있는 기회!)\n" +
                    "4\uFE0F⃣ 내부 월간지(월간 빛누리) 제작\n" +
                    "5\uFE0F⃣ 대여용 필름•디지털 카메라 비치\n" +
                    "6\uFE0F⃣ 할인된 가격으로 필름 판매\n" +
                    "\n" +
                    "| 동아리방\n" +
                    "학생회관 310호");
        }});

        // 6. 체육분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "체육분과");
            put("name", "숭검회");
            put("president", "공민기");
            put("founded", "1993년");
            put("activities", "숭실대학교 검도 동아리");
            put("description", "안녕하세요! 숭실대학교 중앙 검도 동아리 숭검회입니다.\n" +
                    "\n" +
                    "숭검회는 1993년부터 검도에 관심이 있는 사람들이 같이 모여 운동을 하기 시작해 지금까지 이어지고 있는 검도 동아리로 그 역사가 33년간 지속되고 있습니다.\n" +
                    "\n" +
                    "검도를 처음 시작하는 사람이든 검도를 했던 사람이든 상관없이 숭실대학교 신입생, 재학생들 모두 환영합니다. 현재 검도를 처음 접하는 학우분들을 위해 동방에 있는 도복과 죽도, 목검을 대여해 주고 있으니 검도 입문에 대한 두려움은 갖지 않으셔도 됩니다!\n" +
                    "\n" +
                    "동아리방 위치는 학생회관 152호입니다.\n");
        }});

        // 7. 학술분과
        clubs.add(new HashMap<String, Object>() {{
            put("type", "학술분과");
            put("name", "SSCC");
            put("president", "김성규");
            put("founded", "1982년");
            put("activities", "숭실대학교 최대 규모의 컴퓨터 학술 동아리");
            put("description", "❓SSCC는 어떤 동아리인가요?\n" +
                    "SSCC는 1983년에 설립된 숭실대학교 동아리 연합회 학술분과 소속, 교내 IT 특화 동아리입니다. 주된 활동으로 기초 언어 트랙, IT 관련 스터디, 팀 프로젝트 등 IT와 관련된 학술 활동을 진행하며 이외에도 MT, 짝선짝후, 대동제 부스 운영 등 부원들 간의 친목을 도모할 수 있는 행사들도 운영하고 있습니다. 뿐만 아니라 40여년의 유구한 역사를 가지고 있는 만큼 현재 IT업계에 계시는 많은 OB 선배님들께 많은 도움을 받으며 성장하고 있습니다!\uD83D\uDC4A\n" +
                    "\n" +
                    "\uD83D\uDD0DSSCC는 어떤 활동을 해왔나요?1\uFE0F⃣ 매월 진행되는 앱, 서버, AI 등 IT 관련 분야의 업계 선배님들의 정기 세미나\uD83E\uDDD1\u200D\uD83C\uDFEB\n" +
                    "2\uFE0F⃣ Python, PS, ML, PyTorch, 알고리즘 등 다양한 스터디\uD83E\uDDD1\u200D\uD83D\uDCBB\n" +
                    "3\uFE0F⃣ 초심자도 프로젝트 경험을 쌓아볼 수 있는 아이디어톤\uD83D\uDCA1\n" +
                    "4\uFE0F⃣ 한 학기동안 프로젝트를 진행하여 발표하는 해커톤 행사\uD83D\uDEA9\n" +
                    "5\uFE0F⃣ 업계에 종사 중이신 졸업생 선배님들과의 즐거운 만남, 홈커밍 데이 행사\uD83C\uDFE0\n" +
                    "\n" +
                    "\uD83D\uDD0DSSCC는 어떤 활동을 할 계획인가요?\n" +
                    "\n" +
                    "\uD83D\uDCCC 학술 활동 \uD83D\uDCCC\n" +
                    "\n" +
                    "1\uFE0F⃣ 스터디 운영\uD83D\uDCD6\n" +
                    "스터디장들이 운영하는 다양한 분야의 스터디에 참여할 뿐만 아니라 스터디를 직접 운영해 자신이 하고싶은 공부를 하며 지원을 받을 수 있습니다!\n" +
                    "ex) 알고리즘, PyTorch 등등\n" +
                    "\n" +
                    "2\uFE0F⃣ 공모전 참가\uD83C\uDFC6\n" +
                    "동아리 차원에서 팀을 모집해 공모전에 참가해 귀중한 경험을 쌓을 수 있습니다!\n" +
                    "\n" +
                    "\uD83D\uDCCC 친목 활동 \uD83D\uDCCC\n" +
                    "\n" +
                    "✅ 대동제 부스 운영\n" +
                    "✅ 번개 모임\n" +
                    "✅ UNICOSA 연합 MT\n" +
                    "✅ 연말 홈 커밍 데이\n" +
                    "✅ IT 박람회 견학\n" +
                    "\n" +
                    "\uD83D\uDCCD SSCC는 이런 분들을 기다립니다! \uD83D\uDCCD\n" +
                    "- 코딩 실력이 부족하더라도 SSCC에서 배우면서 함께 성장해나가실 분\uD83D\uDCAA\n" +
                    "- 누구보다 열정적으로 참여하며 즐거운 추억을 만들고 싶은 분\uD83C\uDF86\n" +
                    "- 고성능 컴퓨터, 굉장히 넓은 책상, 푹신한 2층 침대 등 작년에 리모델링하여 넓고 쾌적한 동방을 원하시는 분\uD83D\uDECF\uFE0F\n" +
                    "- 여러 부원들과의 만남을 통해 전과 팁이나 족보 등 학교생활에 큰 도움이 될 수 있는 선후배 관계를 맺고 싶은 분\uD83D\uDE4C\n" +
                    "- 타학과 또는 타학교 사람들과 어울리고 싶은 분\uD83C\uDF7A");
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
//    }
//}

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