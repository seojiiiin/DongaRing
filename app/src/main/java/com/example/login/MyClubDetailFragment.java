package com.example.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.login.databinding.FragmentMyClubDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyClubDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyClubDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String clubID;
    private String eventID;
    private FragmentMyClubDetailBinding binding;
    private FirebaseFirestore db;


    public MyClubDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyClubDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyClubDetailFragment newInstance(String param1, String param2) {
        MyClubDetailFragment fragment = new MyClubDetailFragment();
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
            eventID = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyClubDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        db.collection("clubs")
                .document(clubID)
                .collection("events")
                .document(eventID)
                .get()
                .addOnSuccessListener(Doc -> {
                            if (Doc.exists()) {
                                if (Doc.contains("recruitNum"))
                                    binding.joinButton.setVisibility(View.VISIBLE);
                            }
                        })
                .addOnFailureListener(e -> { Log.w("LSJ", "Error getting documents.");
                });
;

        binding.joinButton.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // 이벤트 문서 가져오기
            db.collection("clubs")
                    .document(clubID)
                    .collection("events")
                    .document(eventID)
                    .get()
                    .addOnSuccessListener(eventDoc -> {
                        if (!eventDoc.exists()) return;

                        Long recruitNum = eventDoc.getLong("recruitNum");
                        boolean hasLimit = recruitNum != null;

                        // applicants 수 체크
                        db.collection("clubs")
                                .document(clubID)
                                .collection("events")
                                .document(eventID)
                                .collection("applicants")
                                .get()
                                .addOnSuccessListener(applicantsSnap -> {

                                    int currentCount = applicantsSnap.size();

                                    if (hasLimit && currentCount >= recruitNum) {
                                        Toast.makeText(requireContext(), "신청 인원이 모두 찼습니다.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // 유저 정보 가져오기
                                    db.collection("users")
                                            .document(uid)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                if (!userDoc.exists()) return;

                                                String name = userDoc.getString("name");
                                                String studentId = userDoc.getString("studentId");

                                                Map<String, Object> applyData = new HashMap<>();
                                                applyData.put("name", name);
                                                applyData.put("studentId", studentId);
                                                applyData.put("uid", uid);
                                                applyData.put("timestamp", FieldValue.serverTimestamp());

                                                // 1) 이벤트 아래 applicants 추가
                                                db.collection("clubs")
                                                        .document(clubID)
                                                        .collection("events")
                                                        .document(eventID)
                                                        .collection("applicants")
                                                        .document(uid)
                                                        .set(applyData)
                                                        .addOnSuccessListener(unused -> {

                                                            // 2) users에 appliedEvents 배열 업데이트
                                                            db.collection("users")
                                                                    .document(uid)
                                                                    .update("appliedEvents", FieldValue.arrayUnion(eventID))
                                                                    .addOnSuccessListener(done -> {
                                                                        Toast.makeText(requireContext(), "신청 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                                                        binding.joinButton.setEnabled(false);
                                                                    });
                                                        });
                                            });
                                });
                    });
        });

        binding.back.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        db.collection("clubs")
                .document(clubID)
                .collection("events")
                .document(eventID)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String title = doc.getString("title");
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");
                        String location = doc.getString("location");
                        String content = doc.getString("content");

                        if(doc.contains("recruit num")) binding.joinButton.setVisibility(View.VISIBLE);

                        binding.eventName.setText(title);
                        binding.startDate.setText(startDate);
                        binding.endDate.setText(endDate);
                        binding.location.setText(location);
                        binding.content.setText(content);
                    }
                });
    }
}