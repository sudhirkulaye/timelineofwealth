package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "index_statistics")
public class IndexStatistics implements Serializable{
    @Id
    @Column(name = "ticker")
    private String ticker;
    @Column(name = "mean_returns_1yr")
    private BigDecimal meanReturns1yr;
    @Column(name = "median_returns_1yr")
    private BigDecimal medianReturns1yr;
    @Column(name = "mean_returns_3yr")
    private BigDecimal meanReturns3yr;
    @Column(name = "median_returns_3yr")
    private BigDecimal medianReturns3yr;
    @Column(name = "mean_returns_5yr")
    private BigDecimal meanReturns5yr;
    @Column(name = "median_returns_5yr")
    private BigDecimal medianReturns5yr;
    @Column(name = "mean_returns_10yr")
    private BigDecimal meanReturns10yr;
    @Column(name = "median_returns_10yr")
    private BigDecimal medianReturns10yr;
    @Column(name = "minimum_returns_1yr")
    private BigDecimal minimumReturns1yr;
    @Column(name = "minimum_returns_1yr_duration")
    private String minimumReturns1yrDuration;
    @Column(name = "maximum_returns_1yr")
    private BigDecimal maximumReturns1yr;
    @Column(name = "maximum_returns_1yr_duration")
    private String maximumReturns1yrDuration;
    @Column(name = "minimum_returns_3yr")
    private BigDecimal minimumReturns3yr;
    @Column(name = "minimum_returns_3yr_duration")
    private String minimumReturns3yrDuration;
    @Column(name = "maximum_returns_3yr")
    private BigDecimal maximumReturns3yr;
    @Column(name = "maximum_returns_3yr_duration")
    private String maximumReturns3yrDuration;
    @Column(name = "minimum_returns_5yr")
    private BigDecimal minimumReturns5yr;
    @Column(name = "minimum_returns_5yr_duration")
    private String minimumReturns5yrDuration;
    @Column(name = "maximum_returns_5yr")
    private BigDecimal maximumReturns5yr;
    @Column(name = "maximum_returns_5yr_duration")
    private String maximumReturns5yrDuration;
    @Column(name = "minimum_returns_10yr")
    private BigDecimal minimumReturns10yr;
    @Column(name = "minimum_returns_10yr_duration")
    private String minimumReturns10yrDuration;
    @Column(name = "maximum_returns_10yr")
    private BigDecimal maximumReturns10yr;
    @Column(name = "maximum_returns_10yr_duration")
    private String maximumReturns10yrDuration;
    @Column(name = "standard_deviation_1yr")
    private BigDecimal standardDeviation1yr;
    @Column(name = "standard_deviation_3yr")
    private BigDecimal standardDeviation3yr;
    @Column(name = "standard_deviation_5yr")
    private BigDecimal standardDeviation5yr;
    @Column(name = "standard_deviation_10yr")
    private BigDecimal standardDeviation10yr;
    @Column(name = "mean_pe_1yr")
    private BigDecimal meanPe1yr;
    @Column(name = "median_pe_1yr")
    private BigDecimal medianPe1yr;
    @Column(name = "minimum_pe_1yr")
    private BigDecimal minimumPe1yr;
    @Column(name = "maximum_pe_1yr")
    private BigDecimal maximumPe1yr;
    @Column(name = "mean_pe_3yr")
    private BigDecimal meanPe3yr;
    @Column(name = "median_pe_3yr")
    private BigDecimal medianPe3yr;
    @Column(name = "minimum_pe_3yr")
    private BigDecimal minimumPe3yr;
    @Column(name = "maximum_pe_3yr")
    private BigDecimal maximumPe3yr;
    @Column(name = "mean_pe_5yr")
    private BigDecimal meanPe5yr;
    @Column(name = "median_pe_5yr")
    private BigDecimal medianPe5yr;
    @Column(name = "minimum_pe_5yr")
    private BigDecimal minimumPe5yr;
    @Column(name = "maximum_pe_5yr")
    private BigDecimal maximumPe5yr;
    @Column(name = "mean_pe_10yr")
    private BigDecimal meanPe10yr;
    @Column(name = "median_pe_10yr")
    private BigDecimal medianPe10yr;
    @Column(name = "minimum_pe_10yr")
    private BigDecimal minimumPe10yr;
    @Column(name = "maximum_pe_10yr")
    private BigDecimal maximumPe10yr;
    @Column(name = "mean_pe")
    private BigDecimal meanPe;
    @Column(name = "median_pe")
    private BigDecimal medianPe;
    @Column(name = "minimum_pe")
    private BigDecimal minimumPe;
    @Column(name = "maximum_pe")
    private BigDecimal maximumPe;
    @Column(name = "current_pe")
    private BigDecimal currentPe;
    @Column(name = "last_updated")
    private Date lastUpdated;

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public BigDecimal getMeanReturns1yr() {
        return meanReturns1yr;
    }
    public void setMeanReturns1yr(BigDecimal meanReturns1yr) {
        this.meanReturns1yr = meanReturns1yr;
    }

    public BigDecimal getMedianReturns1yr() {
        return medianReturns1yr;
    }
    public void setMedianReturns1yr(BigDecimal medianReturns1yr) {
        this.medianReturns1yr = medianReturns1yr;
    }

    public BigDecimal getMeanReturns3yr() {
        return meanReturns3yr;
    }
    public void setMeanReturns3yr(BigDecimal meanReturns3yr) {
        this.meanReturns3yr = meanReturns3yr;
    }

    public BigDecimal getMedianReturns3yr() {
        return medianReturns3yr;
    }
    public void setMedianReturns3yr(BigDecimal medianReturns3yr) {
        this.medianReturns3yr = medianReturns3yr;
    }

    public BigDecimal getMeanReturns5yr() {
        return meanReturns5yr;
    }
    public void setMeanReturns5yr(BigDecimal meanReturns5yr) {
        this.meanReturns5yr = meanReturns5yr;
    }

    public BigDecimal getMedianReturns5yr() {
        return medianReturns5yr;
    }
    public void setMedianReturns5yr(BigDecimal medianReturns5yr) {
        this.medianReturns5yr = medianReturns5yr;
    }

    public BigDecimal getMeanReturns10yr() {
        return meanReturns10yr;
    }
    public void setMeanReturns10yr(BigDecimal meanReturns10yr) {
        this.meanReturns10yr = meanReturns10yr;
    }

    public BigDecimal getMedianReturns10yr() {
        return medianReturns10yr;
    }
    public void setMedianReturns10yr(BigDecimal medianReturns10yr) {
        this.medianReturns10yr = medianReturns10yr;
    }

    public BigDecimal getMinimumReturns1yr() {
        return minimumReturns1yr;
    }
    public void setMinimumReturns1yr(BigDecimal minimumReturns1yr) {
        this.minimumReturns1yr = minimumReturns1yr;
    }

    public String getMinimumReturns1yrDuration() {
        return minimumReturns1yrDuration;
    }
    public void setMinimumReturns1yrDuration(String minimumReturns1yrDuration) {
        this.minimumReturns1yrDuration = minimumReturns1yrDuration;
    }

    public BigDecimal getMaximumReturns1yr() {
        return maximumReturns1yr;
    }
    public void setMaximumReturns1yr(BigDecimal maximumReturns1yr) {
        this.maximumReturns1yr = maximumReturns1yr;
    }

    public String getMaximumReturns1yrDuration() {
        return maximumReturns1yrDuration;
    }
    public void setMaximumReturns1yrDuration(String maximumReturns1yrDuration) {
        this.maximumReturns1yrDuration = maximumReturns1yrDuration;
    }

    public BigDecimal getMinimumReturns3yr() {
        return minimumReturns3yr;
    }
    public void setMinimumReturns3yr(BigDecimal minimumReturns3yr) {
        this.minimumReturns3yr = minimumReturns3yr;
    }

    public String getMinimumReturns3yrDuration() {
        return minimumReturns3yrDuration;
    }
    public void setMinimumReturns3yrDuration(String minimumReturns3yrDuration) {
        this.minimumReturns3yrDuration = minimumReturns3yrDuration;
    }

    public BigDecimal getMaximumReturns3yr() {
        return maximumReturns3yr;
    }
    public void setMaximumReturns3yr(BigDecimal maximumReturns3yr) {
        this.maximumReturns3yr = maximumReturns3yr;
    }

    public String getMaximumReturns3yrDuration() {
        return maximumReturns3yrDuration;
    }
    public void setMaximumReturns3yrDuration(String maximumReturns3yrDuration) {
        this.maximumReturns3yrDuration = maximumReturns3yrDuration;
    }

    public BigDecimal getMinimumReturns5yr() {
        return minimumReturns5yr;
    }
    public void setMinimumReturns5yr(BigDecimal minimumReturns5yr) {
        this.minimumReturns5yr = minimumReturns5yr;
    }

    public String getMinimumReturns5yrDuration() {
        return minimumReturns5yrDuration;
    }
    public void setMinimumReturns5yrDuration(String minimumReturns5yrDuration) {
        this.minimumReturns5yrDuration = minimumReturns5yrDuration;
    }

    public BigDecimal getMaximumReturns5yr() {
        return maximumReturns5yr;
    }
    public void setMaximumReturns5yr(BigDecimal maximumReturns5yr) {
        this.maximumReturns5yr = maximumReturns5yr;
    }

    public String getMaximumReturns5yrDuration() {
        return maximumReturns5yrDuration;
    }
    public void setMaximumReturns5yrDuration(String maximumReturns5yrDuration) {
        this.maximumReturns5yrDuration = maximumReturns5yrDuration;
    }

    public BigDecimal getMinimumReturns10yr() {
        return minimumReturns10yr;
    }
    public void setMinimumReturns10yr(BigDecimal minimumReturns10yr) {
        this.minimumReturns10yr = minimumReturns10yr;
    }

    public String getMinimumReturns10yrDuration() {
        return minimumReturns10yrDuration;
    }
    public void setMinimumReturns10yrDuration(String minimumReturns10yrDuration) {
        this.minimumReturns10yrDuration = minimumReturns10yrDuration;
    }

    public BigDecimal getMaximumReturns10yr() {
        return maximumReturns10yr;
    }
    public void setMaximumReturns10yr(BigDecimal maximumReturns10yr) {
        this.maximumReturns10yr = maximumReturns10yr;
    }

    public String getMaximumReturns10yrDuration() {
        return maximumReturns10yrDuration;
    }
    public void setMaximumReturns10yrDuration(String maximumReturns10yrDuration) {
        this.maximumReturns10yrDuration = maximumReturns10yrDuration;
    }

    public BigDecimal getStandardDeviation1yr() {
        return standardDeviation1yr;
    }
    public void setStandardDeviation1yr(BigDecimal standardDeviation1yr) {
        this.standardDeviation1yr = standardDeviation1yr;
    }

    public BigDecimal getStandardDeviation3yr() {
        return standardDeviation3yr;
    }
    public void setStandardDeviation3yr(BigDecimal standardDeviation3yr) {
        this.standardDeviation3yr = standardDeviation3yr;
    }

    public BigDecimal getStandardDeviation5yr() {
        return standardDeviation5yr;
    }
    public void setStandardDeviation5yr(BigDecimal standardDeviation5yr) {
        this.standardDeviation5yr = standardDeviation5yr;
    }

    public BigDecimal getStandardDeviation10yr() {
        return standardDeviation10yr;
    }
    public void setStandardDeviation10yr(BigDecimal standardDeviation10yr) {
        this.standardDeviation10yr = standardDeviation10yr;
    }

    public BigDecimal getMeanPe1yr() {
        return meanPe1yr;
    }
    public void setMeanPe1yr(BigDecimal meanPe1yr) {
        this.meanPe1yr = meanPe1yr;
    }

    public BigDecimal getMedianPe1yr() {
        return medianPe1yr;
    }
    public void setMedianPe1yr(BigDecimal medianPe1yr) {
        this.medianPe1yr = medianPe1yr;
    }

    public BigDecimal getMinimumPe1yr() {
        return minimumPe1yr;
    }
    public void setMinimumPe1yr(BigDecimal minimumPe1yr) {
        this.minimumPe1yr = minimumPe1yr;
    }

    public BigDecimal getMaximumPe1yr() {
        return maximumPe1yr;
    }
    public void setMaximumPe1yr(BigDecimal maximumPe1yr) {
        this.maximumPe1yr = maximumPe1yr;
    }

    public BigDecimal getMeanPe3yr() {
        return meanPe3yr;
    }
    public void setMeanPe3yr(BigDecimal meanPe3yr) {
        this.meanPe3yr = meanPe3yr;
    }

    public BigDecimal getMedianPe3yr() {
        return medianPe3yr;
    }
    public void setMedianPe3yr(BigDecimal medianPe3yr) {
        this.medianPe3yr = medianPe3yr;
    }

    public BigDecimal getMinimumPe3yr() {
        return minimumPe3yr;
    }
    public void setMinimumPe3yr(BigDecimal minimumPe3yr) {
        this.minimumPe3yr = minimumPe3yr;
    }

    public BigDecimal getMaximumPe3yr() {
        return maximumPe3yr;
    }
    public void setMaximumPe3yr(BigDecimal maximumPe3yr) {
        this.maximumPe3yr = maximumPe3yr;
    }

    public BigDecimal getMeanPe5yr() {
        return meanPe5yr;
    }
    public void setMeanPe5yr(BigDecimal meanPe5yr) {
        this.meanPe5yr = meanPe5yr;
    }

    public BigDecimal getMedianPe5yr() {
        return medianPe5yr;
    }
    public void setMedianPe5yr(BigDecimal medianPe5yr) {
        this.medianPe5yr = medianPe5yr;
    }

    public BigDecimal getMinimumPe5yr() {
        return minimumPe5yr;
    }
    public void setMinimumPe5yr(BigDecimal minimumPe5yr) {
        this.minimumPe5yr = minimumPe5yr;
    }

    public BigDecimal getMaximumPe5yr() {
        return maximumPe5yr;
    }
    public void setMaximumPe5yr(BigDecimal maximumPe5yr) {
        this.maximumPe5yr = maximumPe5yr;
    }

    public BigDecimal getMeanPe10yr() {
        return meanPe10yr;
    }
    public void setMeanPe10yr(BigDecimal meanPe10yr) {
        this.meanPe10yr = meanPe10yr;
    }

    public BigDecimal getMedianPe10yr() {
        return medianPe10yr;
    }
    public void setMedianPe10yr(BigDecimal medianPe10yr) {
        this.medianPe10yr = medianPe10yr;
    }

    public BigDecimal getMinimumPe10yr() {
        return minimumPe10yr;
    }
    public void setMinimumPe10yr(BigDecimal minimumPe10yr) {
        this.minimumPe10yr = minimumPe10yr;
    }

    public BigDecimal getMaximumPe10yr() {
        return maximumPe10yr;
    }
    public void setMaximumPe10yr(BigDecimal maximumPe10yr) {
        this.maximumPe10yr = maximumPe10yr;
    }

    public BigDecimal getMeanPe() {
        return meanPe;
    }
    public void setMeanPe(BigDecimal meanPe) {
        this.meanPe = meanPe;
    }

    public BigDecimal getMedianPe() {
        return medianPe;
    }
    public void setMedianPe(BigDecimal medianPe) {
        this.medianPe = medianPe;
    }

    public BigDecimal getMinimumPe() {
        return minimumPe;
    }
    public void setMinimumPe(BigDecimal minimumPe) {
        this.minimumPe = minimumPe;
    }

    public BigDecimal getMaximumPe() {
        return maximumPe;
    }
    public void setMaximumPe(BigDecimal maximumPe) {
        this.maximumPe = maximumPe;
    }

    public BigDecimal getCurrentPe() {
        return currentPe;
    }
    public void setCurrentPe(BigDecimal currentPe) {
        this.currentPe = currentPe;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
