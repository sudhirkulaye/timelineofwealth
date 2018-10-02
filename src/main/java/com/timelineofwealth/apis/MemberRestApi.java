package com.timelineofwealth.apis;

import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "user/api/")
public class MemberRestApi {

    private static final Logger logger = LoggerFactory.getLogger(MemberRestApi.class);

    @RequestMapping(value = "/getusermembers", method = RequestMethod.GET)
    public List<Member> getUserMembers() {
        logger.debug(String.format("Call user/api/getusermembers/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return MemberService.getUserMembers(user.getEmail());
    }

    @RequestMapping(value = "/updatemember", method = RequestMethod.PUT)
    public List<Member> updateMember(@RequestBody Member editedMember) {
        logger.debug("Call user/api/updatemember/ " + editedMember.getFirstName());

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        if(MemberService.isAuthorised(user.getEmail(), editedMember.getMemberid())){
            MemberService.updateMember(editedMember);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
        return MemberService.getUserMembers(user.getEmail());
    }

    @RequestMapping(value = "/addmember", method = RequestMethod.POST)
    public List<Member> addMember(@RequestBody Member newMember) {
        logger.debug("Call user/api/addmember/ " + newMember.getFirstName());

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        MemberService.addMember(user, newMember);
        return MemberService.getUserMembers(user.getEmail());
    }

}
