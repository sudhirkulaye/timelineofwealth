package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "liability")
public class Liability implements Serializable {
    @Embeddable
    public static class LiabilityKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "loanid")
        private int loanid;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public int getLoanid() {
            return loanid;
        }
        public void setLoanid(int loanid) {
            this.loanid = loanid;
        }
    }

    @EmbeddedId
    private LiabilityKey key;

    @Column(name = "loan_desc")
    private String loanDesc;
    @Column(name = "loan_type")
    private String loanType;
    @Column(name = "disbursement_amount")
    private BigDecimal disbursementAmount;
    @Column(name = "disbursement_date")
    private Date disbursementDate;
    @Column(name = "initial_total_emis")
    private int initialTotalEmis;
    @Column(name = "first_emi_date")
    private Date firstEmiDate;
    @Column(name = "initial_emi_amount")
    private BigDecimal initialEmiAmount;
    @Column(name = "current_emi_amount")
    private BigDecimal currentEmiAmount;
    @Column(name = "current_emi_day")
    private String currentEmiDay;
    @Column(name = "last_emi_month")
    private String lastEmiMonth;
    @Column(name = "last_emi_year")
    private String lastEmiYear;
    @Column(name = "remaining_emis")
    private int remainingEmis;
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    @Column(name = "pv_outstanding_emis")
    private BigDecimal pvOutstandingEmis;
    @Column(name = "active_status")
    private String activeStatus;
    @Column(name = "date_last_update")
    private Date dateLastUpdate;

    public LiabilityKey getKey() {
        return key;
    }
    public void setKey(LiabilityKey key) {
        this.key = key;
    }

    public String getLoanDesc() {
        return loanDesc;
    }
    public void setLoanDesc(String loanDesc) {
        this.loanDesc = loanDesc;
    }

    public String getLoanType() {
        return loanType;
    }
    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getDisbursementAmount() {
        return disbursementAmount;
    }
    public void setDisbursementAmount(BigDecimal disbursementAmount) {
        this.disbursementAmount = disbursementAmount;
    }

    public Date getDisbursementDate() {
        return disbursementDate;
    }
    public void setDisbursementDate(Date disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public int getInitialTotalEmis() {
        return initialTotalEmis;
    }
    public void setInitialTotalEmis(int initialTotalEmis) {
        this.initialTotalEmis = initialTotalEmis;
    }

    public Date getFirstEmiDate() {
        return firstEmiDate;
    }
    public void setFirstEmiDate(Date firstEmiDate) {
        this.firstEmiDate = firstEmiDate;
    }

    public BigDecimal getInitialEmiAmount() {
        return initialEmiAmount;
    }
    public void setInitialEmiAmount(BigDecimal initialEmiAmount) {
        this.initialEmiAmount = initialEmiAmount;
    }

    public BigDecimal getCurrentEmiAmount() {
        return currentEmiAmount;
    }
    public void setCurrentEmiAmount(BigDecimal currentEmiAmount) {
        this.currentEmiAmount = currentEmiAmount;
    }

    public String getCurrentEmiDay() {
        return currentEmiDay;
    }
    public void setCurrentEmiDay(String currentEmiDay) {
        this.currentEmiDay = currentEmiDay;
    }

    public String getLastEmiMonth() {
        return lastEmiMonth;
    }
    public void setLastEmiMonth(String lastEmiMonth) {
        this.lastEmiMonth = lastEmiMonth;
    }

    public String getLastEmiYear() {
        return lastEmiYear;
    }
    public void setLastEmiYear(String lastEmiYear) {
        this.lastEmiYear = lastEmiYear;
    }

    public int getRemainingEmis() {
        return remainingEmis;
    }
    public void setRemainingEmis(int remainingEmis) {
        this.remainingEmis = remainingEmis;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }
    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getPvOutstandingEmis() {
        return pvOutstandingEmis;
    }
    public void setPvOutstandingEmis(BigDecimal pvOutstandingEmis) {
        this.pvOutstandingEmis = pvOutstandingEmis;
    }

    public String getActiveStatus() {
        return activeStatus;
    }
    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public Date getDateLastUpdate() {
        return dateLastUpdate;
    }
    public void setDateLastUpdate(Date dateLastUpdate) {
        this.dateLastUpdate = dateLastUpdate;
    }
}
