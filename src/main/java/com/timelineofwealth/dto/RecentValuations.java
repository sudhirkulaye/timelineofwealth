package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class RecentValuations implements Serializable {
    private String ticker;
    private Date date;
    private BigDecimal pe;
    private BigDecimal pb;
    private BigDecimal evToEbita;
    private BigDecimal marketCap;
    private BigDecimal marketPrice;

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getPe() {
        return pe;
    }
    public void setPe(BigDecimal pe) {
        this.pe = pe;
    }

    public BigDecimal getPb() {
        return pb;
    }
    public void setPb(BigDecimal pb) {
        this.pb = pb;
    }

    public BigDecimal getEvToEbita() {
        return evToEbita;
    }
    public void setEvToEbita(BigDecimal evToEbita) {
        this.evToEbita = evToEbita;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }
    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }
    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }
}
