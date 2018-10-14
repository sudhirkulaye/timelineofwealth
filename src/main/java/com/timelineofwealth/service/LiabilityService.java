package com.timelineofwealth.service;

import com.timelineofwealth.entities.Liability;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.LiabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("LiabilityService")
public class LiabilityService {
    private static final Logger logger = LoggerFactory.getLogger(LiabilityService.class);


    @Autowired
    private static LiabilityRepository liabilityRepository;
    @Autowired
    public void setLiabilityRepository(LiabilityRepository liabilityRepository){
        LiabilityService.liabilityRepository = liabilityRepository;
    }

    public static List<Liability> getLiabilityRecords(String email){
        logger.debug(String.format("In LiabilityService.getLiabilityRecords: Email %s", email));

        List<Liability> liabilityRecords;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        liabilityRecords = liabilityRepository.findByKeyMemberidInOrderByKeyMemberidAscKeyLoanidAsc(membersIds);

        return liabilityRecords;
    }

    public static void updateLiabilityRecord(Liability editedRecord) {
        logger.debug(String.format("In LiabilityService.updateLiabilityRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiabilityService.updateLiabilityRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            LiabilityService.liabilityRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void addLiabilityRecord(Liability newRecord) {
        logger.debug(String.format("In LiabilityService.addLiabilityRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiabilityService.addLiabilityRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int count = LiabilityService.liabilityRepository.countByKeyMemberid(newRecord.getKey().getMemberid()) + 1;
            logger.debug(String.format("In LiabilityService.addLiabilityRecord: new loanid %d", count));
            newRecord.getKey().setLoanid(count);
            LiabilityService.liabilityRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void deleteLiabilityRecord(Liability deletedRecord){
        logger.debug(String.format("In LiabilityService.deleteLiabilityRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiabilityService.deleteLiabilityRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            LiabilityService.liabilityRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

}
