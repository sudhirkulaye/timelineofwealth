package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class FinYearProfit implements Serializable {
    private long memberid;
    private int portfolioid;
    private String finYear;
    private String longShortTerm;
    private BigDecimal netProfit;

    public String getFinYear() {
        return finYear;
    }
    public void setFinYear(String finYear) {
        this.finYear = finYear;
    }

    public String getLongShortTerm() {
        return longShortTerm;
    }
    public void setLongShortTerm(String longShortTerm) {
        this.longShortTerm = longShortTerm;
    }

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

    public BigDecimal getNetProfit() {
        return netProfit;
    }
    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }
}
