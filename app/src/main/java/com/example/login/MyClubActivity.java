package com.example.login;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.login.databinding.ActivityMyClubBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyClubActivity extends AppCompatActivity {
    ActivityMyClubBinding binding;
    FirebaseFirestore db;
    String clubID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMyClubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clubID =  getIntent().getStringExtra("clubID");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if(savedInstanceState==null){
            bottomNavigationView.setSelectedItemId(R.id.nav_myEvent);
            transferTo(MyClubFragment.newInstance(clubID, ""));
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_calendar) {
                transferTo(CalendarFragment.newInstance());
                return true;
            }
            if (itemId == R.id.nav_joinRecord) {
                transferTo(JoinRecordFragment.newInstance(clubID, ""));
                return true;
            }
            if (itemId == R.id.nav_myEvent) {
                transferTo(MyClubFragment.newInstance(clubID, ""));
                return true;
            }
            return false;
        });
    }

    private void transferTo(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}