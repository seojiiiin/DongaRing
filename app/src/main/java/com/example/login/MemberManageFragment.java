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
        //TODO : DB에서 회원정보 가져오기
        memberList.add(new Member("이름1", "학과1"));
        memberList.add(new Member("이름2", "학과2"));
        memberList.add(new Member("이름3", "학과3"));
        memberList.add(new Member("이름4", "학과4"));
        memberList.add(new Member("이름5", "학과5"));
        memberList.add(new Member("이름6", "학과6"));
        memberList.add(new Member("이름7", "학과7"));
        memberList.add(new Member("이름8", "학과8"));
        memberList.add(new Member("이름9", "학과9"));
        Log.d("JHM", "프로필 로드 완료, 갯수 : " + memberList.size());
        adapter = new MemberAdapter(memberList);
        profileList.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(profileList.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        profileList.addItemDecoration(dividerItemDecoration);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        profileList.setLayoutManager(layoutManager);
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