package com.timelineofwealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class IndexReturnsDTO  implements Serializable {
    private String ticker;
    private Date fromDate;
    private Date toDate;
    private BigDecimal periodReturns;

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Date getFromDate() {
        return fromDate;
    }
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public BigDecimal getPeriodReturns() {
        return periodReturns;
    }
    public void setPeriodReturns(BigDecimal periodReturns) {
        this.periodReturns = periodReturns;
    }
}
