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
import com.google.firebase.firestore.DocumentSnapshot;
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
        EdgeToEdge.enable(this);

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

            checkStudentNum(name, number, email, password);
        });

        // "로그인" 텍스트 클릭 시 로그인 화면으로 이동
        binding.signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Firebase 회원가입 처리
    private void createUser(String name,  String number, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Log.w("LSJ", "user is null after signup");
                            return;
                        }

                        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                        Log.d("LSJ", "회원가입 성공: " + user.getUid());
                        createDB(name, number, email);

                    } else {
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                        Log.w("LSJ", "회원가입 실패", task.getException());
                    }
                });
    }

    private void checkStudentNum(String name, String number, String email, String password) {
        db.collection("users")
                .whereEqualTo("studentNumber", number)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Log.d("LSJ", "student number exists");
                        Toast.makeText(this, "이미 존재하는 학번입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createUser(name, number, email, password);
                })
                .addOnFailureListener(e -> {
                    Log.w("LSJ", "Error getting documents.", e)
;                   Toast.makeText(this, "오류 발생", Toast.LENGTH_SHORT).show();
                });

    }

    private void createDB(String name, String number, String email) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.w("LSJ", "currentUser is null");
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("studentNumber", number);
        user.put("email", email);
        user.put("uid", currentUser.getUid());

        db.collection("users")
                .document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(documentReference -> {

                    // 동기화 호출
                    updateJoinedClubs(currentUser.getUid(), number);

                    Log.d("LSJ", "Document added with UID");

                    //다음 화면 이동
                    Intent intent = new Intent(SignUpActivity.this, SurveyActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Log.w("LSJ", "Error adding document", e));
    }

    private void updateJoinedClubs(String uid, String studentNumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("clubs")
                .get()
                .addOnSuccessListener(clubQuery -> {

                    for (DocumentSnapshot clubDoc : clubQuery) {

                        String clubId = clubDoc.getId();

                        db.collection("clubs")
                                .document(clubId)
                                .collection("members")
                                .whereEqualTo("studentNumber", studentNumber)
                                .get()
                                .addOnSuccessListener(memberQuery -> {

                                    if (!memberQuery.isEmpty()) {

                                        db.collection("users").document(uid)
                                                .update("joinedClubs",
                                                        com.google.firebase.firestore.FieldValue.arrayUnion(clubId))
                                                .addOnSuccessListener(a -> {
                                                    Log.d("SYNC", "User joinedClubs updated: " + clubId);
                                                });
                                    }

                                });
                    }
                });
    }

}