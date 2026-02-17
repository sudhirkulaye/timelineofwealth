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
    private final Logger logger = LoggerFactory.getLogger(PortfolioService.class);
    private final PortfolioRepository portfolioRepository;
    private final PortfolioHoldingsRepository portfolioHoldingsRepository;
    private final PortfolioHistoricalHoldingsRepository portfolioHistoricalHoldingsRepository;
    private final PortfolioCashflowRepository portfolioCashflowRepository;
    private final PortfolioReturnsCalculationSupportRepository portfolioReturnsCalculationSupportRepository;
    private final PortfolioTwrrMonthlyRepository portfolioTwrrMonthlyRepository;
    private final PortfolioTwrrSummaryRepository portfolioTwrrSummaryRepository;
    private final MemberService memberService;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository,
                            PortfolioHoldingsRepository portfolioHoldingsRepository,
                            PortfolioHistoricalHoldingsRepository portfolioHistoricalHoldingsRepository,
                            PortfolioCashflowRepository portfolioCashflowRepository,
                            PortfolioReturnsCalculationSupportRepository portfolioReturnsCalculationSupportRepository,
                            PortfolioTwrrMonthlyRepository portfolioTwrrMonthlyRepository,
                            PortfolioTwrrSummaryRepository portfolioTwrrSummaryRepository,
                            MemberService memberService){
        this.portfolioRepository = portfolioRepository;
        this.portfolioHoldingsRepository = portfolioHoldingsRepository;
        this.portfolioHistoricalHoldingsRepository = portfolioHistoricalHoldingsRepository;
        this.portfolioCashflowRepository = portfolioCashflowRepository;
        this.portfolioReturnsCalculationSupportRepository = portfolioReturnsCalculationSupportRepository;
        this.portfolioTwrrMonthlyRepository = portfolioTwrrMonthlyRepository;
        this.portfolioTwrrSummaryRepository = portfolioTwrrSummaryRepository;
        this.memberService = memberService;
    }

    public List<Portfolio> getPortfolios(String email){
        logger.debug(String.format("In PortfolioService.getPortfolios: Email %s", email));

        List<Portfolio> portfolios;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolios = portfolioRepository.findAllByKeyMemberidInAndStatusOrderByKeyPortfolioid(membersIds, "Active");

        return portfolios;
    }

    public List<PortfolioHoldings> getPortfolioHoldings(String email){
        logger.debug(String.format("In PortfolioService.getPortfolioHoldings: Email %s", email));

        List<PortfolioHoldings> portfolioHoldings;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioHoldings = portfolioHoldingsRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscAssetClassidAscShortNameAscKeyBuyDateDesc(membersIds);

        return portfolioHoldings;
    }

    public List<ConsolidatedPortfolioHoldings> getConsolidatedPortfolioHoldings(String email){
        logger.debug(String.format("In PortfolioService.getConsolidatedPortfolioHoldings: Email %s", email));

        List<ConsolidatedPortfolioHoldings> consolidatedPortfolioHoldings = new ArrayList<>();
        List<Member> members = memberService.getUserMembers(email);
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

    public List<PortfolioHistoricalHoldings> getPortfolioHistoricalHoldings(String email){
        logger.debug(String.format("In PortfolioService.getPortfolioHistoricalHoldings: Email %s", email));

        List<PortfolioHistoricalHoldings> portfolioHistoricalHoldings;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioHistoricalHoldings = portfolioHistoricalHoldingsRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeySellDateDesc(membersIds);

        return portfolioHistoricalHoldings;
    }

    public List<FinYearProfit> getFinYearProfit(String email){
        logger.debug(String.format("In PortfolioService.getFinYearProfit: Email %s", email));

        List<FinYearProfit> finYearProfits = new ArrayList<>();
        List<Member> members = memberService.getUserMembers(email);
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
    public List<PortfolioReturnsCalculationSupport> getPortfolioCashflows(String email){
        logger.debug(String.format("In PortfolioService.getPortfolioCashflows: Email %s", email));

        List<PortfolioReturnsCalculationSupport> portfolioCashflows;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioCashflows = portfolioReturnsCalculationSupportRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyDateDesc(membersIds);

        return portfolioCashflows;
    }

    public List<PortfolioTwrrSummary> getPortfolioTwrrSummary(String email) {
        logger.debug(String.format("In PortfolioService.getPortfolioTwrrSummary: Email %s", email));

        List<PortfolioTwrrSummary> portfolioTwrrSummaries;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioTwrrSummaries = portfolioTwrrSummaryRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyBenchmarkidAsc(membersIds);

        return portfolioTwrrSummaries;
    }

    public List<PortfolioTwrrMonthly> getPortfolioTwrrMonthly(String email) {
        logger.debug(String.format("In PortfolioService.getPortfolioTwrrMonthly: Email %s", email));

        List<PortfolioTwrrMonthly> portfolioTwrrMonthlies;
        List<Member> members = memberService.getUserMembers(email);
        List<Long> membersIds = new ArrayList<>();
        for (Member member : members ){
            membersIds.add(new Long(member.getMemberid()));
        }
        portfolioTwrrMonthlies = portfolioTwrrMonthlyRepository.findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyReturnsYearDesc(membersIds);

        return portfolioTwrrMonthlies;
    }
}
