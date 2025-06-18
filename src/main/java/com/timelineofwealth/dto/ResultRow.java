package com.timelineofwealth.dto;

import java.sql.Date;

public class ResultRow {
    private Date resultEndDate;
    private Date announcementDate;
    private Double shares;
    private Double ttmPat;
    private Double ttmEps;

    public Date getResultEndDate() {
        return resultEndDate;
    }

    public void setResultEndDate(Date resultEndDate) {
        this.resultEndDate = resultEndDate;
    }

    public java.sql.Date getAnnouncementDate() {
        return announcementDate;
    }

    public void setAnnouncementDate(java.sql.Date announcementDate) {
        this.announcementDate = announcementDate;
    }

    public Double getShares() {
        return shares;
    }

    public void setShares(Double shares) {
        this.shares = shares;
    }

    public Double getTtmPat() {
        return ttmPat;
    }

    public void setTtmPat(Double ttmPat) {
        this.ttmPat = ttmPat;
    }

    public Double getTtmEps() {
        return ttmEps;
    }

    public void setTtmEps(Double ttmEps) {
        this.ttmEps = ttmEps;
    }
}
