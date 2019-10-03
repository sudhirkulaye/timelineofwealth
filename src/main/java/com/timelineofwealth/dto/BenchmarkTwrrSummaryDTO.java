package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class BenchmarkTwrrSummaryDTO implements Serializable {
    String benchmarkType;
    String benchmarkName;
    private long benchmarkid;
    private Date returnsDate;
    private BigDecimal returnsTwrrSinceCurrentMonth;
    private BigDecimal returnsTwrrSinceCurrentQuarter;
    private BigDecimal returnsTwrrSinceFinYear;
    private BigDecimal returnsTwrrYtd;
    private BigDecimal returnsTwrrThreeMonths;
    private BigDecimal returnsTwrrHalfYear;
    private BigDecimal returnsTwrrOneYear;
    private BigDecimal returnsTwrrTwoYear;
    private BigDecimal returnsTwrrThreeYear;
    private BigDecimal returnsTwrrFiveYear;
    private BigDecimal returnsTwrrTenYear;
    private BigDecimal returnsTwrrSinceInception;

    public String getBenchmarkType() {
        return benchmarkType;
    }

    public void setBenchmarkType(String benchmarkType) {
        this.benchmarkType = benchmarkType;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public long getBenchmarkid() {
        return benchmarkid;
    }

    public void setBenchmarkid(long benchmarkid) {
        this.benchmarkid = benchmarkid;
    }

    public Date getReturnsDate() {
        return returnsDate;
    }

    public void setReturnsDate(Date returnsDate) {
        this.returnsDate = returnsDate;
    }

    public BigDecimal getReturnsTwrrSinceCurrentMonth() {
        return returnsTwrrSinceCurrentMonth;
    }

    public void setReturnsTwrrSinceCurrentMonth(BigDecimal returnsTwrrSinceCurrentMonth) {
        this.returnsTwrrSinceCurrentMonth = returnsTwrrSinceCurrentMonth;
    }

    public BigDecimal getReturnsTwrrSinceCurrentQuarter() {
        return returnsTwrrSinceCurrentQuarter;
    }

    public void setReturnsTwrrSinceCurrentQuarter(BigDecimal returnsTwrrSinceCurrentQuarter) {
        this.returnsTwrrSinceCurrentQuarter = returnsTwrrSinceCurrentQuarter;
    }

    public BigDecimal getReturnsTwrrSinceFinYear() {
        return returnsTwrrSinceFinYear;
    }

    public void setReturnsTwrrSinceFinYear(BigDecimal returnsTwrrSinceFinYear) {
        this.returnsTwrrSinceFinYear = returnsTwrrSinceFinYear;
    }

    public BigDecimal getReturnsTwrrYtd() {
        return returnsTwrrYtd;
    }

    public void setReturnsTwrrYtd(BigDecimal returnsTwrrYtd) {
        this.returnsTwrrYtd = returnsTwrrYtd;
    }

    public BigDecimal getReturnsTwrrThreeMonths() {
        return returnsTwrrThreeMonths;
    }

    public void setReturnsTwrrThreeMonths(BigDecimal returnsTwrrThreeMonths) {
        this.returnsTwrrThreeMonths = returnsTwrrThreeMonths;
    }

    public BigDecimal getReturnsTwrrHalfYear() {
        return returnsTwrrHalfYear;
    }

    public void setReturnsTwrrHalfYear(BigDecimal returnsTwrrHalfYear) {
        this.returnsTwrrHalfYear = returnsTwrrHalfYear;
    }

    public BigDecimal getReturnsTwrrOneYear() {
        return returnsTwrrOneYear;
    }

    public void setReturnsTwrrOneYear(BigDecimal returnsTwrrOneYear) {
        this.returnsTwrrOneYear = returnsTwrrOneYear;
    }

    public BigDecimal getReturnsTwrrTwoYear() {
        return returnsTwrrTwoYear;
    }

    public void setReturnsTwrrTwoYear(BigDecimal returnsTwrrTwoYear) {
        this.returnsTwrrTwoYear = returnsTwrrTwoYear;
    }

    public BigDecimal getReturnsTwrrThreeYear() {
        return returnsTwrrThreeYear;
    }

    public void setReturnsTwrrThreeYear(BigDecimal returnsTwrrThreeYear) {
        this.returnsTwrrThreeYear = returnsTwrrThreeYear;
    }

    public BigDecimal getReturnsTwrrFiveYear() {
        return returnsTwrrFiveYear;
    }

    public void setReturnsTwrrFiveYear(BigDecimal returnsTwrrFiveYear) {
        this.returnsTwrrFiveYear = returnsTwrrFiveYear;
    }

    public BigDecimal getReturnsTwrrTenYear() {
        return returnsTwrrTenYear;
    }

    public void setReturnsTwrrTenYear(BigDecimal returnsTwrrTenYear) {
        this.returnsTwrrTenYear = returnsTwrrTenYear;
    }

    public BigDecimal getReturnsTwrrSinceInception() {
        return returnsTwrrSinceInception;
    }

    public void setReturnsTwrrSinceInception(BigDecimal returnsTwrrSinceInception) {
        this.returnsTwrrSinceInception = returnsTwrrSinceInception;
    }
}
