package com.timelineofwealth.service;

import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.entities.UserMembers;
import com.timelineofwealth.repositories.MemberRepository;
import com.timelineofwealth.repositories.UserMembersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("memberService")
@Transactional
public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    @Autowired
    private static MemberRepository memberRepository;
    @Autowired
    public void setMemberRepository(MemberRepository memberRepository){
        MemberService.memberRepository = memberRepository;
    }

    @Autowired
    private static UserMembersRepository userMembersRepository;
    @Autowired
    public  void setUserMembersRepository(UserMembersRepository userMembersRepository){
        MemberService.userMembersRepository = userMembersRepository;
    }

    public static List<Member> getUserMembers(String email){
        logger.debug(String.format("In MemberService.getUserMembers: Email", email));

        List<Member> members = new ArrayList<>();
        List<UserMembers> userMembers = userMembersRepository.findAllByEmail(email);
        for (UserMembers userMember : userMembers ){
            members.add(memberRepository.findByMemberid(userMember.getMemberId()));
        }
        return members;
    }

    @Transactional
    public static void addMember(User user, Member newMember){
        logger.debug(String.format("In MemberService.addMember: Before Add %s", newMember.getMemberid()));
        MemberService.memberRepository.save(newMember);
        logger.debug(String.format("In MemberService.addMember: After Add %s", newMember.getMemberid()));
        UserMembers userMembers = new UserMembers();
        userMembers.setEmail(user.getEmail());
        Member newMemberWithmemberid = MemberService.memberRepository.findTopByFirstNameAndLastNameOrderByMemberidDesc(newMember.getFirstName(),newMember.getLastName());
        logger.debug(String.format("In MemberService.addMember: New Member id  %s", newMemberWithmemberid.getMemberid()));
        userMembers.setMemberId(newMemberWithmemberid.getMemberid());
        userMembers.setRelationship(newMember.getRelationship());
        MemberService.userMembersRepository.save(userMembers);
    }

    public static void updateMember(Member editedMember){
        logger.debug(String.format("In MemberService.updateMember: EditedMember ", editedMember.getMemberid()));
        MemberService.memberRepository.save(editedMember);
    }

    public static boolean isAuthorised(String signInUserEmail, Long memeberId){
        UserMembers userMembers = MemberService.userMembersRepository.findByMemberId(memeberId);
        logger.debug(String.format("In MemberService.isAuthorised: userMembers.getEmail() %s", userMembers.getEmail()));
        logger.debug(String.format("In MemberService.isAuthorised: signInUserEmail %s", signInUserEmail));
        if (userMembers.getEmail().equals(signInUserEmail)){
            return  true;
        } else  {
            return  false;
        }
    }
}
