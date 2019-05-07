package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_price_movement_history")
public class StockPriceMovementHistory implements Serializable {

    @Embeddable
    public static class StockPriceMovementHistoryKey implements Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "date")
        private Date date;

        public StockPriceMovementHistoryKey(){}

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
    }
    @Column(name = "return_1D")
    private BigDecimal return1D;
    @Column(name = "return_1W")
    private BigDecimal return1W;
    @Column(name = "return_2W")
    private BigDecimal return2W;
    @Column(name = "return_1M")
    private BigDecimal return1M;

    @EmbeddedId
    private StockPriceMovementHistoryKey key;

    public StockPriceMovementHistoryKey getKey() {
        return key;
    }
    public void setKey(StockPriceMovementHistoryKey key) {
        this.key = key;
    }

    public BigDecimal getReturn1D() {
        return return1D;
    }
    public void setReturn1D(BigDecimal return1D) {
        this.return1D = return1D;
    }

    public BigDecimal getReturn1W() {
        return return1W;
    }
    public void setReturn1W(BigDecimal return1W) {
        this.return1W = return1W;
    }

    public BigDecimal getReturn2W() {
        return return2W;
    }
    public void setReturn2W(BigDecimal return2W) {
        this.return2W = return2W;
    }

    public BigDecimal getReturn1M() {
        return return1M;
    }
    public void setReturn1M(BigDecimal return1M) {
        this.return1M = return1M;
    }

}
