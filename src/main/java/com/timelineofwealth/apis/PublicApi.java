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
    private static final Logger logger = LoggerFactory.getLogger(PublicApi.class);

    @RequestMapping(value = "/getDates", method = RequestMethod.GET)
    public SetupDates getSetupDates() {
//        logger.debug(String.format("Call public/api/getSetupDates/"));

        return CommonService.getSetupDates();
    }

    @RequestMapping(value = "/getassetclassifications", method = RequestMethod.GET)
    public List<AssetClassification> getAssetClassfication() {
        logger.debug(String.format("Call public/api/getassetclassifications/"));

        return CommonService.getAssetClassfication();
    }

    @RequestMapping(value = "/getsubindustries", method = RequestMethod.GET)
    public List<Subindustry> getSubindusties() {
        logger.debug(String.format("Call public/api/getsubindustries/"));

        return CommonService.getSubindustries();
    }

    @RequestMapping(value = "/getdistinctfundhouse", method = RequestMethod.GET)
    public List<String> getDistinctFundHouse() {
        logger.debug(String.format("Call public/api/getdistinctfundhouse/"));

        return CommonService.getDistinctFundHouse();
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse) {
        logger.debug(String.format("Call public/api/getschemenames/" + fundHouse));

        return CommonService.getSchemeNames(fundHouse);
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}/{directRegular}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse, @PathVariable String directRegular) {
        logger.debug(String.format("Call public/api/getschemenames/"+fundHouse+"/"+directRegular));

        return CommonService.getSchemeNames(fundHouse,directRegular);
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}/{directRegular}/{dividendGrowth}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse, @PathVariable String directRegular, @PathVariable String dividendGrowth) {
        logger.debug(String.format("Call public/api/getschemenames/"+fundHouse+"/"+directRegular+"/"+dividendGrowth));

        return CommonService.getSchemeNames(fundHouse,directRegular,dividendGrowth);
    }

    @RequestMapping(value = "/getschemedetails/{fundHouse}/{category}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeDetails(@PathVariable String fundHouse, @PathVariable String category) {
        logger.debug(String.format("Call public/api/getschemedetails/"+fundHouse+"/"+category));

        return CommonService.getSchemeDetails(fundHouse,category);
    }

    @RequestMapping(value = "/getselectedmf", method = RequestMethod.GET)
    public List<MutualFundStats> getSelectedMF() {
        logger.debug(String.format("Call public/api/getschemedetails/"));

        return CommonService.getSelectedMF();
    }

    @RequestMapping(value = "/getallstocks", method = RequestMethod.GET)
    public List<StockUniverse> getAllStocks(){
        logger.debug(String.format("Call public/api/getallstocks/"));

        return CommonService.getAllStocks();
    }

    @RequestMapping(value = "/getstockvaluationhistory/{ticker}", method = RequestMethod.GET)
    public List<StockValuationHistory> getStockValuationHistory(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockvaluationhistory/"+ticker));

        return CommonService.getStockValuationHistory(ticker);
    }

    @RequestMapping(value = "/getrecentvaluations/{ticker}", method = RequestMethod.GET)
    public List<RecentValuations> getRecentValuations(@PathVariable String ticker){
        logger.debug((String.format("Call public/api/getrecentpe"+ticker)));
        return CommonService.getRecentValuations(ticker);
    }

    @RequestMapping(value = "/getpricemovements/{ticker}", method = RequestMethod.GET)
    public List<StockPriceMovementHistory> getPriceMovements(@PathVariable String ticker){
        logger.debug((String.format("Call public/api/getpricemovements"+ticker)));
        return CommonService.getPriceMovements(ticker);
    }

    @RequestMapping(value = "/getstockpnl/{ticker}", method = RequestMethod.GET)
    public List<StockPnl> getStockPnl(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockpnl/"+ticker));

        return CommonService.getStockPnl(ticker);
    }

    @RequestMapping(value = "/getstockquarter/{ticker}", method = RequestMethod.GET)
    public List<StockQuarter> getStockQuarter(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockquarter/"+ticker));

        return CommonService.getStockQuarter(ticker);
    }

    @RequestMapping(value = "/getnsebse500", method = RequestMethod.GET)
    public List<NseBse500> getNseBse500(){
        logger.debug(String.format("Call public/api/getnsebse500/"));

        return CommonService.getNseBse500();
    }

    @RequestMapping(value = "/getstockdetails/{ticker}", method = RequestMethod.GET)
    public NseBse500 getStockDetails(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getstockdetails/"+ticker));

        return CommonService.getStockDetails(ticker);
    }

    @RequestMapping(value = "/getindexvaluation", method = RequestMethod.GET)
    public List<IndexValuation> getIndexValutiaon(){
        logger.debug(String.format("Call public/api/getindexvaluation"));
        return CommonService.getIndexValuation();
    }

    @RequestMapping(value = "/getindexmonthlyreturns/{ticker}", method = RequestMethod.GET)
    public List<IndexMonthlyReturnsDTO> getIndexMonthlyReturns(@PathVariable String ticker){
        logger.debug(String.format("Call public/api/getindexmonthlyreturns/%s", ticker));
        return CommonService.getIndexMonthlyReturns(ticker);
    }

    @RequestMapping(value = "/getindexstatistics", method = RequestMethod.GET)
    public List<IndexStatistics> getIndexStatistics(){
        logger.debug(String.format("Call public/api/getindexstatistics"));
        return CommonService.getIndexReturnStatistics("NIFTY");
    }

    @RequestMapping(value = "/getindexreturnstats", method = RequestMethod.GET)
    public List<IndexStatistics> getindexreturnstats(){
        logger.debug(String.format("Call public/api/getindexreturnstats"));
        return CommonService.getIndexReturnStatistics("ALL");
    }

    @RequestMapping(value = "/getbenchmarktwrrsummary", method = RequestMethod.GET)
    public List<BenchmarkTwrrSummaryDTO> getBenchmarkTwrrSummary() {
        logger.debug(String.format("Call user/api/getbenchmarktwrrsummary/"));

        return CommonService.getBenchmarkTwrrSummary();
    }

    @RequestMapping(value = "/getbenchmarktwrrmonthly", method = RequestMethod.GET)
    public List<BenchmarkTwrrMonthlyDTO> getBenchmarkTwrrMonthly() {
        logger.debug(String.format("Call user/api/getbenchmarktwrrmonthly/"));
        return CommonService.getBenchmarkTwrrMonthly();
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
                List<Map<String, Object>> prices = CommonService.getPriceSeries(ticker, from, to);
                Map<String, Object> stockMap = new HashMap<>();
                stockMap.put("ticker", ticker);
                stockMap.put("prices", prices);
                result.add(stockMap);
            }

            List<Map<String, Object>> indexPrices = CommonService.getPriceSeries(index, from, to);
            Map<String, Object> indexMap = new HashMap<>();
            indexMap.put("ticker", index);
            indexMap.put("prices", indexPrices);
            result.add(indexMap);
        } else {
            for (String ticker : tickers) {
                List<Map<String, Object>> prices = CommonService.getPriceSeries(ticker, range);
                Map<String, Object> stockMap = new HashMap<>();
                stockMap.put("ticker", ticker);
                stockMap.put("prices", prices);
                result.add(stockMap);
            }

            List<Map<String, Object>> indexPrices = CommonService.getPriceSeries(index, range);
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
                series = CommonService.getMarketCapSeries(ticker, "custom", from, to);
            } else {
                series = CommonService.getMarketCapSeries(ticker, range, null, null);
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
                series = CommonService.computeStockTtmPeSeries(ticker, from, to);
            } else {
                series = CommonService.computeStockTtmPeSeries(ticker, range);
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
            indexSeries = CommonService.computeIndexTtmPeSeries(index, from, to);
        } else {
            indexSeries = CommonService.computeIndexTtmPeSeries(index, range);
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
