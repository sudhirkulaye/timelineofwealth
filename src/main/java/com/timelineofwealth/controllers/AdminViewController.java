package com.timelineofwealth.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timelineofwealth.apis.PublicApi;
import com.timelineofwealth.dto.DailyDataBJSON;
import com.timelineofwealth.dto.Ticker;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import com.timelineofwealth.service.*;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@Controller
public class AdminViewController {

    private static final Logger logger = LoggerFactory.getLogger(AdminViewController.class);
    private java.sql.Date dateToday;
    public static final String AP_PROCESS_EOD = "ap_process_eod";
    public static final String AP_PROCESS_MOSL_TRANSACTIONS = "ap_process_mosl_transactions";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    NsePriceHistoryRepository nsePriceHistoryRepository;
    @Autowired
    public void setNsePriceHistoryRepository(NsePriceHistoryRepository nsePriceHistoryRepository){
        this.nsePriceHistoryRepository = nsePriceHistoryRepository;
    }
    @Autowired
    BsePriceHistoryRepository bsePriceHistoryRepository;
    @Autowired
    public void setBsePriceHistoryRepository(BsePriceHistoryRepository bsePriceHistoryRepository){
        this.bsePriceHistoryRepository = bsePriceHistoryRepository;
    }
    @Autowired
    MutualFundNavHistoryRepository mutualFundNavHistoryRepository;
    @Autowired
    public void setMutualFundNavHistoryRepository(MutualFundNavHistoryRepository mutualFundNavHistoryRepository){
        this.mutualFundNavHistoryRepository = mutualFundNavHistoryRepository;
    }
    @Autowired
    MutualFundUniverseRepository mutualFundUniverseRepository;
    @Autowired
    public void setMutualFundUniverseRepository(MutualFundUniverseRepository mutualFundUniverseRepository){
        this.mutualFundNavHistoryRepository = mutualFundNavHistoryRepository;
    }
    @Autowired
    DailyDataBRepository dailyDataBRepository;
    @Autowired
    public void setDailyDataBRepository(DailyDataBRepository dailyDataBRepository){
        this.dailyDataBRepository = dailyDataBRepository;
    }
    @Autowired
    DailyDataSRepository dailyDataSRepository;
    @Autowired
    public void setDailyDataSRepository(DailyDataSRepository dailyDataSRepository){
        this.dailyDataSRepository = dailyDataSRepository;
    }
    @Autowired
    MOSLTransactionRepository moslTransactionRepository;
    @Autowired
    public void setMoslTransactionRepository(MOSLTransactionRepository moslTransactionRepository){
        this.moslTransactionRepository = moslTransactionRepository;
    }
    @Autowired
    IndexValuationRepository indexValuationRepository;
    public void setIndexValuationRepository(IndexValuationRepository indexValuationRepository){
        this.indexValuationRepository = indexValuationRepository;
    }
    @Autowired
    public AdminViewController(Environment environment){}

    @RequestMapping(value = "/admin/uploadnsedailypricedata")
    public String uploadNseDailyPriceData(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadnsedailypricedata";
    }

    @RequestMapping(value=("/admin/uploadnsedailypricedatastatus"),headers=("content-type=multipart/*"),method= RequestMethod.POST)
    public String uploadNseDailyPriceDataStatus (Model model, @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploadnsedailypricedata";
        }
        try {
            File csvFile = new File(file.getOriginalFilename());
            csvFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(file.getBytes());
            fos.close();
//            file.transferTo(csvFile);
            Scanner scanner = new Scanner(csvFile);
            boolean isHeader = true;
            CSVUtils csvUtils = new CSVUtils();
            List<NsePriceHistory> nsePriceHistories = new ArrayList<>();
            int nseTickerPosition = -1, seriesPosition = -1, openPosition = -1,
                    highPosition = -1, lowPosition = -1, closePosition = -1,
                    lastPosition = -1, previousClosePosition = -1, totalTradedQuantityPosition = -1,
                    totalTradedValuePosition = -1, dateStringPosition = -1, totalTradesPosition = -1, isinCodePosition = -1;
            while (scanner.hasNext()) {
                List<String> line = csvUtils.parseLine(scanner.nextLine());
                if(isHeader){
                    isHeader = false;
                    for (int i=0; i<line.size(); i++) {
                        String column = line.get(i);
                        if (column.trim().equalsIgnoreCase("TckrSymb")){
                            nseTickerPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("SctySrs")){
                            seriesPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("OpnPric")){
                            openPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("HghPric")){
                            highPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LwPric")){
                            lowPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ClsPric")){
                            closePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LastPric")){
                            lastPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("PrvsClsgPric")){
                            previousClosePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TtlTradgVol")){
                            totalTradedQuantityPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TtlTrfVal")){
                            totalTradedValuePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TradDt")){
                            dateStringPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TtlNbOfTxsExctd")){
                            totalTradesPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISIN") ){
                            isinCodePosition = i;
                        }
                    }
                    continue;
                }
                if (nseTickerPosition == -1 || dateStringPosition == -1){
                    continue;
                }
                String nseTicker = line.get(nseTickerPosition);
                String dateString = line.get(dateStringPosition);
                if(nseTicker == null || nseTicker.isEmpty()|| dateString == null || dateString.isEmpty()){
                    continue;
                }
                String series = line.get(seriesPosition);
                if(!(series.equalsIgnoreCase("EQ") ||
                        series.equalsIgnoreCase("BM") ||
                        series.equalsIgnoreCase("SM") ||
                        series.equalsIgnoreCase("BE"))){
                    continue;
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                java.sql.Date date = null;
                try {
                    date = new java.sql.Date(format.parse(dateString).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                BigDecimal openPrice = new BigDecimal(0);
                if(openPosition > -1){
                    openPrice = line.get(openPosition) != null && !line.get(openPosition).isEmpty() ? new BigDecimal(line.get(openPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal highPrice = new BigDecimal(0);
                if(highPosition > -1) {
                    highPrice = line.get(highPosition) != null && !line.get(highPosition).isEmpty() ? new BigDecimal(line.get(highPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal lowPrice = new BigDecimal(0);
                if(lowPosition > -1) {
                    lowPrice = line.get(lowPosition) != null && !line.get(lowPosition).isEmpty() ? new BigDecimal(line.get(lowPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal closePrice = new BigDecimal(0);
                if (closePosition > -1) {
                    closePrice = line.get(closePosition) != null && !line.get(closePosition).isEmpty() ? new BigDecimal(line.get(closePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal lastPrice = new BigDecimal(0);
                if (lastPosition > -1) {
                    lastPrice = line.get(lastPosition) != null && !line.get(lastPosition).isEmpty() ? new BigDecimal(line.get(lastPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal previousClosePrice = new BigDecimal(0);
                if (previousClosePosition > -1) {
                    previousClosePrice = line.get(previousClosePosition) != null && !line.get(previousClosePosition).isEmpty() ? new BigDecimal(line.get(previousClosePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTradedQuantity = new BigDecimal(0);
                if (totalTradedQuantityPosition > -1){
                    totalTradedQuantity = line.get(totalTradedQuantityPosition) != null && !line.get(totalTradedQuantityPosition).isEmpty() ? new BigDecimal(line.get(totalTradedQuantityPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTradedValue = new BigDecimal(0);
                if (totalTradesPosition > -1) {
                    totalTradedValue = line.get(totalTradedValuePosition) != null && !line.get(totalTradedValuePosition).isEmpty() ? new BigDecimal(line.get(totalTradedValuePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTrades = new BigDecimal(0);
                if (totalTradesPosition > -1) {
                    totalTrades = line.get(totalTradesPosition) != null && !line.get(totalTradesPosition).isEmpty() ? new BigDecimal(line.get(totalTradesPosition).trim()) : new BigDecimal(0);
                }

                String isinCode = "";
                if (isinCodePosition > -1) { isinCode = line.get(isinCodePosition);}

                NsePriceHistory nsePriceHistory = new NsePriceHistory();
                nsePriceHistory.setKey(new NsePriceHistory.NsePriceHistoryKey(nseTicker, date));
                nsePriceHistory.setSeries(series);
                nsePriceHistory.setOpenPrice(openPrice);
                nsePriceHistory.setHighPrice(highPrice);
                nsePriceHistory.setLowPrice(lowPrice);
                nsePriceHistory.setClosePrice(closePrice);
                nsePriceHistory.setLastPrice(lastPrice);
                nsePriceHistory.setPreviousClosePrice(previousClosePrice);
                nsePriceHistory.setTotalTradedQuantity(totalTradedQuantity);
                nsePriceHistory.setTotalTradedValue(totalTradedValue);
                nsePriceHistory.setTotalTrades(totalTrades);
                nsePriceHistory.setInsinCode(isinCode);

                nsePriceHistories.add(nsePriceHistory);
            }
            //nsePriceHistories.sort(Comparator.comparing(CompanyDailyDataG::getCompanyDailyMarketCapNo).reversed());
            nsePriceHistoryRepository.saveAll(nsePriceHistories);
            scanner.close();
            csvFile.delete();

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploadnsedailypricedata";
    }

    @RequestMapping(value = "/admin/uploadbsedailypricedata")
    public String uploadBseDailyPriceData(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadbsedailypricedata";
    }

    @RequestMapping(value=("/admin/uploadbsedailypricedatastatus"),headers=("content-type=multipart/*"),method= RequestMethod.POST)
    public String uploadBseDailyPriceDataStatus (Model model, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploadbsedailypricedata";
        }
        try {
            File csvFile = new File(file.getOriginalFilename());
            csvFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(file.getBytes());
            fos.close();
//            file.transferTo(csvFile);
            Scanner scanner = new Scanner(csvFile);
            boolean isHeader = true;
            CSVUtils csvUtils = new CSVUtils();
            List<BsePriceHistory> bsePriceHistories = new ArrayList<>();
            int bseTickerPosition = -1, bseCompanyNamePosition = -1, companyGroupPosition = -1,
                    openPosition = -1, highPosition = -1, lowPosition = -1, closePosition = -1, lastPosition = -1, previousClosePosition = -1,
                    totalTradesPosition = -1, totalTradedQuantityPosition = -1, totalTradedValuePosition = -1,
                    isinCodePosition = -1, dateStringPosition = -1;
            while (scanner.hasNext()) {
                List<String> line = csvUtils.parseLine(scanner.nextLine());
                if(isHeader){
                    isHeader = false;
                    for (int i=0; i<line.size(); i++) {
                        String column = line.get(i);
                        if (column.trim().equalsIgnoreCase("FinInstrmId")){
                            bseTickerPosition = i;
                        } // TckrSymb
                        if (column.trim().equalsIgnoreCase("TckrSymb")){
                            bseCompanyNamePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("SctySrs")){
                            companyGroupPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("OpnPric")){
                            openPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("HghPric")){
                            highPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LwPric")){
                            lowPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ClsPric")){
                            closePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LastPric")){
                            lastPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("PrvsClsgPric")){
                            previousClosePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TtlTradgVol")){
                            totalTradedQuantityPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TtlTrfVal")){
                            totalTradedValuePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TradDt")){
                            dateStringPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TtlNbOfTxsExctd")){
                            totalTradesPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISIN") ){
                            isinCodePosition = i;
                        }
                    }
                    continue;
                }
                if (bseTickerPosition == -1 || dateStringPosition == -1){
                    continue;
                }
                String bseTicker = line.get(bseTickerPosition);
                String dateString = line.get(dateStringPosition);
                String companyName = line.get(bseCompanyNamePosition);
                if(bseTicker == null || bseTicker.isEmpty()|| dateString == null || dateString.isEmpty()){
                    continue;
                }
                String companyGroup = line.get(companyGroupPosition).trim();
                if(!(companyGroup.equalsIgnoreCase("A") ||
                        companyGroup.equalsIgnoreCase("B") ||
                        companyGroup.equalsIgnoreCase("T"))){
                    continue;
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                java.sql.Date date = null;
                try {
                    date = new java.sql.Date(format.parse(dateString).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                BigDecimal openPrice = new BigDecimal(0);
                if(openPosition > -1){
                    openPrice = line.get(openPosition) != null && !line.get(openPosition).isEmpty() ? new BigDecimal(line.get(openPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal highPrice = new BigDecimal(0);
                if(highPosition > -1) {
                    highPrice = line.get(highPosition) != null && !line.get(highPosition).isEmpty() ? new BigDecimal(line.get(highPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal lowPrice = new BigDecimal(0);
                if(lowPosition > -1) {
                    lowPrice = line.get(lowPosition) != null && !line.get(lowPosition).isEmpty() ? new BigDecimal(line.get(lowPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal closePrice = new BigDecimal(0);
                if (closePosition > -1) {
                    closePrice = line.get(closePosition) != null && !line.get(closePosition).isEmpty() ? new BigDecimal(line.get(closePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal lastPrice = new BigDecimal(0);
                if (lastPosition > -1) {
                    lastPrice = line.get(lastPosition) != null && !line.get(lastPosition).isEmpty() ? new BigDecimal(line.get(lastPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal previousClosePrice = new BigDecimal(0);
                if (previousClosePosition > -1) {
                    previousClosePrice = line.get(previousClosePosition) != null && !line.get(previousClosePosition).isEmpty() ? new BigDecimal(line.get(previousClosePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTradedQuantity = new BigDecimal(0);
                if (totalTradedQuantityPosition > -1){
                    totalTradedQuantity = line.get(totalTradedQuantityPosition) != null && !line.get(totalTradedQuantityPosition).isEmpty() ? new BigDecimal(line.get(totalTradedQuantityPosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTradedValue = new BigDecimal(0);
                if (totalTradesPosition > -1) {
                    totalTradedValue = line.get(totalTradedValuePosition) != null && !line.get(totalTradedValuePosition).isEmpty() ? new BigDecimal(line.get(totalTradedValuePosition).trim()) : new BigDecimal(0);
                }

                BigDecimal totalTrades = new BigDecimal(0);
                if (totalTradesPosition > -1) {
                    totalTrades = line.get(totalTradesPosition) != null && !line.get(totalTradesPosition).isEmpty() ? new BigDecimal(line.get(totalTradesPosition).trim()) : new BigDecimal(0);
                }

                String isinCode = "";
                if (isinCodePosition > -1) { isinCode = line.get(isinCodePosition);}

                BsePriceHistory bsePriceHistory = new BsePriceHistory();
                bsePriceHistory.setKey(new BsePriceHistory.BsePriceHistoryKey(bseTicker, date));
                bsePriceHistory.setCompanyName(companyName);
                bsePriceHistory.setCompanyGroup(companyGroup);
                bsePriceHistory.setCompanyType("Q");
                bsePriceHistory.setOpenPrice(openPrice);
                bsePriceHistory.setHighPrice(highPrice);
                bsePriceHistory.setLowPrice(lowPrice);
                bsePriceHistory.setClosePrice(closePrice);
                bsePriceHistory.setLastPrice(lastPrice);
                bsePriceHistory.setPreviousClosePrice(previousClosePrice);
                bsePriceHistory.setTotalTradedQuantity(totalTradedQuantity);
                bsePriceHistory.setTotalTradedValue(totalTradedValue);
                bsePriceHistory.setTotalTrades(totalTrades);
                bsePriceHistory.setInsinCode(isinCode);

                bsePriceHistories.add(bsePriceHistory);
            }
            //bsePriceHistories.sort(Comparator.comparing(CompanyDailyDataG::getCompanyDailyMarketCapNo).reversed());
            bsePriceHistoryRepository.saveAll(bsePriceHistories);
            scanner.close();
            csvFile.delete();

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploadbsedailypricedata";
    }

    @RequestMapping(value = "/admin/uploadmfnavdata")
    public String uploadMfNavData(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadmfnavdata";
    }

    @RequestMapping(value=("/admin/uploadmfnavdatastatus"),headers=("content-type=multipart/*"),method= RequestMethod.POST)
    public String uploadMfNavDataStatus (Model model, @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploadmfnavdata";
        }
        try {
            File csvFile = new File(file.getOriginalFilename());
            csvFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(file.getBytes());
            fos.close();
//            file.transferTo(csvFile);
            Scanner scanner = new Scanner(csvFile);
            boolean isHeader = true;
            CSVUtils csvUtils = new CSVUtils();
            List<MutualFundNavHistory> mutualFundNavHistories = new ArrayList<>();
            int positionOfSchemeCode = 0, positionOfDate = 0, positionOfNav = 0, schemeNamePostion = 0, isinPosition = 0, isinReinvestmentPosition = 0;
            while (scanner.hasNext()) {
                List<String> line = csvUtils.parseLine(scanner.nextLine(),';');
                if(isHeader){
                    isHeader = false;
                    for (int i=0; i<line.size(); i++) {
                        String column = line.get(i);
                        if (column.trim().equalsIgnoreCase("Scheme Code")){
                            positionOfSchemeCode = i;
                        }
                        if (column.trim().equalsIgnoreCase("Net Asset Value") ||
                                column.trim().equalsIgnoreCase("NAV")){
                            positionOfNav = i;
                        }
                        if (column.trim().equalsIgnoreCase("Date")){
                            positionOfDate = i;
                        }
                        if (column.trim().equalsIgnoreCase("Scheme Name")){
                            schemeNamePostion = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISIN Div Payout/ISIN Growth")){
                            isinPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISIN Div Reinvestment")){
                            isinReinvestmentPosition = i;
                        }
                    }
                    continue;
                }
                if(line.isEmpty()|| line.size() < 2){ //any blank spaces
                    continue;
                }
                Integer code = Integer.parseInt(line.get(positionOfSchemeCode));
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                java.sql.Date date = null;

                try {
                    date = new java.sql.Date(format.parse(line.get(positionOfDate)).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                BigDecimal nav = null;
                try {
                    nav = line.get(positionOfNav)!=null && !line.get(positionOfNav).isEmpty() ? new BigDecimal(line.get(positionOfNav)) : new BigDecimal(0);
                } catch (NumberFormatException e) {
                    logger.debug(String.format("/Error in NAV for MutualFund(List)/%d/", code));
                    nav = new BigDecimal(0);
                }
                String schemeName = line.get(schemeNamePostion);
                schemeName.trim();
                String isin = "";
                if(isinPosition != 0) {
                    isin = line.get(isinPosition);
                }
                String isinReinvestment = "";
                if(isinReinvestmentPosition != 0) {
                    isinReinvestment = line.get(isinReinvestmentPosition);
                }

                MutualFundNavHistory mutualFundNavHistory = new MutualFundNavHistory();
                mutualFundNavHistory.setKey(new MutualFundNavHistory.MutualFundNavHistoryKey(code, date));
                mutualFundNavHistory.setNav(nav);
                mutualFundNavHistories.add(mutualFundNavHistory);
                int countBySchemeCode = mutualFundUniverseRepository.countBySchemeCode(new Long(code));

                if (countBySchemeCode == 0 && date.after(CommonService.getSetupDates().getDateLastTradingDay())){
                    MutualFundUniverse mutualFundUniverse = new MutualFundUniverse();
                    mutualFundUniverse.setSchemeCode(new Long(code));
                    mutualFundUniverse.setSchemeNameFull(schemeName);
                    mutualFundUniverse.setIsinDivPayoutIsinGrowth(isin);
                    mutualFundUniverse.setIsinDivReinvestment(isinReinvestment);
                    mutualFundUniverse.setDateLatestNav(date);
                    mutualFundUniverse.setFundHouse("XXX");
                    mutualFundUniverse.setSchemeNamePart("XXX");
                    mutualFundUniverse.setLatestNav(nav);
                    if(schemeName.toLowerCase().contains("direct")){
                        mutualFundUniverse.setDirectRegular("Direct");
                    }
                    if(schemeName.toLowerCase().contains("regular")){
                        mutualFundUniverse.setDirectRegular("Regular");
                    }
                    if(schemeName.toLowerCase().contains("dividend")) {
                        mutualFundUniverse.setDividendGrowth("Dividend");
                    }
                    if(schemeName.toLowerCase().contains("growth")) {
                        mutualFundUniverse.setDividendGrowth("Growth");
                    }
                    mutualFundUniverse.setIsinDivPayoutIsinGrowth("XXX");
                    mutualFundUniverseRepository.save(mutualFundUniverse);
                } /*else { //Run this code once in a quarter or month (offline) to update Mutual Fund
                    MutualFundUniverse existingFund = mutualFundUniverseRepository.findBySchemeCode(new Long(code));
                    if(!existingFund.getSchemeNameFull().equals(schemeName)
                            || (existingFund.getIsinDivReinvestment() != null && !existingFund.getIsinDivReinvestment().equals(isinReinvestment))
                            || (existingFund.getIsinDivPayoutIsinGrowth() != null && !existingFund.getIsinDivPayoutIsinGrowth().equals(isin))) {
                        existingFund.setSchemeNameFull(schemeName);
                        existingFund.setIsinDivPayoutIsinGrowth(isin);
                        existingFund.setIsinDivReinvestment(isinReinvestment);
                        //String temp = "Updating Fund Full Name "+code+"-Old Name: "+existingFund.getSchemeNameFull()+"; New Name: "+schemeName;
                        logger.debug(String.format("Name changed for %d", code));
                        mutualFundUniverseRepository.save(existingFund);
                    }
                }*/
            }
            mutualFundNavHistories.sort(Comparator.comparing(l->l.getKey().getSchemeCode()));
            //mutualFundRepository.deleteAllInBatch();
            mutualFundNavHistoryRepository.saveAll(mutualFundNavHistories);
            scanner.close();
            csvFile.delete();

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploadmfnavdata";
    }

    @RequestMapping(value = "/admin/uploadindexdata")
    public String uploadIndexData(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadindexdata";
    }

    @RequestMapping(value = "/admin/uploaddailydatas")
    public String uploadDailyDataB(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploaddailydatas";
    }

    /*@RequestMapping(value=("/admin/uploaddailydatasstatus"),headers=("content-type=multipart/*"),method= RequestMethod.POST)
    public String uploadDailyDataSStatus (Model model, @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploaddailydatas";
        }
        try {
            List<DailyDataS> dailyDataSList = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet worksheet = workbook.getSheetAt(0);
            String filename = file.getOriginalFilename();
            String dateString = filename.substring(0, 8);
            java.sql.Date date =  java.sql.Date.valueOf(dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6));

            for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
                DailyDataS dailyDataS = new DailyDataS();

                XSSFRow row = worksheet.getRow(i);
                dailyDataS.setKey(new DailyDataS.DailyDataSKey());
                dailyDataS.getKey().setDate(date); // set as date
                dailyDataS.setRank(i);

                if(row.getCell(2)!= null && row.getCell(2).getCellType() != Cell.CELL_TYPE_BLANK && !row.getCell(2).getStringCellValue().trim().isEmpty())
                    if (row.getCell(2).getCellType() == Cell.CELL_TYPE_STRING )
                        dailyDataS.getKey().setName((String) row.getCell(2).getStringCellValue());
                    else {
                        int bse_ticker = (int) row.getCell(2).getNumericCellValue();
                        dailyDataS.getKey().setName("" + bse_ticker);
                    }
                else if (row.getCell(1)!= null && row.getCell(1).getCellType() != Cell.CELL_TYPE_BLANK)
                    if (row.getCell(1).getCellType() == Cell.CELL_TYPE_STRING)
                        dailyDataS.getKey().setName((String) row.getCell(1).getStringCellValue());
                    else {
                        int bse_ticker = (int)row.getCell(1).getNumericCellValue();
                        dailyDataS.getKey().setName("" + bse_ticker);
                    }
                else
                    continue;

                if(row.getCell(4) != null && row.getCell(4).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(4).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setCmp(new BigDecimal(row.getCell(4).getNumericCellValue()));
                else
                    dailyDataS.setCmp(new BigDecimal(0));

                if(row.getCell(5) != null && row.getCell(5).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(5).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setMarketCap(new BigDecimal(row.getCell(5).getNumericCellValue()));
                else
                    dailyDataS.setMarketCap(new BigDecimal(0));

                if(row.getCell(6) != null && row.getCell(6).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(6).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setLastResultDate((int) row.getCell(6).getNumericCellValue());
                else
                    dailyDataS.setLastResultDate(0);

                if(row.getCell(7) != null && row.getCell(7).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(7).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNetProfit(new BigDecimal(row.getCell(7).getNumericCellValue()));
                else
                    dailyDataS.setNetProfit(new BigDecimal(0));

                if(row.getCell(8) != null && row.getCell(8).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(8).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSales(new BigDecimal(row.getCell(8).getNumericCellValue()));
                else
                    dailyDataS.setSales(new BigDecimal(0));

                if(row.getCell(9) != null && row.getCell(9).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(9).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setYoyQuarterlySalesGrowth(new BigDecimal(row.getCell(9).getNumericCellValue()));
                else
                    dailyDataS.setYoyQuarterlySalesGrowth(new BigDecimal(0));

                if(row.getCell(10) != null && row.getCell(10).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(10).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setYoyQuarterlyProfitGrowth(new BigDecimal(row.getCell(10).getNumericCellValue()));
                else
                    dailyDataS.setYoyQuarterlyProfitGrowth(new BigDecimal(0));

                if(row.getCell(11) != null && row.getCell(11).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(11).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setQoqSalesGrowth(new BigDecimal(row.getCell(11).getNumericCellValue()));
                else
                    dailyDataS.setQoqSalesGrowth(new BigDecimal(0));

                if(row.getCell(12) != null && row.getCell(12).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(12).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setQoqProfitGrowth(new BigDecimal(row.getCell(12).getNumericCellValue()));
                else
                    dailyDataS.setQoqProfitGrowth(new BigDecimal(0));

                if(row.getCell(13) != null && row.getCell(13).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(13).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setOpmLatestQuarter(new BigDecimal(row.getCell(13).getNumericCellValue()));
                else
                    dailyDataS.setOpmLatestQuarter(new BigDecimal(0));

                if(row.getCell(14) != null && row.getCell(14).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(14).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setOpmLastYear(new BigDecimal(row.getCell(14).getNumericCellValue()));
                else
                    dailyDataS.setOpmLastYear(new BigDecimal(0));

                if(row.getCell(15) != null && row.getCell(15).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(15).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNpmLatestQuarter(new BigDecimal(row.getCell(15).getNumericCellValue()));
                else
                    dailyDataS.setNpmLatestQuarter(new BigDecimal(0));

                if(row.getCell(16) != null && row.getCell(16).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(16).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNpmLastYear(new BigDecimal(row.getCell(16).getNumericCellValue()));
                else
                    dailyDataS.setNpmLastYear(new BigDecimal(0));

                if(row.getCell(17) != null && row.getCell(17).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(17).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setProfitGrowth3years(new BigDecimal(row.getCell(17).getNumericCellValue()));
                else
                    dailyDataS.setProfitGrowth3years(new BigDecimal(0));

                if(row.getCell(18) != null && row.getCell(18).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(18).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSalesGrowth3years(new BigDecimal(row.getCell(18).getNumericCellValue()));
                else
                    dailyDataS.setSalesGrowth3years(new BigDecimal(0));

                if(row.getCell(19) != null && row.getCell(19).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(19).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setPeTtm(new BigDecimal(row.getCell(19).getNumericCellValue()));
                else
                    dailyDataS.setPeTtm(new BigDecimal(0));

                if(row.getCell(20) != null && row.getCell(20).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(20).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setHistoricalPe3years(new BigDecimal(row.getCell(20).getNumericCellValue()));
                else
                    dailyDataS.setHistoricalPe3years(new BigDecimal(0));

                if(row.getCell(21) != null && row.getCell(21).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(21).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setPegRatio(new BigDecimal(row.getCell(21).getNumericCellValue()));
                else
                    dailyDataS.setPegRatio(new BigDecimal(0));

                if(row.getCell(22) != null && row.getCell(22).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(22).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setPbTtm(new BigDecimal(row.getCell(22).getNumericCellValue()));
                else
                    dailyDataS.setPbTtm(new BigDecimal(0));

                if(row.getCell(23) != null && row.getCell(23).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(23).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setEvToEbit(new BigDecimal(row.getCell(23).getNumericCellValue()));
                else
                    dailyDataS.setEvToEbit(new BigDecimal(0));

                if(row.getCell(24) != null && row.getCell(24).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(24).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDividendPayout(new BigDecimal(row.getCell(24).getNumericCellValue()));
                else
                    dailyDataS.setDividendPayout(new BigDecimal(0));

                if(row.getCell(25) != null && row.getCell(25).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(25).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setRoe(new BigDecimal(row.getCell(25).getNumericCellValue()));
                else
                    dailyDataS.setRoe(new BigDecimal(0));

                if(row.getCell(26) != null && row.getCell(26).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(26).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setAvgRoe3years(new BigDecimal(row.getCell(26).getNumericCellValue()));
                else
                    dailyDataS.setAvgRoe3years(new BigDecimal(0));

                if(row.getCell(27) != null && row.getCell(27).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(27).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDebt(new BigDecimal(row.getCell(27).getNumericCellValue()));
                else
                    dailyDataS.setDebt(new BigDecimal(0));

                if(row.getCell(28) != null && row.getCell(28).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(28).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDebtToEquity(new BigDecimal(row.getCell(28).getNumericCellValue()));
                else
                    dailyDataS.setDebtToEquity(new BigDecimal(0));

                if(row.getCell(29) != null && row.getCell(29).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(29).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setDebt3yearsback(new BigDecimal(row.getCell(29).getNumericCellValue()));
                else
                    dailyDataS.setDebt3yearsback(new BigDecimal(0));

                if(row.getCell(30) != null && row.getCell(30).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(30).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setRoce(new BigDecimal(row.getCell(30).getNumericCellValue()));
                else
                    dailyDataS.setRoce(new BigDecimal(0));

                if(row.getCell(31) != null && row.getCell(31).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(31).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setAvgRoce3years(new BigDecimal(row.getCell(31).getNumericCellValue()));
                else
                    dailyDataS.setAvgRoce3years(new BigDecimal(0));

                if(row.getCell(32) != null && row.getCell(32).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(32).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setFcfS(new BigDecimal(row.getCell(32).getNumericCellValue()));
                else
                    dailyDataS.setFcfS(new BigDecimal(0));

                if(row.getCell(33) != null && row.getCell(33).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(33).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSalesGrowth5years(new BigDecimal(row.getCell(33).getNumericCellValue()));
                else
                    dailyDataS.setSalesGrowth5years(new BigDecimal(0));

                if(row.getCell(34) != null && row.getCell(34).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(34).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setSalesGrowth10years(new BigDecimal(row.getCell(34).getNumericCellValue()));
                else
                    dailyDataS.setSalesGrowth10years(new BigDecimal(0));

                if(row.getCell(35) != null && row.getCell(35).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(35).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setNoplat(new BigDecimal(row.getCell(35).getNumericCellValue()));
                else
                    dailyDataS.setNoplat(new BigDecimal(0));

                if(row.getCell(36) != null && row.getCell(36).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(36).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setCapex(new BigDecimal(row.getCell(36).getNumericCellValue()));
                else
                    dailyDataS.setCapex(new BigDecimal(0));

                if(row.getCell(37) != null && row.getCell(37).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(37).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setFcff(new BigDecimal(row.getCell(37).getNumericCellValue()));
                else
                    dailyDataS.setFcff(new BigDecimal(0));

                if(row.getCell(38) != null && row.getCell(38).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(38).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setInvestedCapital(new BigDecimal(row.getCell(38).getNumericCellValue()));
                else
                    dailyDataS.setInvestedCapital(new BigDecimal(0));

                if(row.getCell(39) != null && row.getCell(39).getCellType() != Cell.CELL_TYPE_BLANK && row.getCell(39).getCellType() == Cell.CELL_TYPE_NUMERIC)
                    dailyDataS.setRoic(new BigDecimal(row.getCell(39).getNumericCellValue()));
                else
                    dailyDataS.setRoic(new BigDecimal(0));

                dailyDataS.setMcapToNetprofit(new BigDecimal(0));
                dailyDataS.setMcapToSales(new BigDecimal(0));
                dailyDataS.setSector("");
                dailyDataS.setIndustry("");
                dailyDataS.setSubIndustry("");

                dailyDataSList.add(dailyDataS);
            }
            dailyDataSList.sort(Comparator.comparing(DailyDataS::getMarketCap).reversed());
            dailyDataSRepository.saveAll(dailyDataSList);

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploaddailydatas";
    }*/

    @RequestMapping(value = "/admin/uploaddailydatasstatus", headers = "content-type=multipart/*", method = RequestMethod.POST)
    public String uploadDailyDataStatus(Model model, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploaddailydatas";
        }

        try {
            List<DailyDataS> dailyDataSList;
            String filename = file.getOriginalFilename();
            String dateString = filename.substring(0, 8);
            Date date = Date.valueOf(dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6));

            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                dailyDataSList = readExcelFile(file, date);
            } else if (filename.endsWith(".csv")) {
                dailyDataSList = readCsvFile(file, date);
            } else {
                model.addAttribute("message", "Unsupported file format");
                return "admin/uploaddailydatas";
            }

            dailyDataSList.sort(Comparator.comparing(DailyDataS::getMarketCap).reversed());
            dailyDataSRepository.saveAll(dailyDataSList);
            model.addAttribute("message", "You successfully uploaded '" + filename + "'");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Failed to upload file: " + e.getMessage());
        }

        return "admin/uploaddailydatas";
    }

    public List<DailyDataS> readExcelFile(MultipartFile file, Date date) throws IOException {
        List<DailyDataS> dataList = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        if (!rowIterator.hasNext()) return dataList;

        // Read Header Row
        Row headerRow = rowIterator.next();
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim().toLowerCase();
            headerMap.put(header, cell.getColumnIndex());
        }

        int rank = 1;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            DailyDataS daily = new DailyDataS();
            daily.setKey(new DailyDataS.DailyDataSKey());
            daily.getKey().setDate(date);
            daily.setRank(rank++);

            String name = getExcelValue(row, headerMap, "name");
            if (name.isEmpty()) name = getExcelValue(row, headerMap, "bse code");
            if (name.isEmpty()) continue;
            daily.getKey().setName(name);

            daily.setCmp(getExcelBigDecimal(row, headerMap, "Current Price"));
            daily.setMarketCap(getExcelBigDecimal(row, headerMap, "Market Capitalization"));

            String resultDateStr = getExcelValue(row, headerMap, "Last result date");
            int resultDate = 0;
            try {
                resultDate = new BigDecimal(resultDateStr).intValue();
            } catch (Exception e) {
                resultDate = 0;
            }
            daily.setLastResultDate(resultDate);

            // Same mappings as your CSV method — just replicate all fields exactly like in CSV code
            daily.setNetProfit(getExcelBigDecimal(row, headerMap, "Net profit"));
            daily.setSales(getExcelBigDecimal(row, headerMap, "Sales"));
            daily.setYoyQuarterlySalesGrowth(getExcelBigDecimal(row, headerMap, "YOY Quarterly sales growth"));
            daily.setYoyQuarterlyProfitGrowth(getExcelBigDecimal(row, headerMap, "YOY Quarterly profit growth"));
            daily.setQoqSalesGrowth(getExcelBigDecimal(row, headerMap, "QoQ sales growth"));
            daily.setQoqProfitGrowth(getExcelBigDecimal(row, headerMap, "QoQ profit growth"));
            daily.setOpmLatestQuarter(getExcelBigDecimal(row, headerMap, "OPM latest quarter"));
            daily.setOpmLastYear(getExcelBigDecimal(row, headerMap, "OPM last year"));
            daily.setNpmLatestQuarter(getExcelBigDecimal(row, headerMap, "NPM latest quarter"));
            daily.setNpmLastYear(getExcelBigDecimal(row, headerMap, "NPM last year"));
            daily.setProfitGrowth3years(getExcelBigDecimal(row, headerMap, "Profit growth 3Years"));
            daily.setSalesGrowth3years(getExcelBigDecimal(row, headerMap, "Sales growth 3Years"));
            daily.setPeTtm(getExcelBigDecimal(row, headerMap, "Price to Earning"));
            daily.setHistoricalPe3years(getExcelBigDecimal(row, headerMap, "Historical PE 3Years"));
            daily.setPegRatio(getExcelBigDecimal(row, headerMap, "PEG Ratio"));
            daily.setPbTtm(getExcelBigDecimal(row, headerMap, "Price to book value"));
            daily.setEvToEbit(getExcelBigDecimal(row, headerMap, "Enterprise Value to EBIT"));
            daily.setDividendPayout(getExcelBigDecimal(row, headerMap, "Dividend Payout Ratio"));
            daily.setRoe(getExcelBigDecimal(row, headerMap, "Return on equity"));
            daily.setAvgRoe3years(getExcelBigDecimal(row, headerMap, "Average return on equity 3Years"));
            daily.setDebt(getExcelBigDecimal(row, headerMap, "Debt"));
            daily.setDebtToEquity(getExcelBigDecimal(row, headerMap, "Debt to equity"));
            daily.setDebt3yearsback(getExcelBigDecimal(row, headerMap, "Debt 3Years back"));
            daily.setRoce(getExcelBigDecimal(row, headerMap, "Return on capital employed"));
            daily.setAvgRoce3years(getExcelBigDecimal(row, headerMap, "Average return on capital employed 3Years"));
            daily.setFcfS(getExcelBigDecimal(row, headerMap, "Free cash flow last year"));
            daily.setSalesGrowth5years(getExcelBigDecimal(row, headerMap, "Sales growth 5Years"));
            daily.setSalesGrowth10years(getExcelBigDecimal(row, headerMap, "Sales growth 10Years"));
            daily.setNoplat(getExcelBigDecimal(row, headerMap, "NOPLAT"));
            daily.setCapex(getExcelBigDecimal(row, headerMap, "Capex"));
            daily.setFcff(getExcelBigDecimal(row, headerMap, "FCFF"));
            daily.setInvestedCapital(getExcelBigDecimal(row, headerMap, "Invested Capital"));
            daily.setRoic(getExcelBigDecimal(row, headerMap, "RoIC"));

            // Additional fields
            daily.setReturn1D(getExcelBigDecimal(row, headerMap, "Return over 1day"));
            daily.setReturn1W(getExcelBigDecimal(row, headerMap, "Return over 1week"));
            daily.setReturn1M(getExcelBigDecimal(row, headerMap, "Return over 1month"));
            daily.setReturn3M(getExcelBigDecimal(row, headerMap, "Return over 3months"));
            daily.setReturn6M(getExcelBigDecimal(row, headerMap, "Return over 6months"));
            daily.setReturn1Y(getExcelBigDecimal(row, headerMap, "Return over 1year"));
            daily.setUp52wMin(getExcelBigDecimal(row, headerMap, "Up from 52w low"));
            daily.setDown52wMax(getExcelBigDecimal(row, headerMap, "Down from 52w high"));
            daily.setVolume1D(getExcelBigDecimal(row, headerMap, "Volume"));
            daily.setVolume1W(getExcelBigDecimal(row, headerMap, "Volume 1week average"));
            daily.setDma50(getExcelBigDecimal(row, headerMap, "DMA 50"));
            daily.setDma200(getExcelBigDecimal(row, headerMap, "DMA 200"));
            daily.setRsi(getExcelBigDecimal(row, headerMap, "RSI"));
            daily.setTotalAssets(getExcelBigDecimal(row, headerMap, "Total Assets"));
            daily.setNetBlock(getExcelBigDecimal(row, headerMap, "Net Block"));
            daily.setWorkingCapital(getExcelBigDecimal(row, headerMap, "Working Capital"));
            daily.setInventory(getExcelBigDecimal(row, headerMap, "Inventory"));
            daily.setTradeReceivables(getExcelBigDecimal(row, headerMap, "Trade Receivables"));
            daily.setTradePayables(getExcelBigDecimal(row, headerMap, "Trade Payables"));
            daily.setSharesOutstandingCr(getExcelBigDecimal(row, headerMap, "Number of equity shares"));

            daily.setIndustry(getExcelValue(row, headerMap, "Industry"));
            daily.setSubIndustry("");
            daily.setSector("");

            // Computed Ratios
            if (daily.getNetProfit() != null && daily.getNetProfit().compareTo(BigDecimal.ZERO) != 0) {
                daily.setMcapToNetprofit(daily.getMarketCap().divide(daily.getNetProfit(), 2, RoundingMode.HALF_UP));
            } else {
                daily.setMcapToNetprofit(BigDecimal.ZERO);
            }

            if (daily.getSales() != null && daily.getSales().compareTo(BigDecimal.ZERO) != 0) {
                daily.setMcapToSales(daily.getMarketCap().divide(daily.getSales(), 2, RoundingMode.HALF_UP));
            } else {
                daily.setMcapToSales(BigDecimal.ZERO);
            }

            dataList.add(daily);
        }

        workbook.close();
        return dataList;
    }


    public List<DailyDataS> readCsvFile(MultipartFile file, Date date) throws IOException {
        List<DailyDataS> dataList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

        String headerLine = reader.readLine();
        if (headerLine == null) return dataList;

        String[] headers = headerLine.split(",", -1);
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].trim().toLowerCase(), i);
        }

        String line;
        int rank = 1;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",", -1);
            DailyDataS daily = new DailyDataS();
            daily.setKey(new DailyDataS.DailyDataSKey());
            daily.getKey().setDate(date);
            daily.setRank(rank++);

            String name = getCsvValue(values, headerMap, "NSE Code");
            if (name.isEmpty()) name = getCsvValue(values, headerMap, "BSE Code");
            if (name.isEmpty()) continue;
            daily.getKey().setName(name);

            daily.setCmp(getCsvBigDecimal(values, headerMap, "Current Price"));
            daily.setMarketCap(getCsvBigDecimal(values, headerMap, "Market Capitalization"));
            String resultDateStr = getCsvValue(values, headerMap, "Last result date");
            int resultDate = 0;
            try {
                resultDate = new BigDecimal(resultDateStr).intValue(); // handle both int and float values
            } catch (Exception e) {
                resultDate = 0;
            }
            daily.setLastResultDate(resultDate);
            daily.setNetProfit(getCsvBigDecimal(values, headerMap, "Net profit"));
            daily.setSales(getCsvBigDecimal(values, headerMap, "Sales"));
            daily.setYoyQuarterlySalesGrowth(getCsvBigDecimal(values, headerMap, "YOY Quarterly sales growth"));
            daily.setYoyQuarterlyProfitGrowth(getCsvBigDecimal(values, headerMap, "YOY Quarterly profit growth"));
            daily.setQoqSalesGrowth(getCsvBigDecimal(values, headerMap, "QoQ sales growth"));
            daily.setQoqProfitGrowth(getCsvBigDecimal(values, headerMap, "QoQ profit growth"));
            daily.setOpmLatestQuarter(getCsvBigDecimal(values, headerMap, "OPM latest quarter"));
            daily.setOpmLastYear(getCsvBigDecimal(values, headerMap, "OPM last year"));
            daily.setNpmLatestQuarter(getCsvBigDecimal(values, headerMap, "NPM latest quarter"));
            daily.setNpmLastYear(getCsvBigDecimal(values, headerMap, "NPM last year"));
            daily.setProfitGrowth3years(getCsvBigDecimal(values, headerMap, "Profit growth 3Years"));
            daily.setSalesGrowth3years(getCsvBigDecimal(values, headerMap, "Sales growth 3Years"));
            daily.setPeTtm(getCsvBigDecimal(values, headerMap, "Price to Earning"));
            daily.setHistoricalPe3years(getCsvBigDecimal(values, headerMap, "Historical PE 3Years"));
            daily.setPegRatio(getCsvBigDecimal(values, headerMap, "PEG Ratio"));
            daily.setPbTtm(getCsvBigDecimal(values, headerMap, "Price to book value"));
            daily.setEvToEbit(getCsvBigDecimal(values, headerMap, "Enterprise Value to EBIT"));
            daily.setDividendPayout(getCsvBigDecimal(values, headerMap, "Dividend Payout Ratio"));
            daily.setRoe(getCsvBigDecimal(values, headerMap, "Return on equity"));
            daily.setAvgRoe3years(getCsvBigDecimal(values, headerMap, "Average return on equity 3Years"));
            daily.setDebt(getCsvBigDecimal(values, headerMap, "Debt"));
            daily.setDebtToEquity(getCsvBigDecimal(values, headerMap, "Debt to equity"));
            daily.setDebt3yearsback(getCsvBigDecimal(values, headerMap, "Debt 3Years back"));
            daily.setRoce(getCsvBigDecimal(values, headerMap, "Return on capital employed"));
            daily.setAvgRoce3years(getCsvBigDecimal(values, headerMap, "Average return on capital employed 3Years"));
            daily.setFcfS(getCsvBigDecimal(values, headerMap, "Free cash flow last year"));
            daily.setSalesGrowth5years(getCsvBigDecimal(values, headerMap, "Sales growth 5Years"));
            daily.setSalesGrowth10years(getCsvBigDecimal(values, headerMap, "Sales growth 10Years"));
            daily.setNoplat(getCsvBigDecimal(values, headerMap, "NOPLAT"));
            daily.setCapex(getCsvBigDecimal(values, headerMap, "Capex"));
            daily.setFcff(getCsvBigDecimal(values, headerMap, "FCFF"));
            daily.setInvestedCapital(getCsvBigDecimal(values, headerMap, "Invested Capital"));
            daily.setRoic(getCsvBigDecimal(values, headerMap, "RoIC"));

            // New additional fields
            daily.setReturn1D(getCsvBigDecimal(values, headerMap, "Return over 1day"));
            daily.setReturn1W(getCsvBigDecimal(values, headerMap, "Return over 1week"));
            daily.setReturn1M(getCsvBigDecimal(values, headerMap, "Return over 1month"));
            daily.setReturn3M(getCsvBigDecimal(values, headerMap, "Return over 3months"));
            daily.setReturn6M(getCsvBigDecimal(values, headerMap, "Return over 6months"));
            daily.setReturn1Y(getCsvBigDecimal(values, headerMap, "Return over 1year"));
            daily.setUp52wMin(getCsvBigDecimal(values, headerMap, "Up from 52w low"));
            daily.setDown52wMax(getCsvBigDecimal(values, headerMap, "Down from 52w high"));
            daily.setVolume1D(getCsvBigDecimal(values, headerMap, "Volume"));
            daily.setVolume1W(getCsvBigDecimal(values, headerMap, "Volume 1week average"));
            daily.setDma50(getCsvBigDecimal(values, headerMap, "DMA 50"));
            daily.setDma200(getCsvBigDecimal(values, headerMap, "DMA 200"));
            daily.setRsi(getCsvBigDecimal(values, headerMap, "RSI"));
            daily.setTotalAssets(getCsvBigDecimal(values, headerMap, "Total Assets"));
            daily.setNetBlock(getCsvBigDecimal(values, headerMap, "Net Block"));
            daily.setWorkingCapital(getCsvBigDecimal(values, headerMap, "Working Capital"));
            daily.setInventory(getCsvBigDecimal(values, headerMap, "Inventory"));
            daily.setTradeReceivables(getCsvBigDecimal(values, headerMap, "Trade Receivables"));
            daily.setTradePayables(getCsvBigDecimal(values, headerMap, "Trade Payables"));
            daily.setSharesOutstandingCr(getCsvBigDecimal(values, headerMap, "Number of equity shares"));

            daily.setIndustry(getCsvValue(values, headerMap, "Industry"));
            daily.setSubIndustry("");
            daily.setSector("");

            // Computed ratios
            if (daily.getNetProfit() != null && daily.getNetProfit().compareTo(BigDecimal.ZERO) != 0) {
                daily.setMcapToNetprofit(daily.getMarketCap().divide(daily.getNetProfit(), 2, RoundingMode.HALF_UP));
            } else {
                daily.setMcapToNetprofit(BigDecimal.ZERO);
            }

            if (daily.getSales() != null && daily.getSales().compareTo(BigDecimal.ZERO) != 0) {
                daily.setMcapToSales(daily.getMarketCap().divide(daily.getSales(), 2, RoundingMode.HALF_UP));
            } else {
                daily.setMcapToSales(BigDecimal.ZERO);
            }

            dataList.add(daily);
        }

        return dataList;
    }

    private String getExcelValue(Row row, Map<String, Integer> map, String key) {
        Integer index = map.get(key.toLowerCase());
        if (index == null) return "";
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            default:
                return "";
        }
    }

    private BigDecimal getExcelBigDecimal(Row row, Map<String, Integer> map, String key) {
        try {
            String val = getExcelValue(row, map, key);
            return val.isEmpty() ? BigDecimal.ZERO : new BigDecimal(val);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private int getExcelInt(Row row, Integer colIndex) {
        if (colIndex == null || colIndex == -1 || row.getCell(colIndex) == null) return 0;
        try {
            Cell cell = row.getCell(colIndex);
            return (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) ? (int) cell.getNumericCellValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCsvValue(String[] row, Map<String, Integer> map, String key) {
        Integer index = map.get(key.toLowerCase());
        return (index != null && index < row.length) ? row[index].trim() : "";
    }

    private BigDecimal getCsvBigDecimal(String[] row, Map<String, Integer> map, String key) {
        try {
            String val = getCsvValue(row, map, key);
            return val.isEmpty() ? BigDecimal.ZERO : new BigDecimal(val);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @RequestMapping(value = "/admin/uploadmosltxn")
    public String uploadMOSLTxn(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadmosltxn";
    }

    @RequestMapping(value=("/admin/uploadmosltxnstatus"),headers=("content-type=multipart/*"),method= RequestMethod.POST)
    public String uploadMOSLTxnStatus(Model model, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploadmosltxn";
        }

        try {
            List<MOSLTransaction> moslTransactions = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet worksheet = workbook.getSheetAt(0);

            // Extract column headers from the first row
            XSSFRow headerRow = worksheet.getRow(0);
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                XSSFCell cell = headerRow.getCell(i);
                String columnHeader = cell.getStringCellValue().trim();
                columnIndexMap.put(columnHeader, i);
            }

            for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
                MOSLTransaction moslTransaction = new MOSLTransaction();
                XSSFRow row = worksheet.getRow(i);

                moslTransaction.setKey(new MOSLTransaction.MOSLTransactionKey());

                // Retrieve data dynamically based on column header
                moslTransaction.getKey().setMoslCode(getStringValue(row, columnIndexMap, "CLIENTCODE"));
                moslTransaction.setExchange(getStringValue(row, columnIndexMap, "EXCHANGE"));
                moslTransaction.getKey().setDate(getDateValue(row, columnIndexMap, "TRADE DATE"));
                moslTransaction.getKey().setScriptName(getStringValue(row, columnIndexMap, "SCRIP NAME"));
                String buySell = getStringValue(row, columnIndexMap, "SELL/BUY");
                if(buySell.equals("S"))
                    buySell = "Sell";
                if(buySell.equals("B"))
                    buySell = "Buy";
                moslTransaction.getKey().setSellBuy(buySell);
                moslTransaction.setQuantity(getBigDecimalValue(row, columnIndexMap, "TRADE QTY").abs());
                moslTransaction.setRate(getBigDecimalValue(row, columnIndexMap, "MARKET PRICE"));
                moslTransaction.setAmount(getBigDecimalValue(row, columnIndexMap, "MARKET AMOUNT"));
                moslTransaction.setBrokerage(getBigDecimalValue(row, columnIndexMap, "BROKERAGE"));
                moslTransaction.setTxnCharges(getBigDecimalValue(row, columnIndexMap, "TRANSACTION CHARGES"));
                moslTransaction.setServiceTax(getBigDecimalValue(row, columnIndexMap, "GST"));
                moslTransaction.setStampDuty(getBigDecimalValue(row, columnIndexMap, "STAMP DUTY"));
                moslTransaction.setSttCtt(getBigDecimalValue(row, columnIndexMap, "STT/CTT"));
                moslTransaction.setNetRate(getBigDecimalValue(row, columnIndexMap, "NET RATE"));
                moslTransaction.setNetAmount(getBigDecimalValue(row, columnIndexMap, "NET AMOUNT"));
                moslTransaction.getKey().setOrderNo(getStringValue(row, columnIndexMap, "ORDER NO"));
                moslTransaction.getKey().setTradeNo(getStringValue(row, columnIndexMap, "TRADE NO"));
                moslTransaction.setIsProcessed("N");
                BigDecimal portfolioid = getBigDecimalValue(row, columnIndexMap, "PORTFOLIO ID");
                if(portfolioid != null)
                    moslTransaction.getKey().setPortfolioid(portfolioid.intValue());
                else
                    moslTransaction.getKey().setPortfolioid(1);
                // Add mapping for other fields

                boolean recordExists = moslTransactionRepository.existsById(moslTransaction.getKey());

                if (!recordExists) {
                    moslTransactions.add(moslTransaction);
                } else {
                    System.out.println("MOSL Transaction already exists. for Date: " + moslTransaction.getKey().getDate() + " | Client: " + moslTransaction.getKey().getMoslCode() + " | Script: "  + moslTransaction.getKey().getScriptName());
                }
            }

            moslTransactionRepository.saveAll(moslTransactions);

            // Call stored procedure
            StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery(AP_PROCESS_MOSL_TRANSACTIONS);
            boolean result = storedProcedure.execute();
            if (!result) {
                model.addAttribute("message", "Successfully uploaded transactions in the file " + file.getOriginalFilename());
            } else {
                model.addAttribute("message", "Failed to upload transactions successfully. Check log_table.");
            }

        } catch (IOException e) {
            model.addAttribute("error", "Exception in Processing the file.");
            e.printStackTrace();
        }
        return "admin/uploadmosltxn";
    }
    // Helper methods to retrieve cell values based on column header
    private String getStringValue(XSSFRow row, Map<String, Integer> columnIndexMap, String columnHeader) {
        Integer index = columnIndexMap.get(columnHeader);
        if (index != null) {
            XSSFCell cell = row.getCell(index);
            if (cell != null) {
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    // Convert numeric value to string
                    return String.valueOf(cell.getNumericCellValue());
                } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    // Retrieve string value
                    return cell.getStringCellValue().trim();
                } else {
                    // Handle other cell types if necessary
                    return null;
                }
            }
        }
        return null;
    }

    private Date getDateValue(XSSFRow row, Map<String, Integer> columnIndexMap, String columnHeader) {
        Integer index = columnIndexMap.get(columnHeader);
        Date date = null;
        if (index != null) {
            XSSFCell cell = row.getCell(index);
            if (cell != null && DateUtil.isCellDateFormatted(cell)) {
                // Retrieve date value if cell is formatted as date
                java.util.Date utilDate = cell.getDateCellValue();
                // Convert java.util.Date to java.sql.Date
                date = new Date(utilDate.getTime());
            }
        }
        return date;
    }

    private BigDecimal getBigDecimalValue(XSSFRow row, Map<String, Integer> columnIndexMap, String columnHeader) {
        Integer index = columnIndexMap.get(columnHeader);
        if (index != null) {
            XSSFCell cell = row.getCell(index);
            if (cell != null) {
                // Parse BigDecimal value
                BigDecimal value;
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    value = BigDecimal.valueOf(cell.getNumericCellValue());
                } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    try {
                        value = new BigDecimal(cell.getStringCellValue().trim());
                    } catch (NumberFormatException e) {
                        value = null; // Handle invalid number format
                    }
                } else {
                    value = null; // Handle other cell types
                }
                return value;
            }
        }
        return null;
    }

    /*public String uploadMOSLTxnStatus (Model model, @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploadmosltxn";
        }
        try {

            List<MOSLTransaction> moslTransactions = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet worksheet = workbook.getSheetAt(0);

            for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
                MOSLTransaction moslTransaction = new MOSLTransaction();

                XSSFRow row = worksheet.getRow(i);
                moslTransaction.setKey(new MOSLTransaction.MOSLTransactionKey());
                String moslCode = (String) row.getCell(0).getStringCellValue();
                if (moslCode.equalsIgnoreCase("H20488")){ // hardcoded for time being account that I am not handling
                    continue;
                }
                if(moslCode.equalsIgnoreCase("Total")){
                    break;
                }
                moslTransaction.getKey().setMoslCode(moslCode);
                moslTransaction.setExchange((String) row.getCell(1).getStringCellValue());
                String dateString = (String)row.getCell(2).getStringCellValue();
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                java.sql.Date date = null;
                try {
                    date = new java.sql.Date(format.parse(dateString).getTime());
                    if (date == null && dateString != null){
                        format = new SimpleDateFormat("yyyy-mm-dd");
                        date = new java.sql.Date(format.parse(dateString).getTime());
                    }
                } catch (ParseException e) {
                    try{
                        if (date == null && dateString != null){
                            format = new SimpleDateFormat("yyyy-mm-dd");
                            date = new java.sql.Date(format.parse(dateString).getTime());
                        }
                    } catch (ParseException e1) {
                        if (date == null)
                            continue;
                    }
                }

                moslTransaction.getKey().setDate(date);
                String scriptName = (String) row.getCell(3).getStringCellValue();
                moslTransaction.getKey().setScriptName(scriptName);
                String sellBuy = (String) row.getCell(4).getStringCellValue().trim();
                moslTransaction.getKey().setSellBuy(sellBuy);
                try{
                    moslTransaction.setQuantity(new BigDecimal(row.getCell(5).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setQuantity(new BigDecimal(row.getCell(5).getNumericCellValue()));
                }
                try{
                    moslTransaction.setRate(new BigDecimal(row.getCell(6).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setRate(new BigDecimal(row.getCell(6).getNumericCellValue()));
                }
                try{
                    moslTransaction.setAmount(new BigDecimal(row.getCell(7).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setAmount(new BigDecimal(row.getCell(7).getNumericCellValue()));
                }
                try{
                    moslTransaction.setBrokerage(new BigDecimal(row.getCell(8).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setBrokerage(new BigDecimal(row.getCell(8).getNumericCellValue()));
                }
                try{
                    moslTransaction.setTxnCharges(new BigDecimal(row.getCell(9).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setTxnCharges(new BigDecimal(row.getCell(9).getNumericCellValue()));
                }
                try{
                    moslTransaction.setServiceTax(new BigDecimal(row.getCell(10).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setServiceTax(new BigDecimal(row.getCell(10).getNumericCellValue()));
                }
                try{
                    moslTransaction.setStampDuty(new BigDecimal(row.getCell(11).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setStampDuty(new BigDecimal(row.getCell(11).getNumericCellValue()));
                }
                try{
                    moslTransaction.setSttCtt(new BigDecimal(row.getCell(12).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setSttCtt(new BigDecimal(row.getCell(12).getNumericCellValue()));
                }
                try{
                    moslTransaction.setNetRate(new BigDecimal(row.getCell(13).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setNetRate(new BigDecimal(row.getCell(13).getNumericCellValue()));
                }
                try{
                    moslTransaction.setNetAmount(new BigDecimal(row.getCell(14).getStringCellValue()));
                } catch (Exception e) {
                    moslTransaction.setNetAmount(new BigDecimal(row.getCell(14).getNumericCellValue()));
                }
                String orderNo = (String) row.getCell(15).getStringCellValue();
                moslTransaction.getKey().setOrderNo(orderNo);
                String tradeNO = (String) row.getCell(16).getStringCellValue();
                moslTransaction.getKey().setTradeNo(tradeNO);
                moslTransaction.setIsProcessed("N");
                int portfolioid = 1;
                try {
                    portfolioid = Double.valueOf(row.getCell(17).getNumericCellValue()).intValue();
                } catch (Exception e){
                    portfolioid = 1;
                }
                moslTransaction.getKey().setPortfolioid(portfolioid);

                int count = moslTransactionRepository.countByKeyMoslCodeAndKeyDateAndKeyScriptNameAndKeySellBuyAndKeyOrderNoAndKeyTradeNoAndKeyPortfolioid(moslCode, date, scriptName, sellBuy, orderNo, tradeNO, portfolioid);
                if (count == 0) {
                    moslTransactions.add(moslTransaction);
                }
            }
            //moslTransactions.sort(Comparator.comparing(MOSLTransaction::getMarketCap).reversed());
            moslTransactionRepository.saveAll(moslTransactions);

            StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery(AP_PROCESS_MOSL_TRANSACTIONS);
            boolean result = storedProcedure.execute();
            if (!result) {
                model.addAttribute("message", "Successfully uploaded transactions in the file "+ file.getOriginalFilename());
            } else {
                model.addAttribute("message", "Failed to upload transactions successfully. Check log_table.");
            }

        } catch (IOException e) {
            model.addAttribute("error", "Exception in Processing the file.");
            e.printStackTrace();
        }
        return "admin/uploadmosltxn";
    }*/



    @RequestMapping(value = "/admin/eodprocs")
    public String eodProcs(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        List<Object[]> objects = nsePriceHistoryRepository.findMaxDateAndCount();
        for (Object[] object : objects) {
            model.addAttribute("nsecount", object[0]);
            model.addAttribute("nsemaxdate", object[1]);
        }
        List<Object[]> objects1 = bsePriceHistoryRepository.findMaxDateAndCount();
        for (Object[] object : objects1) {
            model.addAttribute("bsecount", object[0]);
            model.addAttribute("bsemaxdate", object[1]);
        }
        List<Object[]> objects2 = mutualFundNavHistoryRepository.findMaxDateAndCount();
        for (Object[] object : objects2) {
            model.addAttribute("mfcount", object[0]);
            model.addAttribute("mfmaxdate", object[1]);
        }
        List<Object[]> objects3 = dailyDataSRepository.findMaxDateAndCount();
        for (Object[] object : objects3) {
            model.addAttribute("screenercount", object[0]);
            model.addAttribute("screenermaxdate", object[1]);
        }

        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/eodprocs";
    }

    @RequestMapping(value=("/admin/eodprocsstatus"),method=RequestMethod.POST)
    public String eodProcsStatus(Model model, @RequestParam("confirmation") String confirmation){
        if (confirmation.equalsIgnoreCase("yes")) {
            StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery(AP_PROCESS_EOD);
            storedProcedure.setHint("javax.persistence.query.timeout", 1000000); //16.67 mins for timeout
            boolean result = storedProcedure.execute();
            if (!result) {
                model.addAttribute("message", "Successfully ran EOD procedures");
            } else {
                model.addAttribute("message", "Failed to run EOD procedures successfully. Check log_table.");
            }
            return "admin/eodprocs";
        } else {
            model.addAttribute("message", "Please confirm Yes/No to process daily data");
            return "admin/eodprocs";
        }
    }

    @RequestMapping(value = "/admin/oneclickupload")
    public String oneClickUpload(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        List<Object[]> objects = nsePriceHistoryRepository.findMaxDateAndCount();
        for (Object[] object : objects) {
            model.addAttribute("nsecount", object[0]);
            model.addAttribute("nsemaxdate", object[1]);
        }
        List<Object[]> objects1 = bsePriceHistoryRepository.findMaxDateAndCount();
        for (Object[] object : objects1) {
            model.addAttribute("bsecount", object[0]);
            model.addAttribute("bsemaxdate", object[1]);
        }
        List<Object[]> objects2 = mutualFundNavHistoryRepository.findMaxDateAndCount();
        for (Object[] object : objects2) {
            model.addAttribute("mfcount", object[0]);
            model.addAttribute("mfmaxdate", object[1]);
        }
        List<Object[]> objects3 = dailyDataSRepository.findMaxDateAndCount();
        for (Object[] object : objects3) {
            model.addAttribute("screenercount", object[0]);
            model.addAttribute("screenermaxdate", object[1]);
        }

        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/oneclickupload";
    }

    @RequestMapping(value=("/admin/oneclickuploadstatus"),method=RequestMethod.POST)
    public String oneClickUploadStatus(Model model, @RequestParam("confirmation") String confirmation){
        if (confirmation.equalsIgnoreCase("yes")) {
            DownloadEODFiles downloadEODFiles = new DownloadEODFiles();
            int returnvalue = 0;
            try {
                returnvalue = downloadEODFiles.oneClickUpload(nsePriceHistoryRepository, bsePriceHistoryRepository, mutualFundNavHistoryRepository, dailyDataSRepository, mutualFundUniverseRepository);
                if (returnvalue < 0) {
                    model.addAttribute("error", "Failed to Download and Upload Data files successfully. Check log files.");
                } else {
                    model.addAttribute("message", "Successfully Downloaded and uploaded Data files");
                }
            } catch (Exception e) {
                model.addAttribute("error", "Failed to Upload Data files successfully. Check log files.");
            }
            try {
                List<Object[]> objects = nsePriceHistoryRepository.findMaxDateAndCount();
                java.sql.Date sqlDate = null;
                for (Object[] object : objects) {
                    sqlDate = (java.sql.Date) object[1];
                }
                if (sqlDate != null) {
                    downloadEODFiles.setIndexValuationRepository(indexValuationRepository);
                    returnvalue = downloadEODFiles.downloadAndSaveNSEIndexData(sqlDate);
                    if (returnvalue < 0) {
                        System.out.println("Failed to upload NSE Index Data successfully.");
                        model.addAttribute("error", "Failed to upload NSE Index Data successfully. Check log files.");
                    } else {
                        System.out.println("Uploaded NSE Index Data successfully.");
                        try {
                            downloadEODFiles = new DownloadEODFiles();
                            downloadEODFiles.setIndexValuationRepository(indexValuationRepository);
                            int returnValue = 0;
                            returnValue = downloadEODFiles.uploadBSEIndexData(sqlDate, "BSEMidCap");
                            if (returnValue < 0) {
                                System.out.println("Failed to upload BSEMidCap Index Data.");
                                model.addAttribute("error", "Failed to upload BSEMidCap Index Data. Please check log files.");
                            } else {
                                System.out.println("Uploaded BSEMidCap Index Data successfully.");
                                downloadEODFiles.uploadBSEIndexData(sqlDate, "BSESmallCap");
                                if (returnValue < 0) {
                                    System.out.println("Failed to upload BSESmallCap Index Data.");
                                    model.addAttribute("error", "Failed to upload BSESmallCap Index Data. Please check log files.");
                                }
                                else {
                                    System.out.println("Uploaded BSESmallCap Index Data successfully.");
                                    model.addAttribute("message", "Successfully uploaded both BSE Mid and Small Cap Index Data");
                                }
                            }
                        } catch (Exception e) {
                            model.addAttribute("error", "Failed to download and save BSE Index Data. Please check log files.");
                        }
                    }
                } else {
                    model.addAttribute("error", "Failed to upload NSE Index Data successfully. Check log files.");
                }
            } catch (Exception e) {
                model.addAttribute("error", "Failed to upload NSE Index Data successfully. Check log files.");
            }
            return "admin/oneclickupload";
        } else {
            model.addAttribute("message", "Please confirm Yes/No to process Download, upload and running EOD");
            return "admin/oneclickupload";
        }
    }

    /**
     * Bloomberg JSON file upload login -- Now not in use since Bloomberg Data is not available
     * @param model
     * @param file
     * @return
     */
    @RequestMapping(value=("/admin/uploaddailydatabstatus"),headers=("content-type=multipart/*"),method= RequestMethod.POST)
    public String uploadDailyDataBStatus (Model model, @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "redirect:admin/uploaddailydatab";
        }
        try {
            byte[] bytes = file.getBytes();
            String jsonAsString = new String(bytes);

            ObjectMapper jsonMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DailyDataBJSON dailyDataBJSON = jsonMapper.readValue(jsonAsString, DailyDataBJSON.class);
            assertNotNull(dailyDataBJSON);

            List<DailyDataB> dailyDataBList = new ArrayList<>();
            for(Ticker ticker : dailyDataBJSON.getTickers()){
                DailyDataB dailyDataB = new DailyDataB(ticker);
                dailyDataBList.add(dailyDataB);
//                companyDailyDataBRepository.save(companyDailyDataB);
            }
            dailyDataBList.sort(Comparator.comparing(DailyDataB::getMarketCap).reversed());
            dailyDataBRepository.saveAll(dailyDataBList);

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploaddailydatab";
    }

    @RequestMapping(value = "/admin/uploadresultexcel")
    public String uploadResultExcel(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadresultexcel";
    }

    @RequestMapping(value = ("/admin/uploadresultexcelstatus"), headers = ("content-type=multipart/*"), method = RequestMethod.POST)
    public String uploadResultExcelStatus(Model model, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return "admin/uploadresultexcel";
        }

        try {
            // Get the original filename
            String originalFilename = file.getOriginalFilename();

            // Define a pattern to extract ticker and quarter from the filename
            Pattern pattern = Pattern.compile("^(\\w+)_FY(\\d{2})Q([1-4])\\..*$");
            Matcher matcher = pattern.matcher(originalFilename);

            if (matcher.matches()) {
                try {
                    String ticker = matcher.group(1);
                    String year = matcher.group(2);
                    String quarter = matcher.group(3);

                    // Create the subfolder path based on year and quarter
                    String subfolder = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\20" + year + "Q" + quarter;

                    // Create the subfolder if it doesn't exist
                    File subfolderDir = new File(subfolder);
                    if (!subfolderDir.exists()) {
                        subfolderDir.mkdirs();
                    }

                    // Check if the file already exists in the subfolder
                    File newFile = new File(subfolder, originalFilename);
                    int counter = 0;
                    while (newFile.exists()) {
                        counter++;
                        // Rename the existing file with "_n.xlsx" where n is the next available number
                        String baseFilename = ticker + "_FY" + year + "Q" + quarter;
                        String existingFilename = baseFilename + (counter > 0 ? "_" + counter : "") + ".xlsx";
                        File existingFile = new File(subfolder, existingFilename);
                        newFile.renameTo(existingFile);
                    }

                    // Save the file to the subfolder with the newFile name
                    file.transferTo(newFile);

                    String industrySubindustry = findSubfolderForTicker(ticker);
                    String yearQuarter = "20" + year + "Q" + quarter;
                    updateConfigFile(ticker, industrySubindustry, yearQuarter, originalFilename);
                    // Assuming UpdateQuarterlyExcels.main() accepts no arguments
                    com.timelineofwealth.service.UpdateQuarterlyExcels.main(new String[]{});
                    removeParametersFromConfigFile("C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis\\config.properties");

                    model.addAttribute("message", "File '" + originalFilename + "' uploaded successfully.");
                } catch (Exception e) {
                    e.printStackTrace();
                    model.addAttribute("error", "Exception in Processing the file.");
                }
            } else {
                model.addAttribute("error", "Invalid file name format. Please use '<<ticker>>_FY<<year>>Q<<quarter>>' format.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Main Exception in Processing the file.");
            e.printStackTrace();
        }

        return "admin/uploadresultexcel";
    }

    private String findSubfolderForTicker(String ticker) {

        String configFile = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis\\tickerfolderonfig.properties";

        // Create a map to store ticker-subfolder mappings
        Map<String, String> tickerSubfolderMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split each line by whitespace to get ticker and subfolder
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    String currentTicker = parts[0];
                    String subfolder = parts[1];
                    tickerSubfolderMap.put(currentTicker, subfolder);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check if the given ticker exists in the map
        if (tickerSubfolderMap.containsKey(ticker)) {
            return tickerSubfolderMap.get(ticker);
        } else {
            // Return a default value or handle the case when the ticker is not found
            return "";
        }
    }

    private void updateConfigFile(String ticker, String industrySubindustry, String yearQuarter, String originalFilename) {
        String configFile = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis\\config.properties";

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Construct the parameter keys based on ticker, industrySubindustry, and yearQuarter
        String sourcePathKey = "C" + "&#58;" +  "\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\" + yearQuarter;
        String oldFileNameKey = "C" + "&#58;" + "\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis\\" + industrySubindustry + "\\" + ticker + ".xlsx";
        String newFileNameKey = "C" + "&#58;" +  "\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\" + yearQuarter + "\\" + originalFilename;
        String sourceFileKey = originalFilename;
        String destinationPathKey = "C" + "&#58;" + "\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels\\Analysis\\" + industrySubindustry + "\\";


        // Update the properties
        properties.setProperty("FileCopyFlag", "false");
        properties.setProperty("FileContentCopyFlag", "true");
        properties.setProperty("SourcePath", sourcePathKey);
        properties.setProperty("OldFileName1", oldFileNameKey);
        properties.setProperty("NewFileName1", newFileNameKey);
        properties.setProperty("SourceFile1", sourceFileKey);
        properties.setProperty("DestinationPath1", destinationPathKey);

        // Save the updated properties back to the file
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeParametersFromConfigFile(String configFile) {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);

            // Remove the parameters associated with the given ticker
//            properties.remove("SourcePath");
            properties.remove("OldFileName1");
            properties.remove("NewFileName1");
            properties.remove("SourceFile1");
            properties.remove("DestinationPath1");

            // If you have other parameters to remove, add them here

            // Save the updated properties back to the file
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                properties.store(fos, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/admin/downloadresultexcel")
    public String downloadResultExcel(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/downloadresultexcel";
    }

    @RequestMapping(value = "/admin/downloadresultexcelname/{excelFileName}", method = RequestMethod.GET)
    public void downloadResultExcelName(@PathVariable String excelFileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug(String.format("/admin/downloadresultexcelname/ %s", excelFileName));

        // Define the base folder path
        String basePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\QuarterResultsScreenerExcels";

        // Find the latest quarter result folder
        File baseFolder = new File(basePath);
        File[] subdirectories = baseFolder.listFiles(File::isDirectory);
        File latestQuarterFolder = null;

        if (subdirectories != null) {
            for (File folder : subdirectories) {
                if (isQuarterFolder(folder)) {
                    if (latestQuarterFolder == null || folder.getName().compareTo(latestQuarterFolder.getName()) > 0) {
                        latestQuarterFolder = folder;
                    }
                }
            }
        }

        // Check if the latestQuarterFolder is not null
        if (latestQuarterFolder != null) {
            // Construct the full path to the Excel file
            String excelFilePath = latestQuarterFolder.getAbsolutePath() + File.separator + excelFileName;

            File excelFile = new File(excelFilePath);

            // Check if the file exists
            if (excelFile.exists()) {
                // Set the response content type
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=" + excelFileName);

                try (FileInputStream fis = new FileInputStream(excelFile);
                     OutputStream os = response.getOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Helper method to check if a folder name is in the format YYYYQn
    private static boolean isQuarterFolder(File folder) {
        String folderName = folder.getName();
        return folderName.matches("\\d{4}Q[1-4]");
    }

    @RequestMapping(value = "/admin/updateanalystreco")
    public String updateAnalystReco(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/updateanalystreco";
    }

    @RequestMapping(value = ("/admin/updateanalystrecostatus"), headers = ("content-type=multipart/*"), method = RequestMethod.POST)
    public String updateAnalystRecoStatus(Model model, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a property file to upload");
            return "admin/updateanalystreco";
        }

        // Check if the uploaded file has the correct name "reportconfig.properties"
        if (!file.getOriginalFilename().equals("reportconfig.properties")) {
            model.addAttribute("error", "Please upload a file with the name 'reportconfig.properties'");
            return "admin/updateanalystreco";
        }

        try {
            // Define the path where the property file should be saved
            String configFilePath = "C:\\MyDocuments\\03Business\\05ResearchAndAnalysis\\StockInvestments\\ResearchReports\\CompanyResearchReports\\reportconfig.properties";

            // Save the uploaded property file to the specified path
            try (FileOutputStream fos = new FileOutputStream(configFilePath)) {
                fos.write(file.getBytes());
            }

            // Load the saved property file to check its contents
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(configFilePath)) {
                properties.load(fis);
            }

            // Check if the property file contains at least the required properties
            if (properties.containsKey("ReportDataExtractConfigFilePath") && properties.containsKey("AnalystNamesFilePath")) {
                // Execute AnalystRecoExtractor.main
                com.timelineofwealth.service.AnalystRecoExtractor.main(new String[]{});

                model.addAttribute("message", "Updated Analyst Recommendations to respective files");
            } else {
                model.addAttribute("error", "The property file is missing required properties");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Exception in Processing the file.");
            e.printStackTrace();
        }

        return "admin/updateanalystreco";
    }

    @RequestMapping(value = "/admin/uploadscreenerdata")
    public String uploadScreenerData(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadscreenerdata";
    }

    @RequestMapping(value = "/admin/stockuniverse")
    public String stockUniverse(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/stockuniverse";
    }

    @RequestMapping(value = "/admin/stocksplit")
    public String stockSplit(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/stocksplit";
    }

    @RequestMapping(value = "/admin/stockdividend")
    public String stockDividend(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/stockdividend";
    }

    @RequestMapping(value = "/admin/mfuniverse")
    public String mfUniverse(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/mfuniverse";
    }

    @RequestMapping(value = "/admin/computeindexstat")
    public String computeIndexStat(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();

        Date maxNiftyDate, maxBSEMidCapDate, maxBSESmallCapDate;
        maxNiftyDate = indexValuationRepository.findMaxKeyDateForKeyTicker("NIFTY");
        maxBSEMidCapDate = indexValuationRepository.findMaxKeyDateForKeyTicker("BSEMidCap");
        maxBSESmallCapDate = indexValuationRepository.findMaxKeyDateForKeyTicker("BSESmallCap");


        model.addAttribute("maxNiftyDate", maxNiftyDate);
        model.addAttribute("maxBSEMidCapDate", maxBSEMidCapDate);
        model.addAttribute("maxBSESmallCapDate", maxBSESmallCapDate);
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/computeindexstat";
    }

    @RequestMapping(value=("/admin/computeindexstatstatus"),method=RequestMethod.POST)
    public String computeIndexStatStatus(Model model, @RequestParam("confirmation") String confirmation){
        if (confirmation.equalsIgnoreCase("yes")) {
            try {
                IndexService.computeAndSaveIndexMonthlyReturns("NIFTY", true);
                IndexService.computeAndSaveIndexMonthlyReturns("BSEMidCap", true);
                IndexService.computeAndSaveIndexMonthlyReturns("BSESmallCap", true);

                IndexService.computeAndSavePeriodReturnStatistics("NIFTY", 1);
                IndexService.computeAndSavePeriodReturnStatistics("NIFTY", 3);
                IndexService.computeAndSavePeriodReturnStatistics("NIFTY", 5);
                IndexService.computeAndSavePeriodReturnStatistics("NIFTY", 10);

                IndexService.computeAndSavePeriodReturnStatistics("BSEMidCap", 1);
                IndexService.computeAndSavePeriodReturnStatistics("BSEMidCap", 3);
                IndexService.computeAndSavePeriodReturnStatistics("BSEMidCap", 5);
                IndexService.computeAndSavePeriodReturnStatistics("BSEMidCap", 10);

                IndexService.computeAndSavePeriodReturnStatistics("BSESmallCap", 1);
                IndexService.computeAndSavePeriodReturnStatistics("BSESmallCap", 3);
                IndexService.computeAndSavePeriodReturnStatistics("BSESmallCap", 5);
                IndexService.computeAndSavePeriodReturnStatistics("BSESmallCap", 10);

                Date maxNiftyDate, maxBSEMidCapDate, maxBSESmallCapDate;
                maxNiftyDate = indexValuationRepository.findMaxKeyDateForKeyTicker("NIFTY");
                maxBSEMidCapDate = indexValuationRepository.findMaxKeyDateForKeyTicker("BSEMidCap");
                maxBSESmallCapDate = indexValuationRepository.findMaxKeyDateForKeyTicker("BSESmallCap");

                IndexService.setDateLastUpdatedForIndexStats("NIFTY", maxNiftyDate);
                IndexService.setDateLastUpdatedForIndexStats("BSEMidCap", maxBSEMidCapDate);
                IndexService.setDateLastUpdatedForIndexStats("BSESmallCap", maxBSESmallCapDate);


            } catch (Exception e) {
                model.addAttribute("message", "Failed to compute index statistics.");
            }
            model.addAttribute("message", "Index Statistics computed");
            return "admin/computeindexstat";
        } else {
            model.addAttribute("message", "Please confirm Yes/No to process Download, upload and running EOD");
            return "admin/computeindexstat";
        }
    }

    @RequestMapping(value = "/admin/uploadbseindexdata")
    public String uploadBSEIndexData(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        Date maxBSEMidCapDate, maxBSESmallCapDate;
        maxBSEMidCapDate = indexValuationRepository.findMaxKeyDateForKeyTicker("BSEMidCap");
        maxBSESmallCapDate = indexValuationRepository.findMaxKeyDateForKeyTicker("BSESmallCap");
        model.addAttribute("maxBSEMidCapDate", maxBSEMidCapDate);
        model.addAttribute("maxBSESmallCapDate", maxBSESmallCapDate);
        model.addAttribute("dateToday", dateToday);
        return "admin/uploadbseindexdata";
    }

    @RequestMapping(value = "/admin/downloadandsavenseindexdata")
    public String downloadAndSaveNSEIndexData(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        Date maxNiftyDate, maxBSEMidCapDate, maxBSESmallCapDate;
        maxNiftyDate = indexValuationRepository.findMaxKeyDateForKeyTicker("NIFTY");
        model.addAttribute("maxNiftyDate", maxNiftyDate);
        return "admin/downloadandsavenseindexdata";
    }

    @RequestMapping(value=("/admin/downloadandsavenseindexdatastatus"),method=RequestMethod.POST)
    public String downloadAndSaveNSEIndexDataStatus(Model model, @RequestParam("indexProcessDate") String indexProcessDate){
        java.sql.Date sqlDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = dateFormat.parse(indexProcessDate);
            sqlDate = new java.sql.Date(utilDate.getTime());
        } catch (Exception e){
            sqlDate = null;
            model.addAttribute("error", "Please enter the date in the valid form");
            return "admin/downloadandsavenseindexdata";
        }

        if (sqlDate != null) {
            try {
                DownloadEODFiles downloadEODFiles = new DownloadEODFiles();
                downloadEODFiles = new DownloadEODFiles();
                downloadEODFiles.setIndexValuationRepository(indexValuationRepository);
                int returnValue = downloadEODFiles.downloadAndSaveNSEIndexData(sqlDate);
                if (returnValue < 0)
                    model.addAttribute("error", "Failed to download and save NSE Index Data. Please check log files.");
                else
                    model.addAttribute("message", "Successfully downloaded and saved NSE Index Data");
            } catch (Exception e) {
                model.addAttribute("error", "Failed to download and save NSE Index Data. Please check log files.");
            }
            return "admin/downloadandsavenseindexdata";
        } else {
            model.addAttribute("error", "Please enter the date in the valid form");
            return "admin/downloadandsavenseindexdata";
        }
    }

    @RequestMapping(value=("/admin/uploadbseindexdatastatus"),method=RequestMethod.POST)
    public String uploadBSEIndexDataStatus(Model model, @RequestParam("indexProcessDate") String indexProcessDate){
        java.sql.Date sqlDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = dateFormat.parse(indexProcessDate);
            sqlDate = new java.sql.Date(utilDate.getTime());
        } catch (Exception e){
            sqlDate = null;
            model.addAttribute("error", "Please enter the date in the valid form");
            return "admin/uploadbseindexdata";
        }

        if (sqlDate != null) {
            try {
                DownloadEODFiles downloadEODFiles = new DownloadEODFiles();
                downloadEODFiles = new DownloadEODFiles();
                downloadEODFiles.setIndexValuationRepository(indexValuationRepository);
                int returnValue = 0;
                returnValue = downloadEODFiles.uploadBSEIndexData(sqlDate, "BSEMidCap");
                if (returnValue < 0) {
                    model.addAttribute("error", "Failed to upload BSEMidCap Index Data. Please check log files.");
                } else {
                    downloadEODFiles.uploadBSEIndexData(sqlDate, "BSESmallCap");
                    if (returnValue < 0)
                        model.addAttribute("error", "Failed to upload BSESmallCap Index Data. Please check log files.");
                    else
                        model.addAttribute("message", "Successfully uploaded both BSE Mid and Small Cap Index Data");
                }
            } catch (Exception e) {
                model.addAttribute("error", "Failed to download and save BSE Index Data. Please check log files.");
            }
            return "admin/uploadbseindexdata";
        } else {
            model.addAttribute("error", "Please enter the date in the valid form");
            return "admin/uploadbseindexdata";
        }
    }

    @RequestMapping(value = "/admin/generatedbinsertscript")
    public String createDBInsertScript(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/generatedbinsertscript";
    }

    @RequestMapping(value=("/admin/generatedbinsertscriptstatus"),method=RequestMethod.POST)
    public static String generateDBInsertScriptStatus(Model model, @RequestParam("dateRange") String dateRange ) {

        File inputFolderPath = AdminService.getLatestQuarterFolder();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        Date now = new Date(System.currentTimeMillis());
        String outputFileName = dateFormat.format(now) + "_DBInsertScript.sql";

        File outputFile = new File(inputFolderPath, outputFileName);

        try (PrintWriter writer = new PrintWriter(outputFile)) {
            File folder = inputFolderPath;
            // Define a filter to select only Excel files (with .xlsx extension)
            FileFilter excelFilter = file -> file.isFile() &&
                    file.getName().endsWith(".xlsx") &&
                    !file.getName().startsWith("$") &&
                    !file.getName().contains("$");
            File[] files = folder.listFiles(excelFilter);

            // Sort the files based on last modified timestamp (latest first).
            Arrays.sort(files, (file1, file2) -> Long.compare(file2.lastModified(), file1.lastModified()));

            for (File file : files) {
                if (dateRange.equals("Today")) {
                    // Check if the file was modified today.
                    long lastModified = file.lastModified();
                    if (!isToday(lastModified)) {
                        continue;
                    }
                } else if (dateRange.equals("TodayAndYesterday")) {
                    // Check if the file was modified yesterday.
                    long lastModified = file.lastModified();
                    if (!isYesterday(lastModified)) {
                        continue;
                    }
                } else if (dateRange.equals("Last7Days")) {
                    // Check if the file was modified in the last 7 days.
                    long lastModified = file.lastModified();
                    if (!isLast7Days(lastModified)) {
                        continue;
                    }
                }

                if (file.getName().endsWith(".xlsx")) {
                    FileInputStream excelFile = new FileInputStream(file);
                    Workbook workbook = new XSSFWorkbook(excelFile);

                    // Initialize a formula evaluator.
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

                    /*
                    // Evaluate all cells in all sheet
                    for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                        Sheet sheet = workbook.getSheetAt(sheetIndex);

                        // Evaluate all formulas on the current sheet.
                        for (Row row : sheet) {
                            for (Cell cell : row) {
                                try {
                                    evaluator.evaluateInCell(cell);
                                } catch (Exception e) {
                                    // Handle or ignore this specific exception as needed.
                                }
                            }
                        }
                    }*/

                    Sheet sheet = workbook.getSheet("DBInsert");
                    for (int rowNumber = 72; rowNumber <= 488; rowNumber++) {
                        Row row = sheet.getRow(rowNumber);
                        if (row != null) {
                            Cell cell = row.getCell(0);
                            // Evaluate the cell's formula to get the value.
                            CellValue cellValue = evaluator.evaluate(cell);
                            String cellText = cellValue.formatAsString();
                            if(cell != null) {
                                try {
                                    String cellValueStr = cell.getStringCellValue();
                                    writer.println(cellValueStr);
                                }catch (Exception e){

                                }
                            }
                        }
                    }
                    writer.println(); // Separate SQL scripts.
                    workbook.close();
                }
            }
            model.addAttribute("message", "DB insert script generated");
            return "admin/generatedbinsertscript";
        } catch (IOException e) {
            model.addAttribute("error", "Error while generating DB insert script");
            return "admin/generatedbinsertscript";
        }
    }

    private static boolean isToday(long timestamp) {
        long now = System.currentTimeMillis();
        return timestamp >= startOfDay(now) && timestamp <= endOfDay(now);
    }

    private static boolean isYesterday(long timestamp) {
        long now = System.currentTimeMillis();
        return timestamp >= startOfDay(now - 24 * 60 * 60 * 1000) && timestamp <= endOfDay(now - 24 * 60 * 60 * 1000);
    }

    private static boolean isLast7Days(long timestamp) {
        long now = System.currentTimeMillis();
        return timestamp >= startOfDay(now - 7 * 24 * 60 * 60 * 1000);
    }

    private static long startOfDay(long timestamp) {
        return timestamp - (timestamp % (24 * 60 * 60 * 1000));
    }

    private static long endOfDay(long timestamp) {
        return startOfDay(timestamp) + (24 * 60 * 60 * 1000) - 1;
    }

}
