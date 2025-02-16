package edu.northeastern.myapplication;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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

    private static final int TEXT_SIZE_SMALL_SP = 10;
    private static final int BTN_TEXT_SIZE_SMALL_SP = 6;
    private static final int LOGO_SMALL_DP = 24;
    private static final int BTN_WIDTH_SMALL_DP = 80;
    private static final int BTN_HEIGHT_SMALL_DP = 30;

    private static final int TEXT_SIZE_TITLE_BIG_SP = 16;
    private static final int TEXT_SIZE_SUBTITLE_BIG_SP = 14;
    private static final int LOGO_LARGE_DP = 48;
    private static final int BTN_MIN_HEIGHT_LARGE = 48;

    private static final int SMALL_MARGIN = 0;
    private static final int LARGE_MARGIN = 0;


    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
        CryptoCoin coin = coinList.get(position);

        if (coinList.size() >= 10) {
            holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL_SP);
            holder.tvSymbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL_SP);
            holder.tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL_SP);
            holder.editNoteInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL_SP);
            holder.btnAddNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, BTN_TEXT_SIZE_SMALL_SP);

            ViewGroup.LayoutParams logoParams = holder.imageViewLogo.getLayoutParams();
            logoParams.width = dpToPx(holder.itemView.getContext(), LOGO_SMALL_DP);
            logoParams.height = dpToPx(holder.itemView.getContext(), LOGO_SMALL_DP);
            holder.imageViewLogo.setLayoutParams(logoParams);

            ConstraintLayout.LayoutParams editTextParams = (ConstraintLayout.LayoutParams) holder.editNoteInput.getLayoutParams();
            editTextParams.topMargin = dpToPx(holder.itemView.getContext(), SMALL_MARGIN);
            holder.editNoteInput.setLayoutParams(editTextParams);

            ConstraintLayout.LayoutParams btnParams = (ConstraintLayout.LayoutParams) holder.btnAddNote.getLayoutParams();
            btnParams.width = dpToPx(holder.itemView.getContext(), BTN_WIDTH_SMALL_DP);
            btnParams.height = dpToPx(holder.itemView.getContext(), BTN_HEIGHT_SMALL_DP);
            btnParams.topMargin = dpToPx(holder.itemView.getContext(), SMALL_MARGIN);
            btnParams.bottomMargin = dpToPx(holder.itemView.getContext(), SMALL_MARGIN);
            holder.btnAddNote.setLayoutParams(btnParams);
            holder.btnAddNote.setLayoutParams(btnParams);

            ConstraintLayout.LayoutParams noteLayoutParams = (ConstraintLayout.LayoutParams) holder.layoutNotes.getLayoutParams();
            noteLayoutParams.topMargin = dpToPx(holder.itemView.getContext(), SMALL_MARGIN);
            noteLayoutParams.bottomMargin = dpToPx(holder.itemView.getContext(), SMALL_MARGIN);
            holder.layoutNotes.setLayoutParams(noteLayoutParams);

            ViewGroup.MarginLayoutParams itemParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            itemParams.topMargin = dpToPx(holder.itemView.getContext(), SMALL_MARGIN);
            itemParams.bottomMargin = dpToPx(holder.itemView.getContext(), SMALL_MARGIN);
            holder.itemView.setLayoutParams(itemParams);
        } else {    // can't remove for each viewholder will be reused
            holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_TITLE_BIG_SP);
            holder.tvSymbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SUBTITLE_BIG_SP);
            holder.tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SUBTITLE_BIG_SP);
            holder.editNoteInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SUBTITLE_BIG_SP);
            holder.btnAddNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SUBTITLE_BIG_SP);

            ViewGroup.LayoutParams logoParams = holder.imageViewLogo.getLayoutParams();
            logoParams.width = dpToPx(holder.itemView.getContext(), LOGO_LARGE_DP);
            logoParams.height = dpToPx(holder.itemView.getContext(), LOGO_LARGE_DP);
            holder.imageViewLogo.setLayoutParams(logoParams);

            holder.btnAddNote.setMinHeight(BTN_MIN_HEIGHT_LARGE);
            holder.btnAddNote.setMinimumHeight(BTN_MIN_HEIGHT_LARGE);

            ConstraintLayout.LayoutParams editTextParams = (ConstraintLayout.LayoutParams) holder.editNoteInput.getLayoutParams();
            editTextParams.topMargin = dpToPx(holder.itemView.getContext(), LARGE_MARGIN);
            holder.editNoteInput.setLayoutParams(editTextParams);

            ConstraintLayout.LayoutParams btnParams = (ConstraintLayout.LayoutParams) holder.btnAddNote.getLayoutParams();
            btnParams.topMargin = dpToPx(holder.itemView.getContext(), LARGE_MARGIN);
            btnParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            btnParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            holder.btnAddNote.setLayoutParams(btnParams);

            ConstraintLayout.LayoutParams noteLayoutParams = (ConstraintLayout.LayoutParams) holder.layoutNotes.getLayoutParams();
            noteLayoutParams.topMargin = dpToPx(holder.itemView.getContext(), LARGE_MARGIN);
            noteLayoutParams.bottomMargin = dpToPx(holder.itemView.getContext(), LARGE_MARGIN);
            holder.layoutNotes.setLayoutParams(noteLayoutParams);
        }

        holder.bind(coin);
    }
    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
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