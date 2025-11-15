package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.login.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // 회원가입 버튼 클릭 시
        binding.signupButton.setOnClickListener(v -> {
            String name = binding.name.getText().toString().trim();
            String number = binding.studentNum.getText().toString().trim();
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();
            String passwordCheck = binding.passwordCheck.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || number.isEmpty() ||  password.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordCheck)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            createUser(email, password);
        });

        // "로그인" 텍스트 클릭 시 로그인 화면으로 이동
        binding.signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Firebase 회원가입 처리
    private void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "회원가입 성공!\n" + email, Toast.LENGTH_SHORT).show();
                        Log.d("LSJ", "회원가입 성공: " + user.getUid());

                        createDB(binding.name.getText().toString().trim(),
                                binding.studentNum.getText().toString().trim(),
                                email,
                                password);

                        Intent intent = new Intent(SignUpActivity.this, SurveyActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "회원가입 실패: 이미 존재하는 계정일 수 있습니다.", Toast.LENGTH_LONG).show();
                        Log.w("LSJ", "회원가입 실패", task.getException());
                    }
                });
    }

    private void createDB(String name, String email, String number, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("number", number);
        user.put("email", email);
        user.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("LSJ", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("LSJ", "Error adding document", e);
                    }
                });
    }
}