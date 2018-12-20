package com.timelineofwealth.apis;

import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.entities.AdviserUserMapping;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.AdviserService;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "adviser/api/")
public class AdviserRestApi {
    private static final Logger logger = LoggerFactory.getLogger(AdviserRestApi.class);

    @RequestMapping(value = "/getclients", method = RequestMethod.GET)
    public List<ClientDTO> getClients() {
        logger.debug(String.format("Call adviser/api/getusermembers/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return AdviserService.getClients(user.getEmail());
    }
}
