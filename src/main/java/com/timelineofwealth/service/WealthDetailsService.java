package com.timelineofwealth.service;

import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.entities.WealthDetails;
import com.timelineofwealth.repositories.WealthDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("WealthDetailsService")
public class WealthDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(WealthDetailsService.class);

    @Autowired
    private static WealthDetailsRepository wealthDetailsRepository;
    @Autowired
    public void setWealthDetailsRepository(WealthDetailsRepository wealthDetailsRepository){
        WealthDetailsService.wealthDetailsRepository = wealthDetailsRepository;
    }

    public static List<WealthDetails> getWealthDetailsRecords(String email){
        logger.debug(String.format("In WealthDetailsService.getWealthDetailsRecords: Email %s", email));

        List<WealthDetails> wealthDetailsRecords;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        wealthDetailsRecords = wealthDetailsRepository.findByKeyMemberidInOrderByKeyMemberidAscAssetClassidAscKeyTickerAscKeyBuyDateAsc(membersIds);

        return wealthDetailsRecords;
    }

    public static void updateWealthDetailsRecord(WealthDetails editedRecord) {
        logger.debug(String.format("In WealthDetailsService.updateWealthDetailsRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In WealthDetailsService.updateWealthDetailsRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            WealthDetailsService.wealthDetailsRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void addWealthDetailsRecord(WealthDetails newRecord) {
        logger.debug(String.format("In WealthDetailsService.addWealthDetailsRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In WealthDetailsService.addWealthDetailsRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int count = WealthDetailsService.wealthDetailsRepository.countByKeyMemberidAndKeyBuyDateAndKeyTicker(newRecord.getKey().getMemberid(), newRecord.getKey().getBuyDate(), newRecord.getKey().getTicker());
            logger.debug(String.format("In LiabilityService.addLiabilityRecord: new loanid %d", count));
            if (count == 0) {
                WealthDetailsService.wealthDetailsRepository.save(newRecord);
            } else {
                throw new IllegalArgumentException("Record already exists.");
            }
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void deleteWealthDetailsRecord(WealthDetails deletedRecord){
        logger.debug(String.format("In WealthDetailsService.deleteWealthDetailsRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In WealthDetailsService.deleteWealthDetailsRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            WealthDetailsService.wealthDetailsRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

}
