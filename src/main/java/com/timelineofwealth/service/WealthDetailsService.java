package com.timelineofwealth.service;

import com.timelineofwealth.dto.ConsolidatedAssetsDTO;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.WealthAssetAllocationHistoryRepository;
import com.timelineofwealth.repositories.WealthDetailsRepository;
import com.timelineofwealth.repositories.WealthHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("WealthDetailsService")
public class WealthDetailsService {

    private final Logger logger = LoggerFactory.getLogger(WealthDetailsService.class);
    private final WealthDetailsRepository wealthDetailsRepository;
    private final WealthHistoryRepository wealthHistoryRepository;
    private final WealthAssetAllocationHistoryRepository wealthAssetAllocationHistoryRepository;
    private final MemberService memberService;
    private final CommonService commonService;

    @Autowired
    public WealthDetailsService(WealthDetailsRepository wealthDetailsRepository,
                                WealthHistoryRepository wealthHistoryRepository,
                                WealthAssetAllocationHistoryRepository wealthAssetAllocationHistoryRepository,
                                MemberService memberService,
                                CommonService commonService){
        this.wealthDetailsRepository = wealthDetailsRepository;
        this.wealthHistoryRepository = wealthHistoryRepository;
        this.wealthAssetAllocationHistoryRepository = wealthAssetAllocationHistoryRepository;
        this.memberService = memberService;
        this.commonService = commonService;
    }

    public List<WealthDetails> getWealthDetailsRecords(String email){
        logger.debug(String.format("In this.getWealthDetailsRecords: Email %s", email));

        List<WealthDetails> wealthDetailsRecords;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        wealthDetailsRecords = wealthDetailsRepository.findByKeyMemberidInOrderByKeyMemberidAscAssetClassidAscKeyTickerAscKeyBuyDateAsc(membersIds);

        return wealthDetailsRecords;
    }

    public List<ConsolidatedAssetsDTO> getConsolidatedWealthDetailsRecords(String email){
        logger.debug(String.format("In this.getConsolidatedWealthDetailsRecords: Email %s", email));

        List<Object[]> objects;
        List<ConsolidatedAssetsDTO> dtos = new ArrayList<>();
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        objects = wealthDetailsRepository.findByKeyMemberidInGroupByKeyMemberidAndKeyTicker(membersIds);
        for (Object[] object : objects) {
            ConsolidatedAssetsDTO dto = new ConsolidatedAssetsDTO();
            dto.setMemberid(""+object[0]);
            dto.setTicker(""+object[1]);
            dto.setName(""+object[2]);
            dto.setShortName(""+object[3]);
            dto.setAssetClassid(""+object[4]);
            dto.setSubindustryid(""+object[5]);
            dto.setQuantity(""+object[6]);
            dto.setRate(""+object[7]);
            dto.setBrokerage(""+object[8]);
            dto.setTax(""+object[9]);
            dto.setTotalCost(""+object[10]);
            dto.setNetRate(""+object[11]);
            dto.setCmp(""+object[12]);
            dto.setMarketValue(""+object[13]);
            dto.setNetProfit(""+object[14]);
            dto.setAbsoluteReturn(""+object[15]);
            dto.setAnnualizedReturn(""+object[16]);
            dtos.add(dto);
        }

        return dtos;
    }

    public void updateWealthDetailsRecord(WealthDetails editedRecord) {
        logger.debug(String.format("In this.updateWealthDetailsRecord: editedRecord.key.memberid %d", editedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In this.updateWealthDetailsRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), editedRecord.getKey().getMemberid())){
            this.wealthDetailsRepository.save(editedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public void addWealthDetailsRecord(WealthDetails newRecord) {
        logger.debug(String.format("In this.addWealthDetailsRecord: newRecord.key.memberid %d", newRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In this.addWealthDetailsRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), newRecord.getKey().getMemberid())){
            int count = this.wealthDetailsRepository.countByKeyMemberidAndKeyBuyDateAndKeyTicker(newRecord.getKey().getMemberid(), newRecord.getKey().getBuyDate(), newRecord.getKey().getTicker());
            logger.debug(String.format("In this.addWealthDetailsRecord: record count is %d", count));
            if (count == 0) {
                this.wealthDetailsRepository.save(newRecord);
            } else {
                throw new IllegalArgumentException("Record already exists.");
            }
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public void deleteWealthDetailsRecord(WealthDetails deletedRecord){
        logger.debug(String.format("In this.deleteWealthDetailsRecord: deletedRecord.key.memberid %d", deletedRecord.getKey().getMemberid()));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        logger.debug(String.format("In this.deleteWealthDetailsRecord: Email %s", user.getEmail()));
        if(memberService.isAuthorised(user.getEmail(), deletedRecord.getKey().getMemberid())){
            this.wealthDetailsRepository.delete(deletedRecord);
        } else {
            throw new InsufficientAuthenticationException("User is not authorized");
        }
    }

    public List<WealthAssetAllocationHistory> getCurrentAssetAllocation(String email){
        logger.debug(String.format("In this.getCurrentAssetAllocation: Email %s", email));

        List<WealthAssetAllocationHistory> wealthAssetAllocationHistoryRecords;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }

        Date dateToday = commonService.getSetupDates().getDateToday();
        wealthAssetAllocationHistoryRecords = wealthAssetAllocationHistoryRepository.findAllByKeyMemberidInAndKeyDateOrderByKeyMemberidAscKeyAssetClassGroupAsc(membersIds,dateToday);

        return wealthAssetAllocationHistoryRecords;
    }

    public Map<Date, Map<Long, BigDecimal>> getWealthHistoryRecords(String email){
        logger.debug(String.format("In this.getWealthHistoryRecords: Email %s", email));

        List<WealthHistory> wealthHistoryRecords;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        //sorted by date and memberid
        wealthHistoryRecords = wealthHistoryRepository.findByKeyMemberidInOrderByKeyDateAscKeyMemberid(membersIds);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Map historyByDate = new TreeMap<Date, Map<Long, BigDecimal>>();
        for (int i=0; i <wealthHistoryRecords.size();  i++){
            WealthHistory wealthHistory = wealthHistoryRecords.get(i);
            Map historyByMember = null;

            if (!historyByDate.containsKey(wealthHistory.getKey().getDate())){
                // Putting consolidated entry for all members
                historyByMember = new TreeMap<Long, BigDecimal>();
                historyByMember.put(new Long(0), new BigDecimal(0));
                for (Member member : members){
                    //create map for each member initialize history value to zero in case data is absent
                    historyByMember.put(new Long(member.getMemberid()),new BigDecimal(0));
                }
                //
                historyByDate.put(wealthHistory.getKey().getDate(),historyByMember);
            }
            historyByMember = (Map)historyByDate.get(wealthHistory.getKey().getDate());
            historyByMember.replace(new Long(wealthHistory.getKey().getMemberid()), wealthHistory.getValue());
        }
        Iterator iterator = historyByDate.entrySet().iterator();
        Map historyByMember = null;
        Date date;

        while (iterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)iterator.next();
            historyByMember = (Map<Long,BigDecimal>)mapElement.getValue();
            BigDecimal consolidatedHistoryValue = new BigDecimal(0);
            for (Member member : members){
                //create map for each member initialize history value to zero in case data is absent
                long memberid = new Long(member.getMemberid());
                BigDecimal memberHistoryValue = (BigDecimal)historyByMember.get(new Long(memberid));
                consolidatedHistoryValue = consolidatedHistoryValue.add(memberHistoryValue);
            }
            historyByMember.replace(new Long(0),consolidatedHistoryValue);
        }

        return historyByDate;
    }

}
