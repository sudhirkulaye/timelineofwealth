package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ConsolidatedAssetsDTO implements Serializable {
    private String memberid;
    private String ticker;
    private String name;
    private String shortName;
    private String assetClassid;
    private String subindustryid;
    private String quantity;
    private String rate;
    private String brokerage;
    private String tax;
    private String totalCost;
    private String netRate;
    private String cmp;
    private String marketValue;
    private String netProfit;
    private String absoluteReturn;
    private String annualizedReturn;

    public String getMemberid() {
        return memberid;
    }
    public void setMemberid(String memberid) {
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

    public String getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(String assetClassid) {
        this.assetClassid = assetClassid;
    }

    public String getSubindustryid() {
        return subindustryid;
    }
    public void setSubindustryid(String subindustryid) {
        this.subindustryid = subindustryid;
    }

    public String getQuantity() {
        return quantity;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getRate() {
        return rate;
    }
    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getBrokerage() {
        return brokerage;
    }
    public void setBrokerage(String brokerage) {
        this.brokerage = brokerage;
    }

    public String getTax() {
        return tax;
    }
    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getTotalCost() {
        return totalCost;
    }
    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public String getNetRate() {
        return netRate;
    }
    public void setNetRate(String netRate) {
        this.netRate = netRate;
    }

    public String getCmp() {
        return cmp;
    }
    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public String getMarketValue() {
        return marketValue;
    }
    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public String getNetProfit() {
        return netProfit;
    }
    public void setNetProfit(String netProfit) {
        this.netProfit = netProfit;
    }

    public String getAbsoluteReturn() {
        return absoluteReturn;
    }
    public void setAbsoluteReturn(String absoluteReturn) {
        this.absoluteReturn = absoluteReturn;
    }

    public String getAnnualizedReturn() {
        return annualizedReturn;
    }
    public void setAnnualizedReturn(String annualizedReturn) {
        this.annualizedReturn = annualizedReturn;
    }
}
