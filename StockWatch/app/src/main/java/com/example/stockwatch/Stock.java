package com.example.stockwatch;

import android.util.JsonWriter;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

public class Stock implements Comparable<Stock>,Serializable {
    private final String symbol;
    private final String companyName;
    private String latestPrice;
    private String change;
    private String changePercent;

    Stock(String symbol, String companyName, String latestPrice, String change, String changePercent) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.latestPrice = latestPrice;
        this.change = change;
        this.changePercent = changePercent;
    }

    String getSymbol(){
        return symbol;
    }

    String getCompanyName(){
        return companyName;
    }

    String getLatestPrice(){
        return latestPrice;
    }
    void setLatestPrice(String s) {this.latestPrice = s;}

    float getChange(){
        return Float.valueOf(change);
    }
    void setChange(String s) {this.change = s;}

    float getChangePercent(){
        return Float.valueOf(changePercent);
    }
    void setChangePercent(String s) {this.changePercent = s;}

    @Override
    public int compareTo(Stock stock) {
        return symbol.compareTo(stock.getSymbol());
    }


    @NonNull
    public String toString() {

        try {
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(sw);
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            jsonWriter.name("symbol").value(getSymbol());
            jsonWriter.name("companyName").value(getCompanyName());
            jsonWriter.name("latestPrice").value(getLatestPrice());
            jsonWriter.name("change").value(getChange());
            jsonWriter.name("changePercent").value(getChangePercent());
            jsonWriter.endObject();
            jsonWriter.close();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
