package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_pnl")
public class StockPnl implements Serializable {
    @Embeddable
    public static class StockPnlKey implements Serializable {
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
    private StockPnlKey key;
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
    @Column(name = "eps")
    private BigDecimal eps;
    @Column(name = "pe")
    private BigDecimal pe;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "dummy1")
    private BigDecimal dummy1;
    @Column(name = "ratios")
    private BigDecimal ratios;
    @Column(name = "dividend_payout")
    private BigDecimal dividendPayout;
    @Column(name = "opm")
    private BigDecimal opm;
    @Column(name = "npm")
    private BigDecimal npm;
    @Column(name = "re")
    private BigDecimal re;

    public StockPnlKey getKey() {
        return key;
    }
    public void setKey(StockPnlKey key) {
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

    public BigDecimal getEps() {
        return eps;
    }
    public void setEps(BigDecimal eps) {
        this.eps = eps;
    }

    public BigDecimal getPe() {
        return pe;
    }
    public void setPe(BigDecimal pe) {
        this.pe = pe;
    }

    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDummy1() {
        return dummy1;
    }
    public void setDummy1(BigDecimal dummy1) {
        this.dummy1 = dummy1;
    }

    public BigDecimal getRatios() {
        return ratios;
    }
    public void setRatios(BigDecimal ratios) {
        this.ratios = ratios;
    }

    public BigDecimal getDividendPayout() {
        return dividendPayout;
    }
    public void setDividendPayout(BigDecimal dividendPayout) {
        this.dividendPayout = dividendPayout;
    }

    public BigDecimal getOpm() {
        return opm;
    }
    public void setOpm(BigDecimal opm) {
        this.opm = opm;
    }

    public BigDecimal getNpm() {
        return npm;
    }
    public void setNpm(BigDecimal npm) {
        this.npm = npm;
    }

    public BigDecimal getRe() {
        return re;
    }
    public void setRe(BigDecimal re) {
        this.re = re;
    }
}
