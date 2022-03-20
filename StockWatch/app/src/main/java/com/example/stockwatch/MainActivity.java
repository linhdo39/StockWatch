package com.example.stockwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    private final String TAG ="Main Activity";
    private final ArrayList<Stock> stockList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Adapter adapter;
    private SwipeRefreshLayout swiper;
    private NameDownload nameLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stockList.addAll(loadFile());
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new Adapter(stockList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this::doRefresh);
        checkInternet("On Create");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            if(checkInternet("Added"))
                find_stock();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        stockList.clear();
        stockList.addAll(loadFile());
        super.onResume();
    }

    @Override
    protected void onPause() {
        saveStock();
        super.onPause();
    }

    public void find_stock(){
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.find_stock, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol");
        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, id) -> {
            EditText enteredValue = view.findViewById(R.id.stockSymbol);
            if (enteredValue.getText().length() != 0) {
                String value = enteredValue.getText().toString();
                value = value.toUpperCase();
                value = value.replace(" ", "");
                add_stock(value);
            }
        });
        builder.setNegativeButton("CANCEL", (dialog, id) -> Log.d(TAG, "No new stock added"));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void add_stock(String s) {
        ArrayList<String> tempList;
        tempList = nameLoader.checkStock(s);
        Log.d(TAG, String.valueOf(tempList.size()));
        if (tempList.size() == 0 ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Symbol Not Found: " + s);
            builder.setMessage("Data for stock symbol");
            builder.setPositiveButton("OK", (dialog, id) -> Log.d(TAG, "Symbol Not Found"));
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(tempList.size() == 1) {
            String [] temp = tempList.get(0).split(" - ");
            for (int i = 0; i < stockList.size(); i++){
                if(stockList.get(i).getSymbol().equals(temp[0])){
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    builder2.setIcon(R.mipmap.warning);
                    builder2.setTitle("Duplicate Stock");
                    builder2.setMessage("Stock Symbol " + temp[0] + " is already displayed");
                    AlertDialog dialog1 = builder2.create();
                    dialog1.show();
                    return;
                }
            }
            StockDownloadRunnable stockLoaderRunnable = new StockDownloadRunnable(temp[0], MainActivity.this, "Add");
            new Thread(stockLoaderRunnable).start();
        }
        else {
            CharSequence[] cs = tempList.toArray(new CharSequence[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Make a selection");
            builder.setItems(cs, (dialog, pos) -> {
                String [] temp = cs[pos].toString().split(" - ");
                for (int i = 0; i < stockList.size(); i++){
                    if(stockList.get(i).getSymbol().equals(temp[0])){
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                        builder2.setIcon(R.mipmap.warning);
                        builder2.setTitle("Duplicate Stock");
                        builder2.setMessage("Stock Symbol " + temp[0] + " is already displayed");
                        AlertDialog dialog1 = builder2.create();
                        dialog1.show();
                        return;
                    }
                }
                StockDownloadRunnable stockLoaderRunnable = new StockDownloadRunnable(temp[0], MainActivity.this, "Add");
                new Thread(stockLoaderRunnable).start();
            });
            builder.setNegativeButton("Nevermind", (dialog, id) -> Log.d(TAG, "No new stock added"));

            AlertDialog dialog = builder.create();
            dialog.show();
            }
    }


    private void doRefresh() {
        ArrayList<Stock> tempList = new ArrayList<>(loadFile());
        if(checkInternet("Updated"))
            stockList.clear();
            for (int i = 0; i < tempList.size(); i++){
                StockDownloadRunnable stockLoaderRunnable = new StockDownloadRunnable(tempList.get(i).getSymbol(),this,"Refresh");
                new Thread(stockLoaderRunnable).start();
            }
        swiper.setRefreshing(false);
    }

    public void updateList(Stock s, String tag){
        int pos = 0;
        if(tag.equals("Add")){
            stockList.add(0,s);
            Collections.sort(stockList);
            for(int i = 0; i <stockList.size();i++){
                if(stockList.get(i).getSymbol().equals(s.getSymbol())){
                    pos = i;
                    break;
                }
            }
            saveStock();
            adapter.notifyItemInserted(pos);
        }
        else {
            stockList.add(0,s);
            Collections.sort(stockList);
            adapter.notifyDataSetChanged();
            saveStock();
        }
    }

    @Override
    public void onClick(View v) {
        String baseURL ="http://www.marketwatch.com/investing/stock/";
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock c = stockList.get(pos);
        Log.d(TAG,"On Click method");
        if(checkInternet("Opened")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(baseURL + c.getSymbol()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int pos = recyclerView.getChildLayoutPosition(v);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + stockList.get(pos).getSymbol() +"?");
        builder.setPositiveButton("Cancel", (dialog, id) -> Log.d(TAG, "No deletion"));
        builder.setNegativeButton("Delete", (dialog, id) -> deleteStock(stockList.get(pos),pos));
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    public void deleteStock(Stock temp, int position){
        stockList.remove(temp);
        saveStock();
        adapter.notifyItemRemoved(position);
    }


    public boolean checkInternet(String method){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            nameLoader = new NameDownload();
            new Thread(nameLoader).start();
            return true;
        }
        else {
            for(int i = 0; i < stockList.size(); i++){
                stockList.get(i).setLatestPrice("0");
                stockList.get(i).setChange("0");
                stockList.get(i).setChangePercent("0");
            }
            adapter.notifyDataSetChanged();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            if(method.equals("Opened"))
                builder.setMessage("Link Cannot Be Opened Without A Network Connection");
            else if(method.equals("On Create")){
                builder.setMessage("Stocks Cannot Be Loaded/Updated Without A Network Connection");
            }
            else
                builder.setMessage("Stocks Cannot Be " + method + " Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return false;
    }

    private ArrayList<Stock> loadFile() {
        ArrayList<Stock> temp = new ArrayList<>();
        try {
            InputStream is = getApplicationContext().openFileInput(getString(R.string.file_name));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String symbol = jsonObject.getString("symbol");
                String name = jsonObject.getString("companyName");
                String price = jsonObject.getString("latestPrice");
                String change = jsonObject.getString("change");
                String changePercent = jsonObject.getString("changePercent");
                Stock stock = new Stock(symbol, name, price, change, changePercent);
                temp.add(stock);
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG,"loadJson: no Json file");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    private void saveStock() {
        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);
            PrintWriter printWriter = new PrintWriter(fos);
            printWriter.print(stockList);
            printWriter.close();
            Log.d(TAG,"saveStock call");
            Log.d(TAG, stockList.toString());
            fos.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}