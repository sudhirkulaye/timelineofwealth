package com.timelineofwealth.apis;

import com.timelineofwealth.service.MarketHomeService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Home page feeds: live markets (delayed), a year of daily closes for 1M to 1Y returns, NIFTY 50 movers,
 * the NIFTY 50 valuation, long-term index returns, research teasers and newspaper headlines. All cached server side.
 */
@RestController
@RequestMapping(value = "public/api/home")
public class HomeApi {
    private final MarketHomeService service;

    public HomeApi(MarketHomeService service) {
        this.service = service;
    }

    @RequestMapping(value = "/markets", method = RequestMethod.GET)
    public Map<String, Object> markets() { return service.markets(); }

    /** daily closes change once a day: let the browser keep them for 15 minutes */
    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> history() {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic()).body(service.history());
    }

    @RequestMapping(value = "/movers", method = RequestMethod.GET)
    public Map<String, Object> movers() { return service.movers(); }

    @RequestMapping(value = "/valuation", method = RequestMethod.GET)
    public Map<String, Object> valuation() { return service.valuation(); }

    @RequestMapping(value = "/longterm", method = RequestMethod.GET)
    public Map<String, Object> longTerm() { return service.longTerm(); }

    @RequestMapping(value = "/research", method = RequestMethod.GET)
    public Map<String, Object> research() { return service.research(); }

    @RequestMapping(value = "/news", method = RequestMethod.GET)
    public Map<String, Object> news() { return service.news(); }
}
