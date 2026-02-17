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
    private final Logger logger = LoggerFactory.getLogger(InsuranceService.class);
    private final InsuranceRepository insuranceRepository;
    private final MemberService memberService;
    private final CommonService commonService;

    @Autowired
    public InsuranceService(InsuranceRepository insuranceRepository,
                            MemberService memberService,
                            CommonService commonService){
        this.insuranceRepository = insuranceRepository;
        this.memberService = memberService;
        this.commonService = commonService;
    }

    public List<Insurance> getInsuranceRecords(String email){
        logger.debug(String.format("In InsuranceService.getInsuranceRecords: Email %s", email));

        List<Insurance> insuranceRecords;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        insuranceRecords = insuranceRepository.findByKeyMemberidInOrderByExpiryDateAsc(membersIds);

        return insuranceRecords;
    }

    public void updateInsuranceRecord(Insurance editedRecord) {
        logger.debug(String.format("In InsuranceService.updateInsuranceRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In InsuranceService.updateInsuranceRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            this.insuranceRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public void addInsuranceRecord(Insurance newRecord) {
        logger.debug(String.format("In InsuranceService.addInsuranceRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In InsuranceService.addInsuranceRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int newInsuranceid = 0;
            int count = this.insuranceRepository.countByKeyMemberid(newRecord.getKey().getMemberid());
            if (count == 0) { newInsuranceid = 1; } else {
                newInsuranceid = this.insuranceRepository.findTopByKeyMemberidOrderByKeyInsuranceidDesc(newRecord.getKey().getMemberid()).getKey().getInsuranceid() + 1;
            }
            logger.debug(String.format("In InsuranceService.addInsuranceRecord: new insuranceid %d", newInsuranceid));
            newRecord.getKey().setInsuranceid(newInsuranceid);
            this.insuranceRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public void deleteInsuranceRecord(Insurance deletedRecord){
        logger.debug(String.format("In InsuranceService.deleteInsuranceRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In InsuranceService.deleteInsuranceRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            this.insuranceRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }


}
