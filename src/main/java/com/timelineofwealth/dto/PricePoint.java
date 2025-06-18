package com.timelineofwealth.dto;


import java.sql.Date;

public class PricePoint {
    private java.sql.Date date;
    private Double closePrice;
    private double tradeVolume;
    private double tradeValue;

    public PricePoint(java.sql.Date date, Double closePrice) {
        this.date = date;
        this.closePrice = closePrice;
    }

    public java.sql.Date getDate() { return date; }
    public Double getClosePrice() { return closePrice; }

    public double getTradeVolume() {
        return tradeVolume;
    }
    public void setTradeVolume(double tradeVolume) {
        this.tradeVolume = tradeVolume;
    }

    public double getTradeValue() {
        return tradeValue;
    }
    public void setTradeValue(double tradeValue) {
        this.tradeValue = tradeValue;
    }
}
