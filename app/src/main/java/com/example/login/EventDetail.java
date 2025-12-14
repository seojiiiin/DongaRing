package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.databinding.FragmentEventDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventDetail extends Fragment {
    FragmentEventDetailBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String title;
    private String startDate;
    private String endDate;
    private String location;
    private String content;
    private String imageUri;
    private String docID;
    private String currentClubID;
    private List<ApplyModel> applyList = new ArrayList<>();
    private ApplyAdapter applyAdapter;

    public static EventDetail newInstance(String none) {
        EventDetail fragment = new EventDetail();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyAdapter = new ApplyAdapter(requireContext(), applyList);
        RecyclerView applyRecyclerView = binding.applyInform;
        applyRecyclerView.setAdapter(applyAdapter);
        applyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        if (getArguments() != null) {
            docID = getArguments().getString("documentID");
        }
        //뒤로가기 버튼
        binding.back.setOnClickListener(v-> getParentFragmentManager().popBackStack());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // 3. users_admin 컬렉션에서 현재 유저의 관리자 정보 확인
            db.collection("users_admin").document(uid).get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            // 관리자로 있는 동아리 ID (clubAdminOf) 가져오기
                            currentClubID = userDoc.getString("clubAdminOf");

                            if (currentClubID != null && !currentClubID.isEmpty()) {
                                // 4. 동아리 ID와 이벤트 ID(docID)를 바탕으로 데이터 로드 시작

                                // (1) 이벤트 상세 내용 로드
                                loadEventDetails(currentClubID, docID);

                                // (2) 신청자 목록 로드
                                loadApplicants(currentClubID, docID);

                            } else {
                                Log.d("동아링", "User is not an admin of any club");
                                Toast.makeText(getContext(), "동아리 관리자 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                                binding.applyInform.setVisibility(View.GONE);
                            }
                        } else {
                            // users_admin에 문서가 없는 경우
                            Log.d("동아링", "User info not found in users_admin");
                            binding.applyInform.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> Log.d("동아링", "Failed to fetch user info", e));
        } else {
            if(getContext() != null)
                Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }

        //수정
        binding.editButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("documentId", docID);
            bundle.putString("title", title);
            bundle.putString("startDate", startDate);
            bundle.putString("endDate", endDate);
            bundle.putString("location", location);
            bundle.putString("content", content);
            bundle.putString("imageUri", imageUri);

            AddEventFragment fragment = new AddEventFragment();
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        //applyList.add(new ApplyModel("신청자", "56781234", null));
        //applyList.add(new ApplyModel("신청자2", "56789101", null));




    }
    private void loadApplicants(String clubId, String eventDocId) {
        if (clubId == null || eventDocId == null) return;

        // 경로: clubs -> {clubId} -> events -> {eventDocId} -> applicants
        db.collection("clubs").document(clubId)
                .collection("events").document(eventDocId)
                .collection("applicants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    applyList.clear(); // 기존 목록 초기화

                    if (!queryDocumentSnapshots.isEmpty()) {
                        binding.applyInform.setVisibility(View.VISIBLE);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            // name과 StudentNumber 필드 로드
                            String name = doc.getString("name");
                            String studentNum = doc.getString("studentNumber"); // 필드명 정확히 일치해야 함
                            String profileUri = doc.getString("profileUri"); // 필요하다면 사용

                            applyList.add(new ApplyModel(name, studentNum, profileUri));
                        }
                        // 데이터 변경 알림
                        applyAdapter.notifyDataSetChanged();

                    } else {
                        Log.d("동아링", "신청자가 없습니다.");
                        // 신청자가 없더라도 리스트는 비워진 상태로 유지
                        applyAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("동아링", "Failed to load applicants", e);
                });
    }
    private void loadEventDetails(String clubId, String eventDocId) {
        // 경로 수정: clubs -> {clubId} -> events -> {eventDocId}
        db.collection("clubs").document(clubId)
                .collection("events").document(eventDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()){
                        // 필드명 파싱
                        title = doc.getString("event name");
                        // 만약 DB에 title로 저장되어 있을 수도 있으니 null 체크 (선택사항)
                        if (title == null) title = doc.getString("title");

                        startDate = doc.getString("startDate");
                        endDate = doc.getString("endDate");
                        location = doc.getString("location");
                        content = doc.getString("content");
                        imageUri = doc.getString("imageUri");

                        binding.eventName.setText(title);
                        binding.startDate.setText("시작시각 : " + startDate);
                        binding.endDate.setText("종료시각 : " + endDate);
                        binding.location.setText("장소 : " + location);
                        binding.content.setText(content);

                        // recruit num이 있으면 신청자 뷰 보이기 (없으면 숨기기 로직은 유지하되, loadApplicants에서 데이터가 있으면 보이게 처리됨)
                        if (doc.contains("recruit num")) {
                            // binding.applyInform.setVisibility(View.VISIBLE); // loadApplicants 결과에 맡김
                        }
                    } else {
                        Log.d("동아링", "Event document not found");
                    }
                })
                .addOnFailureListener(e -> Log.d("동아링", "fail to load document", e));
    }

    class ApplyModel{
        private final String name;
        private final String  studentNum;
        private final String profileUri;

        public ApplyModel(String name, String  studentNum, String profileUri){
            this.name = name;
            this.studentNum = studentNum;
            this.profileUri = profileUri;
        }

        public String getName(){ return name; }
        public String  getStudentNum(){ return studentNum; }
        public String getProfileUri(){ return profileUri; }
    }

    public class ApplyAdapter extends RecyclerView.Adapter<ApplyAdapter.ViewHolder> {

        private final List<ApplyModel> applyList;
        private final Context context;

        public ApplyAdapter(Context context, List<ApplyModel> applyList) {
            this.context = context;
            this.applyList = applyList;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.apply_inform, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ApplyModel model = applyList.get(position);
            holder.bind(model);
        }

        @Override
        public int getItemCount() {
            return applyList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView name;
            TextView studentNum;

            public ViewHolder(View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.profileImage);
                name = itemView.findViewById(R.id.name);
                studentNum = itemView.findViewById(R.id.studentNum);
            }

            public void bind(ApplyModel applyModel){
                name.setText(applyModel.getName());
                studentNum.setText(applyModel.getStudentNum());

                String uri = applyModel.getProfileUri();
                if(uri != null && !uri.isEmpty()){
                    Uri imageUri = android.net.Uri.parse(uri);
                    profileImage.setImageURI(imageUri);
                } else profileImage.setImageResource(R.drawable.image);
            }
        }
    }
}
