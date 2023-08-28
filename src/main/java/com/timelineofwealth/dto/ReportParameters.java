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
    private BigDecimal y0AUM;
    private BigDecimal y1AUM;
    private BigDecimal y2AUM;
    private BigDecimal y0CreditCost;
    private BigDecimal y1CreditCost;
    private BigDecimal y2CreditCost;
    private BigDecimal y0GNPA;
    private BigDecimal y1GNPA;
    private BigDecimal y2GNPA;
    private BigDecimal y0NNPA;
    private BigDecimal y1NNPA;
    private BigDecimal y2NNPA;
    private String analystsNames;

    public ReportParameters(){
        this.quarter = "";
        this.reportDate = "";
        this.mcap = new BigDecimal("0");
        this.price = new BigDecimal("0");
        this.priceChange = new BigDecimal("0");
        this.broker = "";
        this.rating = "";
        this.target = new BigDecimal("0");
        this.targetChange = new BigDecimal("0");
        this.y0Revenue = new BigDecimal("0");
        this.y1Revenue = new BigDecimal("0");
        this.y2Revenue = new BigDecimal("0");
        this.revenueChange = new BigDecimal("0");
        this.y0EBIT = null;
        this.y1EBIT = null;
        this.y2EBIT = null;
        this.ebitChange = "";
        this.y0OPM = new BigDecimal("0");
        this.y1OPM = new BigDecimal("0");
        this.y2OPM = new BigDecimal("0");
        this.y0ROCE = new BigDecimal("0");
        this.y1ROCE = new BigDecimal("0");
        this.y2ROCE = new BigDecimal("0");
        this.y0EVBYEBIT = new BigDecimal("0");
        this.y1EVBYEBIT = new BigDecimal("0");
        this.y2EVBYEBIT = new BigDecimal("0");
        this.analystsNames = "";

    }

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

    public BigDecimal getY0AUM() {
        return y0AUM;
    }

    public void setY0AUM(BigDecimal y0AUM) {
        this.y0AUM = y0AUM;
    }

    public BigDecimal getY1AUM() {
        return y1AUM;
    }

    public void setY1AUM(BigDecimal y1AUM) {
        this.y1AUM = y1AUM;
    }

    public BigDecimal getY2AUM() {
        return y2AUM;
    }

    public void setY2AUM(BigDecimal y2AUM) {
        this.y2AUM = y2AUM;
    }

    public BigDecimal getY0CreditCost() {
        return y0CreditCost;
    }

    public void setY0CreditCost(BigDecimal y0CreditCost) {
        this.y0CreditCost = y0CreditCost;
    }

    public BigDecimal getY1CreditCost() {
        return y1CreditCost;
    }

    public void setY1CreditCost(BigDecimal y1CreditCost) {
        this.y1CreditCost = y1CreditCost;
    }

    public BigDecimal getY2CreditCost() {
        return y2CreditCost;
    }

    public void setY2CreditCost(BigDecimal y2CreditCost) {
        this.y2CreditCost = y2CreditCost;
    }

    public BigDecimal getY0GNPA() {
        return y0GNPA;
    }

    public void setY0GNPA(BigDecimal y0GNPA) {
        this.y0GNPA = y0GNPA;
    }

    public BigDecimal getY1GNPA() {
        return y1GNPA;
    }

    public void setY1GNPA(BigDecimal y1GNPA) {
        this.y1GNPA = y1GNPA;
    }

    public BigDecimal getY2GNPA() {
        return y2GNPA;
    }

    public void setY2GNPA(BigDecimal y2GNPA) {
        this.y2GNPA = y2GNPA;
    }

    public BigDecimal getY0NNPA() {
        return y0NNPA;
    }

    public void setY0NNPA(BigDecimal y0NNPA) {
        this.y0NNPA = y0NNPA;
    }

    public BigDecimal getY1NNPA() {
        return y1NNPA;
    }

    public void setY1NNPA(BigDecimal y1NNPA) {
        this.y1NNPA = y1NNPA;
    }

    public BigDecimal getY2NNPA() {
        return y2NNPA;
    }

    public void setY2NNPA(BigDecimal y2NNPA) {
        this.y2NNPA = y2NNPA;
    }
}
