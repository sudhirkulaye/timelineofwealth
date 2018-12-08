package com.timelineofwealth.apis;

import com.timelineofwealth.entities.User;
import com.timelineofwealth.entities.WealthDetails;
import com.timelineofwealth.entities.WealthHistory;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.WealthDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "user/api/")
public class WealthDetailsRestApi {
    private static final Logger logger = LoggerFactory.getLogger(WealthDetailsRestApi.class);

    @RequestMapping(value = "/getwealthdetailsrecords", method = RequestMethod.GET)
    public List<WealthDetails> getWealthDetailsRecords() {
        logger.debug(String.format("Call user/api/getwealthdetailsrecords/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return WealthDetailsService.getWealthDetailsRecords(user.getEmail());
    }

    @RequestMapping(value = "/updatewealthdetailsrecord", method = RequestMethod.PUT)
    public List<WealthDetails> updateWealthDetailsRecord(@RequestBody WealthDetails editedRecord) {
        logger.debug("Call user/api/updatewealthdetailsrecord/ " + editedRecord.getKey().getMemberid());
        WealthDetailsService.updateWealthDetailsRecord(editedRecord);
        return getWealthDetailsRecords();
    }

    @RequestMapping(value = "/addwealthdetailsrecord", method = RequestMethod.POST)
    public List<WealthDetails> addWealthDetailsRecord(@RequestBody WealthDetails newRecord) {
        logger.debug("Call user/api/addwealthdetailsrecord/ " + newRecord.getKey().getMemberid());
        WealthDetailsService.addWealthDetailsRecord(newRecord);
        return getWealthDetailsRecords();
    }

    @RequestMapping(value = "/deletewealthdetailsrecord", method = RequestMethod.POST)
    public List<WealthDetails> deleteWealthDetailsRecord(@RequestBody WealthDetails deleteRecord) {
        logger.debug("Call user/api/deletewealthdetailsrecord/ " + deleteRecord.getKey().getMemberid());
        WealthDetailsService.deleteWealthDetailsRecord(deleteRecord);
        return getWealthDetailsRecords();
    }

    @RequestMapping(value = "/getwealthhistory", method = RequestMethod.GET)
    public Map<Date, Map<Long, BigDecimal>> getWealthHistory() {
        logger.debug(String.format("Call user/api/getwealthhistory/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return WealthDetailsService.getWealthHistoryRecords(user.getEmail());
    }


}
