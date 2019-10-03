package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class BenchmarkTwrrMonthlyDTO implements Serializable {
    String benchmarkType;
    String benchmarkName;
    private long benchmarkid;
    private  int year;
    private BigDecimal returnsCalendarYear;
    private BigDecimal returnsFinYear;
    private BigDecimal returnsMarEndingQuarter;
    private BigDecimal returnsJunEndingQuarter;
    private BigDecimal returnsSepEndingQuarter;
    private BigDecimal returnsDecEndingQuarter;
    private BigDecimal returnsJan;
    private BigDecimal returnsFeb;
    private BigDecimal returnsMar;
    private BigDecimal returnsApr;
    private BigDecimal returnsMay;
    private BigDecimal returnsJun;
    private BigDecimal returnsJul;
    private BigDecimal returnsAug;
    private BigDecimal returnsSep;
    private BigDecimal returnsOct;
    private BigDecimal returnsNov;
    private BigDecimal returnsDec;

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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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
