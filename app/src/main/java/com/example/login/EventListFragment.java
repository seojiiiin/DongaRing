package com.example.login;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.login.databinding.ActivityEventListBinding; // 바인딩 클래스 이름은 XML 파일명에 따라 다를 수 있습니다.
import com.example.login.databinding.EventCardviewBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends Fragment {
    private ActivityEventListBinding binding;
    private ArrayList<EventModel> events;
    private EventListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        events = new ArrayList<>();
        // this 대신 requireContext() 사용
        adapter = new EventListAdapter(requireContext(), events);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.addButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new AddEventFragment())
                    .addToBackStack(null)
                    .commit();
        });

        getParentFragmentManager().setFragmentResultListener(
                "event_added",
                this,
                (requestKey, result) -> loadEvent());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void refreshList(){
        loadEvent();
    }

    private void loadEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e("LSJ", "User not logged in");
            return;
        }

        db.collection("users_admin").document(user.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String clubId = userDoc.getString("clubAdminOf");

                        if (clubId != null && !clubId.isEmpty()) {
                            db.collection("clubs").document(clubId)
                                    .get()
                                    .addOnSuccessListener(clubDoc -> {
                                        if (clubDoc.exists()) {
                                            String clubName = clubDoc.getString("club name");

                                            clubDoc.getReference().collection("events")
                                                    .get()
                                                    .addOnSuccessListener(eventSnapshots -> {
                                                        events.clear();

                                                        for (var eventDoc : eventSnapshots.getDocuments()) {
                                                            EventModel model = new EventModel(
                                                                    eventDoc.getString("club name") != null ? eventDoc.getString("club name") : clubName,
                                                                    eventDoc.getString("title"),
                                                                    eventDoc.getString("startDate"),
                                                                    eventDoc.getString("endDate"),
                                                                    eventDoc.getString("location"),
                                                                    eventDoc.getString("content"),
                                                                    eventDoc.getString("imageUri"),
                                                                    eventDoc.getId()
                                                            );
                                                            events.add(model);
                                                        }
                                                        if (adapter != null) {
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> Log.d("LSJ", "Failed to load sub events", e));
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("LSJ", "Failed to load club info", e));
                        } else {
                            Log.d("LSJ", "This user is not an admin of any club.");
                            events.clear();
                            if (adapter != null) adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("LSJ", "User admin info not found.");
                    }
                })
                .addOnFailureListener(e -> Log.e("LSJ", "Failed to load user admin doc", e));
    }

    public static class EventModel {
        private String clubName;
        private String title;
        private String startDate;
        private String endDate;
        private String location;
        private String content;
        private String imageUri;
        private String documentID;

        public EventModel(String clubName, String title, String startDate, String endDate,
                          String location, String content, String imageUri,
                          String documentID) {
            this.clubName = clubName;
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.location = location;
            this.content = content;
            this.imageUri = imageUri;
            this.documentID = documentID;
        }

        public String getClubName() { return clubName; }
        public String getTitle() { return title; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getLocation() { return location; }
        public String getContent() { return content; }
        public String getImageUri() { return imageUri; }
        public String getDocumentID() { return documentID; }
    }

    private class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder>{
        private Context context;
        private List<EventModel> eventlist;

        EventListAdapter(Context context, List<EventModel> eventlist){
            this.context = context;
            this.eventlist = eventlist;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            EventCardviewBinding binding = EventCardviewBinding.inflate(LayoutInflater.from(context), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EventModel event = eventlist.get(position);

            holder.binding.titleArea.setText(event.getTitle());
            holder.binding.dateArea.setText(formatDateString(event.getStartDate()));
            holder.binding.amountArea.setText(event.getClubName());


            String imageUri = event.getImageUri();
            if (imageUri == null || imageUri.isEmpty()) {
                holder.binding.imageArea.setImageResource(R.drawable.image);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(imageUri)
                        .placeholder(R.drawable.image)
                        .error(R.drawable.image)
                        .into(holder.binding.imageArea);
            }

            holder.binding.viewButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("documentID", event.getDocumentID());

                EventDetail detail = new EventDetail();
                detail.setArguments(bundle);

                getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.full_screen_container, detail)
                            .addToBackStack(null)
                            .commit();

            });
        }

        @Override
        public int getItemCount() {
            return eventlist.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            EventCardviewBinding binding;

            public ViewHolder(EventCardviewBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        public String formatDateString(String s){
            try{
                String[] t = s.split(" ")[0].split("-");
                return t[0] + "년 " + t[1] + "월 " + t[2] + "일";
            }catch(Exception e){
                return s;
            }
        }
    }
}