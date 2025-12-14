package com.example.login;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.login.databinding.FragmentClubInformBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClubInformFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClubInformFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String clubID;
    private String mParam2;
    private FragmentClubInformBinding binding;
    private FirebaseFirestore db;
    private EventImageAdapter adapter;
    private List<InfoModel> events = new ArrayList<>();

    public ClubInformFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClubInformFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClubInformFragment newInstance(String param1, String param2) {
        ClubInformFragment fragment = new ClubInformFragment();
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
            clubID = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentClubInformBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // 동아리 정보 불러오기
        db.collection("clubs")
                .document(clubID)
                .get()
                .addOnSuccessListener(doc -> {
                    binding.clubName.setText(doc.getString("name"));
                    binding.clubDescription.setText(doc.getString("activities"));
                }).addOnFailureListener(e -> {
                    Log.w("LSJ", "Error getting documents.", e);
                });

        // 동아리 사진 불러오기
        binding.eventList.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        adapter = new EventImageAdapter(events);
        binding.eventList.setAdapter(adapter);
        db.collection("clubs")
                .document(clubID)
                .collection("events")
                .whereNotEqualTo("imageUri", null)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    events.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        InfoModel event = doc.toObject(InfoModel.class);

                        if (event != null) {
                            events.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("LSJ", "Failed to load events", e)
                );


        View chatRow = view.findViewById(R.id.chatInquiryRow);

        chatRow.setOnClickListener(v -> {
            ChatRoomFragment f = new ChatRoomFragment();
            Bundle b = new Bundle();
            b.putString("clubId", clubID);
            f.setArguments(b);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.full_screen_container, f) // 여기 컨테이너 id를 프로젝트에 맞게
                    .addToBackStack(null)
                    .commit();
        });

        /// users의 문서에 appliedClubs 라는 필드로 동아리 uid들의 배열이 생긴다고 가정하고
        /// 신청버튼 활성화 유무를 결정한 것이기 때문에 db 구조에 따라 변경 해야할 수도 있음
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (!doc.exists()) return;

                            List<String> favoriteClubs = (List<String>) doc.get("favoriteClubs");
                            if (favoriteClubs != null && favoriteClubs.contains(clubID)) {
                                binding.favBtn.setImageResource(R.drawable.ic_heart_filled);
                                binding.favBtn.setOnClickListener(v -> {
                                    binding.favBtn.setImageResource(R.drawable.ic_heart);
                                    java.util.Map<String, Object> update = new java.util.HashMap<>();
                                    update.put("favoriteClubs", com.google.firebase.firestore.FieldValue.arrayRemove(clubID));
                                });
                            } else {
                                binding.favBtn.setImageResource(R.drawable.ic_heart);
                                binding.favBtn.setOnClickListener(v -> {
                                    binding.favBtn.setImageResource(R.drawable.ic_heart_filled);
                                    java.util.Map<String, Object> update = new java.util.HashMap<>();
                                    update.put("favoriteClubs", com.google.firebase.firestore.FieldValue.arrayUnion(clubID));
                                });
                                }
                            }).addOnFailureListener(e -> {
                            Log.w("LSJ", "Error getting documents.", e);
                        });

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    List<String> appliedClubs = (List<String>) doc.get("appliedClubs");

                    if (appliedClubs != null && appliedClubs.contains(clubID)) {
                        binding.applyBtn.setText("신청 완료");
                        binding.applyBtn.setEnabled(false);
                    } else {
                        binding.applyBtn.setText("가입신청 하러가기");
                        binding.applyBtn.setEnabled(true);
                    }
                });



        binding.backBtn.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        getChildFragmentManager().setFragmentResultListener("afterApply", this, (requestKey, result) -> {
            binding.applyBtn.setText(result.getString("afterApply"));
            binding.applyBtn.setEnabled(false);
        });

        binding.applyBtn.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.clubInfo, new ClubApplyFragment().newInstance(clubID, ""))
                    .addToBackStack(null)
                    .commit();
        });
    }



    static class InfoModel {
        private String title;
        private String imageUri;

        public InfoModel() {}

        public InfoModel(String eventTitle, String imageUri) {
            this.title = eventTitle;
            this.imageUri = imageUri;
        }

        public String getTitle() {
            return title;
        }

        public String getImageUri() {
            return imageUri;
        }
    }

    class EventImageAdapter extends RecyclerView.Adapter<EventImageAdapter.EventViewHolder> {

        private List<InfoModel> eventList;

        public EventImageAdapter(List<InfoModel> eventList) {
            this.eventList = eventList;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.club_inform_image, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            InfoModel event = eventList.get(position);

            holder.eventTitle.setText(event.getTitle());

            String imageUrl = event.getImageUri();
            Log.d("LSJ", "image uri= " + imageUrl);

            if (imageUrl == null || imageUrl.isEmpty()) {
                holder.posterImage.setImageResource(R.drawable.image);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.image)
                        .error(R.drawable.image)
                        .into(holder.posterImage);
            }
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        static class EventViewHolder extends RecyclerView.ViewHolder {
            ImageView posterImage;
            TextView eventTitle;

            public EventViewHolder(@NonNull View itemView) {
                super(itemView);
                posterImage = itemView.findViewById(R.id.posterImage);
                eventTitle = itemView.findViewById(R.id.eventTitle);
            }
        }
    }

}