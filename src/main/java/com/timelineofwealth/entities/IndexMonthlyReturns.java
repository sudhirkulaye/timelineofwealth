package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "index_monthly_returns")
public class IndexMonthlyReturns implements Serializable {

    @Embeddable
    public static class IndexMonthlyReturnsKey implements  Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "year")
        private BigDecimal year;

        public String getTicker() {
            return ticker;
        }
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public BigDecimal getYear() {
            return year;
        }
        public void setYear(BigDecimal year) {
            this.year = year;
        }
    }
    @EmbeddedId
    private IndexMonthlyReturns.IndexMonthlyReturnsKey key;

    @Column(name = "jan_return")
    private BigDecimal janReturn;
    @Column(name = "feb_return")
    private BigDecimal febReturn;
    @Column(name = "mar_return")
    private BigDecimal marReturn;
    @Column(name = "apr_return")
    private BigDecimal aprReturn;
    @Column(name = "may_return")
    private BigDecimal mayReturn;
    @Column(name = "jun_return")
    private BigDecimal junReturn;
    @Column(name = "jul_return")
    private BigDecimal julReturn;
    @Column(name = "aug_return")
    private BigDecimal augReturn;
    @Column(name = "sep_return")
    private BigDecimal sepReturn;
    @Column(name = "oct_return")
    private BigDecimal octReturn;
    @Column(name = "nov_return")
    private BigDecimal novReturn;
    @Column(name = "dec_return")
    private BigDecimal decReturn;
    @Column(name = "annual_return")
    private BigDecimal annualReturn;

    public IndexMonthlyReturnsKey getKey() {
        return key;
    }
    public void setKey(IndexMonthlyReturnsKey key) {
        this.key = key;
    }

    public BigDecimal getJanReturn() {
        return janReturn;
    }
    public void setJanReturn(BigDecimal janReturn) {
        this.janReturn = janReturn;
    }

    public BigDecimal getFebReturn() {
        return febReturn;
    }
    public void setFebReturn(BigDecimal febReturn) {
        this.febReturn = febReturn;
    }

    public BigDecimal getMarReturn() {
        return marReturn;
    }
    public void setMarReturn(BigDecimal marReturn) {
        this.marReturn = marReturn;
    }

    public BigDecimal getAprReturn() {
        return aprReturn;
    }
    public void setAprReturn(BigDecimal aprReturn) {
        this.aprReturn = aprReturn;
    }

    public BigDecimal getMayReturn() {
        return mayReturn;
    }
    public void setMayReturn(BigDecimal mayReturn) {
        this.mayReturn = mayReturn;
    }

    public BigDecimal getJunReturn() {
        return junReturn;
    }
    public void setJunReturn(BigDecimal junReturn) {
        this.junReturn = junReturn;
    }

    public BigDecimal getJulReturn() {
        return julReturn;
    }
    public void setJulReturn(BigDecimal julReturn) {
        this.julReturn = julReturn;
    }

    public BigDecimal getAugReturn() {
        return augReturn;
    }
    public void setAugReturn(BigDecimal augReturn) {
        this.augReturn = augReturn;
    }

    public BigDecimal getSepReturn() {
        return sepReturn;
    }
    public void setSepReturn(BigDecimal sepReturn) {
        this.sepReturn = sepReturn;
    }

    public BigDecimal getOctReturn() {
        return octReturn;
    }
    public void setOctReturn(BigDecimal octReturn) {
        this.octReturn = octReturn;
    }

    public BigDecimal getNovReturn() {
        return novReturn;
    }
    public void setNovReturn(BigDecimal novReturn) {
        this.novReturn = novReturn;
    }

    public BigDecimal getDecReturn() {
        return decReturn;
    }
    public void setDecReturn(BigDecimal decReturn) {
        this.decReturn = decReturn;
    }

    public BigDecimal getAnnualReturn() {
        return annualReturn;
    }
    public void setAnnualReturn(BigDecimal annualReturn) {
        this.annualReturn = annualReturn;
    }
}

