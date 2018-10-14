package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "insurance")
public class Insurance implements Serializable {
    @Embeddable
    public static class InsuranceKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "insuranceid")
        private int insuranceid;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public int getInsuranceid() {
            return insuranceid;
        }
        public void setInsuranceid(int insuranceid) {
            this.insuranceid = insuranceid;
        }
    }

    @EmbeddedId
    private InsuranceKey key;

    @Column(name = "product_UIN")
    private String productUIN;
    @Column(name = "product_name")
    private String productName;
    @Column(name = "category")
    private String category;
    @Column(name = "cover_amount ")
    private BigDecimal coverAmount;
    @Column(name = "premium_amount")
    private BigDecimal premiumAmount;
    @Column(name = "premium_frequency_in_months")
    private int premiumFrequencyInMonths;
    @Column(name = "last_date_premium_paid")
    private Date lastDatePremiumPaid;
    @Column(name = "life_time_cover")
    private String lifeTimeCover;
    @Column(name = "expiry_date")
    private Date expiryDate;
    @Column(name = "maturity_amount")
    private BigDecimal maturityAmount;
    @Column(name = "maturity_frequency")
    private int maturityFrequency;
    @Column(name = "expected_bonus_amount")
    private BigDecimal expectedBonusAmount;
    @Column(name = "date_last_update")
    private Date dateLastUpdate;

    public InsuranceKey getKey() {
        return key;
    }
    public void setKey(InsuranceKey key) {
        this.key = key;
    }

    public String getProductUIN() {
        return productUIN;
    }
    public void setProductUIN(String productUIN) {
        this.productUIN = productUIN;
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getCoverAmount() {
        return coverAmount;
    }
    public void setCoverAmount(BigDecimal coverAmount) {
        this.coverAmount = coverAmount;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }
    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public int getPremiumFrequencyInMonths() {
        return premiumFrequencyInMonths;
    }
    public void setPremiumFrequencyInMonths(int premiumFrequencyInMonths) {
        this.premiumFrequencyInMonths = premiumFrequencyInMonths;
    }

    public Date getLastDatePremiumPaid() {
        return lastDatePremiumPaid;
    }
    public void setLastDatePremiumPaid(Date lastDatePremiumPaid) {
        this.lastDatePremiumPaid = lastDatePremiumPaid;
    }

    public String getLifeTimeCover() {
        return lifeTimeCover;
    }
    public void setLifeTimeCover(String lifeTimeCover) {
        this.lifeTimeCover = lifeTimeCover;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getMaturityAmount() {
        return maturityAmount;
    }
    public void setMaturityAmount(BigDecimal maturityAmount) {
        this.maturityAmount = maturityAmount;
    }

    public int getMaturityFrequency() {
        return maturityFrequency;
    }
    public void setMaturityFrequency(int maturityFrequency) {
        this.maturityFrequency = maturityFrequency;
    }

    public BigDecimal getExpectedBonusAmount() {
        return expectedBonusAmount;
    }
    public void setExpectedBonusAmount(BigDecimal expectedBonusAmount) {
        this.expectedBonusAmount = expectedBonusAmount;
    }

    public Date getDateLastUpdate() {
        return dateLastUpdate;
    }
    public void setDateLastUpdate(Date dateLastUpdate) {
        this.dateLastUpdate = dateLastUpdate;
    }
}
