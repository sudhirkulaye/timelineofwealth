package com.timelineofwealth.service;

import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.entities.AdviserUserMapping;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.UserMembers;
import com.timelineofwealth.repositories.AdviserUserMappingRepository;
import com.timelineofwealth.repositories.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("AdviserService")
public class AdviserService {
    private static final Logger logger = LoggerFactory.getLogger(AdviserService.class);

    @Autowired
    private static AdviserUserMappingRepository adviserUserMappingRepository;
    @Autowired
    public void setAdviserUserMappingRepository(AdviserUserMappingRepository adviserUserMappingRepository){
        AdviserService.adviserUserMappingRepository = adviserUserMappingRepository;
    }

    public static List<ClientDTO> getClients(String email){
        logger.debug(String.format("In MemberService.getUserMembers: Email %s", email));

        List<ClientDTO> clientDTOS = new ArrayList<>();
        List<AdviserUserMapping> clients = adviserUserMappingRepository.findByKeyAdviseridOrderByKeyUseridAsc(email);
        for (AdviserUserMapping client : clients ){
            //members.add(memberRepository.findByMemberid(userMember.getMemberid()));
            List<Member> members = MemberService.getUserMembers(client.getKey().getUserid());
            for (Member member : members){
                ClientDTO clientDTO = new ClientDTO();
                clientDTO.setMemberid(member.getMemberid());
                clientDTO.setRelationship(member.getRelationship());
                clientDTO.setUserid(client.getKey().getUserid());
                clientDTO.setMemberName(member.getFirstName());
                clientDTOS.add(clientDTO);
            }
        }
        return clientDTOS;
    }
}
