package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "daily_data_s")
public class DailyDataS implements Serializable{
    @Embeddable
    public static class DailyDataSKey implements Serializable {
        @Column(name = "name")
        private String name;
        @Column(name = "date")
        private Date date;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @EmbeddedId
    private DailyDataSKey key;
    @Column(name = "rank")
    private int rank;
    @Column(name = "cmp")
    private BigDecimal cmp;
    @Column(name = "market_cap")
    private BigDecimal marketCap;
    @Column(name = "last_result_date")
    private int lastResultDate;
    @Column(name = "net_profit")
    private BigDecimal netProfit;
    @Column(name = "sales")
    private BigDecimal sales;
    @Column(name = "yoy_quarterly_sales_growth")
    private BigDecimal yoyQuarterlySalesGrowth;
    @Column(name = "yoyQuarterlyProfitGrowth")
    private BigDecimal yoyQuarterlyProfitGrowth;
    @Column(name = "qoq_sales_growth")
    private BigDecimal qoqSalesGrowth;
    @Column(name = "qoq_profit_growth")
    private BigDecimal qoqProfitGrowth;
    @Column(name = "opm_latest_quarter")
    private BigDecimal opmLatestQuarter;
    @Column(name = "opm_last_year")
    private BigDecimal opmLastYear;
    @Column(name = "npm_latest_quarter")
    private BigDecimal npmLatestQuarter;
    @Column(name = "npm_last_year")
    private BigDecimal npmLastYear;
    @Column(name = "profit_growth_3years")
    private BigDecimal profitGrowth3years;
    @Column(name = "sales_growth_3years")
    private BigDecimal salesGrowth3years;
    @Column(name = "pe_ttm")
    private BigDecimal peTtm;
    @Column(name = "historical_pe_3years")
    private BigDecimal historicalPe3years;
    @Column(name = "peg_ratio")
    private BigDecimal pegRatio;
    @Column(name = "pb_ttm")
    private BigDecimal pbTtm;
    @Column(name = "ev_to_ebit")
    private BigDecimal evToEbit;
    @Column(name = "dividend_payout")
    private BigDecimal dividendPayout;
    @Column(name = "roce")
    private BigDecimal roce;
    @Column(name = "roe")
    private BigDecimal roe;
    @Column(name = "avg_roce_3years")
    private BigDecimal avgRoce3years;
    @Column(name = "avg_roe_3years")
    private BigDecimal avgRoe3years;
    @Column(name = "debt")
    private BigDecimal debt;
    @Column(name = "debt_to_equity")
    private BigDecimal debtToEquity;
    @Column(name = "debt_3years_back")
    private BigDecimal debt3yearsback;
    @Column(name = "mcap_to_netprofit")
    private BigDecimal mcapToNetprofit;
    @Column(name = "mcap_to_sales")
    private BigDecimal mcapToSales;
    @Column(name = "sector")
    private String sector;
    @Column(name = "industry")
    private String industry;
    @Column(name = "sub_industry")
    private String subIndustry;
    // Newly added
    @Column(name = "fcf_s")
    private BigDecimal fcfS;
    @Column(name = "sales_growth_5years")
    private BigDecimal salesGrowth5years;
    @Column(name = "sales_growth_10years")
    private BigDecimal salesGrowth10years;
    @Column(name = "noplat")
    private BigDecimal noplat;
    @Column(name = "capex")
    private BigDecimal capex;
    @Column(name = "fcff")
    private BigDecimal fcff;
    @Column(name = "invested_capital")
    private BigDecimal investedCapital;
    @Column(name = "roic")
    private BigDecimal roic;


    public DailyDataSKey getKey() {
        return key;
    }
    public void setKey(DailyDataSKey key) {
        this.key = key;
    }

    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }

    public BigDecimal getCmp() {
        return cmp;
    }
    public void setCmp(BigDecimal cmp) {
        this.cmp = cmp;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }
    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public int getLastResultDate() {
        return lastResultDate;
    }
    public void setLastResultDate(int lastResultDate) {
        this.lastResultDate = lastResultDate;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }
    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getSales() {
        return sales;
    }
    public void setSales(BigDecimal sales) {
        this.sales = sales;
    }

    public BigDecimal getYoyQuarterlySalesGrowth() {
        return yoyQuarterlySalesGrowth;
    }
    public void setYoyQuarterlySalesGrowth(BigDecimal yoyQuarterlySalesGrowth) {
        this.yoyQuarterlySalesGrowth = yoyQuarterlySalesGrowth;
    }

    public BigDecimal getYoyQuarterlyProfitGrowth() {
        return yoyQuarterlyProfitGrowth;
    }
    public void setYoyQuarterlyProfitGrowth(BigDecimal yoyQuarterlyProfitGrowth) {
        this.yoyQuarterlyProfitGrowth = yoyQuarterlyProfitGrowth;
    }

    public BigDecimal getQoqSalesGrowth() {
        return qoqSalesGrowth;
    }
    public void setQoqSalesGrowth(BigDecimal qoqSalesGrowth) {
        this.qoqSalesGrowth = qoqSalesGrowth;
    }

    public BigDecimal getQoqProfitGrowth() {
        return qoqProfitGrowth;
    }
    public void setQoqProfitGrowth(BigDecimal qoqProfitGrowth) {
        this.qoqProfitGrowth = qoqProfitGrowth;
    }

    public BigDecimal getOpmLatestQuarter() {
        return opmLatestQuarter;
    }
    public void setOpmLatestQuarter(BigDecimal opmLatestQuarter) {
        this.opmLatestQuarter = opmLatestQuarter;
    }

    public BigDecimal getOpmLastYear() {
        return opmLastYear;
    }
    public void setOpmLastYear(BigDecimal opmLastYear) {
        this.opmLastYear = opmLastYear;
    }

    public BigDecimal getNpmLatestQuarter() {
        return npmLatestQuarter;
    }
    public void setNpmLatestQuarter(BigDecimal npmLatestQuarter) {
        this.npmLatestQuarter = npmLatestQuarter;
    }

    public BigDecimal getNpmLastYear() {
        return npmLastYear;
    }
    public void setNpmLastYear(BigDecimal npmLastYear) {
        this.npmLastYear = npmLastYear;
    }

    public BigDecimal getProfitGrowth3years() {
        return profitGrowth3years;
    }
    public void setProfitGrowth3years(BigDecimal profitGrowth3years) {
        this.profitGrowth3years = profitGrowth3years;
    }

    public BigDecimal getSalesGrowth3years() {
        return salesGrowth3years;
    }
    public void setSalesGrowth3years(BigDecimal salesGrowth3years) {
        this.salesGrowth3years = salesGrowth3years;
    }

    public BigDecimal getPeTtm() {
        return peTtm;
    }
    public void setPeTtm(BigDecimal peTtm) {
        this.peTtm = peTtm;
    }

    public BigDecimal getHistoricalPe3years() {
        return historicalPe3years;
    }
    public void setHistoricalPe3years(BigDecimal historicalPe3years) {
        this.historicalPe3years = historicalPe3years;
    }

    public BigDecimal getPegRatio() {
        return pegRatio;
    }
    public void setPegRatio(BigDecimal pegRatio) {
        this.pegRatio = pegRatio;
    }

    public BigDecimal getPbTtm() {
        return pbTtm;
    }
    public void setPbTtm(BigDecimal pbTtm) {
        this.pbTtm = pbTtm;
    }

    public BigDecimal getEvToEbit() {
        return evToEbit;
    }
    public void setEvToEbit(BigDecimal evToEbit) {
        this.evToEbit = evToEbit;
    }

    public BigDecimal getDividendPayout() {
        return dividendPayout;
    }
    public void setDividendPayout(BigDecimal dividendPayout) {
        this.dividendPayout = dividendPayout;
    }

    public BigDecimal getRoce() {
        return roce;
    }
    public void setRoce(BigDecimal roce) {
        this.roce = roce;
    }

    public BigDecimal getRoe() {
        return roe;
    }
    public void setRoe(BigDecimal roe) {
        this.roe = roe;
    }

    public BigDecimal getAvgRoce3years() {
        return avgRoce3years;
    }
    public void setAvgRoce3years(BigDecimal avgRoce3years) {
        this.avgRoce3years = avgRoce3years;
    }

    public BigDecimal getAvgRoe3years() {
        return avgRoe3years;
    }
    public void setAvgRoe3years(BigDecimal avgRoe3years) {
        this.avgRoe3years = avgRoe3years;
    }

    public BigDecimal getDebt() {
        return debt;
    }
    public void setDebt(BigDecimal debt) {
        this.debt = debt;
    }

    public BigDecimal getDebtToEquity() {
        return debtToEquity;
    }
    public void setDebtToEquity(BigDecimal debtToEquity) {
        this.debtToEquity = debtToEquity;
    }

    public BigDecimal getDebt3yearsback() {
        return debt3yearsback;
    }
    public void setDebt3yearsback(BigDecimal debt3yearsback) {
        this.debt3yearsback = debt3yearsback;
    }

    public BigDecimal getMcapToNetprofit() {
        return mcapToNetprofit;
    }
    public void setMcapToNetprofit(BigDecimal mcapToNetprofit) {
        this.mcapToNetprofit = mcapToNetprofit;
    }

    public BigDecimal getMcapToSales() {
        return mcapToSales;
    }
    public void setMcapToSales(BigDecimal mcapToSales) {
        this.mcapToSales = mcapToSales;
    }

    public String getSector() {
        return sector;
    }
    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }
    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getSubIndustry() {
        return subIndustry;
    }
    public void setSubIndustry(String subIndustry) {
        this.subIndustry = subIndustry;
    }

    public BigDecimal getFcfS() {
        return fcfS;
    }
    public void setFcfS(BigDecimal fcfS) {
        this.fcfS = fcfS;
    }

    public BigDecimal getSalesGrowth5years() {
        return salesGrowth5years;
    }
    public void setSalesGrowth5years(BigDecimal salesGrowth5years) {
        this.salesGrowth5years = salesGrowth5years;
    }

    public BigDecimal getSalesGrowth10years() {
        return salesGrowth10years;
    }
    public void setSalesGrowth10years(BigDecimal salesGrowth10years) {
        this.salesGrowth10years = salesGrowth10years;
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

    public BigDecimal getInvestedCapital() {
        return investedCapital;
    }
    public void setInvestedCapital(BigDecimal investedCapital) {
        this.investedCapital = investedCapital;
    }

    public BigDecimal getRoic() {
        return roic;
    }
    public void setRoic(BigDecimal roic) {
        this.roic = roic;
    }

}
