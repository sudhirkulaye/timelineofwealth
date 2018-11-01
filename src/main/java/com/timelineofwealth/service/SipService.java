package com.timelineofwealth.service;

import com.timelineofwealth.apis.PublicApi;
import com.timelineofwealth.dto.SipForm;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.MutualFundUniverse;
import com.timelineofwealth.entities.Sip;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.MutualFundUniverseRepository;
import com.timelineofwealth.repositories.SipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("SipService")
public class SipService {
    private static final Logger logger = LoggerFactory.getLogger(SipService.class);

    @Autowired
    private static SipRepository sipRepository;
    @Autowired
    public void setSipRepository(SipRepository sipRepository){
        SipService.sipRepository = sipRepository;
    }

    @Autowired
    private static MutualFundUniverseRepository mutualFundUniverseRepository;
    @Autowired
    public void setMutualFundUniverseRepository(MutualFundUniverseRepository mutualFundUniverseRepository){
        SipService.mutualFundUniverseRepository = mutualFundUniverseRepository;
    }

    public static List<SipForm> getSipRecords(String email){
        logger.debug(String.format("In SipService.getSipRecords: Email %s", email));

        List<Sip> sipRecords;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        sipRecords = sipRepository.findByKeyMemberidInOrderByKeySipid(membersIds);
        List<SipForm> sipFormRecords = new ArrayList<>();
        MutualFundUniverse scheme;
        for (Sip sipRecord : sipRecords) {
            if (sipRecord.getInstrumentType().equals("Mutual Fund")) {
                scheme = SipService.mutualFundUniverseRepository.findBySchemeCode(sipRecord.getSchemeCode());
            } else {
                scheme = null;
            }
            SipForm sipFormRecord = new SipForm(sipRecord,scheme);
            sipFormRecords.add(sipFormRecord);

        }

        return sipFormRecords;
    }

    public static void updateSipRecord(Sip editedRecord) {
        logger.debug(String.format("In SipService.updateSipRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In SipService.updateSipRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            SipService.sipRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void addSipRecord(Sip newRecord) {
        logger.debug(String.format("In SipService.addSipRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In SipService.addSipRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int count = SipService.sipRepository.countByKeyMemberid(newRecord.getKey().getMemberid()) + 1;
            logger.debug(String.format("In SipService.addSipRecord: new sipid %d", count));
            newRecord.getKey().setSipid(count);
            SipService.sipRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void deleteSipRecord(Sip deletedRecord){
        logger.debug(String.format("In SipService.deleteSipRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In SipService.deleteSipRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            SipService.sipRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }
}
