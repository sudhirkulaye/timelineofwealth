package com.timelineofwealth.service;

import com.timelineofwealth.entities.IncomeExpenseSavings;
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

    private static final Logger logger = LoggerFactory.getLogger(IncomeExpenseSavingsService.class);

    @Autowired
    private static IncomeExpenseSavingsRepository incomeExpenseSavingsRepository;
    @Autowired
    public void setIncomeExpenseSavingsRepository(IncomeExpenseSavingsRepository incomeExpenseSavingsRepository){
        IncomeExpenseSavingsService.incomeExpenseSavingsRepository = incomeExpenseSavingsRepository;
    }

    @Autowired
    private static UserMembersRepository userMembersRepository;
    @Autowired
    public  void setUserMembersRepository(UserMembersRepository userMembersRepository){
        IncomeExpenseSavingsService.userMembersRepository = userMembersRepository;
    }

    public static List<IncomeExpenseSavings> getIncomeExpenseSavingsRecords(String email){
        logger.debug(String.format("In IncomeExpenseSavingsService.getIncomeExpenseSavingsRecords: Email %s", email));

        List<IncomeExpenseSavings> incomeExpenseSavingsRecords;
        List<UserMembers> userMembers = userMembersRepository.findAllByEmail(email);
        List<Long> members = new ArrayList<>();
        for (UserMembers userMember : userMembers ){
            members.add(new Long(userMember.getMemberId()));
        }
        incomeExpenseSavingsRecords = incomeExpenseSavingsRepository.findByKeyMemberidInOrderByKeyFinyearDescKeyMemberidAsc(members);

        return incomeExpenseSavingsRecords;
    }

    public static void updateIncomeExpenseSavingsRecord(IncomeExpenseSavings editedRecord) {
        logger.debug(String.format("In IncomeExpenseSavingsService.updateIncomeExpenseSavingsRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In IncomeExpenseSavingsService.updateIncomeExpenseSavingsRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            IncomeExpenseSavingsService.incomeExpenseSavingsRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public static void addIncomeExpenseSavingsRecord(IncomeExpenseSavings newRecord) {
        logger.debug(String.format("In IncomeExpenseSavingsService.addIncomeExpenseSavingsRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In IncomeExpenseSavingsService.addIncomeExpenseSavingsRecord: Email %s", user.getEmail()));
        if(MemberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            IncomeExpenseSavingsService.incomeExpenseSavingsRepository.save(newRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }
}
