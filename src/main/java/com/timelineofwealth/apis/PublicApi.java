package com.timelineofwealth.apis;

import com.timelineofwealth.dto.*;
import com.timelineofwealth.dto.IndexMonthlyReturnsDTO;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "public/api/")
public class PublicApi {
    private final Logger logger = LoggerFactory.getLogger(PublicApi.class);
    private final CommonService commonService;

    public PublicApi(CommonService commonService){
        this.commonService = commonService;
    }

    @RequestMapping(value = "/getDates", method = RequestMethod.GET)
    public SetupDates getSetupDates() {
//        logger.debug(String.format("Call public/api/getSetupDates/"));

        return commonService.getSetupDates();
    }

    @RequestMapping(value = "/getassetclassifications", method = RequestMethod.GET)
    public List<AssetClassification> getAssetClassfication() {
        logger.debug(String.format("Call public/api/getassetclassifications/"));

        return commonService.getAssetClassfication();
    }

    @RequestMapping(value = "/getsubindustries", method = RequestMethod.GET)
    public List<Subindustry> getSubindusties() {
        logger.debug(String.format("Call public/api/getsubindustries/"));

        return commonService.getSubindustries();
    }

    @RequestMapping(value = "/getdistinctfundhouse", method = RequestMethod.GET)
    public List<String> getDistinctFundHouse() {
        logger.debug(String.format("Call public/api/getdistinctfundhouse/"));

        return commonService.getDistinctFundHouse();
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse) {
        logger.debug(String.format("Call public/api/getschemenames/" + fundHouse));

        return commonService.getSchemeNames(fundHouse);
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}/{directRegular}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse, @PathVariable String directRegular) {
        logger.debug(String.format("Call public/api/getschemenames/"+fundHouse+"/"+directRegular));

        return commonService.getSchemeNames(fundHouse,directRegular);
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}/{directRegular}/{dividendGrowth}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse, @PathVariable String directRegular, @PathVariable String dividendGrowth) {
        logger.debug(String.format("Call public/api/getschemenames/"+fundHouse+"/"+directRegular+"/"+dividendGrowth));

        return commonService.getSchemeNames(fundHouse,directRegular,dividendGrowth);
    }

    @RequestMapping(value = "/getschemedetails/{fundHouse}/{category}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeDetails(@PathVariable String fundHouse, @PathVariable String category) {
        logger.debug(String.format("Call public/api/getschemedetails/"+fundHouse+"/"+category));

        return commonService.getSchemeDetails(fundHouse,category);
    }

    @RequestMapping(value = "/getselectedmf", method = RequestMethod.GET)
    public List<MutualFundStats> getSelectedMF() {
        logger.debug(String.format("Call public/api/getschemedetails/"));

        return commonService.getSelectedMF();
    }

    @RequestMapping(value = "/getallstocks", method = RequestMethod.GET)
    public List<StockUniverse> getAllStocks(){
        logger.debug(String.format("Call public/api/getallstocks/"));

        return commonService.getAllStocks();
    }

    @RequestMapping(value = "/getstockvaluationhistory/{ticker}", method = RequestMethod.GET)
    public List<StockValuationHistory> getStockValuationHistory(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockvaluationhistory/"+ticker));

        return commonService.getStockValuationHistory(ticker);
    }

    @RequestMapping(value = "/getrecentvaluations/{ticker}", method = RequestMethod.GET)
    public List<RecentValuations> getRecentValuations(@PathVariable String ticker){
        logger.debug((String.format("Call public/api/getrecentpe"+ticker)));
        return commonService.getRecentValuations(ticker);
    }

    @RequestMapping(value = "/getpricemovements/{ticker}", method = RequestMethod.GET)
    public List<StockPriceMovementHistory> getPriceMovements(@PathVariable String ticker){
        logger.debug((String.format("Call public/api/getpricemovements"+ticker)));
        return commonService.getPriceMovements(ticker);
    }

    @RequestMapping(value = "/getstockpnl/{ticker}", method = RequestMethod.GET)
    public List<StockPnl> getStockPnl(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockpnl/"+ticker));

        return commonService.getStockPnl(ticker);
    }

    @RequestMapping(value = "/getstockquarter/{ticker}", method = RequestMethod.GET)
    public List<StockQuarter> getStockQuarter(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockquarter/"+ticker));

        return commonService.getStockQuarter(ticker);
    }

    @RequestMapping(value = "/getnsebse500", method = RequestMethod.GET)
    public List<NseBse500> getNseBse500(){
        logger.debug(String.format("Call public/api/getnsebse500/"));

        return commonService.getNseBse500();
    }

    @RequestMapping(value = "/getstockdetails/{ticker}", method = RequestMethod.GET)
    public NseBse500 getStockDetails(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockdetails/"+ticker));

        return commonService.getStockDetails(ticker);
    }

    @RequestMapping(value = "/getindexvaluation", method = RequestMethod.GET)
    public List<IndexValuation> getIndexValutiaon(){
        logger.debug(String.format("Call public/api/getindexvaluation"));
        return commonService.getIndexValuation();
    }

    @RequestMapping(value = "/getindexmonthlyreturns/{ticker}", method = RequestMethod.GET)
    public List<IndexMonthlyReturnsDTO> getIndexMonthlyReturns(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getindexmonthlyreturns/%s", ticker));
        return commonService.getIndexMonthlyReturns(ticker);
    }

    @RequestMapping(value = "/getindexstatistics", method = RequestMethod.GET)
    public List<IndexStatistics> getIndexStatistics(){
        logger.debug(String.format("Call public/api/getindexstatistics"));
        return commonService.getIndexReturnStatistics("NIFTY");
    }

    @RequestMapping(value = "/getindexreturnstats", method = RequestMethod.GET)
    public List<IndexStatistics> getindexreturnstats(){
        logger.debug(String.format("Call public/api/getindexreturnstats"));
        return commonService.getIndexReturnStatistics("ALL");
    }

    @RequestMapping(value = "/getbenchmarktwrrsummary", method = RequestMethod.GET)
    public List<BenchmarkTwrrSummaryDTO> getBenchmarkTwrrSummary() {
        logger.debug(String.format("Call user/api/getbenchmarktwrrsummary/"));

        return commonService.getBenchmarkTwrrSummary();
    }

    @RequestMapping(value = "/getbenchmarktwrrmonthly", method = RequestMethod.GET)
    public List<BenchmarkTwrrMonthlyDTO> getBenchmarkTwrrMonthly() {
        logger.debug(String.format("Call user/api/getbenchmarktwrrmonthly/"));
        return commonService.getBenchmarkTwrrMonthly();
    }

    @RequestMapping(value = "/getpricehistory", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getPriceHistory(@RequestParam List<String> tickers,
                                                     @RequestParam(required = false, defaultValue = "ALL") String range,
                                                     @RequestParam(required = false, defaultValue = "NIFTY") String index,
                                                     @RequestParam(required = false) String from,
                                                     @RequestParam(required = false) String to) {
        List<Map<String, Object>> result = new ArrayList<>();

        if ("custom".equalsIgnoreCase(range) && (from != null || to != null)) {
            for (String ticker : tickers) {
                List<Map<String, Object>> prices = commonService.getPriceSeries(ticker, from, to);
                Map<String, Object> stockMap = new HashMap<>();
                stockMap.put("ticker", ticker);
                stockMap.put("prices", prices);
                result.add(stockMap);
            }

            List<Map<String, Object>> indexPrices = commonService.getPriceSeries(index, from, to);
            Map<String, Object> indexMap = new HashMap<>();
            indexMap.put("ticker", index);
            indexMap.put("prices", indexPrices);
            result.add(indexMap);
        } else {
            for (String ticker : tickers) {
                List<Map<String, Object>> prices = commonService.getPriceSeries(ticker, range);
                Map<String, Object> stockMap = new HashMap<>();
                stockMap.put("ticker", ticker);
                stockMap.put("prices", prices);
                result.add(stockMap);
            }

            List<Map<String, Object>> indexPrices = commonService.getPriceSeries(index, range);
            Map<String, Object> indexMap = new HashMap<>();
            indexMap.put("ticker", index);
            indexMap.put("prices", indexPrices);
            result.add(indexMap);
        }

        return result;
    }

    @RequestMapping(value = "/getmarketcaphistory", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getMarketCapHistory(
            @RequestParam List<String> tickers,
            @RequestParam(required = false, defaultValue = "ALL") String range,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        List<Map<String, Object>> output = new ArrayList<>();

        for (String ticker : tickers) {
            List<Map<String, Object>> series;

            // Use from/to only when range is 'custom'
            if ("custom".equalsIgnoreCase(range) && (from != null || to != null)) {
                series = commonService.getMarketCapSeries(ticker, "custom", from, to);
            } else {
                series = commonService.getMarketCapSeries(ticker, range, null, null);
            }

            Map<String, Object> entry = new HashMap<>();
            entry.put("ticker", ticker);
            entry.put("series", series);
            output.add(entry);
        }

        return output;
    }

    @RequestMapping(value = "/getttmpehistory", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getTtmPeHistory(
            @RequestParam List<String> tickers,
            @RequestParam(required = false, defaultValue = "NIFTY") String index,
            @RequestParam(required = false, defaultValue = "ALL") String range,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (String ticker : tickers) {
            List<Map<String, Object>> series;
            if ("custom".equalsIgnoreCase(range) && (from != null || to != null)) {
                series = commonService.computeStockTtmPeSeries(ticker, from, to);
            } else {
                series = commonService.computeStockTtmPeSeries(ticker, range);
            }

            if (!series.isEmpty()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ticker", ticker);
                row.put("series", series);
                result.add(row);
            }
        }

        // Handle index TTM PE similarly
        List<Map<String, Object>> indexSeries;
        if ("custom".equalsIgnoreCase(range) && (from != null || to != null)) {
            indexSeries = commonService.computeIndexTtmPeSeries(index, from, to);
        } else {
            indexSeries = commonService.computeIndexTtmPeSeries(index, range);
        }

        if (!indexSeries.isEmpty()) {
            Map<String, Object> row = new HashMap<>();
            row.put("ticker", index);
            row.put("series", indexSeries);
            result.add(row);
        }

        return result;
    }

}
