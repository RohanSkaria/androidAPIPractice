package edu.northeastern.myapplication;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CryptoCoinAdapter extends RecyclerView.Adapter<CryptoCoinAdapter.CoinViewHolder> {

    private List<CryptoCoin> coinList;

    public CryptoCoinAdapter(List<CryptoCoin> coinList) {
        this.coinList = coinList;
    }

    @NonNull
    @Override
    public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coin, parent, false);
        return new CoinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
        CryptoCoin coin = coinList.get(position);
        holder.bind(coin);
    }

    @Override
    public int getItemCount() {
        return coinList.size();
    }

    // Optionally allow updating the list at runtime
    public void updateList(List<CryptoCoin> newList) {
        this.coinList = newList;
        notifyDataSetChanged();
    }

    // ----------------------------------------------------
    // ViewHolder
    // ----------------------------------------------------
    class CoinViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewLogo;
        private TextView tvName, tvSymbol, tvPrice;
        private EditText editNoteInput;
        private Button btnAddNote;
        private LinearLayout layoutNotes;

        public CoinViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewLogo = itemView.findViewById(R.id.imageViewLogo);
            tvName = itemView.findViewById(R.id.tvCoinName);
            tvSymbol = itemView.findViewById(R.id.tvCoinSymbol);
            tvPrice = itemView.findViewById(R.id.tvCoinPrice);

            editNoteInput = itemView.findViewById(R.id.editNoteInput);
            btnAddNote = itemView.findViewById(R.id.btnAddNote);
            layoutNotes = itemView.findViewById(R.id.layoutNotes);
        }

        public void bind(CryptoCoin coin) {
            // Basic coin data
            tvName.setText(coin.getName());
            tvSymbol.setText("Symbol: " + coin.getSymbol());
            tvPrice.setText(String.format("Price: $%.2f", coin.getPriceUsd()));

            // Load logo (optional custom code or ImageLoader)
            imageViewLogo.setImageResource(R.drawable.ic_launcher_foreground);
            String logoUrl = coin.getLogoUrl();
            if (!TextUtils.isEmpty(logoUrl)) {
                ImageLoader.loadImageAsync(logoUrl, imageViewLogo);
            }

            // Clear existing dynamic note views
            layoutNotes.removeAllViews();

            // Re-build the note TextViews
            for (String note : coin.getNotes()) {
                TextView tvNote = new TextView(itemView.getContext());
                tvNote.setText(note);
                layoutNotes.addView(tvNote);
            }

            // When user taps "Add Note":
            btnAddNote.setOnClickListener(v -> {
                String typedText = editNoteInput.getText().toString().trim();
                if (!typedText.isEmpty()) {
                    // 1) Create a timestamp e.g. "[12:34:56]"
                    String timeStamp = getCurrentTimeString();
                    // 2) Combine the timestamp + typed note
                    String noteWithTime = "[" + timeStamp + "] " + typedText;

                    // 3) Add to the coin's notes
                    coin.getNotes().add(noteWithTime);

                    // 4) Clear the input field
                    editNoteInput.setText("");

                    // 5) Re-bind to update the UI (so the new note appears)
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }

        // Helper method to get "HH:mm:ss" for current time
        private String getCurrentTimeString() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date());
        }
    }
}
