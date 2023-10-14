package com.timelineofwealth.dto;

import java.math.BigDecimal;

public class BSEIndexDataEntry {
    private int Day;
    private String Month;
    private String year;
    private String Turnover;
    private String tdate;
    private String I_name;
    private BigDecimal I_open;
    private BigDecimal I_high;
    private BigDecimal I_low;
    private BigDecimal I_close;
    private BigDecimal I_pe;
    private BigDecimal I_pb;
    private BigDecimal I_yl;
    private String Turnover_1;
    private String TOTAL_SHARES_TRADED;

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }

    public String getMonth() {
        return Month;
    }

    public void setMonth(String month) {
        Month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTurnover() {
        return Turnover;
    }

    public void setTurnover(String turnover) {
        Turnover = turnover;
    }

    public String getTdate() {
        return tdate;
    }

    public void setTdate(String tdate) {
        this.tdate = tdate;
    }

    public String getI_name() {
        return I_name;
    }

    public void setI_name(String i_name) {
        I_name = i_name;
    }

    public BigDecimal getI_open() {
        return I_open;
    }

    public void setI_open(BigDecimal i_open) {
        I_open = i_open;
    }

    public BigDecimal getI_high() {
        return I_high;
    }

    public void setI_high(BigDecimal i_high) {
        I_high = i_high;
    }

    public BigDecimal getI_low() {
        return I_low;
    }

    public void setI_low(BigDecimal i_low) {
        I_low = i_low;
    }

    public BigDecimal getI_close() {
        return I_close;
    }

    public void setI_close(BigDecimal i_close) {
        I_close = i_close;
    }

    public BigDecimal getI_pe() {
        return I_pe;
    }

    public void setI_pe(BigDecimal i_pe) {
        I_pe = i_pe;
    }

    public BigDecimal getI_pb() {
        return I_pb;
    }

    public void setI_pb(BigDecimal i_pb) {
        I_pb = i_pb;
    }

    public BigDecimal getI_yl() {
        return I_yl;
    }

    public void setI_yl(BigDecimal i_yl) {
        I_yl = i_yl;
    }

    public String getTurnover_1() {
        return Turnover_1;
    }

    public void setTurnover_1(String turnover_1) {
        Turnover_1 = turnover_1;
    }

    public String getTOTAL_SHARES_TRADED() {
        return TOTAL_SHARES_TRADED;
    }

    public void setTOTAL_SHARES_TRADED(String TOTAL_SHARES_TRADED) {
        this.TOTAL_SHARES_TRADED = TOTAL_SHARES_TRADED;
    }
}
