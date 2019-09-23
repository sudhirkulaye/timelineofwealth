package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_twrr_monthly")
public class PortfolioTwrrMonthly  implements Serializable {
    @Embeddable
    public static class PortfolioTwrrMonthlyKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "portfolioid")
        private int portfolioid;
        @Column(name = "returns_year")
        private  int returnsYear;

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

        public int getReturnsYear() {
            return returnsYear;
        }
        public void setReturnsYear(int returnsYear) {
            this.returnsYear = returnsYear;
        }
    }

    @EmbeddedId
    private PortfolioTwrrMonthlyKey key;
    @Column(name = "returns_calendar_year")
    private BigDecimal returnsCalendarYear;
    @Column(name = "returns_fin_year")
    private BigDecimal returnsFinYear;
    @Column(name = "returns_mar_ending_quarter")
    private BigDecimal returnsMarEndingQuarter;
    @Column(name = "returns_jun_ending_quarter")
    private BigDecimal returnsJunEndingQuarter;
    @Column(name = "returns_sep_ending_quarter")
    private BigDecimal returnsSepEndingQuarter;
    @Column(name = "returns_dec_ending_quarter")
    private BigDecimal returnsDecEndingQuarter;
    @Column(name = "returns_jan")
    private BigDecimal returnsJan;
    @Column(name = "returns_feb")
    private BigDecimal returnsFeb;
    @Column(name = "returns_mar")
    private BigDecimal returnsMar;
    @Column(name = "returns_apr")
    private BigDecimal returnsApr;
    @Column(name = "returns_may")
    private BigDecimal returnsMay;
    @Column(name = "returns_jun")
    private BigDecimal returnsJun;
    @Column(name = "returns_jul")
    private BigDecimal returnsJul;
    @Column(name = "returns_aug")
    private BigDecimal returnsAug;
    @Column(name = "returns_Sep")
    private BigDecimal returnsSep;
    @Column(name = "returns_oct")
    private BigDecimal returnsOct;
    @Column(name = "returns_nov")
    private BigDecimal returnsNov;
    @Column(name = "returns_dec")
    private BigDecimal returnsDec;

    public PortfolioTwrrMonthlyKey getKey() {
        return key;
    }
    public void setKey(PortfolioTwrrMonthlyKey key) {
        this.key = key;
    }

    public BigDecimal getReturnsCalendarYear() {
        return returnsCalendarYear;
    }
    public void setReturnsCalendarYear(BigDecimal returnsCalendarYear) {
        this.returnsCalendarYear = returnsCalendarYear;
    }

    public BigDecimal getReturnsFinYear() {
        return returnsFinYear;
    }
    public void setReturnsFinYear(BigDecimal returnsFinYear) {
        this.returnsFinYear = returnsFinYear;
    }

    public BigDecimal getReturnsMarEndingQuarter() {
        return returnsMarEndingQuarter;
    }
    public void setReturnsMarEndingQuarter(BigDecimal returnsMarEndingQuarter) {
        this.returnsMarEndingQuarter = returnsMarEndingQuarter;
    }

    public BigDecimal getReturnsJunEndingQuarter() {
        return returnsJunEndingQuarter;
    }
    public void setReturnsJunEndingQuarter(BigDecimal returnsJunEndingQuarter) {
        this.returnsJunEndingQuarter = returnsJunEndingQuarter;
    }

    public BigDecimal getReturnsSepEndingQuarter() {
        return returnsSepEndingQuarter;
    }
    public void setReturnsSepEndingQuarter(BigDecimal returnsSepEndingQuarter) {
        this.returnsSepEndingQuarter = returnsSepEndingQuarter;
    }

    public BigDecimal getReturnsDecEndingQuarter() {
        return returnsDecEndingQuarter;
    }
    public void setReturnsDecEndingQuarter(BigDecimal returnsDecEndingQuarter) {
        this.returnsDecEndingQuarter = returnsDecEndingQuarter;
    }

    public BigDecimal getReturnsJan() {
        return returnsJan;
    }
    public void setReturnsJan(BigDecimal returnsJan) {
        this.returnsJan = returnsJan;
    }

    public BigDecimal getReturnsFeb() {
        return returnsFeb;
    }
    public void setReturnsFeb(BigDecimal returnsFeb) {
        this.returnsFeb = returnsFeb;
    }

    public BigDecimal getReturnsMar() {
        return returnsMar;
    }
    public void setReturnsMar(BigDecimal returnsMar) {
        this.returnsMar = returnsMar;
    }

    public BigDecimal getReturnsApr() {
        return returnsApr;
    }
    public void setReturnsApr(BigDecimal returnsApr) {
        this.returnsApr = returnsApr;
    }

    public BigDecimal getReturnsMay() {
        return returnsMay;
    }
    public void setReturnsMay(BigDecimal returnsMay) {
        this.returnsMay = returnsMay;
    }

    public BigDecimal getReturnsJun() {
        return returnsJun;
    }
    public void setReturnsJun(BigDecimal returnsJun) {
        this.returnsJun = returnsJun;
    }

    public BigDecimal getReturnsJul() {
        return returnsJul;
    }
    public void setReturnsJul(BigDecimal returnsJul) {
        this.returnsJul = returnsJul;
    }

    public BigDecimal getReturnsAug() {
        return returnsAug;
    }
    public void setReturnsAug(BigDecimal returnsAug) {
        this.returnsAug = returnsAug;
    }

    public BigDecimal getReturnsSep() {
        return returnsSep;
    }
    public void setReturnsSep(BigDecimal returnsSep) {
        this.returnsSep = returnsSep;
    }

    public BigDecimal getReturnsOct() {
        return returnsOct;
    }
    public void setReturnsOct(BigDecimal returnsOct) {
        this.returnsOct = returnsOct;
    }

    public BigDecimal getReturnsNov() {
        return returnsNov;
    }
    public void setReturnsNov(BigDecimal returnsNov) {
        this.returnsNov = returnsNov;
    }

    public BigDecimal getReturnsDec() {
        return returnsDec;
    }
    public void setReturnsDec(BigDecimal returnsDec) {
        this.returnsDec = returnsDec;
    }
}
