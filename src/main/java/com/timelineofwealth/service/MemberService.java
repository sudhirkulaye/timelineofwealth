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

@Service
@Transactional
public class MemberService {
    private final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;  // ✅ Remove setter injection
    private final UserMembersRepository userMembersRepository;  // ✅ Use final

    // ✅ Constructor injection (best practice)
    @Autowired
    public MemberService(MemberRepository memberRepository,
                         UserMembersRepository userMembersRepository) {
        this.memberRepository = memberRepository;
        this.userMembersRepository = userMembersRepository;
    }

    public List<Member> getUserMembers(String email){
        logger.debug("In MemberService.getUserMembers: Email {}", email);  // ✅ Modern logging

        List<Member> members = new ArrayList<>();
        List<UserMembers> userMembers = userMembersRepository.findAllByEmail(email);
        for (UserMembers userMember : userMembers ){
            members.add(memberRepository.findByMemberid(userMember.getMemberid()));
        }
        return members;
    }

    public void addMember(User user, Member newMember){
        logger.debug("In MemberService.addMember: Before Add {}", newMember.getMemberid());

        Member savedMember = memberRepository.save(newMember);  // ✅ Capture result!
        logger.debug("In MemberService.addMember: After Add {}", savedMember.getMemberid());

        UserMembers userMembers = new UserMembers();
        userMembers.setEmail(user.getEmail());
        userMembers.setMemberid(savedMember.getMemberid());  // ✅ Use saved member's ID
        userMembers.setRelationship(newMember.getRelationship());
        userMembersRepository.save(userMembers);
    }

    public void updateMember(Member editedMember){
        logger.debug("In MemberService.updateMember: EditedMember {}", editedMember.getMemberid());
        memberRepository.save(editedMember);
    }

    public boolean isAuthorised(String signInUserEmail, Long memberId){  // ✅ Fixed typo
        UserMembers userMembers = userMembersRepository.findByMemberid(memberId);
        logger.debug("In MemberService.isAuthorised: userMembers.getEmail() {}", userMembers.getEmail());
        logger.debug("In MemberService.isAuthorised: signInUserEmail {}", signInUserEmail);

        return userMembers.getEmail().equals(signInUserEmail);  // ✅ Simplified
    }
}