package com.example.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.login.databinding.FragmentMyClubBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyClubFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyClubFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String clubID;
    private String mParam2;
    private FragmentMyClubBinding binding;

    private FirebaseFirestore db;

    public MyClubFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyClubFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyClubFragment newInstance(String param1, String param2) {
        MyClubFragment fragment = new MyClubFragment();
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
        binding = FragmentMyClubBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        binding.home.setOnClickListener(v -> {
            requireActivity().finish();
        });

        //동아리 이름 반영
        db.collection("clubs")
                .document(clubID).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        binding.clubName.setText(name);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("LSJ", "Error getting documents.", e);
                });

        db.collection("clubs")
                .document(clubID)
                .collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            String eventTitle = doc.getString("title");
                            String startDate = doc.getString("startDate");
                            String endDate = doc.getString("endDate");
                            String eventContent = doc.getString("content");

                            Log.d("LSJ", "Event Title: " + eventTitle);
                        }
                }else Log.d("LSJ", "No events found");
                })
                .addOnFailureListener(e -> Log.e("LSJ", "Failed to load events.", e));

    }
}