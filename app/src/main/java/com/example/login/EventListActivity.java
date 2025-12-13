package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Printer;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.login.databinding.ActivityEventListBinding;
import com.example.login.databinding.EventCardviewBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventListActivity extends AppCompatActivity {
    ActivityEventListBinding binding;
    ArrayList<EventModel> events;
    EventListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEventListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        events = new ArrayList<>();
        adapter = new EventListAdapter(this, events);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.addButton.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new AddEventFragment())
                    .addToBackStack(null)
                    .commit();
        });

        getSupportFragmentManager().setFragmentResultListener(
                "event_added",
                this,
                (requestKey, result) -> loadEvent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvent();
    }

    public void refreshList(){
        loadEvent();
    }

    /// firestore의 clubs 컬렉션에서 동아리 문서에 접근해 'club name' 가져오고,
    /// 해당 club name의 하위 컬렉션인 events에서 문서들 불러오게 코드변경해야함!!
    private void loadEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // 로그인이 안 되어 있다면 리턴
        if (user == null) {
            Log.e("LSJ", "User not logged in");
            return;
        }

        // 1. users_admin 컬렉션에서 현재 로그인한 유저의 문서 접근
        db.collection("users_admin").document(user.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        // 관리자로 속해 있는 동아리 ID 가져오기
                        String clubId = userDoc.getString("clubAdminOf");

                        if (clubId != null && !clubId.isEmpty()) {
                            // 2. 해당 동아리(club) 문서 접근 (이름 가져오기 위함 + 하위 컬렉션 접근)
                            db.collection("clubs").document(clubId)
                                    .get()
                                    .addOnSuccessListener(clubDoc -> {
                                        if (clubDoc.exists()) {
                                            String clubName = clubDoc.getString("club name");

                                            // 3. 해당 동아리의 하위 컬렉션 'events'에서 이벤트 목록 가져오기
                                            clubDoc.getReference().collection("events")
                                                    .get()
                                                    .addOnSuccessListener(eventSnapshots -> {
                                                        events.clear(); // 기존 리스트 초기화

                                                        for (var eventDoc : eventSnapshots.getDocuments()) {
                                                            EventModel model = new EventModel(
                                                                    // 이벤트 문서 내에 club name이 있으면 쓰고, 없으면 상위 문서의 이름을 사용
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
                                                        adapter.notifyDataSetChanged();
                                                    })
                                                    .addOnFailureListener(e -> Log.e("LSJ", "Failed to load sub events", e));
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("LSJ", "Failed to load club info", e));
                        } else {
                            Log.d("LSJ", "This user is not an admin of any club.");
                            events.clear();
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("LSJ", "User admin info not found.");
                    }
                })
                .addOnFailureListener(e -> Log.e("LSJ", "Failed to load user admin doc", e));
    }
}

class EventModel {
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

class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder>{
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
        if (imageUri != null){
            Uri uri = Uri.parse(imageUri);
            holder.binding.imageArea.setImageURI(uri);
            Log.d("LSJ", "Image changed");
        } else holder.binding.imageArea.setImageResource(R.drawable.image);

        holder.binding.viewButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("documentID", event.getDocumentID());

            EventDetail detail = new EventDetail();
            detail.setArguments(bundle);

            ((AppCompatActivity) context)
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, detail)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return eventlist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EventCardviewBinding binding;

        public ViewHolder(EventCardviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static String formatDateString(String s){
        try{
            String[] t = s.split(" ")[0].split("-");
            return t[0] + "년 " + t[1] + "월 " + t[2] + "일";
        }catch(Exception e){
            return s;
        }
    }

}