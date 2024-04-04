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
    public String services(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/services" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Services");
        return "public/services";
    }

    @RequestMapping(value = "/public/contactus")
    public String contactus(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/contactus" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Contact Us");
        return "public/contactus";
    }

    @RequestMapping(value = "/public/blog0001")
    public String blog0001(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/blog0001" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Notes on Financial Shenanigans");
        return "public/blog0001";
    }

    @RequestMapping(value = "/public/wealthmanagementapp")
    public String wealthManagementapp(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/wealthmanagementapp" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/wealthmanagementapp";
    }

    @RequestMapping(value = "/public/pmsapp")
    public String pmsApp(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/pmsapp" + ipAddress + ", browser - " + browser + ", OS - " + os));

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/pmsapp";
    }

    @RequestMapping(value = "/public/ethicalstandards")
    public String ethicalStandards(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/ethicalstandards" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Ethical Standards");
        return "public/ethicalstandards";
    }

    @RequestMapping(value = "/public/whyindependentadvice")
    public String whyIndependentAdvice(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/whyindependentadvice" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/whyindependentadvice";
    }

    @RequestMapping(value = "/public/privacy")
    public String privacy(Model model, HttpServletRequest request){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/privacy" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/privacy";
    }


    @RequestMapping(value = "/public/saa")
    public String assetClasses(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/saa" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Sudhir Kulaye");
        return "public/saa";
    }

    @RequestMapping(value = "/public/mftypes")
    public String mutualFundTypes(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/mftypes" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Types of Mutual Funds");
        return "public/mftypes";
    }

    @RequestMapping(value = "/public/mflist")
    public String mutualFundList(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/mflist" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Funds' Performance");
        return "public/mflist";
    }

    @RequestMapping(value = "/public/nifty50stats")
    public String indexStats(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/nifty50stats" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "NIFTY-50");
        return "public/nifty50stats";
    }

    @RequestMapping(value = "/public/stocklist")
    public String stockList(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/stocklist" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Stocks Fundamentals");
        return "public/stocklist";
    }

    @RequestMapping(value = "/public/stockanalysis/{ticker}")
    public String stockAnalysis(HttpServletRequest request, @PathVariable("ticker") String ticker, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/stockanalysis" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Fundamentals of "+ CommonService.getStockDetails(ticker).getName());
        model.addAttribute("ticker", ticker);
        return "public/stockanalysis";
    }

    @RequestMapping(value = "/public/indianeconomy")
    public String indianEconomy(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/indianeconomy" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Indian Economic Indicators");
        return "public/indianeconomy";
    }

    @RequestMapping(value = "/public/retirementfundcalculation")
    public String retirementFundCalculation(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/retirementfundcalculation" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Retirement Fund Calculation");
        return "public/retirementfundcalculation";
    }

    @RequestMapping(value = "/public/indexreturnstats")
    public String indexReturnStats(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/indexreturnstats" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Index Statistics");
        return "public/indexreturnstats";
    }

    @RequestMapping(value = "/public/benchmarkreturns")
    public String benchmarkReturns(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/benchmarkreturns" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Benchmarks Returns");
        return "public/benchmarkreturns";
    }

    @RequestMapping(value = "/public/bankanalysis1")
    public String bankAnalysis1(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/bankanalysis1" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Banking Stocks Analysis - Large Cap");
        return "public/bankanalysis1";
    }

    @RequestMapping(value = "/public/bankanalysis2")
    public String bankAnalysis2(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/bankanalysis2" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Banking Stocks Analysis - Mid Cap");
        return "public/bankanalysis2";
    }

    @RequestMapping(value = "/public/bankanalysis3")
    public String bankAnalysis3(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/bankanalysis3" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Banking Stocks Analysis - Small Cap");
        return "public/bankanalysis3";
    }

    @RequestMapping(value = "/public/stockbubblechart")
    public String stockBubbleChart(HttpServletRequest request, Model model){
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        logger.debug(String.format("Visited /public/stockbubblechart" + ipAddress + ", browser - " + browser + ", OS - " + os));
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Stock Bubble Chart");
        return "/public/stockbubblechart";
    }


}
