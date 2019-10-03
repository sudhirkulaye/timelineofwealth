package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "benchmark")
public class Benchmark implements Serializable {
    @Id
    @Column(name = "benchmarkid")
    private long benchmarkid;
    @Column(name = "benchmark_name")
    private String benchmarkName;
    @Column(name = "benchmark_type")
    private String benchmarkType;
    @Column(name = "date_last_returns_process")
    private Date dateLastReturnsProcess;
    @Column(name = "is_mutual_fund")
    private String isMutualFund;

    public long getBenchmarkid() {
        return benchmarkid;
    }
    public void setBenchmarkid(long benchmarkid) {
        this.benchmarkid = benchmarkid;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }
    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public String getBenchmarkType() {
        return benchmarkType;
    }
    public void setBenchmarkType(String benchmarkType) {
        this.benchmarkType = benchmarkType;
    }

    public Date getDateLastReturnsProcess() {
        return dateLastReturnsProcess;
    }
    public void setDateLastReturnsProcess(Date dateLastReturnsProcess) {
        this.dateLastReturnsProcess = dateLastReturnsProcess;
    }

    public String getIsMutualFund() {
        return isMutualFund;
    }
    public void setIsMutualFund(String isMutualFund) {
        this.isMutualFund = isMutualFund;
    }

}
