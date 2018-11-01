package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class MutualFundDTO  implements Serializable {

    private long schemeCode;
    private String isinDivReinvestment;
    private long schemeCodeDirectGrowth;
    private long schemeCodeRegularGrowth;
    private String fundHouse;
    private String directRegular;
    private String dividendGrowth;
    private String dividendFreq;
    private String schemeNamePart;
    private String schemeNameFull;
    private int assetClassid;
    private String category;
    private String equityStyleBox;
    private String debtStyleBox;
    private BigDecimal latestNav;
    private Date dateLatestNav;
    private String benchmarkTicker;

    public long getSchemeCode() {
        return schemeCode;
    }
    public void setSchemeCode(long schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getIsinDivReinvestment() {
        return isinDivReinvestment;
    }
    public void setIsinDivReinvestment(String isinDivReinvestment) {
        this.isinDivReinvestment = isinDivReinvestment;
    }

    public long getSchemeCodeDirectGrowth() {
        return schemeCodeDirectGrowth;
    }
    public void setSchemeCodeDirectGrowth(long schemeCodeDirectGrowth) {
        this.schemeCodeDirectGrowth = schemeCodeDirectGrowth;
    }

    public long getSchemeCodeRegularGrowth() {
        return schemeCodeRegularGrowth;
    }
    public void setSchemeCodeRegularGrowth(long schemeCodeRegularGrowth) {
        this.schemeCodeRegularGrowth = schemeCodeRegularGrowth;
    }

    public String getFundHouse() {
        return fundHouse;
    }
    public void setFundHouse(String fundHouse) {
        this.fundHouse = fundHouse;
    }

    public String getDirectRegular() {
        return directRegular;
    }
    public void setDirectRegular(String directRegular) {
        this.directRegular = directRegular;
    }

    public String getDividendGrowth() {
        return dividendGrowth;
    }
    public void setDividendGrowth(String dividendGrowth) {
        this.dividendGrowth = dividendGrowth;
    }

    public String getDividendFreq() {
        return dividendFreq;
    }
    public void setDividendFreq(String dividendFreq) {
        this.dividendFreq = dividendFreq;
    }

    public String getSchemeNamePart() {
        return schemeNamePart;
    }
    public void setSchemeNamePart(String schemeNamePart) {
        this.schemeNamePart = schemeNamePart;
    }

    public String getSchemeNameFull() {
        return schemeNameFull;
    }
    public void setSchemeNameFull(String schemeNameFull) {
        this.schemeNameFull = schemeNameFull;
    }

    public int getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(int assetClassid) {
        this.assetClassid = assetClassid;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getEquityStyleBox() {
        return equityStyleBox;
    }
    public void setEquityStyleBox(String equityStyleBox) {
        this.equityStyleBox = equityStyleBox;
    }

    public String getDebtStyleBox() {
        return debtStyleBox;
    }
    public void setDebtStyleBox(String debtStyleBox) {
        this.debtStyleBox = debtStyleBox;
    }

    public BigDecimal getLatestNav() {
        return latestNav;
    }
    public void setLatestNav(BigDecimal latestNav) {
        this.latestNav = latestNav;
    }

    public Date getDateLatestNav() {
        return dateLatestNav;
    }
    public void setDateLatestNav(Date dateLatestNav) {
        this.dateLatestNav = dateLatestNav;
    }

    public String getBenchmarkTicker() {
        return benchmarkTicker;
    }
    public void setBenchmarkTicker(String benchmarkTicker) {
        this.benchmarkTicker = benchmarkTicker;
    }
}
