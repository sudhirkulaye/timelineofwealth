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
        model.addAttribute("title", "Timeline of Wealth");
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
        model.addAttribute("title", "Timeline of Wealth");
        return "public/services";
    }

    @RequestMapping(value = "/public/contactus")
    public String contactus(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/contactus";
    }

    @RequestMapping(value = "/public/whatwedo")
    public String whatWeDo(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/whatwedo";
    }

    @RequestMapping(value = "/public/ethicalstandards")
    public String ethicalStandards(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
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
        model.addAttribute("title", "Timeline of Wealth");
        return "public/mftypes";
    }

    @RequestMapping(value = "/public/mflist")
    public String mutualFundList(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/mflist";
    }

    @RequestMapping(value = "/public/indexstats")
    public String indexStats(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/indexstats";
    }

    @RequestMapping(value = "/public/stocklist")
    public String stockList(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/stocklist";
    }

    @RequestMapping(value = "/public/stockanalysis/{ticker}")
    public String stockAnalysis(@PathVariable("ticker") String ticker, Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        model.addAttribute("ticker", ticker);
        return "public/stockanalysis";
    }

    @RequestMapping(value = "/public/indianeconomy")
    public String indianEconomy(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/indianeconomy";
    }

    @RequestMapping(value = "/public/retirementfundcalculation")
    public String retirementFundCalculation(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/retirementfundcalculation";
    }

    @RequestMapping(value = "/public/midandsmallcapindexstats")
    public String midAndSmallCapIndexStats(Model model){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Timeline of Wealth");
        return "public/midandsmallcapindexstats";
    }
}
