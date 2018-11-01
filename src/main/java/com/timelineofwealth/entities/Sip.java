package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "sip")
public class Sip implements Serializable {
    @Embeddable
    public static class SipKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "sipid")
        private int sipid;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public int getSipid() {
            return sipid;
        }
        public void setSipid(int sipid) {
            this.sipid = sipid;
        }
    }

    @EmbeddedId
    private Sip.SipKey key;

    @Column(name = "instrument_type")
    private String instrumentType;
    @Column(name = "scheme_code")
    private long schemeCode;
    @Column(name = "scheme_name")
    private String schemeName;
    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "end_date")
    private Date endDate;
    @Column(name = "deduction_day")
    private int deductionDay;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "sip_freq")
    private int sipFreq;
    @Column(name = "is_active")
    private String isActive;

    public Sip(){
        this.key = new Sip.SipKey();
    }

    public SipKey getKey() {
        return key;
    }
    public void setKey(SipKey key) {
        this.key = key;
    }

    public String getInstrumentType() {
        return instrumentType;
    }
    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public long getSchemeCode() {
        return schemeCode;
    }
    public void setSchemeCode(long schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }
    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getDeductionDay() {
        return deductionDay;
    }
    public void setDeductionDay(int deductionDay) {
        this.deductionDay = deductionDay;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getSipFreq() {
        return sipFreq;
    }
    public void setSipFreq(int sipFreq) {
        this.sipFreq = sipFreq;
    }

    public String getIsActive() {
        return isActive;
    }
    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
}
