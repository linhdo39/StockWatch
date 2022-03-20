package com.example.stockwatch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = "Adapter";
    private final List<Stock> stockList;
    private final MainActivity mainAct;

    Adapter(List<Stock> empList, MainActivity ma) {
        this.stockList = empList;
        mainAct = ma;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW MyViewHolder");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: FILLING VIEW HOLDER Note " + position);
        String data = "";
        Stock item = stockList.get(position);
        if(item.getChange() <0){
            holder.symbol.setTextColor(Color.RED);
            holder.companyName.setTextColor(Color.RED);
            holder.price.setTextColor(Color.RED);
            holder.change.setTextColor(Color.RED);
            data = "▼ ";
        }
        else {
            holder.symbol.setTextColor(Color.GREEN);
            holder.companyName.setTextColor(Color.GREEN);
            holder.price.setTextColor(Color.GREEN);
            holder.change.setTextColor(Color.GREEN);
            if(item.getChange() != 0)
                data = "▲ ";
        }

        holder.symbol.setText(item.getSymbol());
        holder.companyName.setText(item.getCompanyName());
        holder.price.setText(item.getLatestPrice());
        data = data+ String.format("%#.2f", item.getChange())+ " (" + String.format("%#.2f", item.getChangePercent()) + "%)";
        holder.change.setText(data);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

}