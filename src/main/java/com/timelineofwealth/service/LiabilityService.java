package com.timelineofwealth.service;

import com.timelineofwealth.entities.Liability;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.LiabilitiesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("LiabilitiesService")
public class LiabilitiesService {
    private static final Logger logger = LoggerFactory.getLogger(LiabilitiesService.class);


    @Autowired
    private static LiabilitiesRepository LiabilitiesRepository;
    @Autowired
    public void setLiabilitiesRepository(LiabilitiesRepository LiabilitiesRepository){
        LiabilitiesService.LiabilitiesRepository = LiabilitiesRepository;
    }

    public static List<Liability> getLiabilitiesRecords(String email){
        logger.debug(String.format("In LiabilitiesService.getLiabilitiesRecords: Email %s", email));

        List<Liability> liabilityRecords;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        liabilityRecords = LiabilitiesRepository.findByKeyMemberidInOrderByKeyMemberidAscKeyLoanidAsc(membersIds);

        return liabilityRecords;
    }

    public static void updateLiabilitiesRecord(Liability editedRecord) {
        logger.debug(String.format("In LiabilitiesService.updateLiabilitiesRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiabilitiesService.updateLiabilitiesRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            LiabilitiesService.LiabilitiesRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void addLiabilitiesRecord(Liability newRecord) {
        logger.debug(String.format("In LiabilitiesService.addLiabilitiesRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiabilitiesService.addLiabilitiesRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int count = LiabilitiesService.LiabilitiesRepository.countByKeyMemberid(newRecord.getKey().getMemberid()) + 1;
            logger.debug(String.format("In LiabilitiesService.addLiabilitiesRecord: new loanid %d", count));
            newRecord.getKey().setLoanid(count);
            LiabilitiesService.LiabilitiesRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void deleteLiabilitiesRecord(Liability deletedRecord){
        logger.debug(String.format("In LiabilitiesService.deleteLiabilitiesRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In LiabilitiesService.deleteLiabilitiesRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            LiabilitiesService.LiabilitiesRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

}
