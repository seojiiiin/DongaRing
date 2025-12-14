package com.example.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.login.databinding.FragmentMyClubBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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

        if (getArguments() != null) {
            clubID = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (clubID == null) {
            Log.d("동아링", "clubID is null. Cannot load data.");
            return;
        }
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
                    Log.d("동아링", "Error getting documents.", e);
                });

        List<EventModel> myClubEvents = new ArrayList<>();
        MyClubAdapter myClubAdapter = new MyClubAdapter(myClubEvents, clubID, requireActivity().getSupportFragmentManager());
        binding.rvEvents.setAdapter(myClubAdapter);
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        db.collection("clubs")
                .document(clubID)
                .collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            String eventTitle = doc.getString("title");
                            String startDate = doc.getString("startDate");
                            int image = R.drawable.image;

                            EventModel event = new EventModel(doc.getId(), eventTitle, "동아리 이름", startDate, image);
                            myClubEvents.add(event);

                            Log.d("동아링", "Event loaded: " + eventTitle);
                        }

                        myClubAdapter.notifyDataSetChanged();

                    } else Log.d("동아링", "No events found");
                })
                .addOnFailureListener(e -> Log.d("동아링", "Failed to load events.", e));

    }

    class EventModel {
        private final String docID;
        private final String title;
        private final String clubName;

        private final String  date;

        private final int image;

        public EventModel(String docID, String title, String clubName, String date, int image) {
            this.docID = docID;
            this.title = title;
            this.clubName = clubName;
            this.image = image;
            this.date = date;
        }

        public String getDocID() { return docID; }

        public String getTitle() {
            return title;
        }

        public String getClubName() {
            return clubName;
        }

        public int getImage() {
            return image;
        }

        public String getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "CardModel{" + "title='" + title + '\'' + ", clubName='" + clubName + '\'' + ", date=" + date + '\'' + ", image=" + image + '}';
        }
    }

    class MyClubAdapter extends RecyclerView.Adapter<MyClubAdapter.MyClubViewHolder> {

        private final String clubId;
        private final List<EventModel> eventList;
        private final FragmentManager fragmentManager;

        public MyClubAdapter(List<EventModel> eventList, String clubId, FragmentManager fragmentManager) {
            this.clubId = clubId;
            this.eventList = eventList;
            this.fragmentManager = fragmentManager;
        }

        @NonNull
        @Override
        public MyClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_cardview, parent, false);
           return new MyClubViewHolder(view, clubId, fragmentManager);
        }

        @Override
        public void onBindViewHolder(@NonNull MyClubViewHolder holder, int position) {
            EventModel event = eventList.get(position);
            holder.bind(event);
        }

        @Override
        public int getItemCount() { return eventList.size(); }

        static class MyClubViewHolder extends RecyclerView.ViewHolder {
            private final String clubId;
            private final TextView eventTitle;
            private final TextView clubName;
            private final TextView startDate;
            private final ImageView imageArea;
            private final FragmentManager fragmentManager;



            public MyClubViewHolder(@NonNull View itemView, String clubId, FragmentManager fragmentManager) {
                super(itemView);
                this.clubId = clubId;
                this.fragmentManager = fragmentManager;

                eventTitle = itemView.findViewById(R.id.titleArea);
                startDate = itemView.findViewById(R.id.dateArea);
                clubName = itemView.findViewById(R.id.amountArea);
                imageArea = itemView.findViewById(R.id.imageArea);
            }

            public void bind(EventModel event){
                eventTitle.setText(event.getTitle());
                clubName.setText(event.getClubName());
                imageArea.setImageResource(event.getImage());
                startDate.setText(event.getDate());

                Button viewButton = itemView.findViewById(R.id.viewButton);
                viewButton.setOnClickListener(v -> {
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.full_screen_container, MyClubDetailFragment.newInstance(clubId, event.getDocID()))
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

    }
}