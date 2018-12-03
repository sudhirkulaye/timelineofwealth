package com.timelineofwealth.apis;

import com.timelineofwealth.entities.Liability;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.LiabilityService;
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
public class LiabilityRestApi {
    private static final Logger logger = LoggerFactory.getLogger(LiabilityRestApi.class);

    @RequestMapping(value = "/getliabilities", method = RequestMethod.GET)
    public List<Liability> getLiabilities() {
        logger.debug(String.format("Call user/api/getliabilities/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return LiabilityService.getLiabilityRecords(user.getEmail());
    }

    @RequestMapping(value = "/updateliability", method = RequestMethod.PUT)
    public List<Liability> updateLiability(@RequestBody Liability editedRecord) {
        logger.debug("Call user/api/updateliability/ " + editedRecord.getKey().getMemberid());
        editedRecord.setPvOutstandingEmis(editedRecord.getPvOutstandingEmis().setScale(0, BigDecimal.ROUND_HALF_UP));
        LiabilityService.updateLiabilityRecord(editedRecord);
        return getLiabilities();
    }

    @RequestMapping(value = "/addliability", method = RequestMethod.POST)
    public List<Liability> addLiability(@RequestBody Liability newRecord) {
        logger.debug("Call user/api/addliability/ " + newRecord.getKey().getMemberid());
        newRecord.setPvOutstandingEmis(newRecord.getPvOutstandingEmis().setScale(0, BigDecimal.ROUND_HALF_UP));
        LiabilityService.addLiabilityRecord(newRecord);
        return getLiabilities();
    }

    @RequestMapping(value = "/deleteliability", method = RequestMethod.POST)
    public List<Liability> deleteLiability(@RequestBody Liability deleteRecord) {
        logger.debug("Call user/api/deleteliability/ " + deleteRecord.getKey().getMemberid());
        LiabilityService.deleteLiabilityRecord(deleteRecord);
        return getLiabilities();
    }
}
