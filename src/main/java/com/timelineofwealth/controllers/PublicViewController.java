package com.timelineofwealth.controllers;

import com.timelineofwealth.apis.PublicApi;
import com.timelineofwealth.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;

@Controller
public class PublicViewController {

    private static final Logger logger = LoggerFactory.getLogger(PublicViewController.class);

    private Date dateToday;

    @RequestMapping(value = "/access-denied")
    public String accessDenied(Model model){
        return "public/access-denied";
    }

    @RequestMapping(value = "/")
    public String index(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited Homepage: IP - " + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/indexreturnstats";
    }

    @RequestMapping(value = "/userlogin", method = RequestMethod.GET)
    public String  login(HttpServletRequest request, Model model, String error, String logout){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited Login: IP - " + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Login");
        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "public/userlogin";
    }

    @RequestMapping("/default")
    public String defaultAfterLogin(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug(String.format("New Login As: /%s", userDetails.getUsername()));
        // set user to save last login time
        CommonService.updateLastLoginStatus(userDetails);
        if (CommonService.isAdmin(userDetails)){
            return "redirect:admin/uploadnsedailypricedata";
        }
        if (CommonService.isAdviser(userDetails)){
            return "redirect:adviser/listofclients";
        }
        return "redirect:user/members";
    }

    @RequestMapping(value = "/public/services")
    public String services(Model model){
        logger.debug(String.format("Visited /public/services"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Services");
        return "public/services";
    }

    @RequestMapping(value = "/public/contactus")
    public String contactus(Model model){
        logger.debug(String.format("Visited /public/contactus"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Contact Us");
        return "public/contactus";
    }

    @RequestMapping(value = "/public/blog0001")
    public String blog0001(Model model){
        logger.debug(String.format("Visited /public/blog0001"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Notes on Financial Shenanigans");
        return "public/blog0001";
    }

    @RequestMapping(value = "/public/wealthmanagementapp")
    public String wealthManagementapp(Model model){
        logger.debug(String.format("Visited /public/wealthmanagementapp"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/wealthmanagementapp";
    }

    @RequestMapping(value = "/public/pmsapp")
    public String pmsApp(Model model){
        logger.debug(String.format("Visited /public/pmsapp"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/pmsapp";
    }

    @RequestMapping(value = "/public/ethicalstandards")
    public String ethicalStandards(Model model){
        logger.debug(String.format("Visited /public/ethicalstandards"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Ethical Standards");
        return "public/ethicalstandards";
    }

    @RequestMapping(value = "/public/whyindependentadvice")
    public String whyIndependentAdvice(Model model){
        logger.debug(String.format("Visited /public/whyindependentadvice"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/whyindependentadvice";
    }

    @RequestMapping(value = "/public/privacy")
    public String privacy(Model model){
        logger.debug(String.format("Visited /public/privacy"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/privacy";
    }


    @RequestMapping(value = "/public/saa")
    public String assetClasses(Model model){
        logger.debug(String.format("Visited /public/saa"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/saa";
    }

    @RequestMapping(value = "/public/mftypes")
    public String mutualFundTypes(Model model){
        logger.debug(String.format("Visited /public/mftypes"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Types of Mutual Funds");
        return "public/mftypes";
    }

    @RequestMapping(value = "/public/mflist")
    public String mutualFundList(Model model){
        logger.debug(String.format("Visited /public/mflist"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Performance of Mutual Funds");
        return "public/mflist";
    }

    @RequestMapping(value = "/public/nifty50stats")
    public String indexStats(Model model){
        logger.debug(String.format("Visited /public/nifty50stats"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "NIFTY-50 Index Statistics");
        return "public/nifty50stats";
    }

    @RequestMapping(value = "/public/stocklist")
    public String stockList(Model model){
        logger.debug(String.format("Visited /public/stocklist"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Valuation Matrix of NSE-BSE 500");
        return "public/stocklist";
    }

    @RequestMapping(value = "/public/stockanalysis/{ticker}")
    public String stockAnalysis(@PathVariable("ticker") String ticker, Model model){
        logger.debug(String.format("Visited /public/stockanalysis/"+ticker));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Fundamental Analysis of "+ CommonService.getStockDetails(ticker).getName());
        model.addAttribute("ticker", ticker);
        return "public/stockanalysis";
    }

    @RequestMapping(value = "/public/indianeconomy")
    public String indianEconomy(Model model){
        logger.debug(String.format("Visited /public/indianeconomy"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Indian Economic Indicators");
        return "public/indianeconomy";
    }

    @RequestMapping(value = "/public/retirementfundcalculation")
    public String retirementFundCalculation(Model model){
        logger.debug(String.format("Visited /public/retirementfundcalculation"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Retirement Fund Calculation");
        return "public/retirementfundcalculation";
    }

    @RequestMapping(value = "/public/indexreturnstats")
    public String indexReturnStats(Model model){
        logger.debug(String.format("Visited /public/indexreturnstats"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "BSE-MidCap and BSE-SmallCap Statistics");
        return "public/indexreturnstats";
    }

    @RequestMapping(value = "/public/benchmarkreturns")
    public String benchmarkReturns(Model model){
        logger.debug(String.format("Visited /public/benchmarkreturns"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Returns Comparision of Benchmarks");
        return "public/benchmarkreturns";
    }

    @RequestMapping(value = "/public/bankanalysis1")
    public String bankAnalysis1(Model model){
        logger.debug(String.format("Visited /public/bankanalysis1"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Banking Stocks Analysis - Large Cap");
        return "public/bankanalysis1";
    }

    @RequestMapping(value = "/public/bankanalysis2")
    public String bankAnalysis2(Model model){
        logger.debug(String.format("Visited /public/bankanalysis2"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Banking Stocks Analysis - Mid Cap");
        return "public/bankanalysis2";
    }

    @RequestMapping(value = "/public/bankanalysis3")
    public String bankAnalysis3(Model model){
        logger.debug(String.format("Visited /public/bankanalysis3"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Banking Stocks Analysis - Small Cap");
        return "public/bankanalysis3";
    }

    @RequestMapping(value = "/public/blog1")
    public String blog1(Model model){
        logger.debug(String.format("Visited /public/blog1"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Notes on Financial Shenanigans");
        return "/public/blog1";
    }

    @RequestMapping(value = "/public/stockbubblechart")
    public String stockBubbleChart(Model model){
        logger.debug(String.format("Visited /public/stockbubblechart"));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Stock Bubble Chart");
        return "/public/stockbubblechart";
    }


}
