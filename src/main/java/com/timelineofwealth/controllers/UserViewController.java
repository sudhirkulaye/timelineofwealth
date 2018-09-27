package com.timelineofwealth.controllers;

import com.timelineofwealth.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class UserViewController {

    private static final Logger logger = LoggerFactory.getLogger(UserViewController.class);

    @RequestMapping(value = "/user/members", method = RequestMethod.GET)
    public String members(Model model, @AuthenticationPrincipal UserDetails userDetails){
        Date dateToday = new Date();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "TimelineOfWealth-Members");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/members";
    }

    @RequestMapping(value = "/user/incexpsavings", method = RequestMethod.GET)
    public String income(Model model, @AuthenticationPrincipal UserDetails userDetails){
        Date dateToday = new Date();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "TimelineOfWealth-Income Expense");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "user/incexpsavings";
    }


}
