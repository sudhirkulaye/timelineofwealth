package com.timelineofwealth.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timelineofwealth.apis.PublicApi;
import com.timelineofwealth.dto.DailyDataBJSON;
import com.timelineofwealth.dto.Ticker;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import com.timelineofwealth.service.CSVUtils;
import com.timelineofwealth.service.CommonService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
                        if (column.trim().equalsIgnoreCase("SYMBOL")){
                            nseTickerPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("SERIES")){
                            seriesPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("OPEN") || column.trim().equalsIgnoreCase("Open Price")){
                            openPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("HIGH") || column.trim().equalsIgnoreCase("High Price")){
                            highPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LOW") || column.trim().equalsIgnoreCase("Low Price")){
                            lowPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LOW") || column.trim().equalsIgnoreCase("Low Price")){
                            lowPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("CLOSE") || column.trim().equalsIgnoreCase("Close Price")){
                            closePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("LAST") || column.trim().equalsIgnoreCase("Last Price")){
                            lastPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("PREVCLOSE") || column.trim().equalsIgnoreCase("Prev Close")){
                            previousClosePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TOTTRDQTY") || column.trim().equalsIgnoreCase("Total Traded Quantity")){
                            totalTradedQuantityPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TOTTRDVAL") || column.trim().equalsIgnoreCase("Turnover")){
                            totalTradedValuePosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TIMESTAMP") || column.trim().equalsIgnoreCase("Date")){
                            dateStringPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("TOTALTRADES") || column.trim().equalsIgnoreCase("No. of Trades")){
                            totalTradesPosition = i;
                        }
                        if (column.trim().equalsIgnoreCase("ISINISIN") ){
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
                        series.equalsIgnoreCase("SM"))){
                    continue;
                }
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
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
            nsePriceHistoryRepository.save(nsePriceHistories);
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

            while (scanner.hasNext()) {
                List<String> line = csvUtils.parseLine(scanner.nextLine());
                if(isHeader){
                    isHeader = false;
                    continue;
                }
                String bseTicker = line.get(0);
                String dateString = line.get(15);
                if(bseTicker == null || bseTicker.isEmpty()|| dateString == null || dateString.isEmpty()){
                    continue;
                }
                String companyName = line.get(1);
                String companyGroup = line.get(2).trim();
                String companyType = line.get(3);

                if(!(companyGroup.equalsIgnoreCase("A") ||
                        companyGroup.equalsIgnoreCase("B") ||
                        companyGroup.equalsIgnoreCase("T"))){
                    continue;
                }
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy");
                java.sql.Date date = null;
                try {
                    date = new java.sql.Date(format.parse(dateString).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                BigDecimal opernPrice = line.get(4) != null && !line.get(4).isEmpty() ? new BigDecimal(line.get(4)) : new BigDecimal(0);
                BigDecimal highPrice = line.get(5) != null && !line.get(5).isEmpty() ? new BigDecimal(line.get(5)) : new BigDecimal(0);
                BigDecimal lowPrice = line.get(6) != null && !line.get(6).isEmpty() ? new BigDecimal(line.get(6)) : new BigDecimal(0);
                BigDecimal closePrice = line.get(7) != null && !line.get(7).isEmpty() ? new BigDecimal(line.get(7)) : new BigDecimal(0);
                BigDecimal lastPrice = line.get(8) != null && !line.get(8).isEmpty() ? new BigDecimal(line.get(8)) : new BigDecimal(0);
                BigDecimal previousClosePrice = line.get(9) != null && !line.get(9).isEmpty() ? new BigDecimal(line.get(9)) : new BigDecimal(0);
                BigDecimal totalTrades = line.get(10) != null && !line.get(10).isEmpty() ? new BigDecimal(line.get(10)) : new BigDecimal(0);

                BigDecimal totalTradedQuantity = line.get(11) != null && !line.get(11).isEmpty() ? new BigDecimal(line.get(11)) : new BigDecimal(0);
                BigDecimal totalTradedValue = line.get(12) != null && !line.get(12).isEmpty() ? new BigDecimal(line.get(12)) : new BigDecimal(0);;

                String isinCode = line.get(14);

                BsePriceHistory bsePriceHistory = new BsePriceHistory();
                bsePriceHistory.setKey(new BsePriceHistory.BsePriceHistoryKey(bseTicker, date));
                bsePriceHistory.setCompanyName(companyName);
                bsePriceHistory.setCompanyGroup(companyGroup);
                bsePriceHistory.setCompanyType(companyType);
                bsePriceHistory.setOpenPrice(opernPrice);
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
            //nsePriceHistories.sort(Comparator.comparing(CompanyDailyDataG::getCompanyDailyMarketCapNo).reversed());
            bsePriceHistoryRepository.save(bsePriceHistories);
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
            mutualFundNavHistoryRepository.save(mutualFundNavHistories);
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

    @RequestMapping(value=("/admin/uploaddailydatasstatus"),headers=("content-type=multipart/*"),method= RequestMethod.POST)
    public String uploadDailyDataSStatus (Model model, @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "admin/uploaddailydatas";
        }
        try {
            List<DailyDataS> dailyDataSList = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet worksheet = workbook.getSheetAt(0);

            for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
                DailyDataS dailyDataS = new DailyDataS();

                XSSFRow row = worksheet.getRow(i);
                dailyDataS.setKey(new DailyDataS.DailyDataSKey());
                dailyDataS.getKey().setDate(new java.sql.Date(row.getCell(0).getDateCellValue().getTime()));
                dailyDataS.setRank((int) row.getCell(1).getNumericCellValue());
                dailyDataS.getKey().setName((String) row.getCell(2).getStringCellValue());
                dailyDataS.setCmp(new BigDecimal(row.getCell(3).getNumericCellValue()));
                dailyDataS.setMarketCap(new BigDecimal(row.getCell(4).getNumericCellValue()));
                dailyDataS.setLastResultDate((int) row.getCell(5).getNumericCellValue());
                dailyDataS.setNetProfit(new BigDecimal(row.getCell(6).getNumericCellValue()));
                dailyDataS.setSales(new BigDecimal(row.getCell(7).getNumericCellValue()));
                dailyDataS.setYoyQuarterlySalesGrowth(new BigDecimal(row.getCell(8).getNumericCellValue()));
                dailyDataS.setYoyQuarterlyProfitGrowth(new BigDecimal(row.getCell(9).getNumericCellValue()));
                dailyDataS.setQoqSalesGrowth(new BigDecimal(row.getCell(10).getNumericCellValue()));
                dailyDataS.setQoqProfitGrowth(new BigDecimal(row.getCell(11).getNumericCellValue()));
                dailyDataS.setOpmLatestQuarter(new BigDecimal(row.getCell(12).getNumericCellValue()));
                dailyDataS.setOpmLastYear(new BigDecimal(row.getCell(13).getNumericCellValue()));
                dailyDataS.setNpmLatestQuarter(new BigDecimal(row.getCell(14).getNumericCellValue()));
                dailyDataS.setNpmLastYear(new BigDecimal(row.getCell(15).getNumericCellValue()));
                dailyDataS.setProfitGrowth3years(new BigDecimal(row.getCell(16).getNumericCellValue()));
                dailyDataS.setSalesGrowth3years(new BigDecimal(row.getCell(17).getNumericCellValue()));
                dailyDataS.setPeTtm(new BigDecimal(row.getCell(18).getNumericCellValue()));
                dailyDataS.setHistoricalPe3years(new BigDecimal(row.getCell(19).getNumericCellValue()));
                dailyDataS.setPegRatio(new BigDecimal(row.getCell(20).getNumericCellValue()));
                dailyDataS.setPbTtm(new BigDecimal(row.getCell(21).getNumericCellValue()));
                dailyDataS.setEvToEbit(new BigDecimal(row.getCell(22).getNumericCellValue()));
                dailyDataS.setDividendPayout(new BigDecimal(row.getCell(23).getNumericCellValue()));
                dailyDataS.setRoe(new BigDecimal(row.getCell(24).getNumericCellValue()));
                dailyDataS.setAvgRoe3years(new BigDecimal(row.getCell(25).getNumericCellValue()));
                dailyDataS.setDebt(new BigDecimal(row.getCell(26).getNumericCellValue()));
                dailyDataS.setDebtToEquity(new BigDecimal(row.getCell(27).getNumericCellValue()));
                dailyDataS.setDebt3yearsback(new BigDecimal(row.getCell(28).getNumericCellValue()));
                try{
                    dailyDataS.setRoce(new BigDecimal(row.getCell(29).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setRoce(new BigDecimal(0));
                }
                try{
                    dailyDataS.setAvgRoce3years(new BigDecimal(row.getCell(30).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setAvgRoce3years(new BigDecimal(0));
                }
                try{
                    dailyDataS.setFcfS(new BigDecimal(row.getCell(31).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setFcfS(new BigDecimal(0));
                }
                try{
                    dailyDataS.setSalesGrowth5years(new BigDecimal(row.getCell(32).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setSalesGrowth5years(new BigDecimal(0));
                }
                try{
                    dailyDataS.setSalesGrowth10years(new BigDecimal(row.getCell(33).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setSalesGrowth10years(new BigDecimal(0));
                }
                try{
                    dailyDataS.setNoplat(new BigDecimal(row.getCell(34).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setNoplat(new BigDecimal(0));
                }
                try{
                    dailyDataS.setCapex(new BigDecimal(row.getCell(35).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setCapex(new BigDecimal(0));
                }
                try{
                    dailyDataS.setFcff(new BigDecimal(row.getCell(36).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setFcff(new BigDecimal(0));
                }
                try{
                    dailyDataS.setInvestedCapital(new BigDecimal(row.getCell(37).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setInvestedCapital(new BigDecimal(0));
                }
                try{
                    dailyDataS.setRoic(new BigDecimal(row.getCell(38).getNumericCellValue()));
                } catch (Exception e) {
                    dailyDataS.setRoic(new BigDecimal(0));
                }
                dailyDataS.setMcapToNetprofit(new BigDecimal(0));
                dailyDataS.setMcapToSales(new BigDecimal(0));
                dailyDataS.setSector("");
                dailyDataS.setIndustry("");
                dailyDataS.setSubIndustry("");

                dailyDataSList.add(dailyDataS);
            }
            dailyDataSList.sort(Comparator.comparing(DailyDataS::getMarketCap).reversed());
            dailyDataSRepository.save(dailyDataSList);

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploaddailydatas";
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
    public String uploadMOSLTxnStatus (Model model, @RequestParam("file") MultipartFile file){
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
            moslTransactionRepository.save(moslTransactions);

            StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery(AP_PROCESS_MOSL_TRANSACTIONS);
            boolean result = storedProcedure.execute();
            if (!result) {
                model.addAttribute("message", "Successfully uploaded transactions");
            } else {
                model.addAttribute("message", "Failed to upload transactions successfully. Check log_table.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploadmosltxn";
    }

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
            dailyDataBRepository.save(dailyDataBList);

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "admin/uploaddailydatab";
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

    @RequestMapping(value = "/admin/indexstatistics")
    public String indexStatistics(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/indexstatistics";
    }

}
