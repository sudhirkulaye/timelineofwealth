package com.timelineofwealth.dto;

import com.timelineofwealth.entities.DailyDataS;
import com.timelineofwealth.entities.StockPriceMovement;
import com.timelineofwealth.entities.StockPriceMovementHistory;
import com.timelineofwealth.entities.StockUniverse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class NseBse500 implements Serializable {

    private String ticker;
    private String ticker1;
    private String ticker2;
    private String ticker3;
    private String ticker4;
    private String ticker5;
    private String nseCode;
    private String bseCode;
    private String isinCode;
    private String shortName;
    private String name;
    private int assetClassid;
    private String bseIndustry;
    private int subindustryid;
    private BigDecimal latestPrice;
    private Date dateLatestPrice;
    private int isSensex;
    private int isNifty50;
    private int isNiftyjr;
    private int isBse100;
    private int isNse100;
    private int isBse200;
    private int isNse200;
    private int isBse500;
    private int isNse500;
    private int isFno;
    private BigDecimal marketcap;
    private BigDecimal peTtm;
    private String tickerOld;
    private Date listingDate;

    private String includedIndexName;
    private String sectorNameDisplay;
    private String industryNameDisplay;
    private String subIndustryNameDisplay;
    private DailyDataS dailyDataS;
    private StockPriceMovement stockPriceMovement;
    private String latestMOSLReco;
    private String latestAMBITReco;
    private String latestAXISReco;
    private String latestICICIDIRECTReco;
    private String latestPLReco;
    private String latestKOTAKReco;
    private String latestOTHERReco;
    private String resultValuation;
    private String minValuation;
    private String maxValuation;
    private String wacc;
    private String taxRate;
    private String revenueGrowthNext10yr;
    private String opmNext10yr;
    private String netPpeByRevenue10yr;
    private String depreciationByNetPpe10yr;
    private String historicalRoic;
    private String secondAndTerminalStageAssumptions;
    private String otherIncGrowthAssumptions;
    private List<CustomChartData> excelCharts;
    private List<ReportNotes> reportNotes;

    public NseBse500(){}
    public NseBse500(StockUniverse stockUniverse){
        this.ticker = stockUniverse.getTicker();
        this.ticker1 = stockUniverse.getTicker1();
        this.ticker2 = stockUniverse.getTicker2();
        this.ticker3 = stockUniverse.getTicker3();
        this.ticker4 = stockUniverse.getTicker4();
        this.ticker5 = stockUniverse.getTicker5();
        this.nseCode = stockUniverse.getNseCode();
        this.bseCode = stockUniverse.getBseCode();
        this.isinCode = stockUniverse.getIsinCode();
        this.shortName = stockUniverse.getShortName();
        this.name = stockUniverse.getName();
        this.assetClassid = stockUniverse.getAssetClassid();
        this.bseIndustry = stockUniverse.getBseIndustry();
        this.subindustryid = stockUniverse.getSubindustryid();
        this.latestPrice = stockUniverse.getLatestPrice();
        this.dateLatestPrice = stockUniverse.getDateLatestPrice();
        this.isSensex = stockUniverse.getIsSensex();
        this.isNifty50 = stockUniverse.getIsNifty50();
        this.isNiftyjr = stockUniverse.getIsNiftyjr();
        this.isBse100 = stockUniverse.getIsBse100();
        this.isNse100 = stockUniverse.getIsNse100();
        this.isBse200 = stockUniverse.getIsBse200();
        this.isNse200 = stockUniverse.getIsNse200();
        this.isBse500 = stockUniverse.getIsBse500();
        this.isNse500 = stockUniverse.getIsNse500();
        this.isFno = stockUniverse.getIsFno();
        this.marketcap = stockUniverse.getMarketcap();
        this.peTtm = stockUniverse.getPeTtm();
        this.tickerOld = stockUniverse.getTickerOld();
        this.listingDate = stockUniverse.getListingDate();

        if (isBse500 == 1 || isNse500 == 1) {
            this.includedIndexName = "NSE-BSe500";
        }
        if (isBse200 == 1 || isNse200 == 1) {
            this.includedIndexName = "NSE-BSE200";
        }
        if (isBse100 == 1 || isNse100 == 1) {
            this.includedIndexName = "NSE-BSE100";
        }
        if (isNiftyjr == 1) {
            this.includedIndexName = "NIFTY Next 50";
        }
        if (isNifty50 == 1) {
            this.includedIndexName = "NIFTY50";
        }
        if (isSensex == 1) {
            this.includedIndexName = "SENSEX";
        }

    }

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getTicker1() {
        return ticker1;
    }
    public void setTicker1(String ticker1) {
        this.ticker1 = ticker1;
    }

    public String getTicker2() {
        return ticker2;
    }
    public void setTicker2(String ticker2) {
        this.ticker2 = ticker2;
    }

    public String getTicker3() {
        return ticker3;
    }
    public void setTicker3(String ticker3) {
        this.ticker3 = ticker3;
    }

    public String getTicker4() {
        return ticker4;
    }
    public void setTicker4(String ticker4) {
        this.ticker4 = ticker4;
    }

    public String getTicker5() {
        return ticker5;
    }
    public void setTicker5(String ticker5) {
        this.ticker5 = ticker5;
    }

    public String getNseCode() {
        return nseCode;
    }
    public void setNseCode(String nseCode) {
        this.nseCode = nseCode;
    }

    public String getBseCode() {
        return bseCode;
    }
    public void setBseCode(String bseCode) {
        this.bseCode = bseCode;
    }

    public String getIsinCode() {
        return isinCode;
    }
    public void setIsinCode(String isinCode) {
        this.isinCode = isinCode;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(int assetClassid) {
        this.assetClassid = assetClassid;
    }

    public String getBseIndustry() {
        return bseIndustry;
    }
    public void setBseIndustry(String bseIndustry) {
        this.bseIndustry = bseIndustry;
    }

    public int getSubindustryid() {
        return subindustryid;
    }
    public void setSubindustryid(int subindustryid) {
        this.subindustryid = subindustryid;
    }

    public BigDecimal getLatestPrice() {
        return latestPrice;
    }
    public void setLatestPrice(BigDecimal latestPrice) {
        this.latestPrice = latestPrice;
    }

    public Date getDateLatestPrice() {
        return dateLatestPrice;
    }
    public void setDateLatestPrice(Date dateLatestPrice) {
        this.dateLatestPrice = dateLatestPrice;
    }

    public int getIsSensex() {
        return isSensex;
    }
    public void setIsSensex(int isSensex) {
        this.isSensex = isSensex;
    }

    public int getIsNifty50() {
        return isNifty50;
    }
    public void setIsNifty50(int isNifty50) {
        this.isNifty50 = isNifty50;
    }

    public int getIsNiftyjr() {
        return isNiftyjr;
    }
    public void setIsNiftyjr(int isNiftyjr) {
        this.isNiftyjr = isNiftyjr;
    }

    public int getIsBse100() {
        return isBse100;
    }
    public void setIsBse100(int isBse100) {
        this.isBse100 = isBse100;
    }

    public int getIsNse100() {
        return isNse100;
    }
    public void setIsNse100(int isNse100) {
        this.isNse100 = isNse100;
    }

    public int getIsBse200() {
        return isBse200;
    }
    public void setIsBse200(int isBse200) {
        this.isBse200 = isBse200;
    }

    public int getIsNse200() {
        return isNse200;
    }
    public void setIsNse200(int isNse200) {
        this.isNse200 = isNse200;
    }

    public int getIsBse500() {
        return isBse500;
    }
    public void setIsBse500(int isBse500) {
        this.isBse500 = isBse500;
    }

    public int getIsNse500() {
        return isNse500;
    }
    public void setIsNse500(int isNse500) {
        this.isNse500 = isNse500;
    }

    public int getIsFno() {
        return isFno;
    }
    public void setIsFno(int isFno) {
        this.isFno = isFno;
    }

    public BigDecimal getMarketcap() {
        return marketcap;
    }
    public void setMarketcap(BigDecimal marketcap) {
        this.marketcap = marketcap;
    }

    public BigDecimal getPeTtm() {
        return peTtm;
    }
    public void setPeTtm(BigDecimal peTtm) {
        this.peTtm = peTtm;
    }

    public String getTickerOld() {
        return tickerOld;
    }
    public void setTickerOld(String tickerOld) {
        this.tickerOld = tickerOld;
    }

    public Date getListingDate() {
        return listingDate;
    }
    public void setListingDate(Date listingDate) {
        this.listingDate = listingDate;
    }

    public String getIncludedIndexName() {
        return includedIndexName;
    }
    public void setIncludedIndexName(String includedIndexName) {
        this.includedIndexName = includedIndexName;
    }

    public String getSectorNameDisplay() {
        return sectorNameDisplay;
    }
    public void setSectorNameDisplay(String sectorNameDisplay) {
        this.sectorNameDisplay = sectorNameDisplay;
    }

    public String getIndustryNameDisplay() {
        return industryNameDisplay;
    }
    public void setIndustryNameDisplay(String industryNameDisplay) {
        this.industryNameDisplay = industryNameDisplay;
    }

    public String getSubIndustryNameDisplay() {
        return subIndustryNameDisplay;
    }
    public void setSubIndustryNameDisplay(String subIndustryNameDisplay) {
        this.subIndustryNameDisplay = subIndustryNameDisplay;
    }

    public DailyDataS getDailyDataS() {
        return dailyDataS;
    }
    public void setDailyDataS(DailyDataS dailyDataS) {
        this.dailyDataS = dailyDataS;
    }

    public StockPriceMovement getStockPriceMovement() {
        return stockPriceMovement;
    }
    public void setStockPriceMovement(StockPriceMovement stockPriceMovement) {
        this.stockPriceMovement = stockPriceMovement;
    }

    public String getLatestMOSLReco() {
        return latestMOSLReco;
    }
    public void setLatestMOSLReco(String latestMOSLReco) {
        this.latestMOSLReco = latestMOSLReco;
    }

    public String getLatestAMBITReco() {
        return latestAMBITReco;
    }
    public void setLatestAMBITReco(String latestAMBITReco) {
        this.latestAMBITReco = latestAMBITReco;
    }

    public String getLatestAXISReco() {
        return latestAXISReco;
    }
    public void setLatestAXISReco(String latestAXISReco) {
        this.latestAXISReco = latestAXISReco;
    }

    public String getLatestICICIDIRECTReco() {
        return latestICICIDIRECTReco;
    }
    public void setLatestICICIDIRECTReco(String latestICICIDIRECTReco) {
        this.latestICICIDIRECTReco = latestICICIDIRECTReco;
    }

    public String getLatestPLReco() {
        return latestPLReco;
    }
    public void setLatestPLReco(String latestPLReco) {
        this.latestPLReco = latestPLReco;
    }

    public String getLatestKOTAKReco() {
        return latestKOTAKReco;
    }
    public void setLatestKOTAKReco(String latestKOTAKReco) {
        this.latestKOTAKReco = latestKOTAKReco;
    }

    public String getLatestOTHERReco() {
        return latestOTHERReco;
    }
    public void setLatestOTHERReco(String latestOTHERReco) {
        this.latestOTHERReco = latestOTHERReco;
    }

    public String getResultValuation() {
        return resultValuation;
    }
    public void setResultValuation(String resultValuation) {
        this.resultValuation = resultValuation;
    }

    public String getMinValuation() {
        return minValuation;
    }
    public void setMinValuation(String minValuation) {
        this.minValuation = minValuation;
    }

    public String getMaxValuation() {
        return maxValuation;
    }
    public void setMaxValuation(String maxValuation) {
        this.maxValuation = maxValuation;
    }

    public String getWacc() {
        return wacc;
    }
    public void setWacc(String wacc) {
        this.wacc = wacc;
    }

    public String getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getRevenueGrowthNext10yr() {
        return revenueGrowthNext10yr;
    }
    public void setRevenueGrowthNext10yr(String revenueGrowthNext10yr) {
        this.revenueGrowthNext10yr = revenueGrowthNext10yr;
    }

    public String getOpmNext10yr() {
        return opmNext10yr;
    }
    public void setOpmNext10yr(String opmNext10yr) {
        this.opmNext10yr = opmNext10yr;
    }

    public String getNetPpeByRevenue10yr() {
        return netPpeByRevenue10yr;
    }
    public void setNetPpeByRevenue10yr(String netPpeByRevenue10yr) {
        this.netPpeByRevenue10yr = netPpeByRevenue10yr;
    }

    public String getDepreciationByNetPpe10yr() {
        return depreciationByNetPpe10yr;
    }
    public void setDepreciationByNetPpe10yr(String depreciationByNetPpe10yr) {
        this.depreciationByNetPpe10yr = depreciationByNetPpe10yr;
    }

    public String getHistoricalRoic() {
        return historicalRoic;
    }
    public void setHistoricalRoic(String historicalRoic) {
        this.historicalRoic = historicalRoic;
    }

    public String getSecondAndTerminalStageAssumptions() {
        return secondAndTerminalStageAssumptions;
    }
    public void setSecondAndTerminalStageAssumptions(String secondAndTerminalStageAssumptions) {
        this.secondAndTerminalStageAssumptions = secondAndTerminalStageAssumptions;
    }

    public String getOtherIncGrowthAssumptions() {
        return otherIncGrowthAssumptions;
    }
    public void setOtherIncGrowthAssumptions(String otherIncGrowthAssumptions) {
        this.otherIncGrowthAssumptions = otherIncGrowthAssumptions;
    }

    public List<CustomChartData> getExcelCharts() {
        return excelCharts;
    }
    public void setExcelCharts(List<CustomChartData> excelCharts) {
        this.excelCharts = excelCharts;
    }

    public List<ReportNotes> getReportNotes() {
        return reportNotes;
    }
    public void setReportNotes(List<ReportNotes> reportNotes) {
        this.reportNotes = reportNotes;
    }
}