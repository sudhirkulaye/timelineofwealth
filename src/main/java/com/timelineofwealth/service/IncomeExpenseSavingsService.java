package com.timelineofwealth.service;

import com.timelineofwealth.entities.IncomeExpenseSavings;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.entities.UserMembers;
import com.timelineofwealth.repositories.IncomeExpenseSavingsRepository;
import com.timelineofwealth.repositories.UserMembersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("incomeExpenseSavingsService")
public class IncomeExpenseSavingsService {

    private final Logger logger = LoggerFactory.getLogger(IncomeExpenseSavingsService.class);

    private final IncomeExpenseSavingsRepository incomeExpenseSavingsRepository;
    private final MemberService memberService;
    private final CommonService commonService;

    @Autowired
    public IncomeExpenseSavingsService(IncomeExpenseSavingsRepository incomeExpenseSavingsRepository,
                                       MemberService memberService,
                                       CommonService commonService){
        this.incomeExpenseSavingsRepository = incomeExpenseSavingsRepository;
        this.memberService = memberService;
        this.commonService = commonService;
    }

    public List<IncomeExpenseSavings> getIncomeExpenseSavingsRecords(String email){
        logger.debug(String.format("In IncomeExpenseSavingsService.getIncomeExpenseSavingsRecords: Email %s", email));

        List<IncomeExpenseSavings> incomeExpenseSavingsRecords;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        incomeExpenseSavingsRecords = incomeExpenseSavingsRepository.findByKeyMemberidInOrderByKeyFinyearDescKeyMemberidAsc(membersIds);

        return incomeExpenseSavingsRecords;
    }

    public void updateIncomeExpenseSavingsRecord(IncomeExpenseSavings editedRecord) {
        logger.debug(String.format("In IncomeExpenseSavingsService.updateIncomeExpenseSavingsRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In IncomeExpenseSavingsService.updateIncomeExpenseSavingsRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            this.incomeExpenseSavingsRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public void addIncomeExpenseSavingsRecord(IncomeExpenseSavings newRecord) {
        logger.debug(String.format("In IncomeExpenseSavingsService.addIncomeExpenseSavingsRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In IncomeExpenseSavingsService.addIncomeExpenseSavingsRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            this.incomeExpenseSavingsRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }
}
