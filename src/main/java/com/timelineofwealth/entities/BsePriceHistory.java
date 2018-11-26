package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "bse_price_history")
public class BsePriceHistory  implements Serializable {
    @Embeddable
    public static class BsePriceHistoryKey implements Serializable {
        @Column(name = "bse_ticker")
        private String bseTicker;
        @Column(name = "date")
        private Date date;

        public BsePriceHistoryKey(){}
        public BsePriceHistoryKey(String bseTicker, Date date){
            this.bseTicker = bseTicker;
            this.date = date;
        }

        public String getBseTicker() {
            return bseTicker;
        }
        public void setBseTicker(String bseTicker) {
            this.bseTicker = bseTicker;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @EmbeddedId
    private BsePriceHistory.BsePriceHistoryKey key;

    @Column(name = "company_name")
    private String companyName;
    @Column(name = "company_group")
    private String companyGroup;
    @Column(name = "company_type")
    private String companyType;
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
    @Column(name = "total_trades")
    private BigDecimal totalTrades;
    @Column(name = "total_traded_quantity")
    private BigDecimal totalTradedQuantity;
    @Column(name = "total_traded_value")
    private BigDecimal totalTradedValue;
    @Column(name = "isin_code")
    private String insinCode;

    public BsePriceHistoryKey getKey() {
        return key;
    }
    public void setKey(BsePriceHistoryKey key) {
        this.key = key;
    }

    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyGroup() {
        return companyGroup;
    }
    public void setCompanyGroup(String companyGroup) {
        this.companyGroup = companyGroup;
    }

    public String getCompanyType() {
        return companyType;
    }
    public void setCompanyType(String companyType) {
        this.companyType = companyType;
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

    public BigDecimal getTotalTrades() {
        return totalTrades;
    }
    public void setTotalTrades(BigDecimal totalTrades) {
        this.totalTrades = totalTrades;
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

    public String getInsinCode() {
        return insinCode;
    }
    public void setInsinCode(String insinCode) {
        this.insinCode = insinCode;
    }
}
