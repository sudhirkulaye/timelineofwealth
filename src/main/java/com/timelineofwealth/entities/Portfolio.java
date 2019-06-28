package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "portfolio")
public class Portfolio implements Serializable {
    @Embeddable
    public static class PortfolioKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "portfolioid")
        private int portfolioid;

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
    }

    @EmbeddedId
    private PortfolioKey key;
    @Column(name = "status")
    private String status;
    @Column(name = "description")
    private String description;
    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "end_date")
    private Date endDate;
    @Column(name = "compositeid")
    private int compositeid;
    @Column(name = "net_investment")
    private BigDecimal netInvestment;
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

    public PortfolioKey getKey() {
        return key;
    }
    public void setKey(PortfolioKey key) {
        this.key = key;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getCompositeid() {
        return compositeid;
    }
    public void setCompositeid(int compositeid) {
        this.compositeid = compositeid;
    }

    public BigDecimal getNetInvestment() {
        return netInvestment;
    }
    public void setNetInvestment(BigDecimal netInvestment) {
        this.netInvestment = netInvestment;
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
}
