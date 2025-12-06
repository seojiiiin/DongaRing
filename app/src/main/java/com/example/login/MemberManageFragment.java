package com.example.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberManageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberManageFragment extends Fragment {
    private List<Member> memberList;
    private MemberAdapter adapter;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser user;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MemberManageFragment() {
        // Required empty public constructor
    }
    public static MemberManageFragment newInstance(String param1, String param2) {
        MemberManageFragment fragment = new MemberManageFragment();
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
        return inflater.inflate(R.layout.fragment_member_manage, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView profileList = view.findViewById(R.id.profile_list);
        memberList = new ArrayList<>();
        adapter = new MemberAdapter(memberList);
        profileList.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(profileList.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        profileList.addItemDecoration(dividerItemDecoration);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        profileList.setLayoutManager(layoutManager);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        
        //DB에서 부원목록 불러오기
        if (user != null) {
            String currentUid = user.getUid();
            Log.d("JHM", "로그인된 유저 UID: [" + currentUid + "]");
            
            db.collection("users_admin")
                    .whereEqualTo("uid", currentUid)  // uid 필드가 currentUid와 같은 문서 찾기
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 결과가 비어있지 않은지 확인
                            if (!task.getResult().isEmpty()) {
                                Log.d("JHM", "관리자 문서 발견");

                                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                                String clubId = document.getString("clubAdminOf");
                                if (clubId != null && !clubId.isEmpty()) {
                                    Log.d("JHM", "동아리 ID 발견: " + clubId);
                                    getClubMembers(clubId);
                                } else {
                                    Log.e("JHM", "실패: clubAdminOf 필드가 비어있음");
                                }
                            } else {
                                // 쿼리 결과가 0개일 때
                                Log.e("JHM", "실패: users_admin 컬렉션에서 uid 필드가 [" + currentUid + "]인 문서를 찾지 못함");
                            }
                        } else {
                            Log.e("JHM", "통신 에러 발생", task.getException());
                        }
                    });
        } else {
            Log.e("JHM", "로그인 정보 없음 (user is null)");
        }
    }
    private void getClubMembers(String clubId) {
        db.collection("clubs")
                .document(clubId)
                .collection("members")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        memberList.clear(); // 기존 데이터 초기화
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            // DB에는 studentNumber로 저장되어 있지만, Member 클래스는 department를 사용하므로 매핑
                            String studentNumber = document.getString("studentNumber");

                            // null 처리 후 추가
                            memberList.add(new Member(name, studentNumber != null ? studentNumber : ""));
                        }
                        adapter.notifyDataSetChanged(); // 리사이클러뷰 갱신
                        Log.d("JHM", "프로필 로드 완료, 갯수 : " + memberList.size());
                    } else {
                        Log.w("JHM", "Error getting documents.", task.getException());
                    }
                });
    }
    public class Member {
        private String name;
        private String department;
        public Member(String name, String department) {
            this.name = name;
            this.department = department;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }
    public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

        private List<Member> items;

        public MemberAdapter(List<Member> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 방금 수정한 item_member_profile_horizontal 레이아웃을 inflate 합니다.
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member_profile_horizontal, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Member item = items.get(position);

            // 이름 설정
            holder.tvName.setText(item.getName());

            // 학과 설정 (현재 Member 클래스에 학과 필드가 없으므로 고정된 텍스트 혹은 추후 추가 필요)
            holder.tvDepartment.setText(item.getDepartment()); // 예시 데이터
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvDepartment;
            // 이미지 뷰도 필요하다면 추가 (예: CircleImageView ivProfile;)

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_profile_name);
                tvDepartment = itemView.findViewById(R.id.tv_profile_department);
                // ivProfile = itemView.findViewById(R.id.circleImageView); // ID 확인 필요
            }
        }
    }

}