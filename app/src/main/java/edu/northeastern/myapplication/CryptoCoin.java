package edu.northeastern.myapplication;

import java.util.ArrayList;
import java.util.List;

public class CryptoCoin {
    private String name;
    private String symbol;
    private double priceUsd;
    private String logoUrl;

    private List<String> notes;

    public CryptoCoin(String name, String symbol, double priceUsd, String logoUrl) {
        this.name = name;
        this.symbol = symbol;
        this.priceUsd = priceUsd;
        this.logoUrl = logoUrl;
        this.notes = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getSymbol() { return symbol; }
    public double getPriceUsd() { return priceUsd; }
    public String getLogoUrl() { return logoUrl; }

    public void setName(String name) { this.name = name; }
    public List<String> getNotes() { return notes; }
}