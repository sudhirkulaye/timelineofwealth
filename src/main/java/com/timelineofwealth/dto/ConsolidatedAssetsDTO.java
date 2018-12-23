package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ConsolidatedAssetsDTO implements Serializable {
    private long memberid;
    private String ticker;
    private String name;
    private String shortName;
    private int assetClassid;
    private long subindustryid;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal brokerage;
    private BigDecimal tax;
    private BigDecimal totalCost;
    private BigDecimal netRate;
    private BigDecimal cmp;
    private BigDecimal marketValue;
    private BigDecimal netProfit;
    private BigDecimal absoluteReturn;
    private BigDecimal annualizedReturn;

    public long getMemberid() {
        return memberid;
    }
    public void setMemberid(long memberid) {
        this.memberid = memberid;
    }

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(int assetClassid) {
        this.assetClassid = assetClassid;
    }

    public long getSubindustryid() {
        return subindustryid;
    }
    public void setSubindustryid(long subindustryid) {
        this.subindustryid = subindustryid;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRate() {
        return rate;
    }
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getBrokerage() {
        return brokerage;
    }
    public void setBrokerage(BigDecimal brokerage) {
        this.brokerage = brokerage;
    }

    public BigDecimal getTax() {
        return tax;
    }
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getNetRate() {
        return netRate;
    }
    public void setNetRate(BigDecimal netRate) {
        this.netRate = netRate;
    }

    public BigDecimal getCmp() {
        return cmp;
    }
    public void setCmp(BigDecimal cmp) {
        this.cmp = cmp;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }
    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }
    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getAbsoluteReturn() {
        return absoluteReturn;
    }
    public void setAbsoluteReturn(BigDecimal absoluteReturn) {
        this.absoluteReturn = absoluteReturn;
    }

    public BigDecimal getAnnualizedReturn() {
        return annualizedReturn;
    }
    public void setAnnualizedReturn(BigDecimal annualizedReturn) {
        this.annualizedReturn = annualizedReturn;
    }
}
