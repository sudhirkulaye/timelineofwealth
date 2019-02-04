package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_balancesheet")
public class StockCashflow implements Serializable {
    @Embeddable
    public static class StockCashflowKey implements Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "cons_standalone")
        private String consStandalone;
        @Column(name = "date")
        private Date date;

        public String getTicker() {
            return ticker;
        }
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public String getConsStandalone() {
            return consStandalone;
        }
        public void setConsStandalone(String consStandalone) {
            this.consStandalone = consStandalone;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @EmbeddedId
    private StockCashflowKey key;
    @Column(name = "cash_from_operating_activity")
    private BigDecimal cashFromOperatingActivity;
    @Column(name = "cash_from_investing_activity")
    private BigDecimal cashFromInvestingActivity;
    @Column(name = "cash_from_financing_activity")
    private BigDecimal cashFromFinancingActivity;
    @Column(name = "net_cashflow")
    private BigDecimal netCashflow;

    public StockCashflowKey getKey() {
        return key;
    }
    public void setKey(StockCashflowKey key) {
        this.key = key;
    }

    public BigDecimal getCashFromOperatingActivity() {
        return cashFromOperatingActivity;
    }
    public void setCashFromOperatingActivity(BigDecimal cashFromOperatingActivity) {
        this.cashFromOperatingActivity = cashFromOperatingActivity;
    }

    public BigDecimal getCashFromInvestingActivity() {
        return cashFromInvestingActivity;
    }
    public void setCashFromInvestingActivity(BigDecimal cashFromInvestingActivity) {
        this.cashFromInvestingActivity = cashFromInvestingActivity;
    }

    public BigDecimal getCashFromFinancingActivity() {
        return cashFromFinancingActivity;
    }
    public void setCashFromFinancingActivity(BigDecimal cashFromFinancingActivity) {
        this.cashFromFinancingActivity = cashFromFinancingActivity;
    }

    public BigDecimal getNetCashflow() {
        return netCashflow;
    }
    public void setNetCashflow(BigDecimal netCashflow) {
        this.netCashflow = netCashflow;
    }

}
