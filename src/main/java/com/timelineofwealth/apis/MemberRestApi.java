package com.timelineofwealth.apis;

import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/user/api")  // ✅ Added leading slash
public class MemberRestApi {

    private final Logger logger = LoggerFactory.getLogger(MemberRestApi.class);
    private final MemberService memberService;
    private final CommonService commonService;

    // ✅ Constructor injection
    @Autowired
    public MemberRestApi(MemberService memberService,
                         CommonService commonService) {
        this.memberService = memberService;
        this.commonService = commonService;
    }

    @GetMapping("/members")  // ✅ Modern annotation, better URL
    public List<Member> getUserMembers() {
        logger.debug("Call user/api/members");

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        return memberService.getUserMembers(user.getEmail());
    }

    @PutMapping("/members")  // ✅ Modern annotation
    public List<Member> updateMember(@RequestBody Member editedMember) {
        logger.debug("Call user/api/members PUT: {}", editedMember.getFirstName());

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);

        if(memberService.isAuthorised(user.getEmail(), editedMember.getMemberid())){
            memberService.updateMember(editedMember);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
        return memberService.getUserMembers(user.getEmail());
    }

    @PostMapping("/members")  // ✅ Modern annotation
    public List<Member> addMember(@RequestBody Member newMember) {
        logger.debug("Call user/api/members POST: {}", newMember.getFirstName());

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        memberService.addMember(user, newMember);
        return memberService.getUserMembers(user.getEmail());
    }
}