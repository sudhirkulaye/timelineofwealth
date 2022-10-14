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
    @Column(name = "noplat")
    private BigDecimal noplat;
    @Column(name = "ttm_sales")
    private BigDecimal ttmSales;
    @Column(name = "ttm_ebitda")
    private BigDecimal ttmEbitda;
    @Column(name = "ttm_noplat")
    private BigDecimal ttmNoplat;
    @Column(name = "ttm_opm")
    private BigDecimal ttmOpm;
    @Column(name = "sales_g")
    private BigDecimal salesG;
    @Column(name = "ttm_sales_g")
    private BigDecimal ttmSalesG;
    @Column(name = "ebitda_g")
    private BigDecimal ebitdaG;
    @Column(name = "ttm_ebitda_g")
    private BigDecimal ttmEbitdaG;
    @Column(name = "mcap")
    private BigDecimal mcap;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "result_date")
    private Date resultDate;

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

    public BigDecimal getNoplat() {
        return noplat;
    }
    public void setNoplat(BigDecimal noplat) {
        this.noplat = noplat;
    }

    public BigDecimal getTtmSales() {
        return ttmSales;
    }
    public void setTtmSales(BigDecimal ttmSales) {
        this.ttmSales = ttmSales;
    }

    public BigDecimal getTtmEbitda() {
        return ttmEbitda;
    }
    public void setTtmEbitda(BigDecimal ttmEbitda) {
        this.ttmEbitda = ttmEbitda;
    }

    public BigDecimal getTtmNoplat() {
        return ttmNoplat;
    }
    public void setTtmNoplat(BigDecimal ttmNoplat) {
        this.ttmNoplat = ttmNoplat;
    }

    public BigDecimal getTtmOpm() {
        return ttmOpm;
    }
    public void setTtmOpm(BigDecimal ttmOpm) {
        this.ttmOpm = ttmOpm;
    }

    public BigDecimal getSalesG() {
        return salesG;
    }
    public void setSalesG(BigDecimal salesG) {
        this.salesG = salesG;
    }

    public BigDecimal getTtmSalesG() {
        return ttmSalesG;
    }
    public void setTtmSalesG(BigDecimal ttmSalesG) {
        this.ttmSalesG = ttmSalesG;
    }

    public BigDecimal getEbitdaG() {
        return ebitdaG;
    }
    public void setEbitdaG(BigDecimal ebitdaG) {
        this.ebitdaG = ebitdaG;
    }

    public BigDecimal getTtmEbitdaG() {
        return ttmEbitdaG;
    }
    public void setTtmEbitdaG(BigDecimal ttmEbitdaG) {
        this.ttmEbitdaG = ttmEbitdaG;
    }

    public BigDecimal getMcap() {
        return mcap;
    }
    public void setMcap(BigDecimal mcap) {
        this.mcap = mcap;
    }

    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getResultDate() {
        return resultDate;
    }
    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
    }
}
