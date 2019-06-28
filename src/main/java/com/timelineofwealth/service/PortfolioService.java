package com.timelineofwealth.service;

import com.timelineofwealth.dto.ConsolidatedPortfolioHoldings;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.Portfolio;
import com.timelineofwealth.entities.PortfolioHoldings;
import com.timelineofwealth.repositories.PortfolioHoldingsRepository;
import com.timelineofwealth.repositories.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service("PortfolioService")
public class PortfolioService {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);

    @Autowired
    private static PortfolioRepository portfolioRepository;
    @Autowired
    public void setPortfolioRepository(PortfolioRepository portfolioRepository){
        PortfolioService.portfolioRepository = portfolioRepository;
    }
    @Autowired
    private static PortfolioHoldingsRepository portfolioHoldingsRepository;
    @Autowired
    public void setPortfolioHoldingsRepository(PortfolioHoldingsRepository portfolioHoldingsRepository){
        PortfolioService.portfolioHoldingsRepository = portfolioHoldingsRepository;
    }

    public static List<Portfolio> getPortfolios(String email){
        logger.debug(String.format("In PortfolioService.getPortfolios: Email %s", email));

        List<Portfolio> portfolios;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolios = portfolioRepository.findAllByKeyMemberidInAndStatusOrderByKeyPortfolioid(membersIds, "Active");

        return portfolios;
    }

    public static List<PortfolioHoldings> getPortfolioHoldings(String email){
        logger.debug(String.format("In PortfolioService.getPortfolioHoldings: Email %s", email));

        List<PortfolioHoldings> portfolioHoldings;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioHoldings = portfolioHoldingsRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscAssetClassidAscShortNameAscKeyBuyDateDesc(membersIds);

        return portfolioHoldings;
    }

    public static List<ConsolidatedPortfolioHoldings> getConsolidatedPortfolioHoldings(String email){
        logger.debug(String.format("In PortfolioService.getConsolidatedPortfolioHoldings: Email %s", email));

        List<ConsolidatedPortfolioHoldings> consolidatedPortfolioHoldings = new ArrayList<>();
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        List<Object[]> objects = portfolioHoldingsRepository.getConsolidatedPortfolioHoldings(membersIds);
        for(Object[] object : objects) {
            ConsolidatedPortfolioHoldings holding = new ConsolidatedPortfolioHoldings();
            holding.setMemberid(Integer.parseInt(""+ (int)object[0]));
            holding.setPortfolioid((int) object[1]);
            holding.setName((String) object[2]);
            holding.setQuantity((BigDecimal) object[3]);
            holding.setTotalCost((BigDecimal) object[4]);
            holding.setMarketValue((BigDecimal) object[5]);
            holding.setNetProfit((BigDecimal) object[6]);
            holding.setWeight((BigDecimal) object[7]);
            consolidatedPortfolioHoldings.add(holding);
        }

        return consolidatedPortfolioHoldings;
    }

}
