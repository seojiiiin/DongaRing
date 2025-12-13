package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.login.databinding.ActivityAdminMyPageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AdminMyPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityAdminMyPageBinding binding = ActivityAdminMyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        if(savedInstanceState==null){
            bottomNavigationView.setSelectedItemId(R.id.nav_mypage);
            transferTo(AdminMyPageFragment.newInstance("", ""));
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if(itemId == R.id.nav_calendar){
                    transferTo(AdminCalendarFragment.newInstance("", ""));
                    return true;
                }
                if(itemId == R.id.member_management){
                    transferTo(MemberManageFragment.newInstance("", ""));
                    return true;
                }
                if(itemId == R.id.club_management){
                    startActivity(new Intent(AdminMyPageActivity.this, EventListActivity.class));
                    return true;
                }
                if(itemId == R.id.nav_mypage){
                    transferTo(AdminMyPageFragment.newInstance("", ""));
                    return true;
                }return false;
            }
        });
        bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {

            }
        });

    }
    private void transferTo(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}