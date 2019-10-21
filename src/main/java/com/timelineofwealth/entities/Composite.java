package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "composite")
public class Composite  implements Serializable {
    @Id
    @Column(name = "compositeid")
    private long compositeid;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "min_size")
    private BigDecimal minSize;
    @Column(name = "benchmarkid")
    private int benchmarkid;
    @Column(name = "asset_classid")
    private int assetClassid;
    @Column(name = "amc_name")
    private String amcName;
    @Column(name = "fund_manager_email")
    private String fundManagerEmail;
    @Column(name = "adviser_memberid")
    private long adviserMemberid;
    @Column(name = "portfolioid")
    private int portfolioid;

    public long getCompositeid() {
        return compositeid;
    }
    public void setCompositeid(long compositeid) {
        this.compositeid = compositeid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMinSize() {
        return minSize;
    }
    public void setMinSize(BigDecimal minSize) {
        this.minSize = minSize;
    }

    public int getBenchmarkid() {
        return benchmarkid;
    }
    public void setBenchmarkid(int benchmarkid) {
        this.benchmarkid = benchmarkid;
    }

    public int getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(int assetClassid) {
        this.assetClassid = assetClassid;
    }

    public String getAmcName() {
        return amcName;
    }
    public void setAmcName(String amcName) {
        this.amcName = amcName;
    }

    public String getFundManagerEmail() {
        return fundManagerEmail;
    }
    public void setFundManagerEmail(String fundManagerEmail) {
        this.fundManagerEmail = fundManagerEmail;
    }

    public long getAdviserMemberid() {
        return adviserMemberid;
    }
    public void setAdviserMemberid(long adviserMemberid) {
        this.adviserMemberid = adviserMemberid;
    }

    public int getPortfolioid() {
        return portfolioid;
    }
    public void setPortfolioid(int portfolioid) {
        this.portfolioid = portfolioid;
    }
}
