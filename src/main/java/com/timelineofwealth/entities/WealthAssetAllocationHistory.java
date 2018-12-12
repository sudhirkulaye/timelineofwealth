package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "wealth_asset_allocation_history")
public class WealthAssetAllocationHistory implements Serializable {
    @Embeddable
    public static class WealthAssetAllocationHistoryKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "date")
        private Date date;
        @Column(name = "asset_class_group")
        private String assetClassGroup;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date buyDate) {
            this.date = buyDate;
        }

        public String getAssetClassGroup() {
            return assetClassGroup;
        }
        public void setAssetClassGroup(String assetClassGroup) {
            this.assetClassGroup = assetClassGroup;
        }
    }
    @EmbeddedId
    private WealthAssetAllocationHistoryKey key;
    @Column(name = "value")
    private BigDecimal value;
    @Column(name = "value_percent")
    private BigDecimal valuePercent;

    public WealthAssetAllocationHistoryKey getKey() {
        return key;
    }
    public void setKey(WealthAssetAllocationHistoryKey key) {
        this.key = key;
    }

    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValuePercent() {
        return valuePercent;
    }
    public void setValuePercent(BigDecimal valuePercent) {
        this.valuePercent = valuePercent;
    }

}
