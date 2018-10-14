package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "liquidity")
public class Liquidity implements Serializable {
    @Embeddable
    public static class LiquidityKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "liquidityid")
        private int liquidityid;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public int getLiquidityid() {
            return liquidityid;
        }
        public void setLiquidityid(int liquidityid) {
            this.liquidityid = liquidityid;
        }
    }

    @EmbeddedId
    private Liquidity.LiquidityKey key;

    @Column(name = "liquidity_desc")
    private String liquidityDesc;
    @Column(name = "priority")
    private String priority;
    @Column(name = "expected_start_date")
    private Date expectedStartDate;
    @Column(name = "amount_required_start_date")
    private BigDecimal amountRequiredStartDate;
    @Column(name = "frequency")
    private int frequency;
    @Column(name = "expected_end_date")
    private Date expectedEndDate;
    @Column(name = "date_last_update")
    private Date  dateLastUpdate;

    public LiquidityKey getKey() {
        return key;
    }
    public void setKey(LiquidityKey key) {
        this.key = key;
    }

    public String getLiquidityDesc() {
        return liquidityDesc;
    }
    public void setLiquidityDesc(String liquidityDesc) {
        this.liquidityDesc = liquidityDesc;
    }

    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getExpectedStartDate() {
        return expectedStartDate;
    }
    public void setExpectedStartDate(Date expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }

    public BigDecimal getAmountRequiredStartDate() {
        return amountRequiredStartDate;
    }
    public void setAmountRequiredStartDate(BigDecimal amountRequiredStartDate) {
        this.amountRequiredStartDate = amountRequiredStartDate;
    }

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Date getExpectedEndDate() {
        return expectedEndDate;
    }
    public void setExpectedEndDate(Date expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public Date getDateLastUpdate() {
        return dateLastUpdate;
    }
    public void setDateLastUpdate(Date dateLastUpdate) {
        this.dateLastUpdate = dateLastUpdate;
    }
}
