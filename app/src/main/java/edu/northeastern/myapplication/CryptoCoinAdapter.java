package edu.northeastern.myapplication;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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

        public CoinViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewLogo = itemView.findViewById(R.id.imageViewLogo);
            tvName = itemView.findViewById(R.id.tvCoinName);
            tvSymbol = itemView.findViewById(R.id.tvCoinSymbol);
            tvPrice = itemView.findViewById(R.id.tvCoinPrice);
        }

        public void bind(CryptoCoin coin) {
            tvName.setText(coin.getName());
            tvSymbol.setText("Symbol: " + coin.getSymbol());
            tvPrice.setText(String.format("Price: $%.2f", coin.getPriceUsd()));

            // Load logo image asynchronously
            imageViewLogo.setImageResource(R.drawable.ic_launcher_foreground); // fallback or placeholder
            String logoUrl = coin.getLogoUrl();
            if (logoUrl != null && !logoUrl.isEmpty()) {
                ImageLoader.loadImageAsync(logoUrl, imageViewLogo);
            }
        }
    }
}