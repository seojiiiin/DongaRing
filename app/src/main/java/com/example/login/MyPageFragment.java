package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.login.databinding.FragmentMyPageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPageFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String[] clubs;
    private String mParam2;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FragmentMyPageBinding binding;

    public MyPageFragment() {
        // Required empty public constructor
    }


    public static MyPageFragment newInstance(String[] joinedClubs, String param2) {
        MyPageFragment fragment = new MyPageFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_PARAM1, joinedClubs);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            clubs = getArguments().getStringArray(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMyPageBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //user 이름 불러오기
        user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w("LSJ", "user is null");
            return;
        }
        db.collection("users")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String userName = query.getDocuments().get(0).getString("name");
                        binding.userName.setText(userName);
                        Log.d("LSJ", "userName: " + userName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("LSJ", "Error getting documents.", e);
                });

        //설정
        binding.gear.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.full_screen_container, new SettingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        List<CardModel> eventList = new ArrayList<>();
        eventList.add(new CardModel("이벤트 이름1", "동아리 이름1", R.drawable.logo, 2024, 10, 22));
        eventList.add(new CardModel("이벤트 이름2", "동아리 이름2", R.drawable.logo, 2024, 11, 22));

        RecyclerView eventRecyclerView = binding.eventList;
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        EventAdapter eventAdapter = new EventAdapter(eventList);
        eventRecyclerView.setAdapter(eventAdapter);

        // 신청한 동아리 목록 (기존 clubList)
        List<ClubModel> clubList = new ArrayList<>();
        clubList.add(new ClubModel("SRC", "숭실대학교 중앙 와인동아리", R.drawable.logo));
        clubList.add(new ClubModel("두메", "숭실대학교 중앙 풍물패", R.drawable.logo));
        clubList.add(new ClubModel("SSBC", "숭실대학교 중앙 방송국", R.drawable.logo));

        LinearLayout clubListContainer = binding.clubListContainer;
        if(clubList.isEmpty()){
            // RecyclerView가 아닌 컨테이너를 숨김
            clubListContainer.setVisibility(View.GONE);
            binding.emptyClubMessage1.setVisibility(View.VISIBLE);
        }
        else {
            // clubList의 각 아이템에 대해 뷰를 생성하고 LinearLayout에 추가
            LayoutInflater inflater = getLayoutInflater();
            for (ClubModel club : clubList) {
                // club_cardview.xml을 인플레이트
                View clubView = inflater.inflate(R.layout.club_cardview, clubListContainer, false);

                // 뷰의 각 컴포넌트
                ImageView clubLogo = clubView.findViewById(R.id.club_logo_area);
                TextView clubName = clubView.findViewById(R.id.club_name_text);
                TextView clubDescription = clubView.findViewById(R.id.club_desc_text);

                // 데이터를 뷰에 설정
                clubLogo.setImageResource(club.getImage());
                clubName.setText(club.getClubName());
                clubDescription.setText(club.getDescription());

                // 생성된 뷰를 컨테이너에 추가
                clubListContainer.addView(clubView);
            }
        }

        // 가입한 동아리 목록
        LinearLayout registeredClubListContainer = binding.registeredClubListContainer;
        loadRegisteredClubs(registeredClubListContainer);
    }

    private void loadRegisteredClubs(LinearLayout container){
        if (clubs == null || clubs.length == 0){
            container.setVisibility(View.GONE);
            binding.emptyClubMessage2.setVisibility(View.VISIBLE);
            return;
        }

        List<ClubModel> registeredClubList = new ArrayList<>();
        LayoutInflater inflater = getLayoutInflater();

        for (String clubID : clubs) {
            if (clubID == null) continue;
            db.collection("clubs")
                    .document(clubID)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            String description = doc.getString("activities");
                            int image = R.drawable.logo;

                            ClubModel club = new ClubModel(name, description, image);
                            registeredClubList.add(club);

                            View clubView = inflater.inflate(R.layout.club_cardview, container, false);
                            ImageView clubLogo = clubView.findViewById(R.id.club_logo_area);
                            TextView clubName = clubView.findViewById(R.id.club_name_text);
                            TextView clubDescription = clubView.findViewById(R.id.club_desc_text);
                            ImageView arrowButton = clubView.findViewById(R.id.arrow_button);


                            clubLogo.setImageResource(club.getImage());
                            clubName.setText(club.getClubName());
                            clubDescription.setText(club.getDescription());
                            arrowButton.setOnClickListener(v -> {
                                startActivity(new Intent(getActivity(), MyClubActivity.class).putExtra("clubID", clubID));
                            });

                            container.addView(clubView);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Log.w("LSJ", "Error getting documents.", e);
                    });
        }
    }
    class CardModel {
        private final String title;
        private final String clubName;
        private final int image;

        private final int year;
        private final int month;
        private final int date;

        public CardModel(String title, String clubName, int image, int year, int month, int date) {
            this.title = title;
            this.clubName = clubName;
            this.image = image;
            this.year = year;
            this.month = month;
            this.date = date;
        }
        public String getTitle() {
            return title;
        }
        public String getClubName() {
            return clubName;
        }
        public int getImage() {
            return image;
        }

        public int getYear() {
            return year;
        }
        public int getMonth() {
            return month;
        }

        public int getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "CardModel{" + "title='" + title + '\'' + ", clubName='" + clubName + '\'' + ", image=" + image +
                    "year=" + year + '\'' + "month=" + month + '\'' + "date=" + date + '}';
        }
    }
    class ClubModel {
        private final String clubName;
        private final String description;
        private final int image;

        public ClubModel(String clubName, String description, int image){
            this.clubName = clubName;
            this.description = description;
            this.image = image;
        }

        public String getClubName() {
            return clubName;
        }

        public String getDescription() {
            return description;
        }

        public int getImage() {
            return image;
        }
        @Override
        public String toString(){
            return "CardModel{" + "clubName='" + clubName + '\'' + ", description='" + description + '\'' + ", image=" + image + '}';
        }
    }
    class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

        private final List<CardModel> eventList;

        // 데이터리스트 받는 생성자
        public EventAdapter(List<CardModel> eventList) {
            this.eventList = eventList;
        }

        // ViewHolder를 생성하고 뷰를 붙여주는 부분
        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_cardview, parent, false);
            return new EventViewHolder(view);
        }

        // ViewHolder에 데이터를 바인딩하는 부분
        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            CardModel event = eventList.get(position);
            holder.bind(event);
        }

        // 데이터의 총 개수를 반환
        @Override
        public int getItemCount() {
            return eventList.size();
        }

        // ViewHolder 클래스: event_cardview.xml의 뷰 관리
        static class EventViewHolder extends RecyclerView.ViewHolder {
            private final ImageView imageArea;
            private final TextView dateArea;
            private final TextView titleArea;
            private final TextView amountArea; // clubName을 표시할 TextView

            public EventViewHolder(@NonNull View itemView) {
                super(itemView);
                imageArea = itemView.findViewById(R.id.imageArea);
                dateArea = itemView.findViewById(R.id.dateArea);
                titleArea = itemView.findViewById(R.id.titleArea);
                amountArea = itemView.findViewById(R.id.amountArea);
            }

            // 데이터를 뷰에 설정하는 메서드
            public void bind(CardModel event) {
                imageArea.setImageResource(event.getImage());
                titleArea.setText(event.getTitle());
                amountArea.setText(event.getClubName());

                // 날짜 포맷 설정 (예: "10월 22일")
                String dateText = event.getYear() + "년 " + event.getMonth() + "월 " + event.getDate() + "일";
                dateArea.setText(dateText);
            }
        }
    }
}