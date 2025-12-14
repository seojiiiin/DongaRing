package com.example.login;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.login.databinding.FragmentAdminCalendarBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminCalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminCalendarFragment extends Fragment {
    private FragmentAdminCalendarBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final HashMap<CalendarDay, List<Event>> eventMap = new HashMap<>();
    private final HashSet<CalendarDay> eventDates = new HashSet<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AdminCalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminCalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminCalendarFragment newInstance(String param1, String param2) {
        AdminCalendarFragment fragment = new AdminCalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminCalendarBinding.inflate(inflater, container, false);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 캘린더 기본 설정
        binding.calendar.addDecorator(new TodayDecorator());

        // 날짜 선택 리스너 (선택 시 해당 날짜 이벤트 표시 로직 등 추가 가능)
        binding.calendar.setOnDateChangedListener((widget, date, selected) -> {
            // 선택한 날짜의 이벤트를 하단 텍스트뷰에 표시
            showEventsForDate(date);
        });

        // 이벤트 데이터 로드 시작
        loadEvents();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.gear.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.full_screen_container, new SettingFragment("admin"))
                    .addToBackStack(null)
                    .commit();
        });
    }
    private void loadEvents() {
        eventMap.clear();
        eventDates.clear();

        if (mAuth.getCurrentUser() == null) return;

        String myUid = mAuth.getCurrentUser().getUid();

        // 1. 관리자 정보 조회 (내가 어떤 동아리의 관리자인지 확인)
        db.collection("users_admin")
                .whereEqualTo("uid", myUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // 1. 검색된 문서가 있음
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("JHM", "관리자 정보 찾음 (Doc ID): " + document.getId());
                                Log.d("JHM", "User Admin Data: " + document.getData());

                                String clubId = document.getString("clubAdminOf");
                                Log.d("JHM", "Club ID found: " + clubId);

                                if (clubId != null && !clubId.isEmpty()) {
                                    // 2. 해당 동아리의 이벤트 불러오기
                                    fetchClubEvents(clubId);
                                } else {
                                    Log.d("JHM", "clubAdminOf 필드가 비어있습니다.");
                                }

                                // 한 명의 유저는 하나의 관리자 문서만 갖는다고 가정하고 종료
                                break;
                            }
                        } else {
                            Log.d("JHM", "관리자 정보 없음");
                        }
                    } else {
                        Log.d("JHM", "Error finding admin user: ", task.getException());
                    }
                });
    }
    private void showEventsForDate(CalendarDay date) {
        if (date == null) return;

        // 1. 날짜 표시 (예: 12월 6일)
        // MaterialCalendarView 1.4.3은 month가 0(1월)~11(12월)이므로 +1 해줍니다.
        int month = date.getMonth() + 1;
        int day = date.getDay();
        String dateString = month + "월 " + day + "일";
        binding.eventDetailsDate.setText(dateString);

        // 2. 해당 날짜의 이벤트 목록 가져오기
        List<Event> events = eventMap.get(date);

        if (events != null && !events.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {
                sb.append("• ").append(events.get(i).title);
                // 마지막 아이템이 아니면 줄바꿈 추가
                if (i < events.size() - 1) {
                    sb.append("\n");
                }
            }
            binding.eventDetailsText.setText(sb.toString());
        } else {
            binding.eventDetailsText.setText("일정이 없습니다.");
        }
    }

    private void fetchClubEvents(String clubId) {
        Log.d("JHM", "fetchClubEvents 시작. 대상 동아리 ID: " + clubId);

        // 컬렉션 이름 "evnets" 유지
        db.collection("clubs").document(clubId).collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() == null || task.getResult().isEmpty()) {
                            Log.d("JHM", "이벤트 검색 결과 없음");
                            return;
                        }

                        Log.d("JHM", "이벤트 문서 개수: " + task.getResult().size());

                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String title = document.getString("title");
                                String startDateStr = document.getString("startDate"); // "2025-12-25 10:34"
                                String endDateStr = document.getString("endDate");     // "2025-12-27 14:00"

                                Log.d("JHM", "이벤트 파싱 시도 - Title: " + title + ", Start: " + startDateStr + ", End: " + endDateStr);

                                if (startDateStr != null && endDateStr != null) {
                                    // 공백을 기준으로 날짜와 시간 분리 ("2025-12-25 10:34" -> "2025-12-25")
                                    String datePartStart = startDateStr.split(" ")[0];
                                    String datePartEnd = endDateStr.split(" ")[0];

                                    // 날짜 부분 파싱 ("-" 기준 분리)
                                    String[] startParts = datePartStart.split("-");
                                    String[] endParts = datePartEnd.split("-");

                                    if (startParts.length == 3 && endParts.length == 3) {
                                        int startYear = Integer.parseInt(startParts[0]);
                                        int startMonth = Integer.parseInt(startParts[1]);
                                        int startDay = Integer.parseInt(startParts[2]);

                                        int endYear = Integer.parseInt(endParts[0]);
                                        int endMonth = Integer.parseInt(endParts[1]);
                                        int endDay = Integer.parseInt(endParts[2]);

                                        Log.d("JHM", "날짜 변환 성공: " + startYear + "-" + startMonth + "-" + startDay + " ~ " + endYear + "-" + endMonth + "-" + endDay);

                                        // 시간 추출
                                        String timeString = (startDateStr.split(" ").length > 1) ? startDateStr.split(" ")[1] : "00:00";

                                        // 날짜 범위 처리 및 데이터 추가
                                        addEvent(
                                                startYear, startMonth, startDay,
                                                endYear, endMonth, endDay,
                                                timeString,
                                                title
                                        );
                                    } else {
                                        Log.d("JHM", "날짜 포맷 오류 (split length != 3): " + startDateStr);
                                    }
                                } else {
                                    Log.d("JHM", "날짜 필드가 null임");
                                }
                            } catch (Exception e) {
                                Log.d("JHM", "Error parsing event: " + e.getMessage());
                            }
                        }

                        // UI 업데이트 (데코레이터 갱신)
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                binding.calendar.removeDecorators();
                                binding.calendar.addDecorator(new TodayDecorator());
                                binding.calendar.addDecorator(new EventDecorator(eventDates));
                                binding.calendar.invalidateDecorators();

                                CalendarDay selectedDate = binding.calendar.getSelectedDate();
                                if (selectedDate != null) {
                                    showEventsForDate(selectedDate);
                                } else {
                                    showEventsForDate(CalendarDay.today());
                                }
                            });
                        }
                    } else {
                        Log.d("JHM", "Error getting events: ", task.getException());
                    }
                });
    }


    // 시작일~종료일 사이의 모든 날짜에 이벤트를 등록하는 함수
    private void addEvent(int startYear, int startMonth, int startDay,
                          int endYear, int endMonth, int endDay,
                          String time, String title) {

        java.util.Calendar startCal = java.util.Calendar.getInstance();
        startCal.set(startYear, startMonth - 1, startDay); // Calendar의 월은 0부터 시작

        java.util.Calendar endCal = java.util.Calendar.getInstance();
        endCal.set(endYear, endMonth - 1, endDay);

        // 시작일부터 종료일까지 루프
        while (!startCal.after(endCal)) {
            CalendarDay day = CalendarDay.from(
                    startCal.get(java.util.Calendar.YEAR),
                    startCal.get(java.util.Calendar.MONTH),
                    startCal.get(java.util.Calendar.DAY_OF_MONTH)
            );

            // MaterialCalendarView 1.4.3 버전 기준 CalendarDay.from의 월 처리 확인 필요.
            // 보통 Java Calendar와 맞추려면 여기서 +1을 하지 않고 그대로 쓰거나, 라이브러리 스펙 확인.
            // 위 코드는 Calendar 객체가 0~11을 리턴하므로, 라이브러리가 0~11을 쓰면 +0, 1~12를 쓰면 +1
            // *일반적인 MaterialCalendarView 구버전은 0-11을 사용합니다.*
            // 만약 점이 엉뚱한 달에 찍히면 위 `+1`을 제거하세요.

            eventDates.add(day);

            List<Event> events = eventMap.get(day);
            if (events == null) {
                events = new java.util.ArrayList<>();
                eventMap.put(day, events);
            }
            events.add(new Event(time, title));

            startCal.add(java.util.Calendar.DATE, 1);
        }
    }

    // Event Model Class
    private static class Event {
        String time;
        String title;

        public Event(String time, String title) {
            this.time = time;
            this.title = title;
        }
    }

    // 오늘날짜 데코레이터 - 빨간색
    private class TodayDecorator implements DayViewDecorator {
        private final CalendarDay today;
        private final Drawable backgroundDrawable;

        public TodayDecorator() {
            today = CalendarDay.today();
            backgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selector_today);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(today);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(backgroundDrawable);
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
        }
    }

    // 이벤트날짜 데코레이터 - 파란색
    private class EventDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dates;
        private final Drawable backgroundDrawable;
        private final CalendarDay fixedToday;

        public EventDecorator(HashSet<CalendarDay> dates) {
            this.dates = dates;
            this.fixedToday = CalendarDay.today();
            backgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selector_event);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            // 날짜가 오늘이면 빨간색 이외에는 색칠X
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(backgroundDrawable);
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}