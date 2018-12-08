package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "wealth_history")
public class WealthHistory implements Serializable {
    @Embeddable
    public static class WealthHistoryKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "date")
        private Date date;

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
    }
    @EmbeddedId
    private WealthHistoryKey key;
    @Column(name = "value")
    private BigDecimal value;

    public WealthHistoryKey getKey() {
        return key;
    }
    public void setKey(WealthHistoryKey key) {
        this.key = key;
    }

    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }

}
