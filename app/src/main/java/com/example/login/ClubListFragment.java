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
        // TODO : UserDB에서 가입한 동아리 정보 가져오는 코드 필요
        Log.d("JHM", "동아리 목록 불러오기 시작");
        db.collection("clubs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 기존 리스트 초기화
                        clubList.clear();
                        Log.d("JHM", "문서 가져오기 성공");
                        int count = task.getResult().size();
                        Log.d("JHM", "가져온 문서 개수: " + count);
                        if (count == 0) {
                            Log.d("JHM", "주의: 컬렉션은 찾았으나 문서가 없습니다. Firestore 컬렉션 이름('clubs')이 정확한지 확인하세요.");
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //필드 값 가져오기
                            String name = document.getString("name");
                            String description = document.getString("activities"); // activities 내용을 설명으로 사용
                            String type = document.getString("type");
                            Log.d("JHM", "문서 ID: " + document.getId() + ", 데이터: " + document.getData());
                            // 이미지 URL 필드가 있다면 가져오기 (없으면 null 처리되어 어댑터에서 기본 이미지 사용)
                            String image = document.getString("imageUri");

                            if (name == null) name = "동아리명 없음";
                            if (description == null) description = "";
                            if (type == null) type = "기타";

                            // ClubModel 객체 생성 및 리스트 추가
                            //isFavorites은 일단 false
                            clubList.add(new ClubModel(name, description, image, false, type));
                        }
                        adapter = new ClubAdapter(clubList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.d("JHM", "Error getting documents: ", task.getException());
                    }
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
        private final String clubName;
        private final String description;
        private final String image;

        //관심동아리 체크여부
        private boolean isFavorites=false;
        private String type;

        public ClubModel(String clubName, String description, String image, boolean isFavorites, String type){
            this.clubName = clubName;
            this.description = description;
            this.image = image;
            this.isFavorites = isFavorites;
            this.type = type;
        }


        public String getClubName() { return clubName; }

        public String getDescription() { return description; }

        public String getImage() { return image; }
        public boolean isFavorites() { return isFavorites; }
        public String getType() { return type; }

        @Override
        public String toString(){
            return "CardModel{" + "clubName='" + clubName + '\'' + ", description='" + description
                    + '\'' + ", image=" + image + ", isFavorite= " + isFavorites + '}';
        }
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
                // imageUri가 null인지 체크 후 Glide로 로딩
                if (item.getImage() != null && !item.getImage().isEmpty()) {
                    Glide.with(ivImage.getContext())
                            .load(item.getImage())     // Firestore 또는 Storage 경로
                            .placeholder(R.drawable.logo) // 로딩 중 이미지
                            .error(R.drawable.logo)       // 실패 시 기본 이미지
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.logo);
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
                        // TODO : UserDB 관심동아리 목록에 추가하는 코드
                    }else{
                        // TODO : UserDB 관심동아리 목록에서 삭제하는 코드
                    }
                }
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
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_group_name);
                tvDesc = itemView.findViewById(R.id.tv_group_description);
                ivImage = itemView.findViewById(R.id.club_image);
                btnFavorite = itemView.findViewById(R.id.btn_favorite);
            }
        }
    }
}
