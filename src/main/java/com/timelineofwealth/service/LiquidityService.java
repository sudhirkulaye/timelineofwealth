package com.timelineofwealth.service;

import com.timelineofwealth.entities.Liquidity;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.LiquidityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("LiquidityService")
public class LiquidityService {
    private static final Logger logger = LoggerFactory.getLogger(LiquidityService.class);


    @Autowired
    private static LiquidityRepository liquidityRepository;
    @Autowired
    public void setLiquidityRepository(LiquidityRepository liquidityRepository){
        LiquidityService.liquidityRepository = liquidityRepository;
    }

    public static List<Liquidity> getLiquidityRecords(String email){
        logger.debug(String.format("In LiquidityService.getLiquidityRecords: Email %s", email));

        List<Liquidity> liquidityRecords;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        liquidityRecords = liquidityRepository.findByKeyMemberidInOrderByExpectedStartDateAsc(membersIds);

        return liquidityRecords;
    }

    public static void updateLiquidityRecord(Liquidity editedRecord) {
        logger.debug(String.format("In LiquidityService.updateLiquidityRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiquidityService.updateLiquidityRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            LiquidityService.liquidityRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void addLiquidityRecord(Liquidity newRecord) {
        logger.debug(String.format("In LiquidityService.addLiquidityRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiquidityService.addLiquidityRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int count = LiquidityService.liquidityRepository.countByKeyMemberid(newRecord.getKey().getMemberid()) + 1;
            logger.debug(String.format("In LiquidityService.addLiquidityRecord: new liquidityid %d", count));
            newRecord.getKey().setLiquidityid(count);
            LiquidityService.liquidityRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void deleteLiquidityRecord(Liquidity deletedRecord){
        logger.debug(String.format("In LiquidityService.deleteLiquidityRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiquidityService.deleteLiquidityRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            LiquidityService.liquidityRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

}
