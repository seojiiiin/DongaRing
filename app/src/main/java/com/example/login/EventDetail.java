package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.login.databinding.FragmentEventDetailBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetail extends Fragment {
    FragmentEventDetailBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String title;
    private String startDate;
    private String endDate;
    private String location;
    private String content;
    private String imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //뒤로가기 버튼
        binding.back.setOnClickListener(v-> getParentFragmentManager().popBackStack());


        //이벤트 정보 불러오기
        String docID = getArguments().getString("documentID");

        /// events가 하위컬렉션이므로 코드 수정 필요함. 동아리 문서에 따라 events 하위 컬렉션이 있는거임
        db.collection("events")
                .document(docID)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()){
                        title = doc.getString("event name");
                        startDate = doc.getString("start date");
                        endDate = doc.getString("end date");
                        location = doc.getString("location");
                        content = doc.getString("content");
                        imageUri = doc.getString("imageUri");

                        binding.eventName.setText(title);
                        binding.startDate.setText(startDate);
                        binding.endDate.setText(endDate);
                        binding.location.setText(location);
                        binding.content.setText(content);
                    }
                })
                .addOnFailureListener(e -> Log.w("LSJ", "fail to load document", e));

        //수정
        binding.editButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("documentId", docID);
            bundle.putString("title", title);
            bundle.putString("startDate", startDate);
            bundle.putString("endDate", endDate);
            bundle.putString("location", location);
            bundle.putString("content", content);
            bundle.putString("imageUri", imageUri);

            AddEventFragment fragment = new AddEventFragment();
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
