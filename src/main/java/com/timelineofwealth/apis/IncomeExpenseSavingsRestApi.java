package com.timelineofwealth.apis;
import com.timelineofwealth.entities.IncomeExpenseSavings;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.IncomeExpenseSavingsService;
import com.timelineofwealth.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "user/api/")
public class IncomeExpenseSavingsRestApi {
    private static final Logger logger = LoggerFactory.getLogger(IncomeExpenseSavingsRestApi.class);

    @RequestMapping(value = "/getincexpsavingsrecords", method = RequestMethod.GET)
    public List<IncomeExpenseSavings> getIncExpSavingsRecords() {
        logger.debug(String.format("Call user/api/getIncExpSavingsRecords/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return IncomeExpenseSavingsService.getIncomeExpenseSavingsRecords(user.getEmail());
    }

    @RequestMapping(value = "/updateincexpsavingsrecords", method = RequestMethod.PUT)
    public List<IncomeExpenseSavings> updateMember(@RequestBody IncomeExpenseSavings editedRecord) {
        logger.debug("Call user/api/updateincexpsavingsrecords/ " + editedRecord.getKey().getMemberid());
        IncomeExpenseSavingsService.updateIncomeExpenseSavingsRecord(editedRecord);
        return getIncExpSavingsRecords();
    }

    @RequestMapping(value = "/addincexpsavingsrecords", method = RequestMethod.POST)
    public List<IncomeExpenseSavings> addMember(@RequestBody IncomeExpenseSavings newRecord) {
        logger.debug("Call user/api/addincexpsavingsrecords/ " + newRecord.getKey().getMemberid());
        IncomeExpenseSavingsService.addIncomeExpenseSavingsRecord(newRecord);
        return getIncExpSavingsRecords();
    }
}
