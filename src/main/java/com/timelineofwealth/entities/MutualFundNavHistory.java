package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "mutual_fund_nav_history")
public class MutualFundNavHistory  implements Serializable {

    @Embeddable
    public static class MutualFundNavHistoryKey implements Serializable {
        @Column(name = "scheme_code")
        private long schemeCode;

        @Column(name = "date")
        private Date date;

        public MutualFundNavHistoryKey() {}
        public MutualFundNavHistoryKey(long schemeCode, Date date) {
            this.schemeCode = schemeCode;
            this.date = date;
        }

        public long getSchemeCode() {
            return schemeCode;
        }
        public void setSchemeCode(long schemeCode) {
            this.schemeCode = schemeCode;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @EmbeddedId
    private MutualFundNavHistory.MutualFundNavHistoryKey key;

    @Column(name = "nav")
    private BigDecimal nav;

    public MutualFundNavHistoryKey getKey() {
        return key;
    }
    public void setKey(MutualFundNavHistoryKey key) {
        this.key = key;
    }

    public BigDecimal getNav() {
        return nav;
    }
    public void setNav(BigDecimal nav) {
        this.nav = nav;
    }
}
