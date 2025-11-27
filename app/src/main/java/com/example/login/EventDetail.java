package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.databinding.FragmentEventDetailBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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

        /// 각 동아리 문서의 events 하위컬렉션으로 접근할 수 있도록 변경 해야함!!!
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

                        if (doc.contains("recruit num")) binding.applyInform.setVisibility(View.VISIBLE);
                        else binding.applyInform.setVisibility(View.GONE);

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

        //모집 인원 띄우기
        List<ApplyModel> applyList = new ArrayList<>();
        applyList.add(new ApplyModel("신청자", "56781234", null));
        applyList.add(new ApplyModel("신청자2", "56789101", null));


        ApplyAdapter applyAdapter = new ApplyAdapter(requireContext(), applyList);

        RecyclerView applyRecyclerView = binding.applyInform;
        applyRecyclerView.setAdapter(applyAdapter);
        applyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    class ApplyModel{
        private final String name;
        private final String  studentNum;
        private final String profileUri;

        public ApplyModel(String name, String  studentNum, String profileUri){
            this.name = name;
            this.studentNum = studentNum;
            this.profileUri = profileUri;
        }

        public String getName(){ return name; }
        public String  getStudentNum(){ return studentNum; }
        public String getProfileUri(){ return profileUri; }
    }

    public class ApplyAdapter extends RecyclerView.Adapter<ApplyAdapter.ViewHolder> {

        private final List<ApplyModel> applyList;
        private final Context context;

        public ApplyAdapter(Context context, List<ApplyModel> applyList) {
            this.context = context;
            this.applyList = applyList;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.apply_inform, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ApplyModel model = applyList.get(position);
            holder.bind(model);
        }

        @Override
        public int getItemCount() {
            return applyList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView name;
            TextView studentNum;

            public ViewHolder(View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.profileImage);
                name = itemView.findViewById(R.id.name);
                studentNum = itemView.findViewById(R.id.studentNum);
            }

            public void bind(ApplyModel applyModel){
                name.setText(applyModel.getName());
                studentNum.setText(applyModel.getStudentNum());

                String uri = applyModel.getProfileUri();
                if(uri != null && !uri.isEmpty()){
                    Uri imageUri = android.net.Uri.parse(uri);
                    profileImage.setImageURI(imageUri);
                } else profileImage.setImageResource(R.drawable.image);
            }
        }
    }
}
