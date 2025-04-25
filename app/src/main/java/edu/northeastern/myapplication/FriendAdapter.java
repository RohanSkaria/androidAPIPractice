package edu.northeastern.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<String> friends;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(String friend);
    }

    public FriendAdapter(List<String> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        String friend = friends.get(position);
        holder.bind(friend, listener);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewFriendName;
        private CardView cardViewFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFriendName = itemView.findViewById(R.id.textViewFriendName);
            cardViewFriend = itemView.findViewById(R.id.cardViewFriend);
        }

        public void bind(final String friend, final OnFriendClickListener listener) {
            textViewFriendName.setText(friend);

            cardViewFriend.setOnClickListener(v -> {
                listener.onFriendClick(friend);
            });
        }
    }
}