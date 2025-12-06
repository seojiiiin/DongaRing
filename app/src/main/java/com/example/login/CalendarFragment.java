package com.example.login;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.login.databinding.FragmentCalendarBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    // 이벤트를 Map으로 저장 (Key : CalendarDay, Value : 이벤트 목록 List)
    private Map<CalendarDay, List<Event>> eventMap = new HashMap<>();
    // Set to store dates that have events
    private HashSet<CalendarDay> eventDates = new HashSet<>();

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        MaterialCalendarView calendarView = binding.calendar;

        // 오늘날짜를 targetDate로 설정
        CalendarDay targetDate = CalendarDay.today();

        calendarView.setCurrentDate(targetDate);
        calendarView.setSelectedDate(targetDate);

        // Load events from Firebase
        loadEvents();

        // 데코레이터 생성
        // 1. Today Decorator (Red)
        calendarView.addDecorator(new TodayDecorator());

        // 날짜 클릭하면 이벤트 텍스트뷰 출력하도록 리스너 부착
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                showEventsForDate(date);
            }
        });
    }

    private void loadEvents() {
        eventMap.clear();
        eventDates.clear();

        if (user == null) {
            Log.d("JHM", "user is null");
            return;
        }

        // 모든 동아리(clubs)를 가져와서 이벤트를 조회합니다.
        db.collection("clubs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot clubDoc : task.getResult()) {
                        // 각 동아리 문서 ID를 이용해 이벤트 조회 함수 호출
                        fetchClubEvents(clubDoc.getId());
                    }
                }
            } else {
                Log.e("JHM", "Error getting clubs: ", task.getException());
            }
        });
    }

    private void fetchClubEvents(String clubId) {
        // 1. 먼저 "events" (정상 철자)로 시도
        db.collection("clubs").document(clubId).collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        parseAndShowEvents(task.getResult());
                    } else {
                        // 2. 실패하면 "evnets" (오타)로 재시도
                        db.collection("clubs").document(clubId).collection("evnets")
                                .get()
                                .addOnCompleteListener(retryTask -> {
                                    if (retryTask.isSuccessful() && retryTask.getResult() != null && !retryTask.getResult().isEmpty()) {
                                        parseAndShowEvents(retryTask.getResult());
                                    }
                                });
                    }
                });
    }

    private void parseAndShowEvents(com.google.firebase.firestore.QuerySnapshot result) {
        for (com.google.firebase.firestore.QueryDocumentSnapshot document : result) {
            try {
                // visibility 체크
                Boolean visibility = document.getBoolean("visibility");
                if (visibility != null && !visibility) continue;

                String title = document.getString("title");
                // 날짜 형식이 "yyyy-MM-dd HH:mm" 이거나 "yyyy-MM-dd" 일 수 있음
                String startDateStr = document.getString("startDate");
                String endDateStr = document.getString("endDate");

                if (startDateStr != null && endDateStr != null) {
                    // 공백으로 잘라서 날짜 부분만 가져옴 (예: "2025-12-25 10:34" -> "2025-12-25")
                    String datePartStart = startDateStr.contains(" ") ? startDateStr.split(" ")[0] : startDateStr;
                    String datePartEnd = endDateStr.contains(" ") ? endDateStr.split(" ")[0] : endDateStr;

                    String[] startParts = datePartStart.split("-");
                    String[] endParts = datePartEnd.split("-");

                    if (startParts.length == 3 && endParts.length == 3) {
                        int startYear = Integer.parseInt(startParts[0]);
                        int startMonth = Integer.parseInt(startParts[1]);
                        int startDay = Integer.parseInt(startParts[2]);

                        int endYear = Integer.parseInt(endParts[0]);
                        int endMonth = Integer.parseInt(endParts[1]);
                        int endDay = Integer.parseInt(endParts[2]);

                        // 시간 문자열 추출 (없으면 00:00)
                        String timeString = "00:00";
                        if (startDateStr.contains(" ")) {
                            String[] parts = startDateStr.split(" ");
                            if (parts.length > 1) timeString = parts[1];
                        }

                        addEvent(startYear, startMonth, startDay, endYear, endMonth, endDay, timeString, title);
                    }
                }
            } catch (Exception e) {
                Log.e("JHM", "Error parsing event data: " + e.getMessage());
            }
        }

        // UI 업데이트
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.calendar.removeDecorators();
                binding.calendar.addDecorator(new TodayDecorator());
                // 2. Event Decorator (Blue)
                binding.calendar.addDecorator(new EventDecorator(eventDates));
                binding.calendar.invalidateDecorators(); // 강제 갱신

                // 현재 선택된 날짜가 있다면 목록 갱신, 없으면 오늘 날짜 갱신
                CalendarDay selectedDate = binding.calendar.getSelectedDate();
                if (selectedDate != null) {
                    showEventsForDate(selectedDate);
                } else {
                    showEventsForDate(CalendarDay.today());
                }
            });
        }
    }

    private void addEvent(int startYear, int startMonth, int startDay,
                          int endYear, int endMonth, int endDay,
                          String time, String title) {

        Calendar startCal = Calendar.getInstance();
        // Java Calendar는 월이 0~11이므로 -1
        startCal.set(startYear, startMonth - 1, startDay);

        Calendar endCal = Calendar.getInstance();
        endCal.set(endYear, endMonth - 1, endDay);

        if (startCal.after(endCal)) {
            Calendar temp = startCal;
            startCal = endCal;
            endCal = temp;
        }

        // 시작일부터 종료일까지 하루씩 증가하며 루프
        while (!startCal.after(endCal)) {
            // MaterialCalendarView 1.4.3: Month는 0~11 사용
            CalendarDay day = CalendarDay.from(
                    startCal.get(Calendar.YEAR),
                    startCal.get(Calendar.MONTH),
                    startCal.get(Calendar.DAY_OF_MONTH)
            );

            eventDates.add(day); // 점을 찍기 위한 날짜 집합

            if (!eventMap.containsKey(day)) {
                eventMap.put(day, new ArrayList<>());
            }
            eventMap.get(day).add(new Event(time, title));

            startCal.add(Calendar.DATE, 1);
        }
    }

    private void showEventsForDate(CalendarDay date) {
        LinearLayout container = binding.eventListContainer;
        container.removeAllViews();

        List<Event> events = eventMap.get(date);

        if (events != null && !events.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (Event event : events) {
                View eventView = inflater.inflate(R.layout.item_calendar_event, container, false);

                TextView timeTv = eventView.findViewById(R.id.event_time);
                TextView titleTv = eventView.findViewById(R.id.event_title);

                timeTv.setText(event.time);
                titleTv.setText(event.title);

                container.addView(eventView);
            }
        } else {
            // 이벤트 없으면 텍스트뷰 수정
            TextView noEventTv = new TextView(getContext());
            noEventTv.setText("일정이 없습니다.");
            noEventTv.setPadding(16, 32, 16, 32);
            noEventTv.setTextColor(Color.GRAY);
            noEventTv.setTextSize(16);
            container.addView(noEventTv);
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

        public EventDecorator(HashSet<CalendarDay> dates) {
            // 데코레이터 생성 시점의 데이터를 복사해서 저장
            this.dates = new HashSet<>(dates);
            backgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selector_event);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            // 오늘 날짜여도 이벤트가 있으면 점을 찍도록 조건 수정
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(backgroundDrawable);
            // 필요 시 글자 색상 변경
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
