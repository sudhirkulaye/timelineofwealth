package com.timelineofwealth.service;

import com.timelineofwealth.entities.Insurance;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.InsuranceRepository;
import com.timelineofwealth.repositories.InsuranceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("InsuranceService")
public class InsuranceService {
    private static final Logger logger = LoggerFactory.getLogger(InsuranceService.class);


    @Autowired
    private static InsuranceRepository insuranceRepository;
    @Autowired
    public void setInsuranceRepository(InsuranceRepository insuranceRepository){
        InsuranceService.insuranceRepository = insuranceRepository;
    }

    public static List<Insurance> getInsuranceRecords(String email){
        logger.debug(String.format("In InsuranceService.getInsuranceRecords: Email %s", email));

        List<Insurance> insuranceRecords;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        insuranceRecords = insuranceRepository.findByKeyMemberidInOrderByExpiryDateAsc(membersIds);

        return insuranceRecords;
    }

    public static void updateInsuranceRecord(Insurance editedRecord) {
        logger.debug(String.format("In InsuranceService.updateInsuranceRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In InsuranceService.updateInsuranceRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            InsuranceService.insuranceRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void addInsuranceRecord(Insurance newRecord) {
        logger.debug(String.format("In InsuranceService.addInsuranceRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In InsuranceService.addInsuranceRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int count = InsuranceService.insuranceRepository.countByKeyMemberid(newRecord.getKey().getMemberid()) + 1;
            logger.debug(String.format("In InsuranceService.addInsuranceRecord: new insuranceid %d", count));
            newRecord.getKey().setInsuranceid(count);
            InsuranceService.insuranceRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void deleteInsuranceRecord(Insurance deletedRecord){
        logger.debug(String.format("In InsuranceService.deleteInsuranceRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In InsuranceService.deleteInsuranceRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            InsuranceService.insuranceRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }


}
