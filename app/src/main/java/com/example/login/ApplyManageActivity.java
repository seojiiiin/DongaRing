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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ApplyManageActivity extends AppCompatActivity {

    private List<Applicant> applicantList;
    private ApplicantAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Applicant selectedApplicant = null; // 선택된 지원자
    private String clubId = null;              // 관리하는 클럽 ID

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

        adapter = new ApplicantAdapter(applicantList);
        profileList.setAdapter(adapter);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        profileList.setLayoutManager(layoutManager);

        // 로그인한 관리자가 관리하는 clubID 불러오기
        loadAdminClubInfo();

        // 지원자 클릭 시 상세 정보 표시
        adapter.setOnItemClickListener(applicant -> {
            selectedApplicant = applicant;

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
        });

        btnBack.setOnClickListener(v -> finish());

        // 합격 처리 버튼
        btn_pass.setOnClickListener(v -> processPass());
    }

    // 관리자 클럽 정보 로드
    private void loadAdminClubInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("users_admin").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "관리자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    clubId = documentSnapshot.getString("clubAdminOf");

                    if (clubId == null || clubId.isEmpty()) {
                        Toast.makeText(this, "관리하는 동아리 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("동아링", "관리자 동아리 ID 확인: " + clubId);

                    loadApplicants(clubId);
                })
                .addOnFailureListener(e ->
                        Log.d("동아링", "관리자 정보 로드 실패", e));
    }

    // applicants 컬렉션에서 지원자 목록 불러오기
    private void loadApplicants(String clubId) {

        db.collection("clubs").document(clubId).collection("applicants")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("동아링", "신청자 목록 로드 실패: ", task.getException());
                        return;
                    }

                    applicantList.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String uid = document.getId();
                        String name = document.getString("name");
                        String major = document.getString("major");
                        String studentNumber = document.getString("studentNumber");
                        String phoneNumber = document.getString("phone");
                        String motivation = document.getString("motivation");

                        if (name != null) {
                            applicantList.add(
                                    new Applicant(uid, name,
                                            major != null ? major : "",
                                            studentNumber != null ? studentNumber : "",
                                            phoneNumber != null ? phoneNumber : "",
                                            motivation != null ? motivation : "")
                            );
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (applicantList.isEmpty()) {
                        Toast.makeText(this, "아직 신청자가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 최종 합격 처리 로직
    private void processPass() {

        if (selectedApplicant == null) {
            Toast.makeText(this, "지원자를 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clubId == null) {
            Toast.makeText(this, "클럽 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = selectedApplicant.getUid();
        String name = selectedApplicant.getName();
        String studentNumber = selectedApplicant.getStudentNumber();

        // 1) users/{uid}/joinedClubs 배열에 clubId 추가
        db.collection("users")
                .document(uid)
                .update("joinedClubs", FieldValue.arrayUnion(clubId))
                .addOnSuccessListener(a -> {

                    // 2) clubs/{clubId}/members/{uid} 문서 생성
                    db.collection("clubs")
                            .document(clubId)
                            .collection("members")
                            .document(uid)
                            .set(new MemberInfo(name, studentNumber))
                            .addOnSuccessListener(b -> {

                                // 3) users/{uid}/appliedClubs 배열에서 clubId 제거
                                db.collection("users")
                                        .document(uid)
                                        .update("appliedClubs", FieldValue.arrayRemove(clubId))
                                        .addOnSuccessListener(c -> {

                                            // 4) clubs/{clubId}/applicants/{uid} 삭제
                                            db.collection("clubs")
                                                    .document(clubId)
                                                    .collection("applicants")
                                                    .document(uid)
                                                    .delete()
                                                    .addOnSuccessListener(d -> {

                                                        Toast.makeText(
                                                                this,
                                                                "합격 처리가 완료되었습니다.",
                                                                Toast.LENGTH_SHORT).show();

                                                        applicantList.remove(selectedApplicant);
                                                        adapter.notifyDataSetChanged();
                                                        selectedApplicant = null;
                                                    });
                                        });
                            });
                });
    }

    // 모델 클래스
    public class Applicant {
        private String uid;
        private String name;
        private String major;
        private String studentNumber;
        private String phone;
        private String motivation;

        public Applicant(String uid, String name, String major,
                         String studentNumber, String phone, String motivation) {

            this.uid = uid;
            this.name = name;
            this.major = major;
            this.studentNumber = studentNumber;
            this.phone = phone;
            this.motivation = motivation;
        }

        public String getUid() { return uid; }
        public String getName() { return name; }
        public String getMajor() { return major; }
        public String getStudentNumber() { return studentNumber; }
        public String getPhone() { return phone; }
        public String getMotivation() { return motivation; }
    }

    // Firestore 전송용 객체
    public class MemberInfo {
        public String name;
        public String studentNumber;

        public MemberInfo() {}

        public MemberInfo(String name, String studentNumber) {
            this.name = name;
            this.studentNumber = studentNumber;
        }
    }

    // RecyclerView Adapter
    public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ViewHolder> {

        private List<Applicant> applicantList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(Applicant applicant);
        }

        public ApplicantAdapter(List<Applicant> applicantList) {
            this.applicantList = applicantList;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_applicant_profile_vertical, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(applicantList.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return applicantList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_profile_name);
            }

            public void bind(final Applicant item, final OnItemClickListener listener) {
                tvName.setText(item.getName());

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(item);
                });
            }
        }
    }
}
