package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StockDownloadRunnable implements Runnable {

    private static final String TAG = "StockDownloadRunnable";
    private static final String API_key = "pk_9026a212f50b4a0880e121a73b987bf5";
    private final String symbol;
    private MainActivity mainActivity;
    private String tag;
    StockDownloadRunnable(String symbol, MainActivity mainActivity, String tag) {
        this.symbol = symbol;
        this.mainActivity = mainActivity;
        this.tag = tag;
    }

    @Override
    public void run() {
        Uri dataUri = Uri.parse("https://cloud.iexapis.com/stable/stock/"+symbol+"/quote?token="+API_key);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "run: " + urlToUse);
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
            }
            else {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line = reader.readLine();
                if (line != null) {
                    try {
                        JSONObject stockData = new JSONObject(line);
                        String newSymbol = stockData.getString("symbol");
                        String name = stockData.getString("companyName");
                        String latestPrice = stockData.getString("latestPrice");
                        String change = stockData.getString("change");
                        String changePercent = stockData.getString("changePercent");
                        if (change == null) {
                            change = "0.0";
                        }
                        if (changePercent == null) {
                            changePercent = "0.0";
                        }
                        final Stock s = new Stock(newSymbol, name, latestPrice, change, changePercent);;
                        mainActivity.runOnUiThread(() -> {
                            if (s != null)
                                mainActivity.updateList(s,tag);
                        });
                    } catch (Exception e) {
                        Log.d(TAG, "parseJSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
        }
    }

}
