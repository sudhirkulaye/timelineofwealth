package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "index_valuation")
public class IndexValuation implements Serializable {
    @Embeddable
    public static class IndexValuationKey implements  Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "date")
        private Date date;

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
    @EmbeddedId
    private IndexValuation.IndexValuationKey key;
    @Column(name = "pe")
    private BigDecimal pe;
    @Column(name = "pb")
    private BigDecimal pb;
    @Column(name = "div_yield")
    private BigDecimal divYield;
    @Column(name = "value")
    private BigDecimal value;
    @Column(name = "turnover")
    private BigDecimal turnover;
    @Column(name = "implied_earnings")
    private BigDecimal impliedEarnings;

    public IndexValuationKey getKey() {
        return key;
    }
    public void setKey(IndexValuationKey key) {
        this.key = key;
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

    public BigDecimal getDivYield() {
        return divYield;
    }
    public void setDivYield(BigDecimal divYield) {
        this.divYield = divYield;
    }

    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }
    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public BigDecimal getImpliedEarnings() {
        return impliedEarnings;
    }
    public void setImpliedEarnings(BigDecimal impliedEarnings) {
        this.impliedEarnings = impliedEarnings;
    }
}
