package com.example.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ApplyManageActivity extends AppCompatActivity{

    private List<Applicant> applicantList;
    private ApplicantAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apply_manage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView profileList = findViewById(R.id.rv_profile_list);
        ImageView btnBack = findViewById(R.id.btn_back);
        AppCompatButton btn_pass = findViewById(R.id.btn_pass);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        applicantList = new ArrayList<>();
        Log.d("JHM", "프로필 로드 완료, 갯수 : " + applicantList.size());
        adapter = new ApplicantAdapter(applicantList);
        profileList.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        profileList.setLayoutManager(layoutManager);

        //신청인원 불러오기
        loadAdminClubInfo();

        adapter.setOnItemClickListener(new ApplicantAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Applicant applicant) {
                Log.d("JHM", "Profile Clicked");
                //TODO : 신청양식 가져오기
                TextView name = findViewById(R.id.et_name);
                TextView major = findViewById(R.id.department);
                TextView studentId = findViewById(R.id.student_id);
                TextView phoneNum = findViewById(R.id.phone_num);
                TextView motivation = findViewById(R.id.motivation);

                name.setText(applicant.getName());
                major.setText(applicant.getMajor());
                studentId.setText(applicant.getStudentNumber());
                phoneNum.setText(applicant.getPhone());
                motivation.setText(applicant.getMotivation());

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("JHM", "Back Clicked");
                finish();
            }
        });
        btn_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("JHM", "Pass Clicked");
                //TODO : firebase에서 합격처리
            }
        });
    }
    private void loadAdminClubInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // users_admin 컬렉션에서 현재 유저의 문서를 조회
        db.collection("users_admin").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // clubAdminOf 필드에서 동아리 ID 가져오기
                        String clubId = documentSnapshot.getString("clubAdminOf");

                        if (clubId != null && !clubId.isEmpty()) {
                            Log.d("JHM", "관리자 동아리 ID 확인: " + clubId);
                            loadApplicants(clubId); // 신청자 목록 불러오기 호출
                        } else {
                            Log.e("JHM", "관리자 계정이지만 연결된 동아리가 없습니다.");
                            Toast.makeText(ApplyManageActivity.this, "관리하는 동아리 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("JHM", "관리자 정보를 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("JHM", "관리자 정보 로드 실패", e);
                });
    }

    // 동아리 ID를 기반으로 applicants 컬렉션을 조회하는 함수
    private void loadApplicants(String clubId) {
        db.collection("clubs").document(clubId).collection("applicants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        applicantList.clear(); // 기존 목록 초기화
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // name 필드 가져오기
                            String name = document.getString("name");
                            String major = document.getString("major") != null ? document.getString("major") : "";
                            String studentNumber = document.getString("studentNumber") != null ? document.getString("studentNumber") : "";
                            String phoneNumber = document.getString("phone") != null ? document.getString("phone") : "";
                            String motivation = document.getString("motivation") != null ? document.getString("motivation") : "";

                            if (name != null) {
                                applicantList.add(new Applicant(name, major, studentNumber, phoneNumber, motivation));
                            }
                        }
                        adapter.notifyDataSetChanged(); // 리사이클러뷰 갱신
                        Log.d("JHM", "신청자 목록 로드 완료, 갯수 : " + applicantList.size());

                        if (applicantList.isEmpty()) {
                            Toast.makeText(ApplyManageActivity.this, "아직 신청자가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("JHM", "Error getting documents: ", task.getException());
                        Toast.makeText(ApplyManageActivity.this, "신청자 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public class Applicant {
        private String name;
        private String major;
        private String studentNumber;
        private String phone;
        private String motivation;
        public Applicant(String name, String department,
                         String studentId, String phoneNum, String motivation) {
            this.name = name;
            this.major = department;
            this.studentNumber = studentId;
            this.phone = phoneNum;
            this.motivation = motivation;
        }

        public String getName() {
            return name;
        }
        public String getMajor() { return major; }
        public String getStudentNumber() { return studentNumber; }
        public String getPhone() { return phone; }
        public String getMotivation() { return motivation; }
    }
    public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ViewHolder> {

        private List<Applicant> applicantList;

        public interface OnItemClickListener {
            void onItemClick(Applicant applicant);
        }
        private OnItemClickListener listener;

        public ApplicantAdapter(List<Applicant> applicantList) {
            this.applicantList = applicantList;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // item_applicant_profile.xml 레이아웃을 inflate
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_applicant_profile_vertical, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Applicant item = applicantList.get(position);
            holder.bind(item, listener);
        }

        @Override
        public int getItemCount() {
            return applicantList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            // ImageView ivProfile; // 프로필 이미지가 있다면

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_profile_name);
            }

            public void bind(final Applicant item, final OnItemClickListener listener) {
                tvName.setText(item.getName());

                // 아이템 클릭 리스너 설정
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onItemClick(item);
                        }
                    }
                });
            }
        }
    }
}
