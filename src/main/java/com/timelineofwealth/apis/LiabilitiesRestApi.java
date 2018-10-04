package com.timelineofwealth.apis;

import com.timelineofwealth.entities.Liabilities;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.LiabilitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(value = "user/api/")
public class LiabilitiesRestApi {
    private static final Logger logger = LoggerFactory.getLogger(LiabilitiesRestApi.class);

    @RequestMapping(value = "/getliabilities", method = RequestMethod.GET)
    public List<Liabilities> getliabilitiesRecords() {
        logger.debug(String.format("Call user/api/getliabilities/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return LiabilitiesService.getLiabilitiesRecords(user.getEmail());
    }

    @RequestMapping(value = "/updateliability", method = RequestMethod.PUT)
    public List<Liabilities> updateMember(@RequestBody Liabilities editedRecord) {
        logger.debug("Call user/api/updateliability/ " + editedRecord.getKey().getMemberid());
        editedRecord.setPvOutstandingEmis(editedRecord.getPvOutstandingEmis().setScale(0, BigDecimal.ROUND_HALF_UP));
        LiabilitiesService.updateLiabilitiesRecord(editedRecord);
        return getliabilitiesRecords();
    }

    @RequestMapping(value = "/addliability", method = RequestMethod.POST)
    public List<Liabilities> addMember(@RequestBody Liabilities newRecord) {
        logger.debug("Call user/api/addliability/ " + newRecord.getKey().getMemberid());
        newRecord.setPvOutstandingEmis(newRecord.getPvOutstandingEmis().setScale(0, BigDecimal.ROUND_HALF_UP));
        LiabilitiesService.addLiabilitiesRecord(newRecord);
        return getliabilitiesRecords();
    }
}
