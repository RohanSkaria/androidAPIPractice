package edu.northeastern.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.StatsViewHolder> {

    // This map is { stickerId -> numberOfTimesSentByUser }
    private final Map<String, Long> statsMap;
    // A list of all known sticker objects (id, name, resource)
    private final List<Sticker> availableStickers;

    // We'll iterate over the keys of statsMap to populate items
    private final List<String> stickerIds;

    public StatsAdapter(Map<String, Long> statsMap, List<Sticker> availableStickers) {
        this.statsMap = statsMap;
        this.availableStickers = availableStickers;
        this.stickerIds = new ArrayList<>(statsMap.keySet());
    }

    @NonNull
    @Override
    public StatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stats, parent, false);
        return new StatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
        String stickerId = stickerIds.get(position);
        Long count = statsMap.get(stickerId);

        // Default to unknown if we can't find a match
        int resId = R.drawable.sticker_unknown;
        String stickerName = "Unknown Sticker";

        // See if we can match a known sticker
        for (Sticker s : availableStickers) {
            if (s.getId().equals(stickerId)) {
                resId = s.getResourceId();
                stickerName = s.getName();
                break;
            }
        }

        holder.bind(stickerName, resId, count);
    }

    @Override
    public int getItemCount() {
        return stickerIds.size();
    }

    static class StatsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView countView;

        public StatsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.stickerImage);
            nameView = itemView.findViewById(R.id.stickerName);
            countView = itemView.findViewById(R.id.stickerCount);
        }

        void bind(String stickerName, int stickerResId, Long count) {
            imageView.setImageResource(stickerResId);
            nameView.setText(stickerName);
            // e.g. "Sent 5 times"
            countView.setText("Sent " + count + " times");
        }
    }
}
