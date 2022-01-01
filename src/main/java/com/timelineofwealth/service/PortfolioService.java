package com.timelineofwealth.service;

import com.timelineofwealth.dto.ConsolidatedPortfolioHoldings;
import com.timelineofwealth.dto.FinYearProfit;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
    @Autowired
    private static PortfolioHistoricalHoldingsRepository portfolioHistoricalHoldingsRepository;
    @Autowired
    public void setPortfolioHistoricalHoldingsRepository(PortfolioHistoricalHoldingsRepository portfolioHistoricalHoldingsRepository){
        PortfolioService.portfolioHistoricalHoldingsRepository = portfolioHistoricalHoldingsRepository;
    }
    @Autowired
    private static PortfolioCashflowRepository portfolioCashflowRepository;
    @Autowired
    public void setPortfolioCashflowRepository(PortfolioCashflowRepository portfolioCashflowRepository){
        PortfolioService.portfolioCashflowRepository = portfolioCashflowRepository;
    }
    @Autowired
    private static PortfolioReturnsCalculationSupportRepository portfolioReturnsCalculationSupportRepository;
    @Autowired
    public void setPortfolioReturnsCalculationSupportRepository(PortfolioReturnsCalculationSupportRepository portfolioReturnsCalculationSupportRepository){
        PortfolioService.portfolioReturnsCalculationSupportRepository = portfolioReturnsCalculationSupportRepository;
    }
    @Autowired
    private static PortfolioTwrrMonthlyRepository portfolioTwrrMonthlyRepository;
    @Autowired
    public void setPortfolioTwrrMonthlyRepository(PortfolioTwrrMonthlyRepository portfolioTwrrMonthlyRepository) {
        PortfolioService.portfolioTwrrMonthlyRepository = portfolioTwrrMonthlyRepository;
    }
    @Autowired
    private static PortfolioTwrrSummaryRepository portfolioTwrrSummaryRepository;
    @Autowired
    public void setPortfolioTwrrSummaryRepository(PortfolioTwrrSummaryRepository portfolioTwrrSummaryRepository){
        PortfolioService.portfolioTwrrSummaryRepository = portfolioTwrrSummaryRepository;
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
            holding.setAbsoluteReturn((BigDecimal) object[8]);
            consolidatedPortfolioHoldings.add(holding);
        }

        return consolidatedPortfolioHoldings;
    }

    public static List<PortfolioHistoricalHoldings> getPortfolioHistoricalHoldings(String email){
        logger.debug(String.format("In PortfolioService.getPortfolioHistoricalHoldings: Email %s", email));

        List<PortfolioHistoricalHoldings> portfolioHistoricalHoldings;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioHistoricalHoldings = portfolioHistoricalHoldingsRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeySellDateDesc(membersIds);

        return portfolioHistoricalHoldings;
    }

    public static List<FinYearProfit> getFinYearProfit(String email){
        logger.debug(String.format("In PortfolioService.getFinYearProfit: Email %s", email));

        List<FinYearProfit> finYearProfits = new ArrayList<>();
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        List<Object[]> objects = portfolioHistoricalHoldingsRepository.getLongTermProfit(membersIds);
        for(Object[] object : objects) {
            FinYearProfit finYearProfit = new FinYearProfit();
            finYearProfit.setMemberid(Integer.parseInt(""+ (int)object[0]));
            finYearProfit.setPortfolioid((int) object[1]);
            finYearProfit.setFinYear((String) object[2]);
            finYearProfit.setNetProfit((BigDecimal) object[3]);
            finYearProfit.setLongShortTerm("Long Term");
            finYearProfits.add(finYearProfit);
        }
        List<Object[]> objects1 = portfolioHistoricalHoldingsRepository.getShortTermProfit(membersIds);
        for(Object[] object : objects1) {
            FinYearProfit finYearProfit = new FinYearProfit();
            finYearProfit.setMemberid(Integer.parseInt(""+ (int)object[0]));
            finYearProfit.setPortfolioid((int) object[1]);
            finYearProfit.setFinYear((String) object[2]);
            finYearProfit.setNetProfit((BigDecimal) object[3]);
            finYearProfit.setLongShortTerm("Short Term");
            finYearProfits.add(finYearProfit);
        }
        finYearProfits.sort(Comparator.comparing(FinYearProfit::getFinYear).reversed());
        return finYearProfits;
    }

    //
    public static List<PortfolioReturnsCalculationSupport> getPortfolioCashflows(String email){
        logger.debug(String.format("In PortfolioService.getPortfolioCashflows: Email %s", email));

        List<PortfolioReturnsCalculationSupport> portfolioCashflows;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioCashflows = portfolioReturnsCalculationSupportRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyDateDesc(membersIds);

        return portfolioCashflows;
    }

    public static List<PortfolioTwrrSummary> getPortfolioTwrrSummary(String email) {
        logger.debug(String.format("In PortfolioService.getPortfolioTwrrSummary: Email %s", email));

        List<PortfolioTwrrSummary> portfolioTwrrSummaries;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioTwrrSummaries = portfolioTwrrSummaryRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyBenchmarkidAsc(membersIds);

        return portfolioTwrrSummaries;
    }

    public static List<PortfolioTwrrMonthly> getPortfolioTwrrMonthly(String email) {
        logger.debug(String.format("In PortfolioService.getPortfolioTwrrMonthly: Email %s", email));

        List<PortfolioTwrrMonthly> portfolioTwrrMonthlies;
        List<Member> members = MemberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioTwrrMonthlies = portfolioTwrrMonthlyRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyReturnsYearDesc(membersIds);

        return portfolioTwrrMonthlies;
    }
}
