package com.example.login;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClubListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClubListFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private FirebaseFirestore db;

    //RecyclerView 변수
    private RecyclerView recyclerView;
    private ClubAdapter adapter;
    private List<ClubModel> clubList;
    private List<Button> categoryButtons = new ArrayList<>();


    public ClubListFragment() {
        // Required empty public constructor
    }

    public static ClubListFragment newInstance(String param1, String param2) {
        ClubListFragment fragment = new ClubListFragment();
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
        return inflater.inflate(R.layout.fragment_club_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.club_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        SearchView searchView = view.findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });



        clubList = new ArrayList<>();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();        // 1. 유저의 관심 목록(favoriteClubs)을 먼저 가져옵니다.
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {

                    // 유저의 즐겨찾기 목록 가져오기 (없으면 빈 리스트)
                    List<String> myFavorites = (List<String>) userDoc.get("favoriteClubs");
                    if (myFavorites == null) myFavorites = new ArrayList<>();

                    // 람다식 내부에서 사용하기 위해 final 변수로 저장
                    final List<String> finalMyFavorites = myFavorites;

                    Log.d("동아링", "동아리 목록 불러오기 시작");
                    db.collection("clubs")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // 기존 리스트 초기화
                                    clubList.clear();
                                    Log.d("동아링", "문서 가져오기 성공");
                                    int count = task.getResult().size();
                                    Log.d("동아링", "가져온 문서 개수: " + count);
                                    if (count == 0) {
                                        Log.d("동아링", "주의: 컬렉션은 찾았으나 문서가 없습니다. Firestore 컬렉션 이름('clubs')이 정확한지 확인하세요.");
                                    }
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        //필드 값 가져오기
                                        String name = document.getString("name");
                                        String description = document.getString("activities"); // activities 내용을 설명으로 사용
                                        String type = document.getString("type");
                                        String docId = document.getId();
                                        Log.d("동아링", "문서 ID: " + document.getId() + ", 데이터: " + document.getData());
                                        // 이미지 URL 필드가 있다면 가져오기 (없으면 null 처리되어 어댑터에서 기본 이미지 사용)
                                        String image = document.getString("logo");

                                        if (name == null) name = "동아리명 없음";
                                        if (description == null) description = "";
                                        if (type == null) type = "기타";

                                        // ClubModel 객체 생성 및 리스트 추가
                                        clubList.add(new ClubModel(document.getId(), name, description, image, finalMyFavorites.contains(docId), type));
                                    }
                                    adapter = new ClubAdapter(clubList);
                                    recyclerView.setAdapter(adapter);
                                } else {
                                    Log.d("동아링", "Error getting documents: ", task.getException());
                                }
                            });
                });

        categoryButtons.clear(); // 중복 방지
        categoryButtons.add(view.findViewById(R.id.btn_whole));
        categoryButtons.add(view.findViewById(R.id.btn_liberal));
        categoryButtons.add(view.findViewById(R.id.btn_volunteer));
        categoryButtons.add(view.findViewById(R.id.btn_music));
        categoryButtons.add(view.findViewById(R.id.btn_religion));
        categoryButtons.add(view.findViewById(R.id.btn_art));
        categoryButtons.add(view.findViewById(R.id.btn_sport));
        categoryButtons.add(view.findViewById(R.id.btn_study));

        // 각 버튼에 리스너 및 데이터 연결
        setupCategoryButton(view.findViewById(R.id.btn_whole), "전체");
        setupCategoryButton(view.findViewById(R.id.btn_liberal), "교양분과");
        setupCategoryButton(view.findViewById(R.id.btn_volunteer), "연대사업분과");
        setupCategoryButton(view.findViewById(R.id.btn_music), "연행예술분과");
        setupCategoryButton(view.findViewById(R.id.btn_religion), "종교분과");
        setupCategoryButton(view.findViewById(R.id.btn_art), "창작전시분과");
        setupCategoryButton(view.findViewById(R.id.btn_sport), "체육분과");
        setupCategoryButton(view.findViewById(R.id.btn_study), "학술분과");

        // 초기 상태 설정 (전체 버튼을 선택된 상태로)
        updateButtonStyles(view.findViewById(R.id.btn_whole));
    }
    private void setupCategoryButton(Button btn, String categoryType) {
        if (btn != null) {
            btn.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.filterByType(categoryType);
                }
                updateButtonStyles(btn);
            });
        }
    }
    private void updateButtonStyles(Button selectedBtn) {
        for (Button btn : categoryButtons) {
            if (btn == selectedBtn) {
                // [선택된 버튼] : 배경색, 글자색 변경
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#40C4FF")));
                btn.setTextColor(Color.WHITE); // 글자색 흰색
            } else {
                // [선택되지 않은 버튼들]
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                btn.setTextColor(Color.BLACK);
            }
        }
    }
    class ClubModel {
        private String clubId;
        private final String clubName;
        private final String description;
        private final String image;
        private boolean isFavorites=false;
        private String type;

        public ClubModel(String clubId, String clubName, String description, String image, boolean isFavorites, String type){
            this.clubId = clubId;
            this.clubName = clubName;
            this.description = description;
            this.image = image;
            this.isFavorites = isFavorites;
            this.type = type;
        }

        public String getClubId() { return clubId; }
        public String getClubName() { return clubName; }
        public String getDescription() { return description; }
        public String getImage() { return image; }
        public boolean isFavorites() { return isFavorites; }
        public void setFavorites(boolean favorites) { this.isFavorites = favorites;}
        public String getType() { return type; }
    }
    class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ViewHolder> {
        private List<ClubModel> originalList;
        private List<ClubModel> filteredList;

        public ClubAdapter(List<ClubModel> list) {
            this.originalList = list;
            this.filteredList = new ArrayList<>(list);
        }
        //SearchView에서 텍스트 입력에 따라 실시간으로 리스트 필터링하는 메서드
        public void filter(String query){
            filteredList.clear();
            if(query.isEmpty()){
                filteredList.addAll(originalList);
            }else {
                query = query.toLowerCase();
                for(ClubModel item : originalList){
                    if(item.getClubName().toLowerCase().contains(query) || item.getDescription().toLowerCase().contains(query)){
                        filteredList.add(item);
                    }
                }
            }
            //리사이클러뷰 갱신
            notifyDataSetChanged();
        }

        //분과별 필터링 기능
        public void filterByType(String type) {
            filteredList.clear();
            if(type.equals("전체")){
                filteredList.addAll(originalList);
            } else {
                for(ClubModel item : originalList){
                    if(item.getType().equals(type)) filteredList.add(item);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // clublist_cardview.xml을 inflate
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.clublist_cardview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ClubModel item = filteredList.get(position);

            // 뷰에 데이터 설정
            TextView tvName = holder.itemView.findViewById(R.id.tv_group_name);
            if (tvName != null) tvName.setText(item.getClubName());

            TextView tvDesc = holder.itemView.findViewById(R.id.tv_group_description);
            if (tvDesc != null) tvDesc.setText(item.getDescription());

            ImageView ivImage = holder.itemView.findViewById(R.id.club_image);
            if (ivImage != null) {
                if (item.getImage() != null && !item.getImage().isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(item.getImage())
                            .placeholder(R.drawable.image)
                            .error(R.drawable.image)
                            .centerCrop()
                            .into(holder.ivImage);
                } else {
                    holder.ivImage.setImageResource(R.drawable.image);
                }
            }

            if (item.isFavorites()) {
                holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);
            } else{
                holder.btnFavorite.setImageResource(R.drawable.ic_heart);
            }
            //버튼 누를때마다 하트표시 변경하는 리스너
            holder.btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean newState = !item.isFavorites();
                    item.isFavorites = newState;
                    holder.btnFavorite.setImageResource(newState ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
                    if(newState){
                        // UserDB 관심동아리 목록에 추가
                        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String clubId = item.getClubId(); // ✅ clubs 문서ID

                        java.util.Map<String, Object> update = new java.util.HashMap<>();
                        update.put("favoriteClubs", com.google.firebase.firestore.FieldValue.arrayUnion(clubId));

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .set(update, com.google.firebase.firestore.SetOptions.merge())
                                .addOnFailureListener(e -> {
                                    item.isFavorites = false;
                                    holder.btnFavorite.setImageResource(R.drawable.ic_heart);
                                });

                    } else {
                        // UserDB 관심동아리 목록에서 삭제
                        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String clubId = item.getClubId(); // ✅ clubs 문서ID

                        java.util.Map<String, Object> update = new java.util.HashMap<>();
                        update.put("favoriteClubs", com.google.firebase.firestore.FieldValue.arrayRemove(clubId));

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .set(update, com.google.firebase.firestore.SetOptions.merge())
                                .addOnFailureListener(e -> {
                                    item.isFavorites = true;
                                    holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);
                                });
                }
            }});

            // club inform으로 이동
            holder.btnDetail.setOnClickListener(v -> {
                String clubId = item.getClubId();

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.full_screen_container, new ClubInformFragment().newInstance(clubId, ""))
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc;
            ImageView ivImage;
            ImageButton btnFavorite;
            Button btnDetail;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_group_name);
                tvDesc = itemView.findViewById(R.id.tv_group_description);
                ivImage = itemView.findViewById(R.id.club_image);


                btnFavorite = itemView.findViewById(R.id.btn_favorite);
                btnDetail = itemView.findViewById(R.id.btn_details);
            }
        }
    }
}
