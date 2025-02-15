package edu.northeastern.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.google.android.material.snackbar.Snackbar;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
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

/**
 * Demonstration:
 *  - (Spinner Option 1)  fetchCoinGeckoBitcoin() [unchanged, single call for BTC]
 *  - (Spinner Option 2)  fetchCoinPaprikaTop5()  fetch top 5 from CoinPaprika, then for each coin, call CoinGecko for logo
 *  - (Spinner Option 3)  fetchCoinCapTop5()      fetch top 5 from CoinCap, then for each coin, call CoinGecko for logo
 *
 *  We do a second, smaller "single-coin" call to CoinGecko for each coin symbol, using a
 *  basic map from symbol -> coingeckoId -> coinGeckoLogoURL.
 */




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
    private LinearLayout dynamicInputContainer;
    private Button btnAddInput;
    private ArrayList<View> dynamicInputs = new ArrayList<>();
    private int inputCounter = 0;

    private static final String KEY_COIN_LIST = "KEY_COIN_LIST";

    private void setupDynamicInputs() {
        dynamicInputContainer = findViewById(R.id.dynamicInputContainer);
        btnAddInput = findViewById(R.id.btnAddInput);

        btnAddInput.setOnClickListener(v -> {
            // TODO: Implement filter input addition
        });
    }
    private void addNewFilterInput() {
        inputCounter++;

        LinearLayout inputGroup = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        inputGroup.setLayoutParams(params);
        inputGroup.setOrientation(LinearLayout.HORIZONTAL);

        Spinner filterTypeSpinner = new Spinner(this);
        filterTypeSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Price Above", "Price Below", "Market Cap", "Volume"});
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterTypeSpinner.setAdapter(filterAdapter);

        inputGroup.addView(filterTypeSpinner);
        dynamicInputContainer.addView(inputGroup);
    }

    private View createValueInput(int filterType) {
        switch (filterType % 3) {
            case 0:
                EditText editText = new EditText(this);
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setHint("Enter value");
                return editText;
            case 1:
                SeekBar seekBar = new SeekBar(this);
                seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                seekBar.setMax(100);
                return seekBar;
            default:
                RadioGroup radioGroup = new RadioGroup(this);
                radioGroup.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                radioGroup.setOrientation(LinearLayout.HORIZONTAL);

                RadioButton rbHigh = new RadioButton(this);
                rbHigh.setText("High");
                RadioButton rbLow = new RadioButton(this);
                rbLow.setText("Low");

                radioGroup.addView(rbHigh);
                radioGroup.addView(rbLow);
                return radioGroup;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        spinnerApiSource = findViewById(R.id.spinnerApiSource);
        tvLoading = findViewById(R.id.tvLoading);
        btnFetch = findViewById(R.id.btnFetch);
        recyclerView = findViewById(R.id.recyclerViewCoins);

        // Setup spinner with 3 options
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"CoinGecko: Bitcoin only", "CoinPaprika: Top 5", "CoinCap: Top 5"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApiSource.setAdapter(spinnerAdapter);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CryptoCoinAdapter(coinList);
        recyclerView.setAdapter(adapter);

        // Restore list if orientation changed
        if (savedInstanceState != null) {
            // Rebuild coinList from a JSON string
            String savedJson = savedInstanceState.getString(KEY_COIN_LIST, null);
            if (savedJson != null) {
                coinList.clear();
                coinList.addAll(parseCoinListFromJson(savedJson));
                adapter.notifyDataSetChanged();
            }
        }

        // Fetch button click
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = spinnerApiSource.getSelectedItemPosition();
                switch (position) {
                    case 0:
                        // CoinGecko - Single coin (Bitcoin)
                        fetchCoinGeckoBitcoin();
                        break;
                    case 1:
                        // CoinPaprika - Top 5
                        fetchCoinPaprikaTop5();
                        break;
                    case 2:
                        // CoinCap - Top 5
                        fetchCoinCapTop5();
                        break;
                }
            }
        });
    }

    // --------------------------------------------------------------------------------------
    // Animated "Loading..." text
    // --------------------------------------------------------------------------------------
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

    // Repeats every 500ms, cycling "Loading", "Loading.", "Loading..", etc.
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

            loadingDotCount = (loadingDotCount + 1) % 4; // cycle 0..3

            loadingHandler.postDelayed(this, 500);
        }
    };

    // --------------------------------------------------------------------------------------
    // Networking: fetchCoinGeckoBitcoin, fetchCoinPaprikaTop5, fetchCoinCapTop5
    // --------------------------------------------------------------------------------------

    /**
     * Fetches Bitcoin data from CoinGecko (price + logo) in a single API call:
     * https://api.coingecko.com/api/v3/coins/bitcoin?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false&sparkline=false
     */
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

                    // Keep using CoinPaprika's own CDN for the logo
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

    /**
     * Fetch top 5 from CoinPaprika, use the Paprika data for name/symbol/price,
     * and still rely on Paprika's own CDN for the logo (like before).
     */
    private void fetchCoinPaprikaTop5() {
        startLoadingAnimation();
        coinList.clear();
        adapter.notifyDataSetChanged();

        final String url = "https://api.coinpaprika.com/v1/tickers"; // returns many coins

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
                    int limit = Math.min(arr.length(), 5);
                    for (int i = 0; i < limit; i++) {
                        JSONObject coinObj = arr.getJSONObject(i);

                        String name = coinObj.getString("name");
                        String symbol = coinObj.getString("symbol");

                        JSONObject quotes = coinObj.getJSONObject("quotes");
                        JSONObject usdObj = quotes.getJSONObject("USD");
                        double priceUsd = usdObj.getDouble("price");

                        // Keep using CoinPaprika's own CDN for the logo
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

    /**
     * Fetch top 5 from CoinCap, and for the logos we now use the CoinCap icons CDN:
     * https://assets.coincap.io/assets/icons/<symbol>@2x.png
     */
    private void fetchCoinCapTop5() {
        startLoadingAnimation();
        coinList.clear();
        adapter.notifyDataSetChanged();

        // Endpoint: https://api.coincap.io/v2/assets?limit=5
        final String url = "https://api.coincap.io/v2/assets?limit=5";

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
                    for (int i = 0; i < dataArr.length(); i++) {
                        JSONObject coinObj = dataArr.getJSONObject(i);
                        String name = coinObj.getString("name");
                        String symbol = coinObj.getString("symbol");
                        double priceUsd = coinObj.getDouble("priceUsd");

                        //  Build the coin's icon URL using CoinCap's pattern:
                        //  https://assets.coincap.io/assets/icons/<symbol_lowercase>@2x.png
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

    // --------------------------------------------------------------------------------------
    // Helper: doHttpGet (blocking IO, so must be called on background thread!)
    // --------------------------------------------------------------------------------------
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
                return null; // or handle the code differently
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // --------------------------------------------------------------------------------------
    // Updating UI from background threads
    // --------------------------------------------------------------------------------------
    private void updateRecycler() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
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

    // --------------------------------------------------------------------------------------
    // Handle orientation changes
    // --------------------------------------------------------------------------------------
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save coinList as a JSON string
        String json = convertCoinListToJson(coinList);
        outState.putString(KEY_COIN_LIST, json);
    }

    // Convert coinList to JSON
    private String convertCoinListToJson(ArrayList<CryptoCoin> list) {
        JSONArray arr = new JSONArray();
        for (CryptoCoin coin : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", coin.getName());
                obj.put("symbol", coin.getSymbol());
                obj.put("priceUsd", coin.getPriceUsd());
                obj.put("logoUrl", coin.getLogoUrl());
                arr.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arr.toString();
    }

    // Convert JSON back to list
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
                list.add(new CryptoCoin(name, symbol, priceUsd, logoUrl));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
