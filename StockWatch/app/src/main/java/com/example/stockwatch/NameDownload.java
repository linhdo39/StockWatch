package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NameDownload implements Runnable {
    private static final String TAG = "NameDownload";
    private static final String symbolName_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private final Map<String,String> nameList = new HashMap<>();

    @Override
    public void run() {
        Uri dataUri = Uri.parse(symbolName_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            return;
        }

        parseJSON(sb.toString());

    }

    private void parseJSON(String s) {
        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String companyName = jStock.getString("name");
                String symbol = jStock.getString("symbol");
                nameList.put(symbol, companyName);
            }
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<String> checkStock(String s){
        ArrayList<String> tempList = new ArrayList<>();
        for (Map.Entry<String,String> item : nameList.entrySet())
            if(item.getKey().contains(s)){
                if(!item.getValue().isEmpty())
                    tempList.add(item.getKey() + " - " + item.getValue());
            }
        Collections.sort(tempList);
        return tempList;
    }
}
