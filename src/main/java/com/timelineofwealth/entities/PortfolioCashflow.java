package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "portfolio_cashflow")
public class PortfolioCashflow  implements Serializable {

    @Embeddable
    public static class PortfolioCashflowKey implements Serializable {
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
    private PortfolioCashflowKey key;
    @Column(name = "cashflow")
    private BigDecimal cashflow;

    public PortfolioCashflowKey getKey() {
        return key;
    }
    public void setKey(PortfolioCashflowKey key) {
        this.key = key;
    }

    public BigDecimal getCashflow() {
        return cashflow;
    }
    public void setCashflow(BigDecimal cashflow) {
        this.cashflow = cashflow;
    }

}
