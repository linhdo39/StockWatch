package com.example.stockwatch;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

        TextView symbol;
        TextView price;
        TextView change;
        TextView companyName;

        ViewHolder(View view) {
            super(view);
            symbol = view.findViewById(R.id.symbol);
            price = view.findViewById(R.id.price);
            change = view.findViewById(R.id.change);
            companyName = view.findViewById(R.id.companyName);
        }

}
