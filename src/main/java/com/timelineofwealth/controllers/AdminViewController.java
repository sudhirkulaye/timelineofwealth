package com.timelineofwealth.controllers;

import com.timelineofwealth.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
public class AdminViewController {

    private static final Logger logger = LoggerFactory.getLogger(AdminViewController.class);

    @RequestMapping(value = "/admin/uploadeodstockprices")
    public String services(Model model, @AuthenticationPrincipal UserDetails userDetails){
        Date dateToday = new Date();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "TimelineOfWealth-Members");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "admin/uploadeodstockprices";
    }
}
