package com.example.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClubApplyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClubApplyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String clubID;
    private String mParam2;

    public ClubApplyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClubApplyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClubApplyFragment newInstance(String param1, String param2) {
        ClubApplyFragment fragment = new ClubApplyFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_club_apply, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        Button submitBtn = view.findViewById(R.id.submitBtn);

        // 입력 필드 가져오기
        EditText nameInput = view.findViewById(R.id.name);
        EditText majorInput = view.findViewById(R.id.major);
        EditText studentNumberInput = view.findViewById(R.id.studentNumber);
        EditText phoneInput = view.findViewById(R.id.phoneNumber);
        EditText motivationInput = view.findViewById(R.id.motivation);

        submitBtn.setOnClickListener(v -> {

            // 1) 입력값 가져오기
            String name = nameInput.getText().toString();
            String major = majorInput.getText().toString();
            String studentNumber = studentNumberInput.getText().toString();
            String phone = phoneInput.getText().toString();
            String motivation = motivationInput.getText().toString();

            // 입력값 Map
            Map<String, Object> applyData = new HashMap<>();
            applyData.put("uid", uid);
            applyData.put("name", name);
            applyData.put("major", major);
            applyData.put("studentNumber", studentNumber);
            applyData.put("phone", phone);
            applyData.put("motivation", motivation);
            applyData.put("timestamp", FieldValue.serverTimestamp());

            // 2) clubs → clubID → applicants → uid 로 저장
            db.collection("clubs")
                    .document(clubID)
                    .collection("applicants")
                    .document(uid)
                    .set(applyData)
                    .addOnSuccessListener(unused -> {

                        // 3) users → uid → appliedClubs 배열에 clubID 추가
                        db.collection("users")
                                .document(uid)
                                .update("appliedClubs", FieldValue.arrayUnion(clubID))
                                .addOnSuccessListener(done -> {

                                    Toast.makeText(requireContext(), "지원이 완료되었습니다!", Toast.LENGTH_SHORT).show();

                                    // 결과 전달 + 이전 화면으로
                                    Bundle afterApply = new Bundle();
                                    afterApply.putString("afterApply", "신청 완료");
                                    getParentFragmentManager().setFragmentResult("afterApply", afterApply);
                                    getParentFragmentManager().popBackStack();
                                });
                    });
        });
    }

}