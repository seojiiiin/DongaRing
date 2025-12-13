package com.example.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private ArrayList<ClubModel> favoriteList;
    private TextView tvEmptyMessage;

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    public FavoritesFragment() {
        // Required empty public constructor
    }
    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
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
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.favorite_list);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

        // 더미 데이터 생성 (즐겨찾기 된 상태라고 가정하므로 isFavorites = true)
        // TODO : DB에서 관심동아리목록 불러오는 코드 필요
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        favoriteList = new ArrayList<>();
        adapter = new FavoritesAdapter(favoriteList);
        recyclerView.setAdapter(adapter);

        loadFavoriteClubsFromDB();   // ✅ DB에서 읽어오기

    }

    // 리스트가 비었을 때 안내 문구 표시 메서드
    private void checkEmptyList() {
        if (favoriteList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    // --- 내부 클래스: 데이터 모델 (ClubListFragment와 동일) ---
    class ClubModel {
        private final String clubId;   // ✅ clubs 문서ID
        private final String clubName;
        private final String description;
        private final int image;
        private boolean isFavorites;

        public ClubModel(String clubId, String clubName, String description, int image, boolean isFavorites){
            this.clubId = clubId;
            this.clubName = clubName;
            this.description = description;
            this.image = image;
            this.isFavorites = isFavorites;
        }

        public String getClubId() { return clubId; }
        public boolean isFavorites() { return isFavorites; }
        public void setFavorites(boolean favorites) { isFavorites = favorites; }
        public String getClubName() { return clubName; }
        public String getDescription() { return description; }
        public int getImage() { return image; }
    }

    private void loadFavoriteClubsFromDB() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    List<String> favIds = (List<String>) userDoc.get("favoriteClubs");

                    if (favIds == null || favIds.isEmpty()) {
                        favoriteList.clear();
                        adapter.notifyDataSetChanged();
                        checkEmptyList();
                        return;
                    }

                    fetchClubsByIds(favIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "즐겨찾기 불러오기 실패", Toast.LENGTH_SHORT).show();
                    checkEmptyList();
                });
    }

    private void fetchClubsByIds(List<String> clubIds) {
        favoriteList.clear();

        // Firestore whereIn은 개수 제한이 있어서(버전에 따라 10~30) 안전하게 10개 단위로 끊어 처리
        int batchSize = 10;

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < clubIds.size(); i += batchSize) {
            List<String> batch = clubIds.subList(i, Math.min(i + batchSize, clubIds.size()));

            Task<QuerySnapshot> t = db.collection("clubs")
                    .whereIn(FieldPath.documentId(), batch)
                    .get();
            tasks.add(t);
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    // results: 각 배치의 QuerySnapshot 리스트
                    for (Object obj : results) {
                        QuerySnapshot qs = (QuerySnapshot) obj;
                        for (DocumentSnapshot doc : qs.getDocuments()) {
                            String clubId = doc.getId();
                            String name = doc.getString("name");
                            String desc = doc.getString("activities");

                            if (name == null) name = "(이름 없음)";
                            if (desc == null) desc = "";

                            favoriteList.add(new ClubModel(clubId, name, desc, R.drawable.logo, true));
                        }
                    }

                    adapter.setList(favoriteList);
                    checkEmptyList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "동아리 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                    checkEmptyList();
                });
    }



    // --- 내부 클래스: 어댑터 ---
    class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
        private List<ClubModel> originalList;
        private List<FavoritesFragment.ClubModel> filteredList;

        public FavoritesAdapter(List<ClubModel> list) {
            this.originalList = list;
            this.filteredList = new ArrayList<>(list);
        }
        public void setList(List<ClubModel> list) {
            this.originalList = list;
            this.filteredList = new ArrayList<>(list);
            notifyDataSetChanged();
        }
        public void filter(String query){
            filteredList.clear();
            if(query.isEmpty()){
                filteredList.addAll(originalList);
            }else {
                query = query.toLowerCase();
                for(FavoritesFragment.ClubModel item : originalList){
                    if(item.getClubName().toLowerCase().contains(query) || item.getDescription().toLowerCase().contains(query)){
                        filteredList.add(item);
                    }
                }
            }
            //리사이클러뷰 갱신
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 기존에 만든 clublist 재사용
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.clublist_cardview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ClubModel item = filteredList.get(position);

            holder.tvName.setText(item.getClubName());
            holder.tvDesc.setText(item.getDescription());
            holder.ivImage.setImageResource(item.getImage());

            // 즐겨찾기 화면이므로 무조건 꽉 찬 하트로 시작
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);

            // 하트 클릭 시: 목록에서 삭제
            holder.btnFavorite.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();

                if (currentPos == RecyclerView.NO_POSITION) return;

                ClubModel itemToRemove = filteredList.get(currentPos);

                // TODO: 실제 DB 삭제 로직 추가
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String clubId = itemToRemove.getClubId();

                db.collection("users").document(uid)
                        .update("favoriteClubs", com.google.firebase.firestore.FieldValue.arrayRemove(clubId));


                originalList.remove(itemToRemove);
                filteredList.remove(currentPos);
                notifyItemRemoved(currentPos);

                Toast.makeText(getContext(), "즐겨찾기가 해제되었습니다.", Toast.LENGTH_SHORT).show();
                checkEmptyList();
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