package com.example.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.example.login.databinding.FragmentAdminMyPageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminMyPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminMyPageFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FragmentAdminMyPageBinding binding;
    List<CardModel> eventList = new ArrayList<>();
    EventAdapter eventAdapter;


    public AdminMyPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminMyPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminMyPageFragment newInstance(String param1, String param2) {
        AdminMyPageFragment fragment = new AdminMyPageFragment();
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
        binding = FragmentAdminMyPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //user 이름 불러오기
        user = mAuth.getCurrentUser();
        if (user == null) {
            Log.d("JHM", "user is null");
            return;
        }
        //users_admin컬렉션에서 유저이름 찾아서 텍스트뷰에 설정
        // 유저이름으로 동아리->이벤트의 이벤트목록 조회하여 이벤트리스트에 추가
        db.collection("users_admin")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String userName = query.getDocuments().get(0).getString("name");
                        binding.userName.setText(userName);
                        searchEvents(userName);
                        clubManage(userName);
                        Log.d("JHM", "userName: " + userName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("JHM", "Error getting documents.", e);
                });
        RecyclerView eventRecyclerView = binding.eventList;
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        eventAdapter = new EventAdapter(eventList);
        eventRecyclerView.setAdapter(eventAdapter);

    }
    //가입신청자 관리 뷰 추가
    private void clubManage(String userName) {
        Log.d("JHM", "clubManage 함수 호출");
        db.collection("clubs")
                .whereEqualTo("president", userName) // 회장 이름으로 필터링
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot document = query.getDocuments().get(0);

                        String name = document.getString("name");
                        String description = document.getString("activities");
                        ClubModel club = new ClubModel(name, description, R.drawable.logo);
                        LinearLayout clubContainer = binding.club;
                        clubContainer.removeAllViews();
                        LayoutInflater inflater = getLayoutInflater();
                        View clubView = inflater.inflate(R.layout.admin_club_cardview, clubContainer, false);
                        clubView.findViewById(R.id.apply_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("JHM", "신청현황 버튼 클릭");
                                startActivity(new Intent(getActivity(), ApplyManageActivity.class));
                            }
                        });
                        ImageView clubLogo = clubView.findViewById(R.id.club_logo_area);
                        TextView clubName = clubView.findViewById(R.id.club_name_text);
                        TextView clubDescription = clubView.findViewById(R.id.club_desc_text);

                        clubLogo.setImageResource(club.getImage());
                        clubName.setText(club.getClubName());
                        clubDescription.setText(club.getDescription());

                        clubContainer.addView(clubView);

                        Log.d("JHM", "내 동아리 정보 로드 성공: " + name);
                    } else {
                        Log.d("JHM", "관리 중인 동아리가 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("JHM", "동아리 정보 가져오기 실패", e);
                });
    }

    private void searchEvents(String name){
        db.collection("clubs")
                .whereEqualTo("president", name)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot clubDoc = query.getDocuments().get(0);
                        String clubId = clubDoc.getId();
                        String clubName = clubDoc.getString(name);
                        Log.d("JHM", "찾은 동아리ID : " + clubId);
                        db.collection("clubs").document(clubId).collection("evnets") // 오타 'evnets' 그대로 사용
                                .get()
                                .addOnSuccessListener(eventQuery -> {
                                    // evnets 안에 있는 모든 문서들을 하나씩 꺼내어 이벤트 리스트에 추가
                                    for (QueryDocumentSnapshot eventDoc : eventQuery) {
                                        String title = eventDoc.getString("title");
                                        String startDate = eventDoc.getString("startDate");
                                        int[] date = parseDate(startDate);
                                        eventList.add(new CardModel(title, clubName, R.drawable.logo, date[0], date[1], date[2]));
                                        Log.d("JHM", "행사명: " + title + ", 시작일: " + startDate);
                                    }
                                    if(eventAdapter != null) eventAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("JHM", "evnets 가져오기 실패", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("JHM", "동아리 검색 실패.", e);
                });
    }
    private int[] parseDate(String dateString) {
        String[] parts = dateString.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        return new int[]{year, month, day};
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
        public EventAdapter(List<CardModel> eventList) {
            this.eventList = eventList;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_cardview, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            CardModel event = eventList.get(position);
            holder.bind(event);
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        // ViewHolder 클래스
        class EventViewHolder extends RecyclerView.ViewHolder {
            private final ImageView imageArea;
            private final TextView dateArea;
            private final TextView titleArea;
            private final TextView amountArea;
            public EventViewHolder(@NonNull View itemView) {
                super(itemView);
                imageArea = itemView.findViewById(R.id.imageArea);
                dateArea = itemView.findViewById(R.id.dateArea);
                titleArea = itemView.findViewById(R.id.titleArea);
                amountArea = itemView.findViewById(R.id.amountArea);
            }
            public void bind(CardModel event) {
                imageArea.setImageResource(event.getImage());
                titleArea.setText(event.getTitle());
                amountArea.setText(event.getClubName());
                String month = switch(event.getMonth()){
                    case 1 -> "JAN";
                    case 2 -> "FEB";
                    case 3 -> "MAR";
                    case 4 -> "APR";
                    case 5 -> "MAY";
                    case 6 -> "JUN";
                    case 7 -> "JUL";
                    case 8 -> "AUG";
                    case 9 -> "SEP";
                    case 10 -> "OCT";
                    case 11 -> "NOV";
                    case 12 -> "DEC";
                    default -> "NONE";
                };
                String dateText = month + " " + event.getDate();
                dateArea.setText(dateText);
            }
        }
    }

}