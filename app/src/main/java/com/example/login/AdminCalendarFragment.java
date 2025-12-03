package com.example.login;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.login.databinding.FragmentAdminCalendarBinding;
import com.google.firebase.firestore.FirebaseFirestore;
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_calendar, container, false);
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