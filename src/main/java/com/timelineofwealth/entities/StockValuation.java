package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_valuation")
public class StockValuation implements Serializable {
    @Embeddable
    public static class StockValuationKey implements Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "quarter")
        private String quarter;

        public String getTicker() {
            return ticker;
        }
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public String getQuarter() {
            return quarter;
        }
        public void setQuarter(String quarter) {
            this.quarter = quarter;
        }
    }
    @EmbeddedId
    private StockValuationKey key;
    @Column(name = "ttm_revenue")
    private BigDecimal ttmRevenue;
    @Column(name = "ttm_noplat")
    private BigDecimal ttmNoplat;
    @Column(name = "ttm_fcff")
    private BigDecimal ttmFcff;
    @Column(name = "ttm_after_tax_other_inc")
    private BigDecimal ttmAfterTaxOtherInc;
    @Column(name = "debt_outstanding")
    private BigDecimal debtOutstanding;
    @Column(name = "wacc")
    private BigDecimal wacc;
    @Column(name = "tax_rate")
    private BigDecimal taxRate;
    @Column(name = "revenue_growth_next_10yr")
    private BigDecimal revenueGrowthNext10yr;
    @Column(name = "opm_next_10yr")
    private BigDecimal opmNext10yr;
    @Column(name = "net_ppe_by_revenue_10yr")
    private BigDecimal netPpeByRevenue10yr;
    @Column(name = "depreciation_by_net_ppe_10yr")
    private BigDecimal depreciationByNetPpe10yr;
    @Column(name = "other_inc_growth_next_10yr")
    private BigDecimal otherIncGrowthNext10yr;
    @Column(name = "other_inc_growth_period")
    private BigDecimal otherIncGrowthPeriod;
    @Column(name = "other_inc_terminal_growth")
    private BigDecimal otherIncTerminalGrowth;
    @Column(name = "other_inc_by_investment")
    private BigDecimal otherIncByInvestment;
    @Column(name = "historical_roic")
    private BigDecimal historicalRoic;
    @Column(name = "ronic")
    private BigDecimal ronic;
    @Column(name = "next_stage_growth_period")
    private BigDecimal nextStageGrowthPeriod;
    @Column(name = "revenue_growth_second_stage")
    private BigDecimal revenueGrowthSecondStage;
    @Column(name = "roic_second_stage")
    private BigDecimal roicSecondStage;
    @Column(name = "terminal_growth")
    private BigDecimal terminalGrowth;
    @Column(name = "terminal_roic")
    private BigDecimal terminalRoic;
    @Column(name = "mcap")
    private BigDecimal mcap;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "min_fair_price")
    private BigDecimal minFairPrice;
    @Column(name = "max_fair_price")
    private BigDecimal maxFairPrice;
    @Column(name = "exp_ttm_revenue")
    private BigDecimal expTtmRevenue;
    @Column(name = "exp_ttm_after_tax_other_inc")
    private BigDecimal expTtmAfterTaxOtherInc;
    @Column(name = "min_revenue_growth_next_10yr")
    private BigDecimal minRevenueGrowthNext10yr;
    @Column(name = "max_revenue_growth_next_10yr")
    private BigDecimal maxRevenueGrowthNext10yr;
    @Column(name = "min_mcap")
    private BigDecimal minMcap;
    @Column(name = "max_mcap")
    private BigDecimal maxMcap;
    @Column(name = "actual_min_price")
    private BigDecimal actualMinPrice;
    @Column(name = "actual_max_price")
    private BigDecimal actualMaxPrice;
    @Column(name = "actual_min_mcap")
    private BigDecimal actualMinMcap;
    @Column(name = "actual_max_mcap")
    private BigDecimal actualMaxMcap;

    public StockValuationKey getKey() {
        return key;
    }
    public void setKey(StockValuationKey key) {
        this.key = key;
    }

    public BigDecimal getTtmRevenue() {
        return ttmRevenue;
    }
    public void setTtmRevenue(BigDecimal ttmRevenue) {
        this.ttmRevenue = ttmRevenue;
    }

    public BigDecimal getTtmNoplat() {
        return ttmNoplat;
    }
    public void setTtmNoplat(BigDecimal ttmNoplat) {
        this.ttmNoplat = ttmNoplat;
    }

    public BigDecimal getTtmFcff() {
        return ttmFcff;
    }
    public void setTtmFcff(BigDecimal ttmFcff) {
        this.ttmFcff = ttmFcff;
    }

    public BigDecimal getTtmAfterTaxOtherInc() {
        return ttmAfterTaxOtherInc;
    }
    public void setTtmAfterTaxOtherInc(BigDecimal ttmAfterTaxOtherInc) {
        this.ttmAfterTaxOtherInc = ttmAfterTaxOtherInc;
    }

    public BigDecimal getDebtOutstanding() {
        return debtOutstanding;
    }
    public void setDebtOutstanding(BigDecimal debtOutstanding) {
        this.debtOutstanding = debtOutstanding;
    }

    public BigDecimal getWacc() {
        return wacc;
    }
    public void setWacc(BigDecimal wacc) {
        this.wacc = wacc;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getRevenueGrowthNext10yr() {
        return revenueGrowthNext10yr;
    }
    public void setRevenueGrowthNext10yr(BigDecimal revenueGrowthNext10yr) {
        this.revenueGrowthNext10yr = revenueGrowthNext10yr;
    }

    public BigDecimal getOpmNext10yr() {
        return opmNext10yr;
    }
    public void setOpmNext10yr(BigDecimal opmNext10yr) {
        this.opmNext10yr = opmNext10yr;
    }

    public BigDecimal getNetPpeByRevenue10yr() {
        return netPpeByRevenue10yr;
    }
    public void setNetPpeByRevenue10yr(BigDecimal netPpeByRevenue10yr) {
        this.netPpeByRevenue10yr = netPpeByRevenue10yr;
    }

    public BigDecimal getDepreciationByNetPpe10yr() {
        return depreciationByNetPpe10yr;
    }
    public void setDepreciationByNetPpe10yr(BigDecimal depreciationByNetPpe10yr) {
        this.depreciationByNetPpe10yr = depreciationByNetPpe10yr;
    }

    public BigDecimal getOtherIncGrowthNext10yr() {
        return otherIncGrowthNext10yr;
    }
    public void setOtherIncGrowthNext10yr(BigDecimal otherIncGrowthNext10yr) {
        this.otherIncGrowthNext10yr = otherIncGrowthNext10yr;
    }

    public BigDecimal getOtherIncGrowthPeriod() {
        return otherIncGrowthPeriod;
    }
    public void setOtherIncGrowthPeriod(BigDecimal otherIncGrowthPeriod) {
        this.otherIncGrowthPeriod = otherIncGrowthPeriod;
    }

    public BigDecimal getOtherIncTerminalGrowth() {
        return otherIncTerminalGrowth;
    }
    public void setOtherIncTerminalGrowth(BigDecimal otherIncTerminalGrowth) {
        this.otherIncTerminalGrowth = otherIncTerminalGrowth;
    }

    public BigDecimal getOtherIncByInvestment() {
        return otherIncByInvestment;
    }
    public void setOtherIncByInvestment(BigDecimal otherIncByInvestment) {
        this.otherIncByInvestment = otherIncByInvestment;
    }

    public BigDecimal getHistoricalRoic() {
        return historicalRoic;
    }
    public void setHistoricalRoic(BigDecimal historicalRoic) {
        this.historicalRoic = historicalRoic;
    }

    public BigDecimal getRonic() {
        return ronic;
    }
    public void setRonic(BigDecimal ronic) {
        this.ronic = ronic;
    }

    public BigDecimal getNextStageGrowthPeriod() {
        return nextStageGrowthPeriod;
    }
    public void setNextStageGrowthPeriod(BigDecimal nextStageGrowthPeriod) {
        this.nextStageGrowthPeriod = nextStageGrowthPeriod;
    }

    public BigDecimal getRevenueGrowthSecondStage() {
        return revenueGrowthSecondStage;
    }
    public void setRevenueGrowthSecondStage(BigDecimal revenueGrowthSecondStage) {
        this.revenueGrowthSecondStage = revenueGrowthSecondStage;
    }

    public BigDecimal getRoicSecondStage() {
        return roicSecondStage;
    }
    public void setRoicSecondStage(BigDecimal roicSecondStage) {
        this.roicSecondStage = roicSecondStage;
    }

    public BigDecimal getTerminalGrowth() {
        return terminalGrowth;
    }
    public void setTerminalGrowth(BigDecimal terminalGrowth) {
        this.terminalGrowth = terminalGrowth;
    }

    public BigDecimal getTerminalRoic() {
        return terminalRoic;
    }
    public void setTerminalRoic(BigDecimal terminalRoic) {
        this.terminalRoic = terminalRoic;
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

    public BigDecimal getMinFairPrice() {
        return minFairPrice;
    }
    public void setMinFairPrice(BigDecimal minFairPrice) {
        this.minFairPrice = minFairPrice;
    }

    public BigDecimal getMaxFairPrice() {
        return maxFairPrice;
    }
    public void setMaxFairPrice(BigDecimal maxFairPrice) {
        this.maxFairPrice = maxFairPrice;
    }

    public BigDecimal getExpTtmRevenue() {
        return expTtmRevenue;
    }
    public void setExpTtmRevenue(BigDecimal expTtmRevenue) {
        this.expTtmRevenue = expTtmRevenue;
    }

    public BigDecimal getExpTtmAfterTaxOtherInc() {
        return expTtmAfterTaxOtherInc;
    }
    public void setExpTtmAfterTaxOtherInc(BigDecimal expTtmAfterTaxOtherInc) {
        this.expTtmAfterTaxOtherInc = expTtmAfterTaxOtherInc;
    }

    public BigDecimal getMinRevenueGrowthNext10yr() {
        return minRevenueGrowthNext10yr;
    }
    public void setMinRevenueGrowthNext10yr(BigDecimal minRevenueGrowthNext10yr) {
        this.minRevenueGrowthNext10yr = minRevenueGrowthNext10yr;
    }

    public BigDecimal getMaxRevenueGrowthNext10yr() {
        return maxRevenueGrowthNext10yr;
    }
    public void setMaxRevenueGrowthNext10yr(BigDecimal maxRevenueGrowthNext10yr) {
        this.maxRevenueGrowthNext10yr = maxRevenueGrowthNext10yr;
    }

    public BigDecimal getMinMcap() {
        return minMcap;
    }
    public void setMinMcap(BigDecimal minMcap) {
        this.minMcap = minMcap;
    }

    public BigDecimal getMaxMcap() {
        return maxMcap;
    }
    public void setMaxMcap(BigDecimal maxMcap) {
        this.maxMcap = maxMcap;
    }

    public BigDecimal getActualMinPrice() {
        return actualMinPrice;
    }
    public void setActualMinPrice(BigDecimal actualMinPrice) {
        this.actualMinPrice = actualMinPrice;
    }

    public BigDecimal getActualMaxPrice() {
        return actualMaxPrice;
    }
    public void setActualMaxPrice(BigDecimal actualMaxPrice) {
        this.actualMaxPrice = actualMaxPrice;
    }

    public BigDecimal getActualMinMcap() {
        return actualMinMcap;
    }
    public void setActualMinMcap(BigDecimal actualMinMcap) {
        this.actualMinMcap = actualMinMcap;
    }

    public BigDecimal getActualMaxMcap() {
        return actualMaxMcap;
    }
    public void setActualMaxMcap(BigDecimal actualMaxMcap) {
        this.actualMaxMcap = actualMaxMcap;
    }
}
