package com.timelineofwealth.entities;


import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "nse_price_history")
public class NsePriceHistory implements Serializable {
    @Embeddable
    public static class NsePriceHistoryKey implements Serializable {
        @Column(name = "nse_ticker")
        private String nseTicker;
        @Column(name = "date")
        private Date date;

        public NsePriceHistoryKey(){}
        public NsePriceHistoryKey(String nseTicker, Date date){
            this.nseTicker = nseTicker;
            this.date = date;
        }

        public String getNseTicker() {
            return nseTicker;
        }
        public void setNseTicker(String nseTicker) {
            this.nseTicker = nseTicker;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @EmbeddedId
    private NsePriceHistoryKey key;

    @Column(name = "series")
    private String series;
    @Column(name = "open_price")
    private BigDecimal openPrice;
    @Column(name = "high_price")
    private BigDecimal highPrice;
    @Column(name = "low_price")
    private BigDecimal lowPrice;
    @Column(name = "close_price")
    private BigDecimal closePrice;
    @Column(name = "last_price")
    private BigDecimal lastPrice;
    @Column(name = "previous_close_price")
    private BigDecimal previousClosePrice;
    @Column(name = "total_traded_quantity")
    private BigDecimal totalTradedQuantity;
    @Column(name = "total_traded_value")
    private BigDecimal totalTradedValue;
    @Column(name = "total_trades")
    private BigDecimal totalTrades;
    @Column(name = "isin_code")
    private String insinCode;

    public NsePriceHistory() { this.key = new NsePriceHistoryKey();}


    public NsePriceHistoryKey getKey() {
        return key;
    }
    public void setKey(NsePriceHistoryKey key) {
        this.key = key;
    }

    public String getSeries() {
        return series;
    }
    public void setSeries(String series) {
        this.series = series;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }
    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }
    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }
    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }
    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }
    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getPreviousClosePrice() {
        return previousClosePrice;
    }
    public void setPreviousClosePrice(BigDecimal previousClosePrice) {
        this.previousClosePrice = previousClosePrice;
    }

    public BigDecimal getTotalTradedQuantity() {
        return totalTradedQuantity;
    }
    public void setTotalTradedQuantity(BigDecimal totalTradedQuantity) {
        this.totalTradedQuantity = totalTradedQuantity;
    }

    public BigDecimal getTotalTradedValue() {
        return totalTradedValue;
    }
    public void setTotalTradedValue(BigDecimal totalTradedValue) {
        this.totalTradedValue = totalTradedValue;
    }

    public BigDecimal getTotalTrades() {
        return totalTrades;
    }
    public void setTotalTrades(BigDecimal totalTrades) {
        this.totalTrades = totalTrades;
    }

    public String getInsinCode() {
        return insinCode;
    }
    public void setInsinCode(String insinCode) {
        this.insinCode = insinCode;
    }
}
