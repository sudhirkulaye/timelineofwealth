package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "wealth_details")
public class WealthDetails implements Serializable {

    @Embeddable
    public static class WealthDetailsKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "buy_date")
        private Date buyDate;
        @Column(name = "ticker")
        private String ticker;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public Date getBuyDate() {
            return buyDate;
        }
        public void setBuyDate(Date buyDate) {
            this.buyDate = buyDate;
        }

        public String getTicker() {
            return ticker;
        }
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }
    }

    @EmbeddedId
    private WealthDetailsKey key;

    @Column(name = "name")
    private String name;
    @Column(name = "short_name")
    private String shortName;
    @Column(name = "asset_classid")
    private int assetClassid;
    @Column(name = "subindustryid")
    private long subindustryid;
    @Column(name = "quantity")
    private BigDecimal quantity;
    @Column(name = "rate")
    private BigDecimal rate;
    @Column(name = "brokerage")
    private BigDecimal brokerage;
    @Column(name = "tax")
    private BigDecimal tax;
    @Column(name = "total_cost")
    private BigDecimal totalCost;
    @Column(name = "net_rate")
    private BigDecimal netRate;
    @Column(name = "cmp")
    private BigDecimal cmp;
    @Column(name = "market_value")
    private BigDecimal marketValue;
    @Column(name = "holding_period")
    private BigDecimal holdingPeriod;
    @Column(name = "net_profit")
    private BigDecimal netProfit;
    @Column(name = "absolute_return")
    private BigDecimal absoluteReturn;
    @Column(name = "annualized_return")
    private BigDecimal annualizedReturn;
    @Column(name = "maturity_value")
    private BigDecimal maturityValue;
    @Column(name = "maturity_date")
    private Date maturityDate;
    @Column(name = "last_valuation_date")
    private Date lastValuationDate;

    public WealthDetailsKey getKey() {
        return key;
    }
    public void setKey(WealthDetailsKey key) {
        this.key = key;
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

    public BigDecimal getHoldingPeriod() {
        return holdingPeriod;
    }
    public void setHoldingPeriod(BigDecimal holdingPeriod) {
        this.holdingPeriod = holdingPeriod;
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

    public BigDecimal getMaturityValue() {
        return maturityValue;
    }
    public void setMaturityValue(BigDecimal maturityValue) {
        this.maturityValue = maturityValue;
    }

    public Date getMaturityDate() {
        return maturityDate;
    }
    public void setMaturityDate(Date maturityDate) {
        this.maturityDate = maturityDate;
    }

    public Date getLastValuationDate() {
        return lastValuationDate;
    }
    public void setLastValuationDate(Date lastValuationDate) {
        this.lastValuationDate = lastValuationDate;
    }
}
