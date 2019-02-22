package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_quarter")
public class StockQuarter implements Serializable {
    @Embeddable
    public static class StockQuarterKey implements Serializable {
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
    private StockQuarterKey key;
    @Column(name = "sales")
    private BigDecimal sales;
    @Column(name = "expenses")
    private BigDecimal expenses;
    @Column(name = "operating_profit")
    private BigDecimal operatingProfit;
    @Column(name = "other_income")
    private BigDecimal otherIncome;
    @Column(name = "depreciation")
    private BigDecimal depreciation;
    @Column(name = "interest")
    private BigDecimal interest;
    @Column(name = "profit_before_tax")
    private BigDecimal profitBeforeTax;
    @Column(name = "tax")
    private BigDecimal tax;
    @Column(name = "net_profit")
    private BigDecimal netProfit;
    @Column(name = "dummy1")
    private BigDecimal dummy1;
    @Column(name = "opm")
    private BigDecimal opm;

    public StockQuarterKey getKey() {
        return key;
    }

    public void setKey(StockQuarterKey key) {
        this.key = key;
    }

    public BigDecimal getSales() {
        return sales;
    }

    public void setSales(BigDecimal sales) {
        this.sales = sales;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }

    public BigDecimal getOperatingProfit() {
        return operatingProfit;
    }

    public void setOperatingProfit(BigDecimal operatingProfit) {
        this.operatingProfit = operatingProfit;
    }

    public BigDecimal getOtherIncome() {
        return otherIncome;
    }

    public void setOtherIncome(BigDecimal otherIncome) {
        this.otherIncome = otherIncome;
    }

    public BigDecimal getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(BigDecimal depreciation) {
        this.depreciation = depreciation;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getProfitBeforeTax() {
        return profitBeforeTax;
    }

    public void setProfitBeforeTax(BigDecimal profitBeforeTax) {
        this.profitBeforeTax = profitBeforeTax;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getDummy1() {
        return dummy1;
    }

    public void setDummy1(BigDecimal dummy1) {
        this.dummy1 = dummy1;
    }

    public BigDecimal getOpm() {
        return opm;
    }

    public void setOpm(BigDecimal opm) {
        this.opm = opm;
    }
}
