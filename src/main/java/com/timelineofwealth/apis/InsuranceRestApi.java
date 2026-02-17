package com.timelineofwealth.apis;

import com.timelineofwealth.entities.Insurance;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.InsuranceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class InsuranceRestApi {
    private final Logger logger = LoggerFactory.getLogger(InsuranceRestApi.class);
    private final InsuranceService insuranceService;
    private final CommonService commonService;

    @Autowired
    public InsuranceRestApi(InsuranceService insuranceService,
                            CommonService commonService){
        this.insuranceService = insuranceService;
        this.commonService = commonService;
    }

    @RequestMapping(value = "/getinsurances", method = RequestMethod.GET)
    public List<Insurance> getInsurances() {
        logger.debug(String.format("Call user/api/getinsurances/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        return insuranceService.getInsuranceRecords(user.getEmail());
    }

    @RequestMapping(value = "/updateinsurance", method = RequestMethod.PUT)
    public List<Insurance> updateInsurance(@RequestBody Insurance editedRecord) {
        logger.debug("Call user/api/updateinsurance/ " + editedRecord.getKey().getMemberid());
        insuranceService.updateInsuranceRecord(editedRecord);
        return getInsurances();
    }

    @RequestMapping(value = "/addinsurance", method = RequestMethod.POST)
    public List<Insurance> addInsurance(@RequestBody Insurance newRecord) {
        logger.debug("Call user/api/addinsurance/ " + newRecord.getKey().getMemberid());
        insuranceService.addInsuranceRecord(newRecord);
        return getInsurances();
    }

    @RequestMapping(value = "/deleteinsurance", method = RequestMethod.POST)
    public List<Insurance> deleteInsurance(@RequestBody Insurance deleteRecord) {
        logger.debug("Call user/api/deleteinsurance/ " + deleteRecord.getKey().getMemberid());
        insuranceService.deleteInsuranceRecord(deleteRecord);
        return getInsurances();
    }
}
