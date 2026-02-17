package com.timelineofwealth.service;

import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.dto.ConsolidatedAssetsDTO;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdviserService {
    private final Logger logger = LoggerFactory.getLogger(AdviserService.class);

    // ✅ All dependencies as instance fields (final for immutability)
    private final MemberService memberService;
    private final AdviserUserMappingRepository adviserUserMappingRepository;
    private final WealthDetailsRepository wealthDetailsRepository;
    private final PortfolioRepository portfolioRepository;
    private final CompositeRepository compositeRepository;
    private final CompositeConstituentsRepository compositeConstituentsRepository;
    private final MoslcodeMemberidRepository moslcodeMemberidRepository;
    private final WealthDetailsService wealthDetailsService;  // ✅ Inject instead of static call
    private final CommonService commonService;  // ✅ Inject instead of static call

    // ✅ Constructor injection for ALL dependencies
    @Autowired
    public AdviserService(
            MemberService memberService,
            AdviserUserMappingRepository adviserUserMappingRepository,
            WealthDetailsRepository wealthDetailsRepository,
            PortfolioRepository portfolioRepository,
            CompositeRepository compositeRepository,
            CompositeConstituentsRepository compositeConstituentsRepository,
            MoslcodeMemberidRepository moslcodeMemberidRepository,
            WealthDetailsService wealthDetailsService,
            CommonService commonService) {
        this.memberService = memberService;
        this.adviserUserMappingRepository = adviserUserMappingRepository;
        this.wealthDetailsRepository = wealthDetailsRepository;
        this.portfolioRepository = portfolioRepository;
        this.compositeRepository = compositeRepository;
        this.compositeConstituentsRepository = compositeConstituentsRepository;
        this.moslcodeMemberidRepository = moslcodeMemberidRepository;
        this.wealthDetailsService = wealthDetailsService;
        this.commonService = commonService;
    }

    // ✅ Changed from static to instance method
    public List<ClientDTO> getClients(String email) {
        logger.debug("In AdviserService.getClients: Email {}", email);

        List<ClientDTO> clientDTOS = new ArrayList<>();
        List<AdviserUserMapping> clients = adviserUserMappingRepository.findByKeyAdviseridOrderByKeyUseridAsc(email);

        for (AdviserUserMapping client : clients) {
            List<Member> members = memberService.getUserMembers(client.getKey().getUserid());

            for (Member member : members) {
                ClientDTO clientDTO = new ClientDTO();
                clientDTO.setMemberid(member.getMemberid());
                clientDTO.setRelationship(member.getRelationship());
                clientDTO.setUserid(client.getKey().getUserid());
                clientDTO.setMemberName(member.getFirstName());
                clientDTO.setFirstName(member.getFirstName());
                clientDTO.setLastName(member.getLastName());
                clientDTOS.add(clientDTO);
            }
        }
        return clientDTOS;
    }

    // ✅ Changed from static to instance method
    public List<ClientDTO> getPMSClients(String email) {
        logger.debug("In AdviserService.getPMSClients: Email {}", email);

        List<ClientDTO> clientDTOS = new ArrayList<>();
        List<AdviserUserMapping> clients = adviserUserMappingRepository.findByKeyAdviseridOrderByKeyUseridAsc(email);

        for (AdviserUserMapping client : clients) {
            List<Member> members = memberService.getUserMembers(client.getKey().getUserid());

            // ✅ Better way to collect member IDs using streams
            List<Long> membersIds = members.stream()
                    .map(Member::getMemberid)
                    .collect(Collectors.toList());

            List<Portfolio> portfolios = portfolioRepository.findAllByKeyMemberidInAndStatusOrderByKeyPortfolioid(membersIds, "ACTIVE");

            for (Portfolio portfolio : portfolios) {
                // ✅ Use findFirst() instead of get(0) to avoid IndexOutOfBoundsException
                Member member = members.stream()
                        .filter(m -> m.getMemberid() == portfolio.getKey().getMemberid())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Member not found for portfolio"));

                // ✅ Check if member already exists in DTO list
                boolean memberExists = clientDTOS.stream()
                        .anyMatch(d -> d.getMemberid() == portfolio.getKey().getMemberid());

                if (!memberExists) {
                    ClientDTO clientDTO = new ClientDTO();
                    clientDTO.setMemberid(portfolio.getKey().getMemberid());
                    clientDTO.setRelationship(member.getRelationship());
                    clientDTO.setUserid(member.getEmail());
                    clientDTO.setMemberName(member.getFirstName() + "_" + member.getLastName());
                    clientDTO.setFirstName(member.getFirstName());
                    clientDTO.setLastName(member.getLastName());

                    try {
                        MoslcodeMemberid moslcodeMemberid = moslcodeMemberidRepository.findByMemberid(member.getMemberid());
                        clientDTO.setMoslCode(moslcodeMemberid.getMoslcode());
                    } catch (Exception e) {
                        clientDTO.setMoslCode("HXXX");
                    }
                    clientDTOS.add(clientDTO);
                }
            }
        }
        clientDTOS.sort(Comparator.comparing(ClientDTO::getMemberid));
        return clientDTOS;
    }

    // ✅ Changed from static to instance method
    public List<ConsolidatedAssetsDTO> getConsolidatedAssets(String adviserEmail, String clientEmail) {
        logger.debug("In AdviserService.getConsolidatedAssets: Adviser Email {} AND Client Email {}", adviserEmail, clientEmail);

        List<ConsolidatedAssetsDTO> assets = new ArrayList<>();
        List<AdviserUserMapping> clients = new ArrayList<>();

        if (clientEmail != null) {
            clientEmail = clientEmail.replace("\"", "").trim();
        }

        if (!clientEmail.equals("")) {
            int count = adviserUserMappingRepository.countByKeyAdviseridAndKeyUserid(adviserEmail, clientEmail);
            if (count == 0) {
                throw new InsufficientAuthenticationException("Adviser is not authorized or client doesn't exist");
            } else {
                clients.add(adviserUserMappingRepository.findOneByKeyAdviseridAndKeyUserid(adviserEmail, clientEmail));
            }
        } else {
            clients = adviserUserMappingRepository.findByKeyAdviseridOrderByKeyUseridAsc(adviserEmail);
        }

        for (AdviserUserMapping client : clients) {
            // ✅ Call instance method instead of static
            assets.addAll(wealthDetailsService.getConsolidatedWealthDetailsRecords(client.getKey().getUserid()));
        }
        return assets;
    }

    // ✅ Changed from static to instance method
    public List<Composite> getComposites() {
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // ✅ Call instance method instead of static
        User user = commonService.getLoggedInUser(userDetails);

        logger.debug("In AdviserService.getComposites: Adviser Email {}", user.getEmail());

        return compositeRepository.findByFundManagerEmail(user.getEmail());
    }

    // ✅ Changed from static to instance method
    public List<CompositeConstituents> getCompositeDetails() {
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // ✅ Call instance method instead of static
        User user = commonService.getLoggedInUser(userDetails);

        logger.debug("In AdviserService.getCompositeDetails: Adviser Email {}", user.getEmail());

        List<Composite> composites = compositeRepository.findByFundManagerEmail(user.getEmail());

        // ✅ Use streams to collect composite IDs
        List<Long> compositeids = composites.stream()
                .map(Composite::getCompositeid)
                .collect(Collectors.toList());

        return compositeConstituentsRepository.findAllByKeyCompositeidInOrderByTargetWeightDesc(compositeids);
    }

    // ✅ Changed from static to instance method
    public void updateCompositeDetails(CompositeConstituents editedRecord) {
        logger.debug("In AdviserService.updateCompositeDetails: editedRecord.key.compositeid {} - {}",
                editedRecord.getKey().getCompositeid(), editedRecord.getKey().getTicker());

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);

        logger.debug("In AdviserService.updateCompositeDetails: Email {}", user.getEmail());

        int count = compositeRepository.countByFundManagerEmailAndCompositeid(user.getEmail(), editedRecord.getKey().getCompositeid());
        if (count == 1) {
            compositeConstituentsRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    // ✅ Changed from static to instance method
    public void addCompositeDetails(CompositeConstituents newRecord) {
        logger.debug("In AdviserService.addCompositeDetails: newRecord.key.memberid {} - {}",
                newRecord.getKey().getCompositeid(), newRecord.getKey().getTicker());

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);

        logger.debug("In AdviserService.addCompositeDetails: Email {}", user.getEmail());

        int count = compositeRepository.countByFundManagerEmailAndCompositeid(user.getEmail(), newRecord.getKey().getCompositeid());
        if (count == 1) {
            count = compositeConstituentsRepository.countByKeyCompositeidAndKeyTicker(
                    newRecord.getKey().getCompositeid(), newRecord.getKey().getTicker());
            logger.debug("In AdviserService.addCompositeDetails: record count is {}", count);

            if (count == 0) {
                compositeConstituentsRepository.save(newRecord);
            } else {
                throw new IllegalArgumentException("Record already exists.");
            }
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    // ✅ Changed from static to instance method
    public void deleteCompositeDetails(CompositeConstituents deletedRecord) {
        logger.debug("In AdviserService.deleteCompositeDetails: deletedRecord.shortName {} - {}",
                deletedRecord.getKey().getCompositeid(), deletedRecord.getKey().getTicker());

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);

        logger.debug("In AdviserService.deleteWealthDetailsRecord: Email {}", user.getEmail());

        int count = compositeRepository.countByFundManagerEmailAndCompositeid(user.getEmail(), deletedRecord.getKey().getCompositeid());
        if (count == 1) {
            compositeConstituentsRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }
}