package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_analyst_reco")
public class StockAnalystReco implements Serializable {
    @Embeddable
    public static class StockAnalystRecoKey implements Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "quarter")
        private String quarter;
        @Column(name = "date")
        private Date date;
        @Column(name = "broker")
        private String broker;

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

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }

        public String getBroker() {
            return broker;
        }
        public void setBroker(String broker) {
            this.broker = broker;
        }
    }
    @EmbeddedId
    private StockAnalystRecoKey key;
    @Column(name = "mcap")
    private BigDecimal mcap;
    @Column(name = "cmp")
    private BigDecimal cmp;
    @Column(name = "price_change")
    private BigDecimal priceChange;
    @Column(name = "reco")
    private String reco;
    @Column(name = "target")
    private BigDecimal target;
    @Column(name = "upside_or_downside")
    private BigDecimal upsideOrDownside;
    @Column(name = "sales_y0")
    private BigDecimal salesY0;
    @Column(name = "sales_y1")
    private BigDecimal salesY1;
    @Column(name = "sales_y2")
    private BigDecimal salesY2;
    @Column(name = "sales_growth")
    private BigDecimal salesGrowth;
    @Column(name = "ebit_y0")
    private BigDecimal ebitY0;
    @Column(name = "ebit_y1")
    private BigDecimal ebitY1;
    @Column(name = "ebit_y2")
    private BigDecimal ebitY2;
    @Column(name = "ebit_growth")
    private BigDecimal ebitGrowth;
    @Column(name = "opm_y0")
    private BigDecimal opmY0;
    @Column(name = "opm_y1")
    private BigDecimal opmY1;
    @Column(name = "opm_y2")
    private BigDecimal opmY2;
    @Column(name = "roce_y0")
    private BigDecimal roceY0;
    @Column(name = "roce_y1")
    private BigDecimal roceY1;
    @Column(name = "roce_y2")
    private BigDecimal roceY2;
    @Column(name = "valuation_y0")
    private BigDecimal valuationY0;
    @Column(name = "valuation_y1")
    private BigDecimal valuationY1;
    @Column(name = "valuation_y2")
    private BigDecimal valuationY2;
    @Column(name = "aum_y0")
    private BigDecimal aumY0;
    @Column(name = "aum_y1")
    private BigDecimal aumY1;
    @Column(name = "aum_y2")
    private BigDecimal aumY2;
    @Column(name = "aum_growth")
    private BigDecimal aumGrowth;
    @Column(name = "credit_cost_y0")
    private BigDecimal creditCostY0;
    @Column(name = "credit_cost_y1")
    private BigDecimal creditCostY1;
    @Column(name = "credit_cost_y2")
    private BigDecimal creditCostY2;
    @Column(name = "gnpa_y0")
    private BigDecimal gnpaY0;
    @Column(name = "gnpa_y1")
    private BigDecimal gnpaY1;
    @Column(name = "gnpa_y2")
    private BigDecimal gnpaY2;
    @Column(name = "nnpa_y0")
    private BigDecimal nnpaY0;
    @Column(name = "nnpa_y1")
    private BigDecimal nnpaY1;
    @Column(name = "nnpa_y2")
    private BigDecimal nnpaY2;
    @Column(name = "analyst_names")
    private String analystNames;
    @Column(name = "summary")
    private String summary;


    public StockAnalystRecoKey getKey() {
        return key;
    }
    public void setKey(StockAnalystRecoKey key) {
        this.key = key;
    }

    public BigDecimal getMcap() {
        return mcap;
    }
    public void setMcap(BigDecimal mcap) {
        this.mcap = mcap;
    }

    public BigDecimal getCmp() {
        return cmp;
    }
    public void setCmp(BigDecimal cmp) {
        this.cmp = cmp;
    }

    public BigDecimal getPriceChange() {
        return priceChange;
    }
    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }

    public String getReco() {
        return reco;
    }
    public void setReco(String reco) {
        this.reco = reco;
    }

    public BigDecimal getTarget() {
        return target;
    }
    public void setTarget(BigDecimal target) {
        this.target = target;
    }

    public BigDecimal getUpsideOrDownside() {
        return upsideOrDownside;
    }
    public void setUpsideOrDownside(BigDecimal upsideOrDownside) {
        this.upsideOrDownside = upsideOrDownside;
    }

    public BigDecimal getSalesY0() {
        return salesY0;
    }
    public void setSalesY0(BigDecimal salesY0) {
        this.salesY0 = salesY0;
    }

    public BigDecimal getSalesY1() {
        return salesY1;
    }
    public void setSalesY1(BigDecimal salesY1) {
        this.salesY1 = salesY1;
    }

    public BigDecimal getSalesY2() {
        return salesY2;
    }
    public void setSalesY2(BigDecimal salesY2) {
        this.salesY2 = salesY2;
    }

    public BigDecimal getSalesGrowth() {
        return salesGrowth;
    }
    public void setSalesGrowth(BigDecimal salesGrowth) {
        this.salesGrowth = salesGrowth;
    }

    public BigDecimal getEbitY0() {
        return ebitY0;
    }
    public void setEbitY0(BigDecimal ebitY0) {
        this.ebitY0 = ebitY0;
    }

    public BigDecimal getEbitY1() {
        return ebitY1;
    }
    public void setEbitY1(BigDecimal ebitY1) {
        this.ebitY1 = ebitY1;
    }

    public BigDecimal getEbitY2() {
        return ebitY2;
    }
    public void setEbitY2(BigDecimal ebitY2) {
        this.ebitY2 = ebitY2;
    }

    public BigDecimal getEbitGrowth() {
        return ebitGrowth;
    }
    public void setEbitGrowth(BigDecimal ebitGrowth) {
        this.ebitGrowth = ebitGrowth;
    }

    public BigDecimal getOpmY0() {
        return opmY0;
    }
    public void setOpmY0(BigDecimal opmY0) {
        this.opmY0 = opmY0;
    }

    public BigDecimal getOpmY1() {
        return opmY1;
    }
    public void setOpmY1(BigDecimal opmY1) {
        this.opmY1 = opmY1;
    }

    public BigDecimal getOpmY2() {
        return opmY2;
    }
    public void setOpmY2(BigDecimal opmY2) {
        this.opmY2 = opmY2;
    }

    public BigDecimal getRoceY0() {
        return roceY0;
    }
    public void setRoceY0(BigDecimal roceY0) {
        this.roceY0 = roceY0;
    }

    public BigDecimal getRoceY1() {
        return roceY1;
    }
    public void setRoceY1(BigDecimal roceY1) {
        this.roceY1 = roceY1;
    }

    public BigDecimal getRoceY2() {
        return roceY2;
    }
    public void setRoceY2(BigDecimal roceY2) {
        this.roceY2 = roceY2;
    }

    public BigDecimal getValuationY0() {
        return valuationY0;
    }
    public void setValuationY0(BigDecimal valuationY0) {
        this.valuationY0 = valuationY0;
    }

    public BigDecimal getValuationY1() {
        return valuationY1;
    }
    public void setValuationY1(BigDecimal valuationY1) {
        this.valuationY1 = valuationY1;
    }

    public BigDecimal getValuationY2() {
        return valuationY2;
    }
    public void setValuationY2(BigDecimal valuationY2) {
        this.valuationY2 = valuationY2;
    }

    public BigDecimal getAumY0() {
        return aumY0;
    }
    public void setAumY0(BigDecimal aumY0) {
        this.aumY0 = aumY0;
    }

    public BigDecimal getAumY1() {
        return aumY1;
    }
    public void setAumY1(BigDecimal aumY1) {
        this.aumY1 = aumY1;
    }

    public BigDecimal getAumY2() {
        return aumY2;
    }
    public void setAumY2(BigDecimal aumY2) {
        this.aumY2 = aumY2;
    }

    public BigDecimal getAumGrowth() {
        return aumGrowth;
    }
    public void setAumGrowth(BigDecimal aumGrowth) {
        this.aumGrowth = aumGrowth;
    }

    public BigDecimal getCreditCostY0() {
        return creditCostY0;
    }
    public void setCreditCostY0(BigDecimal creditCostY0) {
        this.creditCostY0 = creditCostY0;
    }

    public BigDecimal getCreditCostY1() {
        return creditCostY1;
    }
    public void setCreditCostY1(BigDecimal creditCostY1) {
        this.creditCostY1 = creditCostY1;
    }

    public BigDecimal getCreditCostY2() {
        return creditCostY2;
    }
    public void setCreditCostY2(BigDecimal creditCostY2) {
        this.creditCostY2 = creditCostY2;
    }

    public BigDecimal getGnpaY0() {
        return gnpaY0;
    }
    public void setGnpaY0(BigDecimal gnpaY0) {
        this.gnpaY0 = gnpaY0;
    }

    public BigDecimal getGnpaY1() {
        return gnpaY1;
    }
    public void setGnpaY1(BigDecimal gnpaY1) {
        this.gnpaY1 = gnpaY1;
    }

    public BigDecimal getGnpaY2() {
        return gnpaY2;
    }
    public void setGnpaY2(BigDecimal gnpaY2) {
        this.gnpaY2 = gnpaY2;
    }

    public BigDecimal getNnpaY0() {
        return nnpaY0;
    }
    public void setNnpaY0(BigDecimal nnpaY0) {
        this.nnpaY0 = nnpaY0;
    }

    public BigDecimal getNnpaY1() {
        return nnpaY1;
    }
    public void setNnpaY1(BigDecimal nnpaY1) {
        this.nnpaY1 = nnpaY1;
    }

    public BigDecimal getNnpaY2() {
        return nnpaY2;
    }
    public void setNnpaY2(BigDecimal nnpaY2) {
        this.nnpaY2 = nnpaY2;
    }

    public String getAnalystNames() {
        return analystNames;
    }
    public void setAnalystNames(String analystNames) {
        this.analystNames = analystNames;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
}
