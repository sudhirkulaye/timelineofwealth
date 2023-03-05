package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class ReportParameters implements Serializable {

    private String quarter;
    private String reportDate;
    private BigDecimal mcap;
    private BigDecimal price;
    private BigDecimal priceChange;
    private String broker;
    private String rating;
    private BigDecimal target;
    private BigDecimal targetChange;
    private BigDecimal y0Revenue;
    private BigDecimal y1Revenue;
    private BigDecimal y2Revenue;
    private BigDecimal revenueChange;
    private String y0EBIT;
    private String y1EBIT;
    private String y2EBIT;
    private String ebitChange;
    private BigDecimal y0OPM;
    private BigDecimal y1OPM;
    private BigDecimal y2OPM;
    private BigDecimal y0ROCE;
    private BigDecimal y1ROCE;
    private BigDecimal y2ROCE;
    private BigDecimal y0EVBYEBIT;
    private BigDecimal y1EVBYEBIT;
    private BigDecimal y2EVBYEBIT;
    private String analystsNames;

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public BigDecimal getMcap() {
        return mcap;
    }

    public void setMcap(BigDecimal mcap) {
        this.mcap = mcap;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public BigDecimal getTarget() {
        return target;
    }

    public void setTarget(BigDecimal target) {
        this.target = target;
    }

    public BigDecimal getTargetChange() {
        return targetChange;
    }

    public void setTargetChange(BigDecimal targetChange) {
        this.targetChange = targetChange;
    }

    public BigDecimal getY0Revenue() {
        return y0Revenue;
    }

    public void setY0Revenue(BigDecimal y0Revenue) {
        this.y0Revenue = y0Revenue;
    }

    public BigDecimal getY1Revenue() {
        return y1Revenue;
    }

    public void setY1Revenue(BigDecimal y1Revenue) {
        this.y1Revenue = y1Revenue;
    }

    public BigDecimal getY2Revenue() {
        return y2Revenue;
    }

    public void setY2Revenue(BigDecimal y2Revenue) {
        this.y2Revenue = y2Revenue;
    }

    public BigDecimal getRevenueChange() {
        return revenueChange;
    }

    public void setRevenueChange(BigDecimal revenueChange) {
        this.revenueChange = revenueChange;
    }

    public String getY0EBIT() {
        return y0EBIT;
    }

    public void setY0EBIT(String y0EBIT) {
        this.y0EBIT = y0EBIT;
    }

    public String getY1EBIT() {
        return y1EBIT;
    }

    public void setY1EBIT(String y1EBIT) {
        this.y1EBIT = y1EBIT;
    }

    public String getY2EBIT() {
        return y2EBIT;
    }

    public void setY2EBIT(String y2EBIT) {
        this.y2EBIT = y2EBIT;
    }

    public String getEbitChange() {
        return ebitChange;
    }

    public void setEbitChange(String ebitChange) {
        this.ebitChange = ebitChange;
    }

    public BigDecimal getY0OPM() {
        return y0OPM;
    }

    public void setY0OPM(BigDecimal y0OPM) {
        this.y0OPM = y0OPM;
    }

    public BigDecimal getY1OPM() {
        return y1OPM;
    }

    public void setY1OPM(BigDecimal y1OPM) {
        this.y1OPM = y1OPM;
    }

    public BigDecimal getY2OPM() {
        return y2OPM;
    }

    public void setY2OPM(BigDecimal y2OPM) {
        this.y2OPM = y2OPM;
    }

    public BigDecimal getY0ROCE() {
        return y0ROCE;
    }

    public void setY0ROCE(BigDecimal y0ROCE) {
        this.y0ROCE = y0ROCE;
    }

    public BigDecimal getY1ROCE() {
        return y1ROCE;
    }

    public void setY1ROCE(BigDecimal y1ROCE) {
        this.y1ROCE = y1ROCE;
    }

    public BigDecimal getY2ROCE() {
        return y2ROCE;
    }

    public void setY2ROCE(BigDecimal y2ROCE) {
        this.y2ROCE = y2ROCE;
    }

    public BigDecimal getY0EVBYEBIT() {
        return y0EVBYEBIT;
    }

    public void setY0EVBYEBIT(BigDecimal y0EVBYEBIT) {
        this.y0EVBYEBIT = y0EVBYEBIT;
    }

    public BigDecimal getY1EVBYEBIT() {
        return y1EVBYEBIT;
    }

    public void setY1EVBYEBIT(BigDecimal y1EVBYEBIT) {
        this.y1EVBYEBIT = y1EVBYEBIT;
    }

    public BigDecimal getY2EVBYEBIT() {
        return y2EVBYEBIT;
    }

    public void setY2EVBYEBIT(BigDecimal y2EVBYEBIT) {
        this.y2EVBYEBIT = y2EVBYEBIT;
    }

    public String getAnalystsNames() {
        return analystsNames;
    }

    public void setAnalystsNames(String analystsNames) {
        this.analystsNames = analystsNames;
    }
}
