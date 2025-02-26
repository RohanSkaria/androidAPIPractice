package edu.northeastern.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<StickerMessage> messages;
    private Map<String, Integer> stickerResources;
    private Context context;
    private SimpleDateFormat dateFormat;

    public HistoryAdapter(List<StickerMessage> messages, Map<String, Integer> stickerResources, Context context) {
        this.messages = messages;
        this.stickerResources = stickerResources;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        StickerMessage message = messages.get(position);

        // Set sender name
        holder.textViewSender.setText("From: " + message.getSender());

        // Set timestamp
        String formattedDate = dateFormat.format(new Date(message.getTimestamp()));
        holder.textViewTimestamp.setText(formattedDate);

        // Set sticker image
        Integer resourceId = stickerResources.get(message.getStickerId());
        if (resourceId != null) {
            holder.imageViewHistorySticker.setImageResource(resourceId);
            holder.textViewUnknownSticker.setVisibility(View.GONE);
        } else {
            // Unknown sticker ID
            holder.imageViewHistorySticker.setImageResource(R.drawable.sticker_unknown);
            holder.textViewUnknownSticker.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewHistorySticker;
        private TextView textViewSender;
        private TextView textViewTimestamp;
        private TextView textViewUnknownSticker;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewHistorySticker = itemView.findViewById(R.id.imageViewHistorySticker);
            textViewSender = itemView.findViewById(R.id.textViewSender);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewUnknownSticker = itemView.findViewById(R.id.textViewUnknownSticker);
        }
    }
}