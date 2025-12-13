package com.example.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatRoomFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String clubId;
    private String chatId;

    private String myUid;
    private String adminUid; // clubAdminOf로 찾음

    private RecyclerView rv;
    private EditText etMessage;
    private ImageButton btnSend;

    private final ArrayList<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        myUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        clubId = getArguments() != null ? getArguments().getString("clubId") : null;

        rv = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter(messages, myUid);
        rv.setAdapter(adapter);

        if (myUid == null || TextUtils.isEmpty(clubId)) return;

        findAdminThenEnterRoom();

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty() || chatId == null) return;
            sendMessage(text);
        });
    }

    private void findAdminThenEnterRoom() {
        db.collection("users_admin")
                .whereEqualTo("clubAdminOf", clubId)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Toast.makeText(getContext(), "관리자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    adminUid = qs.getDocuments().get(0).getId(); // ✅ users_admin 문서ID = admin uid

                    chatId = clubId + "_" + myUid; // ✅ 동아리별 + 유저별 1:1 방
                    ensureChatDocThenListen();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "관리자 조회 실패", Toast.LENGTH_SHORT).show()
                );
    }

    private void ensureChatDocThenListen() {
        DocumentReference chatRef = db.collection("chats").document(chatId);

        chatRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Map<String, Object> chat = new HashMap<>();
                chat.put("clubId", clubId);
                chat.put("userId", myUid);
                chat.put("adminId", adminUid);
                chat.put("participants", Arrays.asList(myUid, adminUid));
                chat.put("lastMessage", "");
                chat.put("updatedAt", FieldValue.serverTimestamp());
                chatRef.set(chat);
            }
            listenMessages();
        });
    }

    private void listenMessages() {
        if (registration != null) registration.remove();

        registration = db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    messages.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Message m = d.toObject(Message.class);
                        if (m != null) messages.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) rv.scrollToPosition(messages.size() - 1);
                });
    }

    private void sendMessage(String text) {
        DocumentReference chatRef = db.collection("chats").document(chatId);

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", myUid); // ✅ 현재 로그인한 사람이 보내는 주체(유저/관리자 공통)
        msg.put("text", text);
        msg.put("createdAt", FieldValue.serverTimestamp());

        chatRef.collection("messages").add(msg).addOnSuccessListener(r -> {
            Map<String, Object> update = new HashMap<>();
            update.put("lastMessage", text);
            update.put("updatedAt", FieldValue.serverTimestamp());
            chatRef.set(update, SetOptions.merge());
            etMessage.setText("");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null) registration.remove();
    }

    public static class Message {
        public String senderId;
        public String text;
        public Timestamp createdAt;
        public Message() {}
    }
}
