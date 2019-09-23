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
public class UserViewController {

    private static final Logger logger = LoggerFactory.getLogger(UserViewController.class);
    private java.sql.Date dateToday; // = new PublicApi().getSetupDates().getDateToday();
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserViewController(Environment environment){}

    @RequestMapping(value = "/user/members", method = RequestMethod.GET)
    public String members(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Members");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/members";
    }

    @RequestMapping(value = "/user/incexpsavings", method = RequestMethod.GET)
    public String incomeExpenseAndSavings(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Income Expense");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/incexpsavings";
    }

    @RequestMapping(value = "/user/liabilities", method = RequestMethod.GET)
    public String liabilites(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Liability");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/liabilities";
    }

    @RequestMapping(value = "/user/insurances", method = RequestMethod.GET)
    public String insurances(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Insurances");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/insurances";
    }

    @RequestMapping(value = "/user/liquidities", method = RequestMethod.GET)
    public String liquidityneed(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Liquidity Needs");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/liquidities";
    }

    @RequestMapping(value = "/user/sip", method = RequestMethod.GET)
    public String sip(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Recurring Investments");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/sip";
    }

    @RequestMapping(value = "/user/addassets", method = RequestMethod.GET)
    public String buyTxn(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Audit Assets");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/addassets";
    }

    /*@RequestMapping(value = "/user/sellassets", method = RequestMethod.GET)
    public String sellTxn(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "TimelineOfWealth- Sell Transaction");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/sellassets";
    }*/

    @RequestMapping(value = "/user/wealthdistribution", method = RequestMethod.GET)
    public String wealthDistribution(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Wealth Distribution");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/wealthdistribution";
    }

    @RequestMapping(value = "/user/wealthhistory", method = RequestMethod.GET)
    public String wealthHistory(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Wealth History");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/wealthhistory";
    }

    @RequestMapping(value = "/user/portfolioholdings", method = RequestMethod.GET)
    public String portfolioHoldings(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Portfolion Current Holdings");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/portfolioholdings";
    }

    @RequestMapping(value = "/user/portfoliohistoricalholdings", method = RequestMethod.GET)
    public String portfolioHistoricalHoldings(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Portfolio Historical Holdings");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/portfoliohistoricalholdings";
    }

    @RequestMapping(value = "/user/portfoliocashflows", method = RequestMethod.GET)
    public String portfolioCashflows(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Portfolio Cashflows");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/portfoliocashflows";
    }

    @RequestMapping(value = "/user/portfolioreturns", method = RequestMethod.GET)
    public String portfolioReturns(Model model, @AuthenticationPrincipal UserDetails userDetails){

        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("dateToday", dateToday);
        model.addAttribute("title", "Portfolio Returns");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/portfolioreturns";
    }

}
