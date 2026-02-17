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
    private final Logger logger = LoggerFactory.getLogger(LiquidityService.class);
    private final MemberService memberService;
    private final LiquidityRepository liquidityRepository;
    private final CommonService commonService;

    @Autowired
    public LiquidityService(LiquidityRepository liquidityRepository,
                            MemberService memberService,
                            CommonService commonService){
        this.liquidityRepository = liquidityRepository;
        this.memberService = memberService;
        this.commonService = commonService;
    }

    public List<Liquidity> getLiquidityRecords(String email){
        logger.debug(String.format("In LiquidityService.getLiquidityRecords: Email %s", email));

        List<Liquidity> liquidityRecords;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        liquidityRecords = liquidityRepository.findByKeyMemberidInOrderByExpectedStartDateAsc(membersIds);

        return liquidityRecords;
    }

    public void updateLiquidityRecord(Liquidity editedRecord) {
        logger.debug(String.format("In LiquidityService.updateLiquidityRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiquidityService.updateLiquidityRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            this.liquidityRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public void addLiquidityRecord(Liquidity newRecord) {
        logger.debug(String.format("In LiquidityService.addLiquidityRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiquidityService.addLiquidityRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int newLiquidityid = 0;
            int count = this.liquidityRepository.countByKeyMemberid(newRecord.getKey().getMemberid());
            if (count == 0) { newLiquidityid = 1; } else {
                newLiquidityid = this.liquidityRepository.findTopByKeyMemberidOrderByKeyLiquidityidDesc(newRecord.getKey().getMemberid()).getKey().getLiquidityid() + 1;
            }
            logger.debug(String.format("In LiquidityService.addLiquidityRecord: new liquidityid %d", newLiquidityid));
            newRecord.getKey().setLiquidityid(newLiquidityid);
            this.liquidityRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public void deleteLiquidityRecord(Liquidity deletedRecord){
        logger.debug(String.format("In LiquidityService.deleteLiquidityRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiquidityService.deleteLiquidityRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            this.liquidityRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

}
