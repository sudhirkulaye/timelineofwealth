package com.timelineofwealth.dto;

import com.timelineofwealth.entities.MutualFundUniverse;
import com.timelineofwealth.entities.Sip;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class SipForm implements Serializable {
    public static class SipKey implements Serializable {
        private long memberid;
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
    private SipForm.SipKey key;
    private String instrumentType;
    private String fundHouse;
    private String directRegular;
    private String dividendGrowth;
    private String dividendFreq;
    private long schemeCode;
    private String schemeName;
    private int assetClassid;
    private String category;
    private String equityStyleBox;
    private String debtStyleBox;
    private Date startDate;
    private Date endDate;
    private int deductionDay;
    private BigDecimal amount;
    private int sipFreq;
    private String isActive;

    public SipForm() {
        this.setKey(new SipKey());
    }
    public SipForm(Sip sipRecord, MutualFundUniverse scheme){
        this.setKey(new SipKey());
        this.getKey().setMemberid(sipRecord.getKey().getMemberid());
        this.getKey().setSipid(sipRecord.getKey().getSipid());
        this.setInstrumentType(sipRecord.getInstrumentType());
        this.setSchemeCode(sipRecord.getSchemeCode());
        this.setStartDate(sipRecord.getStartDate());
        this.setEndDate(sipRecord.getEndDate());
        this.setDeductionDay(sipRecord.getDeductionDay());
        this.setAmount(sipRecord.getAmount());
        this.setSipFreq(sipRecord.getSipFreq());
        this.setIsActive(sipRecord.getIsActive());
        this.setSchemeName(sipRecord.getSchemeName());

        if (scheme != null) {
            this.setFundHouse(scheme.getFundHouse());
            this.setDirectRegular(scheme.getDirectRegular());
            this.setDividendGrowth(scheme.getDividendGrowth());
            this.setDividendFreq(scheme.getDividendFreq());
            //SchemeNamePart to rendering small name
            this.setSchemeName(scheme.getSchemeNamePart());
            this.setAssetClassid(scheme.getAssetClassid());
            this.setCategory(scheme.getCategory());
            this.setEquityStyleBox(scheme.getEquityStyleBox());
            this.setDebtStyleBox(scheme.getDebtStyleBox());
        }

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

    public String getFundHouse() {
        return fundHouse;
    }
    public void setFundHouse(String fundHouse) {
        this.fundHouse = fundHouse;
    }

    public String getDirectRegular() {
        return directRegular;
    }
    public void setDirectRegular(String directRegular) {
        this.directRegular = directRegular;
    }

    public String getDividendGrowth() {
        return dividendGrowth;
    }
    public void setDividendGrowth(String dividendGrowth) {
        this.dividendGrowth = dividendGrowth;
    }

    public String getDividendFreq() {
        return dividendFreq;
    }
    public void setDividendFreq(String dividendFreq) {
        this.dividendFreq = dividendFreq;
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

    public int getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(int assetClassid) {
        this.assetClassid = assetClassid;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getEquityStyleBox() {
        return equityStyleBox;
    }
    public void setEquityStyleBox(String equityStyleBox) {
        this.equityStyleBox = equityStyleBox;
    }

    public String getDebtStyleBox() {
        return debtStyleBox;
    }
    public void setDebtStyleBox(String debtStyleBox) {
        this.debtStyleBox = debtStyleBox;
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
