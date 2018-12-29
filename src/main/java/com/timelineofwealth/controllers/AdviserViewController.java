package com.timelineofwealth.controllers;

import com.timelineofwealth.apis.PublicApi;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

@Controller
public class AdviserViewController {


    private static final Logger logger = LoggerFactory.getLogger(AdviserViewController.class);
    private java.sql.Date dateToday; // = new PublicApi().getSetupDates().getDateToday();
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AdviserViewController(Environment environment){}

    @RequestMapping(value = "/adviser/listofclients")
    public String listOfClients(Model model, @AuthenticationPrincipal UserDetails userDetails){
        Date dateToday = new Date();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "TimelineOfWealth-Clients");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/listofclients";
    }

    @RequestMapping(value = "/adviser/members", method = RequestMethod.GET)
    public String members(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth-Members");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/members";
    }

    @RequestMapping(value = "/adviser/incexpsavings", method = RequestMethod.GET)
    public String incomeExpenseAndSavings(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth-Income Expense");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/incexpsavings";
    }

    @RequestMapping(value = "/adviser/liabilities", method = RequestMethod.GET)
    public String liabilites(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth-Liability");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/liabilities";
    }

    @RequestMapping(value = "/adviser/insurances", method = RequestMethod.GET)
    public String insurances(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth-Insurances");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/insurances";
    }

    @RequestMapping(value = "/adviser/liquidities", method = RequestMethod.GET)
    public String liquidityneed(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth- Liquidity Needs");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/liquidities";
    }

    @RequestMapping(value = "/adviser/sip", method = RequestMethod.GET)
    public String sip(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth- Regular Investments");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/sip";
    }

    @RequestMapping(value = "/adviser/addassets", method = RequestMethod.GET)
    public String buyTxn(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth- Buy Transaction");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/addassets";
    }

    @RequestMapping(value = "/adviser/sellassets", method = RequestMethod.GET)
    public String sellTxn(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth- Sell Transaction");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/sellassets";
    }

    @RequestMapping(value = "/adviser/wealthdistribution", method = RequestMethod.GET)
    public String wealthDistribution(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth- Wealth Distribution");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/wealthdistribution";
    }

    @RequestMapping(value = "/adviser/wealthhistory", method = RequestMethod.GET)
    public String wealthHistory(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth- Wealth History");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/wealthhistory";
    }

    @RequestMapping(value = "/adviser/consolidatedassets")
    public String consolidatedAssets(Model model, @AuthenticationPrincipal UserDetails userDetails){
        Date dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "TimelineOfWealth-Consolidated Assets");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/consolidatedassets";
    }


}
