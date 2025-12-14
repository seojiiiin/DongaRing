package com.example.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class ApplyManageActivity extends AppCompatActivity {

    private List<Applicant> applicantList;
    private ApplicantAdapter adapter;
    private FirebaseFirestore db;

    private String clubID;
    private String selectedUid = null; // 선택된 지원자 UID

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

        db = FirebaseFirestore.getInstance();

        clubID = getIntent().getStringExtra("clubID");
        if (clubID == null) {
            Toast.makeText(this, "clubID 없음", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecyclerView profileList = findViewById(R.id.rv_profile_list);
        ImageView btnBack = findViewById(R.id.btn_back);
        AppCompatButton btn_pass = findViewById(R.id.btn_pass);

        applicantList = new ArrayList<>();
        adapter = new ApplicantAdapter(applicantList);
        profileList.setAdapter(adapter);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        profileList.setLayoutManager(layoutManager);

        loadApplicants(); // Firestore에서 신청자 목록 불러오기

        // 신청자 프로필 클릭 시 지원서 표시
        adapter.setOnItemClickListener(applicant -> {
            selectedUid = applicant.getUid();
            loadApplicantForm(selectedUid);
        });

        btnBack.setOnClickListener(v -> {
            Log.d("JHM", "Back Clicked");
            finish();
        });

        // 합격 처리
        btn_pass.setOnClickListener(v -> processPass());
    }


    // 신청한 유저 목록 가져오기
    private void loadApplicants() {
        db.collection("clubs")
                .document(clubID)
                .collection("applicants")
                .get()
                .addOnSuccessListener(query -> {
                    applicantList.clear();

                    for (DocumentSnapshot doc : query) {
                        String uid = doc.getId();
                        String name = doc.getString("name");

                        applicantList.add(new Applicant(uid, name));
                    }

                    adapter.notifyDataSetChanged();
                });
    }


    // 특정 지원자의 신청서 가져오기
    private void loadApplicantForm(String uid) {

        db.collection("clubs")
                .document(clubID)
                .collection("applicants")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    TextView name = findViewById(R.id.et_name);
                    TextView collage = findViewById(R.id.collage);
                    TextView department = findViewById(R.id.department);
                    TextView studentId = findViewById(R.id.student_id);
                    TextView phoneNum = findViewById(R.id.phone_num);
                    TextView motivation = findViewById(R.id.motivation);

                    name.setText(doc.getString("name"));
                    collage.setText(doc.getString("major"));
                    department.setText(doc.getString("department"));
                    studentId.setText(doc.getString("studentNumber"));
                    phoneNum.setText(doc.getString("phone"));
                    motivation.setText(doc.getString("motivation"));
                });
    }


    // 합격 처리
    private void processPass() {

        if (selectedUid == null) {
            Toast.makeText(this, "지원자를 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // users/{uid}/joinedClubs 배열에 clubID 추가
        db.collection("users")
                .document(selectedUid)
                .update("joinedClubs", FieldValue.arrayUnion(clubID))
                .addOnSuccessListener(unused -> {

                    // applicants에서 삭제
                    db.collection("clubs")
                            .document(clubID)
                            .collection("applicants")
                            .document(selectedUid)
                            .delete()
                            .addOnSuccessListener(done -> {

                                Toast.makeText(this, "합격 처리 완료", Toast.LENGTH_SHORT).show();

                                // UI에서도 제거
                                for (int i = 0; i < applicantList.size(); i++) {
                                    if (applicantList.get(i).getUid().equals(selectedUid)) {
                                        applicantList.remove(i);
                                        break;
                                    }
                                }
                                adapter.notifyDataSetChanged();

                                selectedUid = null;
                            });
                });
    }


    // 모델 클래스
    public static class Applicant {
        private String uid;
        private String name;

        public Applicant(String uid, String name) {
            this.uid = uid;
            this.name = name;
        }

        public String getUid() { return uid; }

        public String getName() { return name; }
    }


    // RecyclerView Adapter
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

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_profile_name);
            }

            public void bind(final Applicant item, final OnItemClickListener listener) {
                tvName.setText(item.getName());

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                });
            }
        }
    }
}
