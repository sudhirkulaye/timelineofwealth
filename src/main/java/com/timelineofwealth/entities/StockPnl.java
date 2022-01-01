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
    @Column(name = "tax_rate")
    private BigDecimal taxRate;
    @Column(name = "sales_g")
    private BigDecimal salesG;
    @Column(name = "ebitda_g")
    private BigDecimal ebitdaG;
    @Column(name = "pat_g")
    private BigDecimal patG;
    @Column(name = "non_op_inc_g")
    private BigDecimal nonOpIncG;
    @Column(name = "debt_to_capital")
    private BigDecimal debtToCapital;
    @Column(name = "ppe_to_sales")
    private BigDecimal ppeToSales;
    @Column(name = "dep_to_ppe")
    private BigDecimal depToPpe;
    @Column(name = "non_op_inc_to_invst")
    private BigDecimal nonOpIncToInvst;
    @Column(name = "noplat")
    private BigDecimal noplat;
    @Column(name = "capex")
    private BigDecimal capex;
    @Column(name = "fcff")
    private BigDecimal fcff;
    @Column(name = "sales_g_3yr")
    private BigDecimal salesG3yr;
    @Column(name = "sales_g_5yr")
    private BigDecimal salesG5yr;
    @Column(name = "sales_g_10yr")
    private BigDecimal salesG10yr;
    @Column(name = "ebitda_g_3yr")
    private BigDecimal ebitdaG3yr;
    @Column(name = "ebitda_g_5yr")
    private BigDecimal ebitdaG5yr;
    @Column(name = "ebitda_g_10yr")
    private BigDecimal ebitdaG10yr;
    @Column(name = "avg_opm_3yr")
    private BigDecimal avgOpm3yr;
    @Column(name = "avg_opm_5yr")
    private BigDecimal avgOpm5yr;
    @Column(name = "avg_opm_10yr")
    private BigDecimal avgOpm10yr;
    @Column(name = "avg_opm")
    private BigDecimal avgOpm;
    @Column(name = "opm_min")
    private BigDecimal opmMin;
    @Column(name = "opm_max")
    private BigDecimal opmMax;
    @Column(name = "avg_roic_3yr")
    private BigDecimal avgRoic3yr;
    @Column(name = "avg_roic_5yr")
    private BigDecimal avgRoic5yr;
    @Column(name = "avg_roic_10yr")
    private BigDecimal avgRoic10yr;
    @Column(name = "avg_roic")
    private BigDecimal avgRoic;
    @Column(name = "roic_min")
    private BigDecimal roicMin;
    @Column(name = "roic_max")
    private BigDecimal roicMax;
    @Column(name = "avg_ppe_to_sales")
    private BigDecimal avgPpeToSales;
    @Column(name = "avg_dep_to_ppe")
    private BigDecimal avgDepToPpe;



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

    public BigDecimal getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getSalesG() {
        return salesG;
    }
    public void setSalesG(BigDecimal salesG) {
        this.salesG = salesG;
    }

    public BigDecimal getEbitdaG() {
        return ebitdaG;
    }
    public void setEbitdaG(BigDecimal ebitdaG) {
        this.ebitdaG = ebitdaG;
    }

    public BigDecimal getPatG() {
        return patG;
    }
    public void setPatG(BigDecimal patG) {
        this.patG = patG;
    }

    public BigDecimal getNonOpIncG() {
        return nonOpIncG;
    }
    public void setNonOpIncG(BigDecimal nonOpIncG) {
        this.nonOpIncG = nonOpIncG;
    }

    public BigDecimal getDebtToCapital() {
        return debtToCapital;
    }
    public void setDebtToCapital(BigDecimal debtToCapital) {
        this.debtToCapital = debtToCapital;
    }

    public BigDecimal getPpeToSales() {
        return ppeToSales;
    }
    public void setPpeToSales(BigDecimal ppeToSales) {
        this.ppeToSales = ppeToSales;
    }

    public BigDecimal getDepToPpe() {
        return depToPpe;
    }
    public void setDepToPpe(BigDecimal depToPpe) {
        this.depToPpe = depToPpe;
    }

    public BigDecimal getNonOpIncToInvst() {
        return nonOpIncToInvst;
    }
    public void setNonOpIncToInvst(BigDecimal nonOpIncToInvst) {
        this.nonOpIncToInvst = nonOpIncToInvst;
    }

    public BigDecimal getNoplat() {
        return noplat;
    }
    public void setNoplat(BigDecimal noplat) {
        this.noplat = noplat;
    }

    public BigDecimal getCapex() {
        return capex;
    }
    public void setCapex(BigDecimal capex) {
        this.capex = capex;
    }

    public BigDecimal getFcff() {
        return fcff;
    }
    public void setFcff(BigDecimal fcff) {
        this.fcff = fcff;
    }

    public BigDecimal getSalesG3yr() {
        return salesG3yr;
    }
    public void setSalesG3yr(BigDecimal salesG3yr) {
        this.salesG3yr = salesG3yr;
    }

    public BigDecimal getSalesG5yr() {
        return salesG5yr;
    }
    public void setSalesG5yr(BigDecimal salesG5yr) {
        this.salesG5yr = salesG5yr;
    }

    public BigDecimal getSalesG10yr() {
        return salesG10yr;
    }
    public void setSalesG10yr(BigDecimal salesG10yr) {
        this.salesG10yr = salesG10yr;
    }

    public BigDecimal getEbitdaG3yr() {
        return ebitdaG3yr;
    }
    public void setEbitdaG3yr(BigDecimal ebitdaG3yr) {
        this.ebitdaG3yr = ebitdaG3yr;
    }

    public BigDecimal getEbitdaG5yr() {
        return ebitdaG5yr;
    }
    public void setEbitdaG5yr(BigDecimal ebitdaG5yr) {
        this.ebitdaG5yr = ebitdaG5yr;
    }

    public BigDecimal getEbitdaG10yr() {
        return ebitdaG10yr;
    }
    public void setEbitdaG10yr(BigDecimal ebitdaG10yr) {
        this.ebitdaG10yr = ebitdaG10yr;
    }

    public BigDecimal getAvgOpm3yr() {
        return avgOpm3yr;
    }
    public void setAvgOpm3yr(BigDecimal avgOpm3yr) {
        this.avgOpm3yr = avgOpm3yr;
    }

    public BigDecimal getAvgOpm5yr() {
        return avgOpm5yr;
    }
    public void setAvgOpm5yr(BigDecimal avgOpm5yr) {
        this.avgOpm5yr = avgOpm5yr;
    }

    public BigDecimal getAvgOpm10yr() {
        return avgOpm10yr;
    }
    public void setAvgOpm10yr(BigDecimal avgOpm10yr) {
        this.avgOpm10yr = avgOpm10yr;
    }

    public BigDecimal getAvgOpm() {
        return avgOpm;
    }
    public void setAvgOpm(BigDecimal avgOpm) {
        this.avgOpm = avgOpm;
    }

    public BigDecimal getOpmMin() {
        return opmMin;
    }
    public void setOpmMin(BigDecimal opmMin) {
        this.opmMin = opmMin;
    }

    public BigDecimal getOpmMax() {
        return opmMax;
    }
    public void setOpmMax(BigDecimal opmMax) {
        this.opmMax = opmMax;
    }

    public BigDecimal getAvgRoic3yr() {
        return avgRoic3yr;
    }
    public void setAvgRoic3yr(BigDecimal avgRoic3yr) {
        this.avgRoic3yr = avgRoic3yr;
    }

    public BigDecimal getAvgRoic5yr() {
        return avgRoic5yr;
    }
    public void setAvgRoic5yr(BigDecimal avgRoic5yr) {
        this.avgRoic5yr = avgRoic5yr;
    }

    public BigDecimal getAvgRoic10yr() {
        return avgRoic10yr;
    }
    public void setAvgRoic10yr(BigDecimal avgRoic10yr) {
        this.avgRoic10yr = avgRoic10yr;
    }

    public BigDecimal getAvgRoic() {
        return avgRoic;
    }
    public void setAvgRoic(BigDecimal avgRoic) {
        this.avgRoic = avgRoic;
    }

    public BigDecimal getRoicMin() {
        return roicMin;
    }
    public void setRoicMin(BigDecimal roicMin) {
        this.roicMin = roicMin;
    }

    public BigDecimal getRoicMax() {
        return roicMax;
    }
    public void setRoicMax(BigDecimal roicMax) {
        this.roicMax = roicMax;
    }

    public BigDecimal getAvgPpeToSales() {
        return avgPpeToSales;
    }
    public void setAvgPpeToSales(BigDecimal avgPpeToSales) {
        this.avgPpeToSales = avgPpeToSales;
    }

    public BigDecimal getAvgDepToPpe() {
        return avgDepToPpe;
    }
    public void setAvgDepToPpe(BigDecimal avgDepToPpe) {
        this.avgDepToPpe = avgDepToPpe;
    }
}
