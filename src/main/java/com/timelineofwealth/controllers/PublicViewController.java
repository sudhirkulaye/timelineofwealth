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
    public String index(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/index";
    }

    @RequestMapping(value = "/userlogin", method = RequestMethod.GET)
    public String  login(Model model, String error, String logout){
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
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Services");
        return "public/services";
    }

    @RequestMapping(value = "/public/contactus")
    public String contactus(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Contact Us");
        return "public/contactus";
    }

    @RequestMapping(value = "/public/wealthmanagementapp")
    public String wealthManagementapp(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/wealthmanagementapp";
    }

    @RequestMapping(value = "/public/pmsapp")
    public String pmsApp(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/pmsapp";
    }

    @RequestMapping(value = "/public/ethicalstandards")
    public String ethicalStandards(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Ethical Standards");
        return "public/ethicalstandards";
    }

    @RequestMapping(value = "/public/whyindependentadvice")
    public String whyIndependentAdvice(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/whyindependentadvice";
    }

    @RequestMapping(value = "/public/privacy")
    public String privacy(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/privacy";
    }


    @RequestMapping(value = "/public/saa")
    public String assetClasses(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/saa";
    }

    @RequestMapping(value = "/public/mftypes")
    public String mutualFundTypes(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Types of Mutual Funds");
        return "public/mftypes";
    }

    @RequestMapping(value = "/public/mflist")
    public String mutualFundList(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Performance of Mutual Funds");
        return "public/mflist";
    }

    @RequestMapping(value = "/public/indexstats")
    public String indexStats(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "NIFTY-50 Index Statistics");
        return "public/indexstats";
    }

    @RequestMapping(value = "/public/stocklist")
    public String stockList(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Valuation Matrix of NSE-BSE 500");
        return "public/stocklist";
    }

    @RequestMapping(value = "/public/stockanalysis/{ticker}")
    public String stockAnalysis(@PathVariable("ticker") String ticker, Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Fundamental Analysis of "+ CommonService.getStockDetails(ticker).getName());
        model.addAttribute("ticker", ticker);
        return "public/stockanalysis";
    }

    @RequestMapping(value = "/public/indianeconomy")
    public String indianEconomy(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Indian Economic Indicators");
        return "public/indianeconomy";
    }

    @RequestMapping(value = "/public/retirementfundcalculation")
    public String retirementFundCalculation(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Retirement Fund Calculation");
        return "public/retirementfundcalculation";
    }

    @RequestMapping(value = "/public/midandsmallcapindexstats")
    public String midAndSmallCapIndexStats(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "BSE-MidCap and BSE-SmallCap Statistics");
        return "public/midandsmallcapindexstats";
    }

    @RequestMapping(value = "/public/benchmarkreturns")
    public String benchmarkReturns(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Returns Comparision of Benchmarks");
        return "public/benchmarkreturns";
    }
}
