package com.timelineofwealth.apis;

import com.timelineofwealth.dto.SipForm;
import com.timelineofwealth.entities.Sip;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.SipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RestController
@RequestMapping(value = "user/api/")
public class SipRestApi {
    private static final Logger logger = LoggerFactory.getLogger(SipRestApi.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SipRestApi(Environment environment){}

    @RequestMapping(value = "/getsips", method = RequestMethod.GET)
    public List<SipForm> getSips() {
        logger.debug(String.format("Call user/api/getsips/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return SipService.getSipRecords(user.getEmail());
    }

    @RequestMapping(value = "/getsipbyschemecode/{memberid}/{schemecode}", method = RequestMethod.GET)
    public List<Sip> getSipsBySchemeCode(@PathVariable int memberid, @PathVariable int schemecode) {
        logger.debug(String.format("Call user/api/getsipbyschemecode/%d/%d",memberid, schemecode));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return SipService.getSipRecordsBySchemeCode(user.getEmail(), memberid, schemecode);
    }

    @RequestMapping(value = "/updatesip", method = RequestMethod.PUT)
    public List<SipForm> updateSip(@RequestBody SipForm editedRecord) {
        logger.debug("Call user/api/updatesip/ " + editedRecord.getKey().getMemberid());
        Sip editedSipRecord = new Sip();
        setSipFromSipForm(editedSipRecord,editedRecord);
        SipService.updateSipRecord(editedSipRecord);
        return getSips();
    }

    @RequestMapping(value = "/addsip", method = RequestMethod.POST)
    public List<SipForm> addSip(Model model, @RequestBody SipForm newRecord) {
        logger.debug("Call user/api/addsip/ " + newRecord.getKey().getMemberid());
        Sip editedSipRecord = new Sip();
        setSipFromSipForm(editedSipRecord,newRecord);
        SipService.addSipRecord(model, editedSipRecord, entityManager);
        return getSips();
    }

    @RequestMapping(value = "/deletesip", method = RequestMethod.POST)
    public List<SipForm> deleteSip(@RequestBody SipForm deleteRecord) {
        logger.debug("Call user/api/deletesip/ " + deleteRecord.getKey().getMemberid());
        Sip editedSipRecord = new Sip();
        setSipFromSipForm(editedSipRecord,deleteRecord);
        SipService.deleteSipRecord(editedSipRecord);
        return getSips();
    }

    private void setSipFromSipForm(Sip editedSipRecord, SipForm editedRecord){
        editedSipRecord.getKey().setMemberid(editedRecord.getKey().getMemberid());
        editedSipRecord.getKey().setSipid(editedRecord.getKey().getSipid());
        editedSipRecord.setInstrumentType(editedRecord.getInstrumentType());
        editedSipRecord.setSchemeCode(editedRecord.getSchemeCode());
        editedSipRecord.setSchemeName(editedRecord.getSchemeName());
        editedSipRecord.setStartDate(editedRecord.getStartDate());
        editedSipRecord.setEndDate(editedRecord.getEndDate());
        editedSipRecord.setDeductionDay(editedRecord.getDeductionDay());
        editedSipRecord.setAmount(editedRecord.getAmount());
        editedSipRecord.setSipFreq(editedRecord.getSipFreq());
        editedSipRecord.setIsActive(editedRecord.getIsActive());
    }
}
