package com.example.login;

import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    private final List<ChatRoomFragment.Message> list;
    private final String myUid;

    public MessageAdapter(List<ChatRoomFragment.Message> list, String myUid) {
        this.list = list;
        this.myUid = myUid;
    }

    @Override
    public int getItemViewType(int position) {
        ChatRoomFragment.Message m = list.get(position);
        if (m != null && m.senderId != null && m.senderId.equals(myUid)) return TYPE_RIGHT;
        return TYPE_LEFT;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_RIGHT) ? R.layout.item_message_right : R.layout.item_message_left;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tvText.setText(list.get(position).text);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvText;
        VH(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
        }
    }
}

