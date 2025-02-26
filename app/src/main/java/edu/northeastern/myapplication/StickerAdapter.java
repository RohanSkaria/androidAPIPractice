package edu.northeastern.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder> {

    private List<Sticker> stickers;
    private OnStickerClickListener listener;

    public interface OnStickerClickListener {
        void onStickerClick(Sticker sticker);
    }

    public StickerAdapter(List<Sticker> stickers, OnStickerClickListener listener) {
        this.stickers = stickers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sticker, parent, false);
        return new StickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        Sticker sticker = stickers.get(position);
        holder.bind(sticker, listener);
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewSticker;
        private TextView textViewStickerName;
        private CardView cardViewSticker;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSticker = itemView.findViewById(R.id.imageViewSticker);
            textViewStickerName = itemView.findViewById(R.id.textViewStickerName);
            cardViewSticker = itemView.findViewById(R.id.cardViewSticker);
        }

        public void bind(final Sticker sticker, final OnStickerClickListener listener) {
            imageViewSticker.setImageResource(sticker.getResourceId());
            textViewStickerName.setText(sticker.getName());

            cardViewSticker.setOnClickListener(v -> {
                listener.onStickerClick(sticker);
            });
        }
    }
}