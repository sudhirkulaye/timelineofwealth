package com.timelineofwealth.service;

import com.timelineofwealth.dto.*;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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
    private static DailyDataBRepository dailyDataBRepository;
    @Autowired
    public void setDailyDataBRepository(DailyDataBRepository dailyDataBRepository) {
        CommonService.dailyDataBRepository = dailyDataBRepository;
    }

    @Autowired
    private static StockPriceMovementRepository stockPriceMovementRepository;
    @Autowired
    public void setStockPriceMovementRepository(StockPriceMovementRepository stockPriceMovementRepository) {
        CommonService.stockPriceMovementRepository = stockPriceMovementRepository;
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

    @Autowired
    private static StockPriceMovementHistoryRepository stockPriceMovementHistoryRepository;
    @Autowired
    public void setStockPriceMovementHistoryRepository(StockPriceMovementHistoryRepository stockPriceMovementHistoryRepository){
        CommonService.stockPriceMovementHistoryRepository = stockPriceMovementHistoryRepository;
    }

    @Autowired
    private static BenchmarkTwrrMonthlyRepository benchmarkTwrrMonthlyRepository;
    @Autowired
    public void setBenchmarkTwrrMonthlyRepository(BenchmarkTwrrMonthlyRepository benchmarkTwrrMonthlyRepository){
        CommonService.benchmarkTwrrMonthlyRepository = benchmarkTwrrMonthlyRepository;
    }

    @Autowired
    private static BenchmarkTwrrSummaryRepository benchmarkTwrrSummaryRepository;
    @Autowired
    public void setBenchmarkTwrrSummaryRepository(BenchmarkTwrrSummaryRepository benchmarkTwrrSummaryRepository){
        CommonService.benchmarkTwrrSummaryRepository = benchmarkTwrrSummaryRepository;
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
    //@Cacheable("SetupDates")
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
        List<StockPriceMovement> stockPriceMovementList = CommonService.stockPriceMovementRepository.findAll();
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
            nseBse500.setStockPriceMovement(stockPriceMovementList.stream().filter(stockPriceMovement -> nseBse500.getTicker().equals(stockPriceMovement.getTicker())).findAny().orElse(null));

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
     *
     * @param ticker
     * @return
     */
    public static List<StockPriceMovementHistory> getPriceMovements(String ticker) {
        List<StockPriceMovementHistory> stockPriceMovementHistoryList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate oneYearOld = now.minusDays(365);

        stockPriceMovementHistoryList = CommonService.stockPriceMovementHistoryRepository.findAllByKeyTickerAndKeyDateGreaterThanOrderByKeyDateAsc(ticker,java.sql.Date.valueOf(oneYearOld));

        return stockPriceMovementHistoryList;
    }

    /**
     *
     * @param ticker
     * @return
     */
    public static List<RecentValuations> getRecentValuations(String ticker) {
        List<RecentValuations> recentPES = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate oneAndhHalYearOld = now.minusMonths(18);
        NseBse500 stockDetails = getStockDetails(ticker);

        List<java.sql.Date> resultDates = CommonService.stockQuarterRepository.findDistinctResultDateForTicker(ticker, java.sql.Date.valueOf(oneAndhHalYearOld));

        List<DailyDataS> dailyDataSList = CommonService.dailyDataSRepository.findAllByKeyNameAndKeyDateGreaterThanOrderByKeyDateAsc(stockDetails.getTicker5(),java.sql.Date.valueOf(oneAndhHalYearOld));
        //List<DailyDataB> dailyDataBList = CommonService.dailyDataBRepository.findAllByKeyTickerBAndKeyDateGreaterThanOrderByKeyDateAsc(stockDetails.getTicker2(),java.sql.Date.valueOf(oneYearOld));
        for(DailyDataS dailyDataS: dailyDataSList){
            RecentValuations recentPE = new RecentValuations();
            recentPE.setTicker(ticker);
            recentPE.setDate(dailyDataS.getKey().getDate());
            recentPE.setPe(dailyDataS.getPeTtm().setScale(2, RoundingMode.HALF_UP));
            recentPE.setPb(dailyDataS.getPbTtm().setScale(2, RoundingMode.HALF_UP));
            recentPE.setEvToEbita(dailyDataS.getEvToEbit().setScale(2, RoundingMode.HALF_UP));
            recentPE.setMarketCap(dailyDataS.getMarketCap().setScale(0, RoundingMode.HALF_UP));
            recentPE.setMarketPrice(dailyDataS.getCmp().setScale(0, RoundingMode.HALF_UP));
            int index = Collections.binarySearch(resultDates, dailyDataS.getKey().getDate());
            if (index >= 0) {
                recentPE.setResultDateMCap(dailyDataS.getMarketCap().setScale(0, RoundingMode.HALF_UP));
            } else {
                recentPE.setResultDateMCap(new BigDecimal(0));
            }
            recentPES.add(recentPE);
        }
        // set PB from bloomberg data
        // commenting following code since bloomberg data is now not available
        /*for(DailyDataB dailyDataB: dailyDataBList){
            List<RecentValuations> recentPES1 = recentPES.stream()
                    .filter( recentPE -> recentPE.getDate().equals(dailyDataB.getKey().getDate()) )
                    .collect(Collectors.toList());
            if (recentPES1.size() > 0) {
                RecentValuations recentPE = recentPES1.get(0);
                if (dailyDataB.getPriceBook().floatValue() > 0) {
                    recentPE.setPb(dailyDataB.getPriceBook());
                }
                if (dailyDataB.getCurrentPe().floatValue() > 0) {
                    recentPE.setPe(dailyDataB.getCurrentPe());
                }
            }
        }*/
        return recentPES;
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

    /**
     * Get Benchmark Returns
     */
    public static List<BenchmarkTwrrMonthlyDTO> getBenchmarkTwrrMonthly() {
        List<Object[]> objects;
        List<BenchmarkTwrrMonthlyDTO> dtos = new ArrayList<>();
        objects = CommonService.benchmarkTwrrMonthlyRepository.findAllBenchmarks();
        for (Object[] object : objects) {
            BenchmarkTwrrMonthlyDTO dto = new BenchmarkTwrrMonthlyDTO();
            dto.setBenchmarkType(""+object[0]);
            dto.setBenchmarkName(""+object[1]);
            dto.setBenchmarkid(Long.valueOf(""+object[2]));
            dto.setYear(Integer.valueOf(""+object[3]));
            try {
                dto.setReturnsCalendarYear(BigDecimal.valueOf(Double.valueOf(""+object[4])));
            } catch (Exception e){
                dto.setReturnsCalendarYear(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsFinYear(BigDecimal.valueOf(Double.valueOf("" + object[5])));
            }catch (Exception e){
                dto.setReturnsFinYear(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsMarEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[6])));
            } catch (Exception e) {
                dto.setReturnsMarEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJunEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[7])));
            } catch (Exception e) {
                dto.setReturnsJunEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsSepEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[8])));
            } catch (Exception e) {
                dto.setReturnsSepEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsDecEndingQuarter(BigDecimal.valueOf(Double.valueOf(""+object[9])));
            } catch (Exception e) {
                dto.setReturnsDecEndingQuarter(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJan(BigDecimal.valueOf(Double.valueOf(""+object[10])));
            } catch (Exception e) {
                dto.setReturnsJan(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsFeb(BigDecimal.valueOf(Double.valueOf(""+object[11])));
            } catch (Exception e) {
                dto.setReturnsFeb(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsMar(BigDecimal.valueOf(Double.valueOf(""+object[12])));
            } catch (Exception e) {
                dto.setReturnsMar(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsApr(BigDecimal.valueOf(Double.valueOf(""+object[13])));
            } catch (Exception e) {
                dto.setReturnsApr(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsMay(BigDecimal.valueOf(Double.valueOf(""+object[14])));
            } catch (Exception e) {
                dto.setReturnsMay(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJun(BigDecimal.valueOf(Double.valueOf(""+object[15])));
            } catch (Exception e) {
                dto.setReturnsJun(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsJul(BigDecimal.valueOf(Double.valueOf(""+object[16])));
            } catch (Exception e) {
                dto.setReturnsJul(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsAug(BigDecimal.valueOf(Double.valueOf(""+object[17])));
            } catch (Exception e) {
                dto.setReturnsAug(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsSep(BigDecimal.valueOf(Double.valueOf(""+object[18])));
            } catch (Exception e) {
                dto.setReturnsSep(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsOct(BigDecimal.valueOf(Double.valueOf(""+object[19])));
            } catch (Exception e) {
                dto.setReturnsOct(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsNov(BigDecimal.valueOf(Double.valueOf(""+object[20])));
            } catch (Exception e) {
                dto.setReturnsNov(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsDec(BigDecimal.valueOf(Double.valueOf(""+object[21])));
            } catch (Exception e) {
                dto.setReturnsDec(BigDecimal.valueOf(0));
            }
            dtos.add(dto);
        }
        return dtos;

    }
    public static List<BenchmarkTwrrSummaryDTO> getBenchmarkTwrrSummary() {
        List<Object[]> objects;
        List<BenchmarkTwrrSummaryDTO> dtos = new ArrayList<>();
        objects = CommonService.benchmarkTwrrSummaryRepository.findAllBenchmarks();
        for (Object[] object : objects) {
            BenchmarkTwrrSummaryDTO dto = new BenchmarkTwrrSummaryDTO();
            dto.setBenchmarkType(""+object[0]);
            dto.setBenchmarkName(""+object[1]);
            dto.setBenchmarkid(Long.valueOf(""+object[2]));
            dto.setReturnsDate(java.sql.Date.valueOf(""+object[3]));
            dto.setReturnsTwrrSinceCurrentMonth(BigDecimal.valueOf(Double.valueOf(""+object[4])));
            dto.setReturnsTwrrSinceCurrentQuarter(BigDecimal.valueOf(Double.valueOf(""+object[5])));
            dto.setReturnsTwrrSinceFinYear(BigDecimal.valueOf(Double.valueOf(""+object[6])));
            dto.setReturnsTwrrYtd(BigDecimal.valueOf(Double.valueOf(""+object[7])));
            dto.setReturnsTwrrThreeMonths(BigDecimal.valueOf(Double.valueOf(""+object[8])));
            dto.setReturnsTwrrHalfYear(BigDecimal.valueOf(Double.valueOf(""+object[9])));
            dto.setReturnsTwrrOneYear(BigDecimal.valueOf(Double.valueOf(""+object[10])));
            dto.setReturnsTwrrTwoYear(BigDecimal.valueOf(Double.valueOf(""+object[11])));
            try {
                dto.setReturnsTwrrThreeYear(BigDecimal.valueOf(Double.valueOf("" + object[12])));
            } catch (Exception e){
                dto.setReturnsTwrrThreeYear(BigDecimal.valueOf(0));
            }
            try {
                dto.setReturnsTwrrFiveYear(BigDecimal.valueOf(Double.valueOf(""+object[13])));
            } catch (Exception e) {
                dto.setReturnsTwrrFiveYear(BigDecimal.valueOf(0));
            }

            try {
                dto.setReturnsTwrrTenYear(BigDecimal.valueOf(Double.valueOf(""+object[14])));
            } catch (Exception e){
                dto.setReturnsTwrrTenYear(BigDecimal.valueOf(0));
            }
            dto.setReturnsTwrrSinceInception(BigDecimal.valueOf(0));

            dtos.add(dto);
        }
        return dtos;
    }
}
