package com.example.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.login.databinding.FragmentSettingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {
    //다른 페이지에서 파라미터로 유저타입 넘어옴(user, admin)
    private String userType;

    private FragmentSettingBinding binding;
    private AlertDialog logoutDialog;
    private AlertDialog alarmDialog;
    private int dialogType = -1;

    private static final int DIALOG_LOGOUT = 0;
    private static final int DIALOG_NOTIFICATION = 1;

    private DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialogType == DIALOG_LOGOUT){
                if (which == DialogInterface.BUTTON_POSITIVE){
                    //로그아웃
                    FirebaseAuth.getInstance().signOut();
                    Log.d("LSJ", "로그아웃 완료");
                    Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(requireContext(), MainActivity.class));
                }
            }
            else if (dialogType == DIALOG_NOTIFICATION){
                String[] data = getResources().getStringArray(R.array.dialog_array);
                Log.d("LSJ", data[which] + " 선택");

                if (which == DialogInterface.BUTTON_POSITIVE){
                    Log.d("LSJ", "알람 설정 적용");
                }
            }

        }
    };

    public SettingFragment(){

    }
    public SettingFragment(String userType) {
        this.userType = userType;
        // Required empty public constructor
    }


    public static SettingFragment newInstance(String userType) {
        SettingFragment fragment = new SettingFragment(userType);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.userType = userType;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //뒤로가기버튼
        binding.back.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();
        });
        
        //유저 이름, 이메일 불러오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid =FirebaseAuth.getInstance().getCurrentUser().getUid();

        String collectionName = userType.equals("user") ? "users" : "users_admin";
        if (uid != null) {
            db.collection(collectionName).document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 데이터 가져오기 성공
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            binding.name.setText(name);
                            binding.email.setText(email);

                            Log.d("JHM", "사용자 정보 로드 성공: " + name);
                        } else {
                            Log.d("JHM", "해당 문서가 존재하지 않습니다.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("JHM", "데이터 가져오기 실패", e);
                    });
        }
        
        //알람버튼
        binding.alarmSetting.setOnClickListener(v -> {
            dialogType = DIALOG_NOTIFICATION;

            String[] data = getResources().getStringArray(R.array.dialog_array);
            boolean[] checked = new boolean[data.length];

            alarmDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("알림 설정")
                    .setMultiChoiceItems(data, checked, (dialog, which, isChecked) -> {
                        checked[which] = isChecked;
                        Log.d("LSJ", data[which] + (isChecked ? " 선택" : " 해제"));
                    })
                    .setPositiveButton("적용", dialogListener)
                    .setNegativeButton("취소", null)
                    .create();

            alarmDialog.show();

        });
        
        //로그아웃버튼
        binding.logout.setOnClickListener(v -> {
            dialogType = DIALOG_LOGOUT;

            logoutDialog = new AlertDialog.Builder(requireContext())
                    .setIcon(android.R.drawable.ic_notification_overlay)
                    .setTitle("로그아웃")
                    .setMessage("로그아웃 하시겠습니까?")
                    .setPositiveButton("네", dialogListener)
                    .setNegativeButton("취소", null)
                    .create();

            logoutDialog.show();
        });
    }
}