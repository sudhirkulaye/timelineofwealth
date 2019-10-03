package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "benchmark_twrr_summary")
public class BenchmarkTwrrSummary implements Serializable {
    @Id
    @Column(name = "benchmarkid")
    private long benchmarkid;
    @Column(name = "returns_date")
    private Date returnsDate;
    @Column(name = "returns_twrr_since_current_month")
    private BigDecimal returnsTwrrSinceCurrentMonth;
    @Column(name = "returns_twrr_since_current_quarter")
    private BigDecimal returnsTwrrSinceCurrentQuarter;
    @Column(name = "returns_twrr_since_fin_year")
    private BigDecimal returnsTwrrSinceFinYear;
    @Column(name = "returns_twrr_ytd")
    private BigDecimal returnsTwrrYtd;
    @Column(name = "returns_twrr_three_months")
    private BigDecimal returnsTwrrThreeMonths;
    @Column(name = "returns_twrr_half_year")
    private BigDecimal returnsTwrrHalfYear;
    @Column(name = "returns_twrr_one_year")
    private BigDecimal returnsTwrrOneYear;
    @Column(name = "returns_twrr_two_year")
    private BigDecimal returnsTwrrTwoYear;
    @Column(name = "returns_twrr_three_year")
    private BigDecimal returnsTwrrThreeYear;
    @Column(name = "returns_twrr_five_year")
    private BigDecimal returnsTwrrFiveYear;
    @Column(name = "returns_twrr_ten_year")
    private BigDecimal returnsTwrrTenYear;
    @Column(name = "returns_twrr_since_inception")
    private BigDecimal returnsTwrrSinceInception;

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
