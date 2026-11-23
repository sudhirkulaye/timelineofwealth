package com.timelineofwealth.apis;

import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.ConsensusService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Yahoo consensus JSON for the stock list, the public consensus dashboard and the adviser's Focus Five page.
 * Public responses carry no broker names (targets are labelled A and B) and no portfolio weights;
 * /adviser/** needs the adviser login (WebSecurityConfigurer).
 */
@RestController
public class ConsensusApi {
    private final ConsensusService consensusService;
    private final CommonService commonService;

    public ConsensusApi(ConsensusService consensusService, CommonService commonService) {
        this.consensusService = consensusService;
        this.commonService = commonService;
    }

    /** ticker -> latest consensus (target and EPS ranges, 4-week revisions, broker targets A / B). */
    @RequestMapping(value = "/public/api/getstocklistconsensus", method = RequestMethod.GET)
    public Map<String, Map<String, Object>> getStockListConsensus() {
        return consensusService.latestByTicker();
    }

    /** Public watchlists for the stock list filter, in order: [{name, group}]. */
    @RequestMapping(value = "/public/api/getwatchlists", method = RequestMethod.GET)
    public List<Map<String, String>> getWatchlists() {
        return commonService.getPublicWatchlists();
    }

    /** Market, industries, idea lists and scoreboard for /public/consensus. */
    @RequestMapping(value = "/public/api/getconsensusdashboard", method = RequestMethod.GET)
    public Map<String, Object> getConsensusDashboard() {
        return consensusService.dashboard();
    }

    /** Focus Five holdings with weights, named broker views and daily performance against the indices. */
    @RequestMapping(value = "/adviser/api/getfocusfiveconsensus", method = RequestMethod.GET)
    public Map<String, Object> getFocusFiveConsensus() {
        return consensusService.focusFive();
    }
}
