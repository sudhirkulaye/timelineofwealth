package com.timelineofwealth.apis;


import com.timelineofwealth.dto.ConsolidatedPortfolioHoldings;
import com.timelineofwealth.dto.FinYearProfit;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "user/api/")
public class PortfolioRestApi {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioRestApi.class);

    @RequestMapping(value = "/getportfolios", method = RequestMethod.GET)
    public List<Portfolio> getPortfolios() {
        logger.debug(String.format("Call user/api/getportfolios/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getPortfolios(user.getEmail());
    }

    @RequestMapping(value = "/getindividualholdings", method = RequestMethod.GET)
    public List<PortfolioHoldings> getIndividualHoldings() {
        logger.debug(String.format("Call user/api/getindividualholdings/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getPortfolioHoldings(user.getEmail());
    }

    //getConsolidatedPortfolioHoldings
    @RequestMapping(value = "/getconsolidatedportfolioholdings", method = RequestMethod.GET)
    public List<ConsolidatedPortfolioHoldings> getConsolidatedPortfolioHoldings() {
        logger.debug(String.format("Call user/api/getconsolidatedportfolioholdings/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getConsolidatedPortfolioHoldings(user.getEmail());
    }

    @RequestMapping(value = "/getportfoliohistoricalholdings", method = RequestMethod.GET)
    public List<PortfolioHistoricalHoldings> getPortfolioHistoricalHoldings() {
        logger.debug(String.format("Call user/api/getportfoliohistoricalholdings/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getPortfolioHistoricalHoldings(user.getEmail());
    }

    @RequestMapping(value = "/getfinyearprofit", method = RequestMethod.GET)
    public List<FinYearProfit> getFinYearProfit() {
        logger.debug(String.format("Call user/api/getfinyearprofit/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getFinYearProfit(user.getEmail());
    }

    @RequestMapping(value = "/getportfoliocashflows", method = RequestMethod.GET)
    public List<PortfolioCashflow> getPortfolioCashflows() {
        logger.debug(String.format("Call user/api/getportfolioCashflows/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getPortfolioCashflows(user.getEmail());
    }

    @RequestMapping(value = "/getportfoliotwrrsummary", method = RequestMethod.GET)
    public List<PortfolioTwrrSummary> getPortfolioTwrrSummary() {
        logger.debug(String.format("Call user/api/getportfoliotwrrsummary/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getPortfolioTwrrSummary(user.getEmail());
    }

    @RequestMapping(value = "/getportfoliotwrrmonthly", method = RequestMethod.GET)
    public List<PortfolioTwrrMonthly> getPortfolioTwrrMonthly() {
        logger.debug(String.format("Call user/api/getportfoliotwrrmonthly/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return PortfolioService.getPortfolioTwrrMonthly(user.getEmail());
    }
}
