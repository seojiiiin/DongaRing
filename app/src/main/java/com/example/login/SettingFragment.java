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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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


    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
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
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.back.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();
        });

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