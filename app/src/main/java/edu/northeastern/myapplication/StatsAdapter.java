package edu.northeastern.myapplication;

import android.annotation.SuppressLint;
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


    private final Map<String, Long> statsMap;

    private final List<Sticker> availableStickers;


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

        int resId = R.drawable.sticker_unknown;
        String stickerName = "Unknown Sticker";


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

        @SuppressLint("SetTextI18n")
        void bind(String stickerName, int stickerResId, Long count) {
            imageView.setImageResource(stickerResId);
            nameView.setText(stickerName);

            if(count == 1){
                countView.setText("Sent " + count + " time");
            } else {
                countView.setText("Sent " + count + " times");
            }
        }
    }
}
