package com.timelineofwealth.service;

import com.google.common.collect.Iterables;
import com.timelineofwealth.controllers.UserViewController;
import com.timelineofwealth.dto.MutualFundDTO;
import com.timelineofwealth.dto.NseBse500;
import com.timelineofwealth.dto.StockValuationHistory;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service("CommonService")
@EnableCaching
public class CommonService {

    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String ADVISER_ROLE = "ROLE_ADVISER";
    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    private static UserRepository userRepository;
    @Autowired
    public  void setUserRepository(UserRepository userRepository){
        CommonService.userRepository = userRepository;
    }

    @Autowired
    private static AssetClassificationRepository assetClassificationRepository;
    @Autowired
    public void setAssetClassificationRepository(AssetClassificationRepository assetClassificationRepository){
        CommonService.assetClassificationRepository = assetClassificationRepository;
    }

    @Autowired
    private static SubindustryRepository subindustryRepository;
    @Autowired
    public void setSubindustryRepository(SubindustryRepository subindustryRepository){
        CommonService.subindustryRepository = subindustryRepository;
    }

    @Autowired
    private static MutualFundUniverseRepository mutualFundUniverseRepository;
    @Autowired
    public void setMutualFundUniverseRepository(MutualFundUniverseRepository mutualFundUniverseRepository){
        CommonService.mutualFundUniverseRepository = mutualFundUniverseRepository;
    }

    @Autowired
    private static MutualFundStatsRepository mutualFundStatsRepository;
    @Autowired
    public void setMutualFundStatsRepository(MutualFundStatsRepository mutualFundStatsRepository){
        CommonService.mutualFundStatsRepository = mutualFundStatsRepository;
    }

    @Autowired
    private static StockUniverseRepository stockUniverseRepository;
    @Autowired
    public void setStockUniverseRepository(StockUniverseRepository stockUniverseRepository){
        CommonService.stockUniverseRepository = stockUniverseRepository;
    }

    @Autowired
    private static StockPnlRepository stockPnlRepository;
    @Autowired
    public void setStockPnlRepository(StockPnlRepository stockPnlRepository){
        CommonService.stockPnlRepository = stockPnlRepository;
    }

    @Autowired
    private static StockQuarterRepository stockQuarterRepository;
    @Autowired
    public void setStockQuarterRepository(StockQuarterRepository stockQuarterRepository){
        CommonService.stockQuarterRepository = stockQuarterRepository;
    }

    @Autowired
    private static DailyDataSRepository dailyDataSRepository;
    @Autowired
    public void setDailyDataSRepository(DailyDataSRepository dailyDataSRepository) {
        CommonService.dailyDataSRepository = dailyDataSRepository;
    }

    @Autowired
    private static SetupDatesRepository setupDatesRepository;
    @Autowired
    public void setSetupDatesRepository(SetupDatesRepository setupDatesRepository){
        CommonService.setupDatesRepository = setupDatesRepository;
    }

    @Autowired
    private static IndexValuationRepository indexValuationRepository;
    @Autowired
    public void setIndexValuationRepository(IndexValuationRepository indexValuationRepository){
        CommonService.indexValuationRepository = indexValuationRepository;
    }

    @Autowired
    private static IndexStatisticsRepository indexStatisticsRepository;
    @Autowired
    public void setIndexStatisticsRepository(IndexStatisticsRepository indexStatisticsRepository){
        CommonService.indexStatisticsRepository = indexStatisticsRepository;
    }

    private static List<StockUniverse> nseBse500BasicList;
    private static List<NseBse500> nseBse500List;

    /**
     * Returns true if SignIn User is Admin
     * @param userDetails
     * @return
     */
    public static boolean isAdmin(@AuthenticationPrincipal UserDetails userDetails){
        boolean isAdmin = false;
        String roleName = getLoggedInUser(userDetails).getRoleName();
        if (roleName.equals(ADMIN_ROLE)) {
            isAdmin = true;
        }
        return isAdmin;
    }

    /**
     * Returns true if SignIn User is Adviser
     * @param userDetails
     * @return
     */
    public static boolean isAdviser(@AuthenticationPrincipal UserDetails userDetails){
        boolean isAdviser = false;
        String roleName = getLoggedInUser(userDetails).getRoleName();
        if (roleName.equals(ADVISER_ROLE)) {
            isAdviser = true;
        }
        return isAdviser;
    }

    public static void updateLastLoginStatus(@AuthenticationPrincipal UserDetails userDetails){
        User loggedInUser = getLoggedInUser(userDetails);
        loggedInUser.setLastLoginTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        userRepository.save(loggedInUser);
    }

    /**
     * Returns User Object for logged-in user
     * @param userDetails
     * @return
     * @throws UsernameNotFoundException
     */
    public static User getLoggedInUser(UserDetails userDetails) throws  UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());

        optionalUser
                .orElseThrow(() -> new UsernameNotFoundException("User Login Id Not Found"));

        return optionalUser.map(User::new).get();

    }

    /**
     * Returns Welcome message to be displayed after signIn
     * @param signInUser
     * @return
     */
    public static String getWelcomeMessage(User signInUser){
        String welcomeMessage = "";
        if (signInUser != null) {
            welcomeMessage = signInUser.getPrefix()+ " "+ signInUser.getLastName();
        }
        return welcomeMessage;
    }

    /**
     * Return Setup Dates
     * @return
     */
    @Cacheable("SetupDates")
    public static SetupDates getSetupDates() {
        List<SetupDates> setupDatesList;
        SetupDates setupDates = new SetupDates();
        if (CommonService.setupDatesRepository != null) {
            setupDatesList = CommonService.setupDatesRepository.findAll();
            setupDates = setupDatesList.get(0);
        }

        return setupDates;
    }

    /**
     * Returns Asset Classifications
     * @return
     */
    @Cacheable("AssetClassfication")
    public static List<AssetClassification> getAssetClassfication() {
//        if (CommonService.assetClassificationRepository != null) {
//            return  CommonService.assetClassificationRepository.findAll();
//        } else {
//            return new ArrayList<>();
//        }
        return  CommonService.assetClassificationRepository.findAll();
    }

    /**
     * Return All Sub Industries
     * @return
     */
    @Cacheable("Subindustries")
    public static List<Subindustry> getSubindustries() {
        return CommonService.subindustryRepository.findAll();
    }

    /**
     * Returns all Fund Houses
     * @return
     */
    @Cacheable("FundHouses")
    public static List<String> getDistinctFundHouse() {
        return CommonService.mutualFundUniverseRepository.findDistinctFundHouse();
    }

    /**
     * Returns all Fund Houses by
     * @param fundHouse
     * @return
     */
    @Cacheable(value = "SchemeNamesByFundHouse")
    public static List<MutualFundDTO> getSchemeNames(String fundHouse){
        List<MutualFundUniverse> funds = CommonService.mutualFundUniverseRepository.findSchemeNamesByFundHouse(fundHouse,new Sort("schemeNamePart"));
        List<MutualFundDTO> fundsDTO = new ArrayList<>();
        setFundsDTO(funds, fundsDTO);
        return  fundsDTO;
    }

    private static void setFundsDTO(List<MutualFundUniverse> funds, List<MutualFundDTO> fundsDTO) {
        for (MutualFundUniverse fund: funds) {
            MutualFundDTO fundDTO = new MutualFundDTO();
            fundDTO.setSchemeCode(fund.getSchemeCode());
            fundDTO.setIsinDivReinvestment(fund.getIsinDivReinvestment());
            fundDTO.setSchemeCodeDirectGrowth(fund.getSchemeCodeDirectGrowth());
            fundDTO.setSchemeCodeRegularGrowth(fund.getSchemeCodeRegularGrowth());
            fundDTO.setFundHouse(fund.getFundHouse());
            fundDTO.setDirectRegular(fund.getDirectRegular());
            fundDTO.setDividendGrowth(fund.getDividendGrowth());
            fundDTO.setDividendFreq(fund.getDividendFreq());
            fundDTO.setSchemeNamePart(fund.getSchemeNamePart());
            fundDTO.setSchemeNameFull(fund.getSchemeNameFull());
            fundDTO.setAssetClassid(fund.getAssetClassid());
            fundDTO.setCategory(fund.getCategory());
            fundDTO.setEquityStyleBox(fund.getEquityStyleBox());
            fundDTO.setDebtStyleBox(fund.getDebtStyleBox());
            fundDTO.setLatestNav(fund.getLatestNav());
            fundDTO.setDateLatestNav(fund.getDateLatestNav());
            fundDTO.setBenchmarkTicker(fund.getBenchmarkTicker());

            fundsDTO.add(fundDTO);
        }
    }

    /**
     * Returns all Fund Houses by
     * @param fundHouse
     * @param directRegular
     * @return
     */
    @Cacheable(value = "SchemeNamesByFundHouseAndPlan")
    public static List<MutualFundDTO> getSchemeNames(String fundHouse,String directRegular){
        List<MutualFundUniverse> funds = CommonService.mutualFundUniverseRepository.findSchemeNamesByFundHouse(fundHouse,directRegular, new Sort("schemeNamePart"));
        List<MutualFundDTO> fundsDTO = new ArrayList<>();
        setFundsDTO(funds, fundsDTO);
        return  fundsDTO;
    }

    /**
     * Returns all Fund Houses by
     * @param fundHouse
     * @param directRegular
     * @param dividendGrowth
     * @return
     */
    @Cacheable(value = "SchemeNamesByFundHouseAndPlanAndOption")
    public static List<MutualFundDTO> getSchemeNames(String fundHouse,String directRegular,String dividendGrowth){
        List<MutualFundUniverse> funds = CommonService.mutualFundUniverseRepository.findSchemeNamesByFundHouse(fundHouse,directRegular, dividendGrowth, new Sort("schemeNamePart"));
        List<MutualFundDTO> fundsDTO = new ArrayList<>();
        setFundsDTO(funds, fundsDTO);
        return  fundsDTO;
    }

    @Cacheable(value = "GetSchemeDetails")
    public  static  List<MutualFundDTO> getSchemeDetails(String fundHouse, String category) {

        List<MutualFundUniverse> funds = CommonService.mutualFundUniverseRepository.findSchemeNamesByFundHouseAndCategory(fundHouse, category, new Sort("schemeNamePart"));
        //List<MutualFundUniverse> funds = CommonService.mutualFundUniverseRepository.findAllByFundHouseAndCategory(fundHouse, category);
        List<MutualFundDTO> fundsDTO = new ArrayList<>();
        setFundsDTO(funds, fundsDTO);
        return fundsDTO;
    }

    @Cacheable(value = "SelectedMFStats")
    public static List<MutualFundStats> getSelectedMF() {
        //return null;
        List<MutualFundStats> mfStats =  CommonService.mutualFundStatsRepository.findAll();
        mfStats.sort(Comparator.comparing(l->l.getSchemeNamePart()));
        mfStats.sort(Comparator.comparing(l->l.getSchemeType()));
        return mfStats;
    }

    /**
     * Returns all stocks
     * @return
     */
    @Cacheable(value = "StockUniverse")
    public static List<StockUniverse> getAllStocks(){
        return CommonService.stockUniverseRepository.findAll();
    }


    /**
     * Returns all stocks
     * @return
     */
    @Cacheable(value = "NseBse500Basic")
    public static List<NseBse500> getNseBse500() {
        nseBse500BasicList = CommonService.stockUniverseRepository.findAllByIsNse500OrIsBse500OrderByMarketcapDesc(1,1);
        List<DailyDataS> dailyDataSList = CommonService.dailyDataSRepository.findAllByKeyDate(getSetupDates().getDateToday());
        nseBse500List = new ArrayList<>();
        List<Subindustry> subindustries = CommonService.getSubindustries();
        for(StockUniverse stockUniverse : nseBse500BasicList){
            NseBse500 nseBse500 = new NseBse500(stockUniverse);
            List<Subindustry> subindustries1 = subindustries.stream()
                    .filter( subindustry -> subindustry.getSubindustryid() == stockUniverse.getSubindustryid() )
                    .collect(Collectors.toList());

            nseBse500.setSectorNameDisplay(subindustries1.get(0).getSectorNameDisplay());
            nseBse500.setIndustryNameDisplay(subindustries1.get(0).getIndustryNameDisplay());
            nseBse500.setSubIndustryNameDisplay(subindustries1.get(0).getSubIndustryNameDisplay());

            nseBse500.setDailyDataS(dailyDataSList.stream().filter(dailyDataS -> nseBse500.getTicker5().equals(dailyDataS.getKey().getName())).findAny().orElse(null));

            nseBse500List.add(nseBse500);
        }
        return nseBse500List;
    }

    /**
     * Get Stock Details
     * @param ticker
     * @return
     */
    public static NseBse500 getStockDetails(String ticker) {
        if (nseBse500List == null) {
            nseBse500List = getNseBse500();
        }
        return nseBse500List.stream().filter(stock -> stock.getTicker().equals(ticker)).collect(Collectors.toList()).get(0);
    }

    /**
     * Get consolidated Sales and Profits, if consolidated not available then standalone
     * @param ticker
     * @return
     */
    public static List<StockQuarter> getStockQuarter(String ticker){
        List<java.sql.Date> dates = CommonService.stockQuarterRepository.findDistinctDatesForTicker(ticker);
        List<StockQuarter> stockQuarterList = new ArrayList<>();
        for(java.sql.Date date: dates){
            StockQuarter stockQuarter = CommonService.stockQuarterRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"C", date);
            if (stockQuarter != null) {
                stockQuarterList.add(stockQuarter);
            } else {
                stockQuarter = CommonService.stockQuarterRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"S", date);
                if (stockQuarter != null){
                    stockQuarterList.add(stockQuarter);
                }
            }
        }
        return stockQuarterList;
    }

    /**
     * Get consolidated Sales and Profits, if consolidated not available then standalone
     * @param ticker
     * @return
     */
    public static List<StockPnl> getStockPnl(String ticker){
        List<java.sql.Date> dates = CommonService.stockPnlRepository.findDistinctDatesForTicker(ticker);
        List<StockPnl> stockPnlList = new ArrayList<>();
        for(java.sql.Date date: dates){
            StockPnl stockPnl = CommonService.stockPnlRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"C", date);
            if (stockPnl != null) {
                stockPnlList.add(stockPnl);
            } else {
                stockPnl = CommonService.stockPnlRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"S", date);
                if (stockPnl != null){
                    stockPnlList.add(stockPnl);
                }
            }
        }
        return stockPnlList;
    }

    /**
     * Get Valuaitn History for Charts
     * @param ticker
     * @return
     */
    public static List<StockValuationHistory> getStockValuationHistory(String ticker){
        List<StockValuationHistory> valuationHistories = new ArrayList<>();
        List<StockPnl> stockPnlList = getStockPnl(ticker);
        stockPnlList.sort(Comparator.comparing(l->l.getKey().getDate()));
        for (StockPnl stockPnl: stockPnlList) {
            StockValuationHistory history = new StockValuationHistory();
            history.setTicker(ticker);
            history.setDate(stockPnl.getKey().getDate());
            history.setPe(stockPnl.getPe());
            //history.setPb(stockPnl.getPrice());
            valuationHistories.add(history);
        }
        return valuationHistories;
    }

    /**
     * Get Index Valuation
     * @return
     */
    //@Cacheable(value = "IndexValuation")
    public static List<IndexValuation> getIndexValuation(){
        return CommonService.indexValuationRepository.findAllByKeyTickerOrderByKeyDate("NIFTY");
        //List<IndexValuation> list = CommonService.indexValuationRepository.findAllByKeyTickerAndKeyDateBetweenOrderByKeyDate("NIFTY", getSetupDates().getDateStart4Quarter() , getSetupDates().getDateToday());
        //System.out.println("list.size()::"+ list.size());
        //return list;

    }

    /**
     * Get Index Statistics
     * @return
     */
    public static List<IndexStatistics> getIndexStatistics(String index){
        if (index.equalsIgnoreCase("NIFTY")) {
            return CommonService.indexStatisticsRepository.findOneByTicker("NIFTY");
        } else {
            List<IndexStatistics> indexStatistics = CommonService.indexStatisticsRepository.findOneByTicker("BSEMidCap");
            indexStatistics.addAll(CommonService.indexStatisticsRepository.findOneByTicker("BSESmallCap"));
            return indexStatistics;
        }
    }
}
