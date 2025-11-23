package com.example.login;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
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
        db = FirebaseFirestore.getInstance();
        MaterialCalendarView calendarView = binding.calendar;

        // 오늘날짜를 targetDate로 설정
        CalendarDay targetDate = CalendarDay.today();

        calendarView.setCurrentDate(targetDate);
        calendarView.setSelectedDate(targetDate);

        // Load dummy data
        loadEvents();

        // 데코레이터 생성
        // 1. Today Decorator (Red)
        calendarView.addDecorator(new TodayDecorator());
        // 2. Event Decorator (Blue)
        calendarView.addDecorator(new EventDecorator(eventDates));

        // Show events for the initially selected date
        showEventsForDate(targetDate);

        // 날짜 클릭하면 이벤트 텍스트뷰 출력하도록 리스너부착
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                showEventsForDate(date);
            }
        });
    }

    /**
     * 이 함수에 firebase기능 추가해야함
     */
    private void loadEvents() {
        eventMap.clear();
        eventDates.clear();

        //임시로 addEvent
        //파라미터 : year, month_start, day_start, month_end, day_end, time, title
        addEvent(2025, 10, 8, 10, 8, "18:00", "빛누리 면접");
        addEvent(2025, 10, 12, 10, 12, "18:00", "면접 결과 발표");
        addEvent(2025, 10, 12, 10, 12, "20:00", "면접 결과 발표2");
        addEvent(2025, 10,13, 10, 15, "14:00", "MT");

    }

    private void addEvent(int year, int month_start, int day_start, int month_end, int day_end, String time, String title) {
        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month_start - 1, day_start, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month_end - 1, day_end, 0, 0, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        if (startCal.after(endCal)) {
            Calendar temp = startCal;
            startCal = endCal;
            endCal = temp;
        }

        while (!startCal.after(endCal)) {
            int y = startCal.get(Calendar.YEAR);
            int m = startCal.get(Calendar.MONTH);
            int d = startCal.get(Calendar.DAY_OF_MONTH);

            CalendarDay day = CalendarDay.from(y, m, d);

            if (!eventMap.containsKey(day)) {
                eventMap.put(day, new ArrayList<>());
            }
            eventMap.get(day).add(new Event(time, title));
            eventDates.add(day);

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
        private final CalendarDay fixedToday;

        public EventDecorator(HashSet<CalendarDay> dates) {
            this.dates = dates;
            this.fixedToday = CalendarDay.today();
            backgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selector_event);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            // 날짜가 오늘이면 빨간색 이외에는 색칠X
            return dates.contains(day) && !day.equals(fixedToday);
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
