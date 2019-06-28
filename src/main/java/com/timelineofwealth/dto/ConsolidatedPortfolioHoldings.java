package com.timelineofwealth.dto;

import java.math.BigDecimal;

public class ConsolidatedPortfolioHoldings {
    private long memberid;
    private int portfolioid;
    private String name;
    private BigDecimal quantity;
    private BigDecimal totalCost;
    private BigDecimal marketValue;
    private BigDecimal netProfit;
    private BigDecimal weight;

    public long getMemberid() {
        return memberid;
    }
    public void setMemberid(long memberid) {
        this.memberid = memberid;
    }

    public int getPortfolioid() {
        return portfolioid;
    }
    public void setPortfolioid(int portfolioid) {
        this.portfolioid = portfolioid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
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

    public BigDecimal getWeight() {
        return weight;
    }
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}
