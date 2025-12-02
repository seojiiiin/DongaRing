package com.example.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemberManageActivity extends AppCompatActivity{

    private List<Applicant> applicantList;
    private ApplicantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_manage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView profileList = findViewById(R.id.rv_profile_list);
        ImageView btnBack = findViewById(R.id.btn_back);
        AppCompatButton btn_pass = findViewById(R.id.btn_pass);

        applicantList = new ArrayList<>();
        //TODO : FireBase에서 신청인원정보 가져오기
        applicantList.add(new Applicant("익명1"));
        applicantList.add(new Applicant("익명2"));
        applicantList.add(new Applicant("익명3"));
        applicantList.add(new Applicant("익명4"));
        Log.d("JHM", "프로필 로드 완료, 갯수 : " + applicantList.size());
        adapter = new ApplicantAdapter(applicantList);
        profileList.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        profileList.setLayoutManager(layoutManager);

        adapter.setOnItemClickListener(new ApplicantAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Applicant applicant) {
                Log.d("JHM", "Profile Clicked");
                //TODO : 신청양식 가져오기
                TextView name = findViewById(R.id.et_name);
                TextView collage = findViewById(R.id.collage);
                TextView department = findViewById(R.id.department);
                TextView studentId = findViewById(R.id.student_id);
                TextView phoneNum = findViewById(R.id.phone_num);
                TextView motivation = findViewById(R.id.motivation);

                name.setText("수정된 이름");
                collage.setText("수정된 단과대");
                department.setText("수정된 학과");
                studentId.setText("수정된 학번");
                phoneNum.setText("수정된 전화번호");
                motivation.setText("수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기" +
                        "수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기" +
                        "수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기수정된 지원동기");
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
    public class Applicant {
        private String name;
        public Applicant(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
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
                    .inflate(R.layout.item_applicant_profile, parent, false);
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
