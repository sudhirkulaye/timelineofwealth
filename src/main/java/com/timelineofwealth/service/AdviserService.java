package com.timelineofwealth.service;

import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.dto.ConsolidatedAssetsDTO;
import com.timelineofwealth.entities.AdviserUserMapping;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.UserMembers;
import com.timelineofwealth.repositories.AdviserUserMappingRepository;
import com.timelineofwealth.repositories.MemberRepository;
import com.timelineofwealth.repositories.WealthDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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

    @Autowired
    private static WealthDetailsRepository wealthDetailsRepository;
    @Autowired
    public void setWealthDetailsRepository(WealthDetailsRepository wealthDetailsRepository){
        AdviserService.wealthDetailsRepository = wealthDetailsRepository;
    }

    public static List<ClientDTO> getClients(String email){
        logger.debug(String.format("In AdviserService.getClients: Email %s", email));

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

    public static List<ConsolidatedAssetsDTO> getConsolidatedAssets(String adviserEmail, String clientEmail){
        logger.debug(String.format("In AdviserService.getConsolidatedAssets: Adviser Email %s AND Client Email %s", adviserEmail, clientEmail));

        List<ConsolidatedAssetsDTO> assets = new ArrayList<>();
        List<AdviserUserMapping> clients = new ArrayList<>();
        if(clientEmail != null){
            clientEmail = clientEmail.replace("\"", ""); //removing end quotes
            clientEmail = clientEmail.trim();
        }
        if (!clientEmail.equals("")){
            int count = adviserUserMappingRepository.countByKeyAdviseridAndKeyUserid(adviserEmail, clientEmail);
            if (count == 0 ){
                throw new InsufficientAuthenticationException("Adviser is not authorized or client doesn't exists");
            } else {
                clients.add(adviserUserMappingRepository.findOneByKeyAdviseridAndKeyUserid(adviserEmail, clientEmail));
            }
        } else {
            clients = adviserUserMappingRepository.findByKeyAdviseridOrderByKeyUseridAsc(adviserEmail);
        }

        for (AdviserUserMapping client : clients ){
            assets.addAll(WealthDetailsService.getConsolidatedWealthDetailsRecords(client.getKey().getUserid()));
        }
        return assets;
    }

}
