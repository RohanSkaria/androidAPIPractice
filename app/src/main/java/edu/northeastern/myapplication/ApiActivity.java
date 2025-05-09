package edu.northeastern.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ApiActivity extends AppCompatActivity {

    private Spinner spinnerApiSource;
    private TextView tvLoading;
    private Button btnFetch;
    private RecyclerView recyclerView;
    private CryptoCoinAdapter adapter;
    private ArrayList<CryptoCoin> coinList = new ArrayList<>();

    private Handler loadingHandler = new Handler();
    private boolean isLoadingAnimationActive = false;
    private int loadingDotCount = 0;

    private static final String KEY_COIN_LIST = "KEY_COIN_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        spinnerApiSource = findViewById(R.id.spinnerApiSource);
        tvLoading = findViewById(R.id.tvLoading);
        btnFetch = findViewById(R.id.btnFetch);
        recyclerView = findViewById(R.id.recyclerViewCoins);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"CoinGecko: Bitcoin only", "CoinPaprika: Top 10", "CoinCap: Worst 3"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApiSource.setAdapter(spinnerAdapter);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CryptoCoinAdapter(coinList);
        recyclerView.setAdapter(adapter);


        if (savedInstanceState != null) {

            String savedJson = savedInstanceState.getString(KEY_COIN_LIST, null);
            if (savedJson != null) {
                coinList.clear();
                coinList.addAll(parseCoinListFromJson(savedJson));
                adapter.notifyDataSetChanged();
            }
        }


        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = spinnerApiSource.getSelectedItemPosition();
                switch (position) {
                    case 0:
                        fetchCoinGeckoBitcoin();
                        break;
                    case 1:

                        fetchCoinPaprikaTop10();
                        break;
                    case 2:
                        fetchCoinCapWorst3();
                        break;
                }
            }
        });
    }

    private void startLoadingAnimation() {
        tvLoading.setVisibility(View.VISIBLE);
        isLoadingAnimationActive = true;
        loadingDotCount = 0;
        loadingHandler.post(loadingRunnable);
    }

    private void stopLoadingAnimation() {
        isLoadingAnimationActive = false;
        tvLoading.setVisibility(View.GONE);
    }


    private Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isLoadingAnimationActive) return;

            String baseText = "Loading";
            StringBuilder sb = new StringBuilder(baseText);
            for (int i = 0; i < loadingDotCount; i++) {
                sb.append(".");
            }
            tvLoading.setText(sb.toString());

            loadingDotCount = (loadingDotCount + 1) % 4;

            loadingHandler.postDelayed(this, 500);
        }
    };


    private void fetchCoinGeckoBitcoin() {
        startLoadingAnimation();
        coinList.clear();
        adapter.notifyDataSetChanged();

        final String url = "https://api.coingecko.com/api/v3/coins/bitcoin"
                + "?localization=false&tickers=false&market_data=true&community_data=false"
                + "&developer_data=false&sparkline=false";

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = doHttpGet(url);
                if (response == null) {
                    showError("Network error or empty response (CoinGecko)");
                    stopLoadingOnUI();
                    return;
                }

                try {
                    JSONObject root = new JSONObject(response);

                    String name = root.optString("name", "Bitcoin");
                    String symbol = root.optString("symbol", "BTC").toUpperCase();

                    JSONObject marketData = root.getJSONObject("market_data");
                    JSONObject currentPrice = marketData.getJSONObject("current_price");
                    double priceUsd = currentPrice.getDouble("usd");

                    String logoUrl = "https://assets.coincap.io/assets/icons/"
                            + symbol.toLowerCase() + "@2x.png";

                    CryptoCoin btcCoin = new CryptoCoin(name, symbol, priceUsd, logoUrl);
                    coinList.add(btcCoin);
                    updateRecycler();

                } catch (JSONException e) {
                    showError("JSON parse error (CoinGecko): " + e.getMessage());
                }
                stopLoadingOnUI();
            }
        }).start();
    }

    private void fetchCoinPaprikaTop10() {
        startLoadingAnimation();
        coinList.clear();
        adapter.notifyDataSetChanged();

        final String url = "https://api.coinpaprika.com/v1/tickers";

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = doHttpGet(url);
                if (response == null) {
                    showError("Network error or empty response (CoinPaprika)");
                    stopLoadingOnUI();
                    return;
                }

                try {
                    JSONArray arr = new JSONArray(response);

                    int limit = Math.min(arr.length(), 10);
                    for (int i = 0; i < limit; i++) {
                        JSONObject coinObj = arr.getJSONObject(i);

                        String name = coinObj.getString("name");
                        String symbol = coinObj.getString("symbol");

                        JSONObject quotes = coinObj.getJSONObject("quotes");
                        JSONObject usdObj = quotes.getJSONObject("USD");
                        double priceUsd = usdObj.getDouble("price");

                        String logoUrl = "https://assets.coincap.io/assets/icons/"
                                + symbol.toLowerCase() + "@2x.png";

                        CryptoCoin coin = new CryptoCoin(name, symbol, priceUsd, logoUrl);
                        coinList.add(coin);
                    }
                    updateRecycler();

                } catch (JSONException e) {
                    showError("JSON parse error (CoinPaprika): " + e.getMessage());
                }
                stopLoadingOnUI();
            }
        }).start();
    }

    private void fetchCoinCapWorst3() {
        startLoadingAnimation();
        coinList.clear();
        adapter.notifyDataSetChanged();

        final String url = "https://api.coincap.io/v2/assets?limit=200";

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = doHttpGet(url);
                if (response == null) {
                    showError("Network error or empty response (CoinCap)");
                    stopLoadingOnUI();
                    return;
                }

                try {
                    JSONObject rootObj = new JSONObject(response);
                    JSONArray dataArr = rootObj.getJSONArray("data");

                    ArrayList<JSONObject> rawList = new ArrayList<>();

                    for (int i = 0; i < dataArr.length(); i++) {
                        rawList.add(dataArr.getJSONObject(i));
                    }


                    Collections.sort(rawList, new Comparator<JSONObject>() {
                        @Override
                        public int compare(JSONObject o1, JSONObject o2) {
                            double v1 = 0.0;
                            double v2 = 0.0;
                            try {
                                v1 = Double.parseDouble(o1.optString("volumeUsd24Hr", "0"));
                                v2 = Double.parseDouble(o2.optString("volumeUsd24Hr", "0"));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            return Double.compare(v1, v2);
                        }
                    });


                    int limit = Math.min(3, rawList.size());
                    for (int i = 0; i < limit; i++) {
                        JSONObject coinObj = rawList.get(i);

                        String name = coinObj.getString("name");
                        String symbol = coinObj.getString("symbol");
                        double priceUsd = Double.parseDouble(coinObj.optString("priceUsd", "0"));

                        String logoUrl = "https://assets.coincap.io/assets/icons/"
                                + symbol.toLowerCase() + "@2x.png";

                        CryptoCoin coin = new CryptoCoin(name, symbol, priceUsd, logoUrl);
                        coinList.add(coin);
                    }

                    updateRecycler();

                } catch (JSONException e) {
                    showError("JSON parse error (CoinCap): " + e.getMessage());
                }
                stopLoadingOnUI();
            }
        }).start();
    }

    private String doHttpGet(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private void updateRecycler() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                adjustRecyclerViewHeight();
            }
        });
    }

    private static final int COIN_COUNT_THRESHOLD = 10;
    private static final int RECYCLER_FIXED_HEIGHT_DP = 750;
    private void adjustRecyclerViewHeight() {
        ConstraintLayout constraintLayout = findViewById(R.id.apiLayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        if (coinList.size() >= COIN_COUNT_THRESHOLD) {

            constraintSet.constrainHeight(R.id.recyclerViewCoins, ConstraintSet.MATCH_CONSTRAINT);
        }
        constraintSet.applyTo(constraintLayout);
    }

    private void stopLoadingOnUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoadingAnimation();
            }
        });
    }

    private void showError(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(recyclerView, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String json = convertCoinListToJson(coinList);
        outState.putString(KEY_COIN_LIST, json);
    }


    private String convertCoinListToJson(ArrayList<CryptoCoin> list) {
        JSONArray arr = new JSONArray();
        for (CryptoCoin coin : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", coin.getName());
                obj.put("symbol", coin.getSymbol());
                obj.put("priceUsd", coin.getPriceUsd());
                obj.put("logoUrl", coin.getLogoUrl());

                JSONArray notesArr = new JSONArray();
                for (String note : coin.getNotes()) {
                    notesArr.put(note);
                }
                obj.put("notes", notesArr);

                arr.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arr.toString();
    }


    private ArrayList<CryptoCoin> parseCoinListFromJson(String json) {
        ArrayList<CryptoCoin> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String name = obj.optString("name");
                String symbol = obj.optString("symbol");
                double priceUsd = obj.optDouble("priceUsd", 0.0);
                String logoUrl = obj.optString("logoUrl");

                CryptoCoin coin = new CryptoCoin(name, symbol, priceUsd, logoUrl);

                JSONArray notesArr = obj.optJSONArray("notes");
                if (notesArr != null) {
                    for (int j = 0; j < notesArr.length(); j++) {
                        coin.getNotes().add(notesArr.getString(j));
                    }
                }
                list.add(coin);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}