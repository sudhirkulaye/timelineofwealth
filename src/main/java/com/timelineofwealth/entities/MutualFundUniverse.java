package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "mutual_fund_universe")
public class MutualFundUniverse {
    @Id
    @Column(name = "scheme_code")
    private long schemeCode;
    @Column(name = "isin_div_payout_or_isin_growth")
    private String isinDivPayoutIsinGrowth;
    @Column(name = "isin_div_reinvestment")
    private String isinDivReinvestment;
    @Column(name = "scheme_code_direct_growth")
    private long schemeCodeDirectGrowth;
    @Column(name = "scheme_code_regular_growth")
    private long schemeCodeRegularGrowth;
    @Column(name = "fund_house")
    private String fundHouse;
    @Column(name = "direct_regular")
    private String directRegular;
    @Column(name = "dividend_growth")
    private String dividendGrowth;
    @Column(name = "dividend_freq")
    private String dividendFreq;
    @Column(name = "scheme_name_part")
    private String schemeNamePart;
    @Column(name = "scheme_name_full")
    private String schemeNameFull;
    @Column(name = "asset_classid")
    private int assetClassid;
    @Column(name = "category")
    private String category;
    @Column(name = "equity_style_box")
    private String equityStyleBox;
    @Column(name = "debt_style_box")
    private String debtStyleBox;
    @Column(name = "latest_nav")
    private BigDecimal latestNav;
    @Column(name = "date_latest_nav")
    private Date dateLatestNav;
    @Column(name = "benchmark_ticker")
    private String benchmarkTicker;

    public long getSchemeCode() {
        return schemeCode;
    }
    public void setSchemeCode(long schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getIsinDivPayoutIsinGrowth() {
        return isinDivPayoutIsinGrowth;
    }
    public void setIsinDivPayoutIsinGrowth(String isinDivPayoutIsinGrowth) {
        this.isinDivPayoutIsinGrowth = isinDivPayoutIsinGrowth;
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
