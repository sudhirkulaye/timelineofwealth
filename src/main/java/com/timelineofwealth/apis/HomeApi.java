package com.timelineofwealth.apis;

import com.timelineofwealth.service.MarketHomeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Home page feeds: live markets (delayed), NIFTY 50 movers and market headlines. All cached server side. */
@RestController
@RequestMapping(value = "public/api/home")
public class HomeApi {
    private final MarketHomeService service;

    public HomeApi(MarketHomeService service) {
        this.service = service;
    }

    @RequestMapping(value = "/markets", method = RequestMethod.GET)
    public Map<String, Object> markets() { return service.markets(); }

    @RequestMapping(value = "/movers", method = RequestMethod.GET)
    public Map<String, Object> movers() { return service.movers(); }

    @RequestMapping(value = "/news", method = RequestMethod.GET)
    public Map<String, Object> news() { return service.news(); }
}
