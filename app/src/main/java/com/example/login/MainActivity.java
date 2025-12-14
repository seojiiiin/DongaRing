package com.example.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.login.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 일반계정 로그인 버튼 클릭 시 로그인 시도
        binding.normalLogin.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            signInUser(email, password);
        });

        // 관리자계정 로그인 버튼 클릭 시 로그인 시도
        binding.adminLogin.setOnClickListener(v->{
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            signInAdmin(email, password);
        });
        
        // 회원가입 텍스트 클릭 시 회원가입 화면으로 이동
        binding.signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void signInAdmin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "로그인 실패: 계정이 없거나 비밀번호가 틀렸습니다.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 로그인 성공 → 사용자 정보 확인
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "로그인 실패: 사용자 정보 없음", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    // Firestore에서 관리자 권한 확인
                    db.collection("users_admin")
                            .whereEqualTo("uid", uid)
                            .get()
                            .addOnSuccessListener(doc -> {

                                if (doc.isEmpty()) {
                                    // FirebaseAuth 로그인은 성공했지만 관리자 아님
                                    Toast.makeText(this,
                                            "관리자 권한이 없습니다.",
                                            Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();   // 권한 없는 로그인은 로그아웃
                                    return;
                                }

                                // 관리자 확인 완료 → 관리자 화면 이동
                                Log.d("동아링", "Admin login success");
                                startActivity(new Intent(MainActivity.this, AdminMyPageActivity.class));
                            })
                            .addOnFailureListener(e -> {
                                Log.d("동아링", "Error checking admin status", e);
                            });
                });
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        // Authentication 로그인 실패
                        Log.d("동아링", "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this,
                                "로그인 실패: 존재하지 않는 계정이거나 비밀번호가 틀렸습니다.",
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                        return;
                    }

                    // Authentication 로그인 성공
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(MainActivity.this, "로그인 오류: 사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = user.getUid();

                    // Firestore에서 uid로 사용자 문서 찾기
                    db.collection("users")
                            .whereEqualTo("uid", uid)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(query -> {

                                if (query.isEmpty()) {
                                    // 문서 없음 → 로그인 실패 처리
                                    Toast.makeText(MainActivity.this,
                                            "로그인 실패: 사용자 정보가 존재하지 않습니다.",
                                            Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    updateUI(null);
                                    return;
                                }

                                // 문서 존재 → 데이터 읽기
                                DocumentSnapshot doc = query.getDocuments().get(0);

                                List<String> clubsList = (List<String>) doc.get("joinedClubs");

                                // 가입한 동아리가 없는 경우
                                if (clubsList == null) clubsList = new ArrayList<>();

                                String[] joinedClubs = clubsList.toArray(new String[0]);

                                // 정상 로그인 → MypageActivity 이동
                                Intent intent = new Intent(MainActivity.this, MyPageActivity.class)
                                        .putExtra("joinedClub", joinedClubs);
                                startActivity(intent);

                                updateUI(user);
                            })
                            .addOnFailureListener(e -> {
                                // Firestore 요청 실패 → 로그인 실패 처리
                                Log.d("동아링", "Error getting user document", e);
                                Toast.makeText(MainActivity.this,
                                        "서버 오류로 사용자 정보를 불러올 수 없습니다.",
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                updateUI(null);
                            });
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String info = "로그인 성공!";
            Toast.makeText(this, info, Toast.LENGTH_LONG).show();
            Log.d("동아링", info);
        } else {
            Log.d("동아링", "updateUI : user is null");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) currentUser.reload();
    }
}