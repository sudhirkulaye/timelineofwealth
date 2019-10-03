package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "portfolio_returns_calculation_support")
public class PortfolioReturnsCalculationSupport  implements Serializable {
    @Embeddable
    public static class PortfolioReturnsCalculationSupportKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "portfolioid")
        private int portfolioid;
        @Column(name = "date")
        private Date date;

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

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @EmbeddedId
    private PortfolioReturnsCalculationSupportKey key;
    @Column(name = "cashflow")
    private BigDecimal cashflow;
    @Column(name = "value")
    private BigDecimal value;
    @Column(name = "description")
    private  String description;

    public PortfolioReturnsCalculationSupportKey getKey() {
        return key;
    }
    public void setKey(PortfolioReturnsCalculationSupportKey key) {
        this.key = key;
    }

    public BigDecimal getCashflow() {
        return cashflow;
    }
    public void setCashflow(BigDecimal cashflow) {
        this.cashflow = cashflow;
    }

    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
