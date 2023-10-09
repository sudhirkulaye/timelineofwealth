package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class IndexMonthlyReturnsDTO implements Serializable {
    private String ticker;
    private BigDecimal year;
    private BigDecimal janReturn;
    private BigDecimal febReturn;
    private BigDecimal marReturn;
    private BigDecimal aprReturn;
    private BigDecimal mayReturn;
    private BigDecimal junReturn;
    private BigDecimal julReturn;
    private BigDecimal augReturn;
    private BigDecimal sepReturn;
    private BigDecimal octReturn;
    private BigDecimal novReturn;
    private BigDecimal decReturn;
    private BigDecimal annualReturn;

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

