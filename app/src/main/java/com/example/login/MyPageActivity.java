package com.example.login;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.databinding.ActivityMyPageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyPageActivity extends AppCompatActivity {
    ActivityMyPageBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    private String[] joinedClubs = new String[]{};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String[] clubsFromIntent = getIntent().getStringArrayExtra("joinedClub");
        if(clubsFromIntent != null) joinedClubs = clubsFromIntent;
        else joinedClubs = new String[]{};


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if(savedInstanceState==null){
            bottomNavigationView.setSelectedItemId(R.id.nav_mypage);
            transferTo(MyPageFragment.newInstance(joinedClubs, ""));
        }


        //네비게이션바에 리스너 부착
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if(itemId == R.id.nav_calendar){
                    transferTo(CalendarFragment.newInstance());
                    return true;
                }
                if(itemId == R.id.nav_club_list){
                    transferTo(ClubListFragment.newInstance("", ""));
                    return true;
                }
                if(itemId == R.id.nav_favorites){
                    transferTo(FavoritesFragment.newInstance("", ""));
                    return true;
                }
                if(itemId == R.id.nav_mypage){
                    transferTo(MyPageFragment.newInstance(joinedClubs, ""));
                    return true;
                }
                return false;
            }
        });

        //현재 페이지 재선택시 아무것도 안함
        bottomNavigationView.setOnItemReselectedListener(menuItem -> {

        });
    }
    private void transferTo(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}