package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

/** Weekly Yahoo Finance analyst consensus, loaded by YahooFinance/weekly_lib/consensus_db.py. */
@Entity
@Table(name = "yahoo_consensus")
public class YahooConsensus implements Serializable {
    @Embeddable
    public static class YahooConsensusKey implements Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "date")
        private Date date;
        @Column(name = "fin_year")
        private String finYear;

        public YahooConsensusKey() {}

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        public String getFinYear() { return finYear; }
        public void setFinYear(String finYear) { this.finYear = finYear; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof YahooConsensusKey)) return false;
            YahooConsensusKey k = (YahooConsensusKey) o;
            return Objects.equals(ticker, k.ticker) && Objects.equals(date, k.date) && Objects.equals(finYear, k.finYear);
        }

        @Override
        public int hashCode() { return Objects.hash(ticker, date, finYear); }
    }

    @EmbeddedId
    private YahooConsensusKey key;
    @Column(name = "yahoo_ticker")
    private String yahooTicker;
    @Column(name = "tp_avg")
    private BigDecimal tpAvg;
    @Column(name = "tp_min")
    private BigDecimal tpMin;
    @Column(name = "tp_max")
    private BigDecimal tpMax;
    @Column(name = "rev_avg")
    private BigDecimal revAvg;
    @Column(name = "rev_min")
    private BigDecimal revMin;
    @Column(name = "rev_max")
    private BigDecimal revMax;
    @Column(name = "eps_avg")
    private BigDecimal epsAvg;
    @Column(name = "eps_min")
    private BigDecimal epsMin;
    @Column(name = "eps_max")
    private BigDecimal epsMax;
    @Column(name = "analyst_count")
    private Integer analystCount;
    @Column(name = "year_ago_rev")
    private BigDecimal yearAgoRev;
    @Column(name = "year_ago_eps")
    private BigDecimal yearAgoEps;
    @Column(name = "current_price")
    private BigDecimal currentPrice;

    public YahooConsensusKey getKey() { return key; }
    public void setKey(YahooConsensusKey key) { this.key = key; }
    public String getYahooTicker() { return yahooTicker; }
    public void setYahooTicker(String yahooTicker) { this.yahooTicker = yahooTicker; }
    public BigDecimal getTpAvg() { return tpAvg; }
    public void setTpAvg(BigDecimal tpAvg) { this.tpAvg = tpAvg; }
    public BigDecimal getTpMin() { return tpMin; }
    public void setTpMin(BigDecimal tpMin) { this.tpMin = tpMin; }
    public BigDecimal getTpMax() { return tpMax; }
    public void setTpMax(BigDecimal tpMax) { this.tpMax = tpMax; }
    public BigDecimal getRevAvg() { return revAvg; }
    public void setRevAvg(BigDecimal revAvg) { this.revAvg = revAvg; }
    public BigDecimal getRevMin() { return revMin; }
    public void setRevMin(BigDecimal revMin) { this.revMin = revMin; }
    public BigDecimal getRevMax() { return revMax; }
    public void setRevMax(BigDecimal revMax) { this.revMax = revMax; }
    public BigDecimal getEpsAvg() { return epsAvg; }
    public void setEpsAvg(BigDecimal epsAvg) { this.epsAvg = epsAvg; }
    public BigDecimal getEpsMin() { return epsMin; }
    public void setEpsMin(BigDecimal epsMin) { this.epsMin = epsMin; }
    public BigDecimal getEpsMax() { return epsMax; }
    public void setEpsMax(BigDecimal epsMax) { this.epsMax = epsMax; }
    public Integer getAnalystCount() { return analystCount; }
    public void setAnalystCount(Integer analystCount) { this.analystCount = analystCount; }
    public BigDecimal getYearAgoRev() { return yearAgoRev; }
    public void setYearAgoRev(BigDecimal yearAgoRev) { this.yearAgoRev = yearAgoRev; }
    public BigDecimal getYearAgoEps() { return yearAgoEps; }
    public void setYearAgoEps(BigDecimal yearAgoEps) { this.yearAgoEps = yearAgoEps; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
}
