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
            return "redirect:admin/uploadnsedailypricedata";
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
            return "redirect:admin/uploadbsedailypricedata";
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
            return "redirect:admin/uploadmfnavdata";
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
            int positionOfSchemeCode = 0, positionOfDate = 0, positionOfNav = 0, schemeNamePostion = 0;
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
                    }
                    continue;
                }
                if(line.isEmpty()|| line.size() < 2){ //any blank spaces
                    continue;
                }
                Integer code = Integer.parseInt(line.get(positionOfSchemeCode));
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                java.sql.Date date = null;
                String isin;
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

                MutualFundNavHistory mutualFundNavHistory = new MutualFundNavHistory();
                mutualFundNavHistory.setKey(new MutualFundNavHistory.MutualFundNavHistoryKey(code, date));
                mutualFundNavHistory.setNav(nav);
                mutualFundNavHistories.add(mutualFundNavHistory);
                int countBySchemeCode = mutualFundUniverseRepository.countBySchemeCode(new Long(code));

                if (countBySchemeCode == 0 && date.after(CommonService.getSetupDates().getDateLastTradingDay())){
                    MutualFundUniverse mutualFundUniverse = new MutualFundUniverse();
                    mutualFundUniverse.setSchemeCode(new Long(code));
                    mutualFundUniverse.setSchemeNameFull(schemeName);
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
                }
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

    @RequestMapping(value = "/admin/uploaddailydatab")
    public String uploadDailyDataB(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploaddailydatab";
    }

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
