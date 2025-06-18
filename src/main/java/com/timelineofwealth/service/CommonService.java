package com.timelineofwealth.service;

import com.timelineofwealth.dto.*;
import com.timelineofwealth.dto.IndexMonthlyReturnsDTO;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.repositories.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.DateFormatConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.cache.annotation.Cacheable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

@Service("CommonService")
@EnableCaching
public class CommonService {

    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String ADVISER_ROLE = "ROLE_ADVISER";
    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);
    private static final String BASE_EXCEL_PATH = "C:/MyDocuments/03Business/05ResearchAndAnalysis/StockInvestments/QuarterResultsScreenerExcels/";

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
    private static StockAnalystRecoRepository stockAnalystRecoRepository;
    @Autowired
    public void setStockAnalystRecoRepository(StockAnalystRecoRepository stockAnalystRecoRepository){
        CommonService.stockAnalystRecoRepository = stockAnalystRecoRepository;
    }

    @Autowired
    private static StockValuationRepository stockValuationRepository;
    @Autowired
    public void setStockAnalystRecoRepository(StockValuationRepository stockValuationRepository){
        CommonService.stockValuationRepository = stockValuationRepository;
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

    @Autowired
    public static JdbcTemplate jdbcTemplate;
    @Autowired
    public void setJdbcTemplate(JdbcTemplate template) {
        CommonService.jdbcTemplate = template;
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
            nseBse500.setLatestMOSLReco(getLatestFourRecommendations(stockUniverse.getTicker(), "MOSL", stockUniverse.getLatestPrice().doubleValue()));
            nseBse500.setLatestAMBITReco(getLatestFourRecommendations(stockUniverse.getTicker(), "AMBIT", stockUniverse.getLatestPrice().doubleValue()));
            nseBse500.setLatestAXISReco(getLatestFourRecommendations(stockUniverse.getTicker(), "AXIS", stockUniverse.getLatestPrice().doubleValue()));
            nseBse500.setLatestICICIDIRECTReco(getLatestFourRecommendations(stockUniverse.getTicker(), "ICICIDIRECT", stockUniverse.getLatestPrice().doubleValue()));
            nseBse500.setLatestPLReco(getLatestFourRecommendations(stockUniverse.getTicker(), "PL", stockUniverse.getLatestPrice().doubleValue()));
            nseBse500.setLatestKOTAKReco(getLatestFourRecommendations(stockUniverse.getTicker(), "KOTAK", stockUniverse.getLatestPrice().doubleValue()));

            getLatestFourValuation(stockUniverse.getTicker(),stockUniverse.getLatestPrice().doubleValue(), nseBse500);

            nseBse500List.add(nseBse500);
        }
        return nseBse500List;
    }

    private static String getLatestFourRecommendations(String ticker, String broker, double cmp) {
        // Get the latest quarter across all records
        String latestQuarter = stockAnalystRecoRepository.findMaxKeyQuarter();

        // Build a list of the four quarters to query for
        List<String> quartersToQuery = new ArrayList<>();
        quartersToQuery.add(latestQuarter);
        int currentYear = Integer.parseInt(latestQuarter.substring(2, 4));
        int currentQuarter = Integer.parseInt(latestQuarter.substring(5));
        for (int i = 1; i <= 3; i++) {
            currentQuarter--;
            if (currentQuarter == 0) {
                currentYear--;
                currentQuarter = 4;
            }
            quartersToQuery.add(String.format("FY%dQ%d", currentYear, currentQuarter));
        }

        // Query the database for the four quarters for the ticker and broker
        List<StockAnalystReco> recoList = stockAnalystRecoRepository
                .findAllByKeyTickerAndKeyBrokerAndKeyQuarterIn(ticker, broker, quartersToQuery);

        // Build the result string with the latest four recommendations
        String result = "";
        for (int i = 0; i < 4; i++) {
            String quarter = quartersToQuery.get(i);
            StockAnalystReco reco = null;
            for (StockAnalystReco r : recoList) {
                if (r.getKey().getQuarter().equals(quarter)) {
                    reco = r;
                    break;
                }
            }
            if (reco != null) {
                String recoStr = reco.getReco();
                BigDecimal target = reco.getTarget();
                BigDecimal growth = reco.getSalesGrowth();
                DecimalFormat df = new DecimalFormat("#,##0");
                String targetStr = "-";
                if (target != null) {
                    targetStr = df.format(target.intValue());
                }
                if (target != null && target.doubleValue() > 0 && cmp > 0) {
                    result += quarter + "-" + recoStr + "/" + targetStr + "(" + df.format(((target.doubleValue()/cmp)-1)*100)+ "%)" + "/g%~" + df.format(growth.doubleValue()*100) + "%";
                } else
                    result += quarter + "-" + recoStr + "/" + targetStr + "/g%~" + df.format(growth.doubleValue()*100) + "%";
            } else {
                result += quarter + "- ";
            }
            if (i < 3) {
                result += " ";
            }
        }

        return result;
    }

    private static void getLatestFourValuation(String ticker, double cmp, NseBse500 nseBse500) {
        // Get the latest quarter across all records
        String latestQuarter = stockValuationRepository.findMaxKeyQuarter();

        // Build a list of the four quarters to query for
        List<String> quartersToQuery = new ArrayList<>();
        quartersToQuery.add(latestQuarter);
        int currentYear = Integer.parseInt(latestQuarter.substring(2, 4));
        int currentQuarter = Integer.parseInt(latestQuarter.substring(5));
        for (int i = 1; i <= 3; i++) {
            currentQuarter--;
            if (currentQuarter == 0) {
                currentYear--;
                currentQuarter = 4;
            }
            quartersToQuery.add(String.format("FY%dQ%d", currentYear, currentQuarter));
        }

        // Query the database for the four quarters for the ticker and broker
        List<StockValuation> recoList = stockValuationRepository
                .findAllByKeyTickerAndKeyQuarterIn(ticker, quartersToQuery);

        // Build the result string with the latest four recommendations
        String resultValuaiton = "";
        String minValuation = "";
        String maxValuation = "";
        String wacc = "";
        String taxRate = "";
        String revenueGrowthNext10yr = "";
        String opmNext10yr = "";
        String netPpeByRevenue10yr = "";
        String depreciationByNetPpe10yr = "";
        String historicalRoic = "";
        String secondAndTerminalStageAssumptions = "";
        String otherIncGrowthAssumptions = "";

        for (int i = 0; i < 4; i++) {
            String quarter = quartersToQuery.get(i);
            StockValuation valuation = null;
            for (StockValuation r : recoList) {
                if (r.getKey().getQuarter().equals(quarter)) {
                    valuation = r;
                    break;
                }
            }
            if (valuation != null) {
                DecimalFormat df = new DecimalFormat("#,##0");
                DecimalFormat pf = new DecimalFormat("#,##0.0");

                if (valuation.getPrice() != null && valuation.getPrice().doubleValue() > 0 && cmp > 0) {

                    String mcap = df.format(valuation.getMcap().doubleValue());
                    String price = df.format(valuation.getPrice().doubleValue());

                    resultValuaiton += quarter + "-" + mcap + "/" + price + "(" + df.format(((cmp/valuation.getPrice().doubleValue())-1)*100)+ "%)";

                } else {
                    resultValuaiton += quarter + "-";
                }

                if (valuation.getMinFairPrice() != null && valuation.getMinFairPrice().doubleValue() > 0 && cmp > 0) {
                    String minMcap = df.format(valuation.getMinMcap().doubleValue());
                    String minPrice = df.format(valuation.getMinFairPrice().doubleValue());

                    String maxMcap = df.format(valuation.getMaxMcap().doubleValue());
                    String maxPrice = df.format(valuation.getMaxFairPrice().doubleValue());

                    minValuation += quarter + "-" + minMcap + "/" + minPrice + "(" + df.format(((cmp/valuation.getMinFairPrice().doubleValue())-1)*100)+ "%)" + "/"+df.format(valuation.getMinRevenueGrowthNext10yr().doubleValue()*100)+"%";
                    maxValuation += quarter + "-" + maxMcap + "/" + maxPrice + "(" + df.format(((valuation.getMaxFairPrice().doubleValue()/cmp)-1)*100)+ "%)" + "/"+df.format(valuation.getMaxRevenueGrowthNext10yr().doubleValue()*100)+"%";

                } else {
                    minValuation += quarter + "-";
                    maxValuation += quarter + "-";
                }

                if (valuation.getWacc() != null) {
                    wacc += quarter + "-" + pf.format(valuation.getWacc().doubleValue()*100)+ "%";
                } else {
                    wacc += quarter + "-";
                }

                if (valuation.getTaxRate() != null) {
                    taxRate += quarter + "-" + df.format(valuation.getTaxRate().doubleValue()*100)+ "%";
                } else {
                    taxRate += quarter + "-";
                }

                if (valuation.getRevenueGrowthNext10yr() != null) {
                    revenueGrowthNext10yr += quarter + "-" + df.format(valuation.getRevenueGrowthNext10yr().doubleValue()*100)+ "%";
                } else {
                    revenueGrowthNext10yr += quarter + "-";
                }

                if (valuation.getOpmNext10yr() != null) {
                    opmNext10yr += quarter + "-" + df.format(valuation.getOpmNext10yr().doubleValue()*100)+ "%";
                } else {
                    opmNext10yr += quarter + "-";
                }

                if (valuation.getNetPpeByRevenue10yr() != null) {
                    netPpeByRevenue10yr += quarter + "-" + df.format(valuation.getNetPpeByRevenue10yr().doubleValue()*100)+ "%";
                } else {
                    netPpeByRevenue10yr += quarter + "-";
                }

                if (valuation.getDepreciationByNetPpe10yr() != null) {
                    depreciationByNetPpe10yr += quarter + "-" + df.format(valuation.getDepreciationByNetPpe10yr().doubleValue()*100)+ "%";
                } else {
                    depreciationByNetPpe10yr += quarter + "-";
                }

                if (valuation.getHistoricalRoic() != null) {
                    historicalRoic += quarter + "-" + df.format(valuation.getHistoricalRoic().doubleValue()*100)+ "%";
                } else {
                    historicalRoic += quarter + "-";
                }

                if (valuation.getRoicSecondStage() != null) {
                    secondAndTerminalStageAssumptions += quarter + "-" + "g% ~ " + df.format(valuation.getRevenueGrowthSecondStage().doubleValue()*100) + "%@" + df.format(valuation.getNextStageGrowthPeriod()) + "Yrs/" + df.format(valuation.getTerminalGrowth().doubleValue()*100) + "%; RoNIC ~ " + df.format(valuation.getRoicSecondStage().doubleValue()*100) + "%/" + df.format(valuation.getTerminalRoic().doubleValue()*100)+"%";
                } else {
                    secondAndTerminalStageAssumptions += quarter + "-";
                }

                if (valuation.getOtherIncGrowthNext10yr() != null) {
                    otherIncGrowthAssumptions += quarter + "-" + df.format(valuation.getOtherIncGrowthNext10yr().doubleValue()*100)+ "%@" + df.format(valuation.getOtherIncGrowthPeriod().doubleValue()) + "Yrs then " + df.format(valuation.getOtherIncTerminalGrowth().doubleValue()*100) + "%";
                } else {
                    otherIncGrowthAssumptions += quarter + "-";
                }

            } else {
                resultValuaiton += quarter + "- ";
                minValuation += quarter + "- ";
                maxValuation += quarter + "- ";
                wacc += quarter + "- ";
                taxRate += quarter + "- ";
                revenueGrowthNext10yr += quarter + "- ";
                opmNext10yr += quarter + "- ";
                netPpeByRevenue10yr += quarter + "- ";
                depreciationByNetPpe10yr += quarter + "- ";
                historicalRoic += quarter + "- ";
                secondAndTerminalStageAssumptions += quarter + "- ";
                otherIncGrowthAssumptions += quarter + "- ";
            }
            if (i < 3) {
                resultValuaiton += " ";
                minValuation += " ";
                maxValuation += " ";
                wacc += " ";
                taxRate += " ";
                revenueGrowthNext10yr += " ";
                opmNext10yr += " ";
                netPpeByRevenue10yr += " ";
                depreciationByNetPpe10yr += " ";
                historicalRoic += " ";
                secondAndTerminalStageAssumptions += " ";
                otherIncGrowthAssumptions += " ";
            }
        }

        nseBse500.setResultValuation(resultValuaiton);
        nseBse500.setMinValuation(minValuation);
        nseBse500.setMaxValuation(maxValuation);
        nseBse500.setWacc(wacc);
        nseBse500.setTaxRate(taxRate);
        nseBse500.setRevenueGrowthNext10yr(revenueGrowthNext10yr);
        nseBse500.setOpmNext10yr(opmNext10yr);
        nseBse500.setNetPpeByRevenue10yr(netPpeByRevenue10yr);
        nseBse500.setDepreciationByNetPpe10yr(depreciationByNetPpe10yr);
        nseBse500.setHistoricalRoic(historicalRoic);
        nseBse500.setSecondAndTerminalStageAssumptions(secondAndTerminalStageAssumptions);
        nseBse500.setOtherIncGrowthAssumptions(otherIncGrowthAssumptions);
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
        NseBse500 dto;
        try {
            dto = nseBse500List.stream()
                    .filter(stock -> stock.getTicker().equals(ticker))
                    .collect(Collectors.toList())
                    .get(0);
        } catch (Exception e) {
            System.out.println("Stock " + ticker + " Not in a NSE500 or BSE500 ");
            StockUniverse stock = CommonService.stockUniverseRepository.findByTicker(ticker);
            dto = new NseBse500(stock);
        }

        // === ADD THIS LINE ===
        dto.setExcelCharts(loadExcelChartsForTicker(ticker));
        dto.setReportNotes(loadReportNotesForTicker(ticker));
        return dto;
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
            //history.setPb(stockPnl.getCmp());
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
        LocalDate tenQuarterBefore = now.minusMonths(30);
        NseBse500 stockDetails = getStockDetails(ticker);

        List<java.sql.Date> resultDates = CommonService.stockQuarterRepository.findDistinctResultDateForTicker(ticker, java.sql.Date.valueOf(tenQuarterBefore));

        List<DailyDataS> dailyDataSList = CommonService.dailyDataSRepository.findAllByKeyNameAndKeyDateGreaterThanOrderByKeyDateAsc(stockDetails.getTicker(),java.sql.Date.valueOf(tenQuarterBefore));
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
     * Get monthly returns of the Index
     * @param ticker
     * @return
     */
    @Cacheable(value = "IndexMonthlyReturnsDTO")
    public static List<IndexMonthlyReturnsDTO> getIndexMonthlyReturns(String ticker){
        return IndexService.getMonthlyReturns(ticker);
    }

    /**
     * Get Index Statistics
     * @return
     */
    public static List<IndexStatistics> getIndexReturnStatistics(String index){
        if (index.equalsIgnoreCase("NIFTY")) {
            return CommonService.indexStatisticsRepository.findOneByTicker("NIFTY");
        } else {
            List<IndexStatistics> indexStatistics = CommonService.indexStatisticsRepository.findOneByTicker("NIFTY");
            indexStatistics.addAll(CommonService.indexStatisticsRepository.findOneByTicker("BSEMidCap"));
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

    private static List<CustomChartData> loadExcelChartsForTicker(String ticker) {
        List<CustomChartData> chartDataList = new ArrayList<>();
        try {
            Map<String, Map<String, String>> chartProperty = loadSectionedProperties(BASE_EXCEL_PATH + "ChartForSheet&Row.property");

            Properties tickerCombination = new Properties();
            tickerCombination.load(new FileInputStream(BASE_EXCEL_PATH + "TickerChartCombination.property"));

            File excelFile = getLatestExcelFileForTicker(ticker);
            if (excelFile == null) return chartDataList;

            Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFile));

            // Read active combinations for this ticker
            String combos = tickerCombination.getProperty(ticker);
            if (combos == null) {
                combos = tickerCombination.getProperty("default");
            }

            // Expand [1-36], [101-149] into a list of integers
            List<Integer> activeCombinations = new ArrayList<>();
            String[] parts = combos.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("[") && part.endsWith("]")) {
                    String[] range = part.substring(1, part.length() - 1).split("-");
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);
                    for (int i = start; i <= end; i++) {
                        activeCombinations.add(i);
                    }
                } else {
                    activeCombinations.add(Integer.parseInt(part));
                }
            }

            // Process each chart combination
            for (Integer comboId : activeCombinations) {
                String section = String.valueOf(comboId);

                if (!chartProperty.containsKey(section)) continue;
                Map<String, String> sectionProps = chartProperty.get(section);

                String sheetName = sectionProps.get("sheet_name");
                int dateRow = Integer.parseInt(sectionProps.get("date_row"));
                int paramRow = Integer.parseInt(sectionProps.get("parameter_row"));
                String startCol = sectionProps.get("date_column_start");
                String endCol = sectionProps.get("date_column_end");
                String title = sectionProps.get("name");

                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) continue;

                Row dateRowObj = sheet.getRow(dateRow - 1);
                Row paramRowObj = sheet.getRow(paramRow - 1);

                String fieldName = "";
                Cell fieldCell = paramRowObj.getCell(0); // Column A is index 0
                if (fieldCell != null) {
                    if (fieldCell.getCellType() == Cell.CELL_TYPE_STRING) {
                        fieldName = fieldCell.getStringCellValue();
                    } else if (fieldCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        fieldName = String.valueOf(fieldCell.getNumericCellValue());
                    }
                }

                int startIdx = CellReference.convertColStringToIndex(startCol);
                int endIdx = CellReference.convertColStringToIndex(endCol);

                List<String> dates = new ArrayList<>();
                List<Double> values = new ArrayList<>();

                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

                for (int col = startIdx; col <= endIdx; col++) {
                    Cell dateCell = dateRowObj.getCell(col);
                    Cell valueCell = paramRowObj.getCell(col);
                    if (dateCell == null || valueCell == null) continue;

                    // === Evaluate Date Cell ===
                    CellValue evaluatedDate = evaluator.evaluate(dateCell);
                    if (evaluatedDate != null) {
                        if (evaluatedDate.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isValidExcelDate(evaluatedDate.getNumberValue())) {
                            dates.add(new DataFormatter().formatRawCellContents(
                                    evaluatedDate.getNumberValue(),
                                    -1,
                                    DateFormatConverter.convert(Locale.ENGLISH, "yyyy-MM-dd")
                            ));
                        } else if (evaluatedDate.getCellType() == Cell.CELL_TYPE_STRING) {
                            dates.add(evaluatedDate.getStringValue());
                        } else {
                            continue;
                        }
                    }

                    // === Evaluate Value Cell ===
                    CellValue evaluatedValue = evaluator.evaluate(valueCell);
                    if (evaluatedValue != null && evaluatedValue.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        values.add(evaluatedValue.getNumberValue());
                    } else {
                        values.add(null);
                    }
                }

                CustomChartData chartData = new CustomChartData();
                chartData.setCombinationId(comboId);
                chartData.setTitle(title);
                chartData.setLabels(dates);
                chartData.setValues(values);
                chartData.setFieldName(fieldName);
                chartDataList.add(chartData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return chartDataList;
    }

    private static File getLatestExcelFileForTicker(String ticker) {
        List<String> quarters = getAvailableQuarters(BASE_EXCEL_PATH);
        for (String quarter : quarters) {
            String filePath = BASE_EXCEL_PATH + quarter + "/" + ticker + "_FY" + quarter.substring(2) + ".xlsx";
            File file = new File(filePath);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private static List<String> getAvailableQuarters(String basePath) {
        File baseDir = new File(basePath);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = baseDir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        List<String> quarters = Arrays.stream(files)
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> name.matches("\\d{4}Q[1-4]")) // Match 2025Q1, 2025Q2 etc.
                .sorted(Comparator.reverseOrder()) // Latest first
                .collect(Collectors.toList());

        return quarters;
    }

    private static Map<String, Map<String, String>> loadSectionedProperties(String filePath) {
        Map<String, Map<String, String>> sectionedProps = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentSection = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // skip blank lines and comments
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    sectionedProps.putIfAbsent(currentSection, new HashMap<>());
                } else if (currentSection != null && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    sectionedProps.get(currentSection).put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sectionedProps;
    }

    public static List<ReportNotes> loadReportNotesForTicker(String ticker) {
        List<ReportNotes> notesList = new ArrayList<>();
        try {
            File excelFile = getLatestExcelFileForTicker(ticker);
            if (excelFile == null) return notesList;

            Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFile));
            Sheet sheet = workbook.getSheet("Reports");
            if (sheet == null) return notesList;

            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ReportNotes note = new ReportNotes();
                note.setTicker(formatter.formatCellValue(row.getCell(0)));
                note.setDate(formatter.formatCellValue(row.getCell(1)));
                note.setDocumentSource(formatter.formatCellValue(row.getCell(2)));
                note.setDocumentSection(formatter.formatCellValue(row.getCell(3)));
                note.setInfoCategory(formatter.formatCellValue(row.getCell(4)));
                note.setInfoSubCategory(formatter.formatCellValue(row.getCell(5)));
                note.setInformation(formatter.formatCellValue(row.getCell(6)));

                notesList.add(note);
            }
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notesList;
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toString();
            } else {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            try {
                return cell.getStringCellValue().trim();
            } catch (Exception e) {
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception ex) {
                    return "";
                }
            }
        }
        return "";
    }

    public static List<Map<String, Object>> getPriceSeries(String ticker, String rangeOrDate) {
        List<Object> params = new ArrayList<>();
        String sql;

        boolean isIndex = ticker.equalsIgnoreCase("NIFTY") || ticker.equalsIgnoreCase("BSE100") || ticker.equalsIgnoreCase("BSEMidCap");

        if (isIndex) {
            sql = "SELECT date, value as price FROM index_valuation WHERE ticker = ? ";
        } else {
            sql = "SELECT date, close_price as price, total_traded_quantity as volume, total_traded_value as value "
                    + "FROM nse_price_history WHERE nse_ticker = ? ";
        }

        params.add(ticker);

        if (rangeOrDate != null && !"ALL".equalsIgnoreCase(rangeOrDate)) {
            if (rangeOrDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sql += " AND date >= ?";
                params.add(java.sql.Date.valueOf(rangeOrDate));
            } else {
                String interval = null;
                switch (rangeOrDate.toUpperCase()) {
                    case "30D": interval = "INTERVAL 30 DAY"; break;
                    case "90D": interval = "INTERVAL 90 DAY"; break;
                    case "1Y":  interval = "INTERVAL 1 YEAR"; break;
                    case "3Y":  interval = "INTERVAL 3 YEAR"; break;
                    case "5Y":  interval = "INTERVAL 5 YEAR"; break;
                }
                if (interval != null) {
                    sql += " AND date >= CURDATE() - " + interval;
                }
            }
        }

        sql += " ORDER BY date";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", rs.getDate("date").toString());
            map.put("price", rs.getDouble("price"));
            if (!isIndex) {
                map.put("volume", rs.getDouble("volume"));
                map.put("value", rs.getDouble("value"));
            }
            return map;
        });
    }

    public static List<Map<String, Object>> getPriceSeries(String ticker, String from, String to) {
        List<Object> params = new ArrayList<>();
        String sql;

        boolean isIndex = ticker.equalsIgnoreCase("NIFTY") || ticker.equalsIgnoreCase("BSE100") || ticker.equalsIgnoreCase("BSEMidCap");

        if (isIndex) {
            sql = "SELECT date, value as price FROM index_valuation WHERE ticker = ?";
        } else {
            sql = "SELECT date, close_price as price, total_traded_quantity as volume, total_traded_value as value "
                    + "FROM nse_price_history WHERE nse_ticker = ?";
        }

        params.add(ticker);

        if (from != null && !from.isEmpty()) {
            sql += " AND date >= ?";
            params.add(java.sql.Date.valueOf(from));
        }

        if (to != null && !to.isEmpty()) {
            sql += " AND date <= ?";
            params.add(java.sql.Date.valueOf(to));
        }

        sql += " ORDER BY date";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", rs.getDate("date").toString());
            map.put("price", rs.getDouble("price"));
            if (!isIndex) {
                map.put("volume", rs.getDouble("volume"));
                map.put("value", rs.getDouble("value"));
            }
            return map;
        });
    }

    public static List<Map<String, Object>> getMarketCapSeries(String ticker, String rangeOrDate, String from, String to) {
        String sql = "SELECT date, market_cap as marketcap, pb_ttm, ev_to_ebit FROM daily_data_s WHERE name = ?";
        List<Object> params = new ArrayList<>();
        params.add(ticker);

        // Case 1: If 'custom' is selected, apply from/to directly
        if ("custom".equalsIgnoreCase(rangeOrDate)) {
            if (from != null && !from.isEmpty()) {
                sql += " AND date >= ?";
                params.add(java.sql.Date.valueOf(from));
            }
            if (to != null && !to.isEmpty()) {
                sql += " AND date <= ?";
                params.add(java.sql.Date.valueOf(to));
            }
        }
        // Case 2: If a standard range is selected (30D, 1Y, etc.)
        else if (!"ALL".equalsIgnoreCase(rangeOrDate)) {
            if (rangeOrDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sql += " AND date >= ?";
                params.add(java.sql.Date.valueOf(rangeOrDate));
            } else {
                String interval = null;
                switch (rangeOrDate.toUpperCase()) {
                    case "30D": interval = "INTERVAL 30 DAY"; break;
                    case "90D": interval = "INTERVAL 90 DAY"; break;
                    case "1Y":  interval = "INTERVAL 1 YEAR"; break;
                    case "3Y":  interval = "INTERVAL 3 YEAR"; break;
                    case "5Y":  interval = "INTERVAL 5 YEAR"; break;
                }
                if (interval != null) {
                    sql += " AND date >= CURDATE() - " + interval;
                }
            }
        }

        sql += " ORDER BY date";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", rs.getDate("date").toString());
            m.put("marketcap", rs.getDouble("marketcap"));
            m.put("ttmpb", rs.getDouble("pb_ttm"));
            m.put("evtoebit", rs.getDouble("ev_to_ebit"));
            return m;
        });
    }

    public static List<Map<String, Object>> computeIndexTtmPeSeries(String indexTicker, String from, String to) {
        return computeIndexTtmPeSeriesInternal(indexTicker, "custom", from, to);
    }

    public static List<Map<String, Object>> computeIndexTtmPeSeries(String indexTicker, String range) {
        return computeIndexTtmPeSeriesInternal(indexTicker, range, null, null);
    }

    public static List<Map<String, Object>> computeIndexTtmPeSeriesInternal(String ticker, String rangeOrDate, String from, String to) {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = "SELECT date, value, pe, implied_earnings FROM index_valuation WHERE ticker = ? ";
        List<Object> params = new ArrayList<>();
        params.add(ticker);

        if ("custom".equalsIgnoreCase(rangeOrDate)) {
            if (from != null) {
                sql += " AND date >= ?";
                params.add(java.sql.Date.valueOf(from));
            }
            if (to != null) {
                sql += " AND date <= ?";
                params.add(java.sql.Date.valueOf(to));
            }
        } else if (!"ALL".equalsIgnoreCase(rangeOrDate)) {
            if (rangeOrDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sql += " AND date >= ?";
                params.add(java.sql.Date.valueOf(rangeOrDate));
            } else {
                String interval = null;
                if ("30D".equalsIgnoreCase(rangeOrDate)) interval = "INTERVAL 30 DAY";
                else if ("90D".equalsIgnoreCase(rangeOrDate)) interval = "INTERVAL 90 DAY";
                else if ("1Y".equalsIgnoreCase(rangeOrDate)) interval = "INTERVAL 1 YEAR";
                else if ("3Y".equalsIgnoreCase(rangeOrDate)) interval = "INTERVAL 3 YEAR";
                else if ("5Y".equalsIgnoreCase(rangeOrDate)) interval = "INTERVAL 5 YEAR";

                if (interval != null) {
                    sql += " AND date >= CURDATE() - " + interval;
                }
            }
        }

        sql += " ORDER BY date";

        // Containers
        List<java.sql.Date> dateList = new ArrayList<>();
        List<Double> valueList = new ArrayList<>();
        List<Double> impliedList = new ArrayList<>();
        List<Double> peList = new ArrayList<>();

        jdbcTemplate.query(sql, params.toArray(), rs -> {
            dateList.add(rs.getDate("date"));
            valueList.add(rs.getDouble("value"));
            impliedList.add(rs.getDouble("implied_earnings"));
            peList.add(rs.getDouble("pe"));
        });

        for (int i = 0; i < dateList.size(); i++) {
            java.sql.Date date = dateList.get(i);
            double price = valueList.get(i);
            double ttmPe = peList.get(i);

            // === Forward EPS lookup
            java.sql.Date fwdDate = new java.sql.Date(date.toLocalDate().plusDays(365).toEpochDay() * 86400000L);
            Double fwdEps = null;

            for (int j = i; j < dateList.size(); j++) {
                if (!dateList.get(j).before(fwdDate)) {
                    fwdEps = impliedList.get(j);
                    break;
                }
            }

            Map<String, Object> row = new HashMap<>();
            row.put("date", date.toString());
            row.put("pe", Math.round(ttmPe * 100.0) / 100.0);

            if (fwdEps != null && fwdEps > 0) {
                double fwdPe = price / fwdEps;
                row.put("forward_pe", Math.round(fwdPe * 100.0) / 100.0);
            }

            result.add(row);
        }

        return result;
    }

    public static List<Map<String, Object>> computeStockTtmPeSeries(String ticker, String from, String to) {
        return computeStockTtmPeSeriesInternal(ticker, "custom", from, to);
    }

    public static List<Map<String, Object>> computeStockTtmPeSeries(String ticker, String range) {
        return computeStockTtmPeSeriesInternal(ticker, range, null, null);
    }

    private static List<Map<String, Object>> computeStockTtmPeSeriesInternal(String ticker, String rangeOrDate, String from, String to) {
        List<Map<String, Object>> output = new ArrayList<>();

        try {
            File excelFile = getLatestExcelFileForTicker(ticker);
            if (excelFile == null || !excelFile.exists()) return output;

            try (FileInputStream fis = new FileInputStream(excelFile)) {
                Workbook workbook = WorkbookFactory.create(fis);
                Map<java.sql.Date, Double> sharesByDate = extractSharesFromAnnualSheet(workbook);
                List<ResultRow> quarterlyResults = extractQuarterlyResults(workbook);

                quarterlyResults.sort(Comparator.comparing(ResultRow::getResultEndDate));
                for (ResultRow row : quarterlyResults) {
                    java.sql.Date resultDate = row.getResultEndDate();
                    java.sql.Date latest = null;
                    for (java.sql.Date d : sharesByDate.keySet()) {
                        if (!d.after(resultDate)) {
                            if (latest == null || d.after(latest)) {
                                latest = d;
                            }
                        }
                    }
                    if (latest != null) {
                        row.setShares(sharesByDate.get(latest));
                    }
                }

                for (ResultRow row : quarterlyResults) {
                    if (row.getTtmEps() == null && row.getTtmPat() != null && row.getShares() != null && row.getShares() > 0) {
                        row.setTtmEps(row.getTtmPat() / row.getShares());
                    }
                }

                List<ResultRow> finalRows = quarterlyResults.stream()
                        .filter(r -> r.getTtmEps() != null && r.getTtmEps() > 0 && r.getAnnouncementDate() != null)
                        .sorted(Comparator.comparing(ResultRow::getAnnouncementDate))
                        .collect(Collectors.toList());

                if (finalRows.isEmpty()) return output;

                // ✅ Fetch price history based on custom or range
                List<PricePoint> priceHistory = getPriceSeriesFromDB(
                        ticker,
                        rangeOrDate,
                        "custom".equalsIgnoreCase(rangeOrDate) ? from : null,
                        "custom".equalsIgnoreCase(rangeOrDate) ? to : null
                );

                if (priceHistory.isEmpty()) return output;

                TreeMap<java.sql.Date, Double> epsMap = new TreeMap<>();
                for (ResultRow row : finalRows) {
                    epsMap.put(row.getAnnouncementDate(), row.getTtmEps());
                }

                Map<java.sql.Date, Double> epsForwardMap = new TreeMap<>();
                Double lastEps = null;
                for (PricePoint price : priceHistory) {
                    Map.Entry<java.sql.Date, Double> applicable = epsMap.floorEntry(price.getDate());
                    if (applicable != null) {
                        lastEps = applicable.getValue();
                    }
                    if (lastEps != null && lastEps > 0) {
                        epsForwardMap.put(price.getDate(), lastEps);
                    }
                }

                List<java.sql.Date> epsDates = new ArrayList<>(epsForwardMap.keySet());
                List<Double> epsValues = epsDates.stream().map(epsForwardMap::get).collect(Collectors.toList());

                for (PricePoint price : priceHistory) {
                    java.sql.Date date = price.getDate();
                    Double eps = epsForwardMap.get(date);
                    Double pe = (eps != null && eps > 0) ? price.getClosePrice() / eps : null;

                    java.sql.Date targetDate = java.sql.Date.valueOf(date.toLocalDate().plusDays(365));
                    Double forwardEps = null;
                    for (int i = 0; i < epsDates.size(); i++) {
                        if (!epsDates.get(i).before(targetDate)) {
                            forwardEps = epsValues.get(i);
                            break;
                        }
                    }
                    Double forwardPe = (forwardEps != null && forwardEps > 0) ? price.getClosePrice() / forwardEps : null;

                    if (pe != null || forwardPe != null) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("date", date.toString());
                        if (pe != null) row.put("pe", Math.round(pe * 100.0) / 100.0);
                        if (forwardPe != null) row.put("forward_pe", Math.round(forwardPe * 100.0) / 100.0);
                        output.add(row);
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("Error computing TTM PE for " + ticker);
            e.printStackTrace();
        }

        return output;
    }

    private static Map<java.sql.Date, Double> extractSharesFromAnnualSheet(Workbook workbook) {
        Map<java.sql.Date, Double> sharesByDate = new TreeMap<>();
        Sheet sheet = workbook.getSheet("AnnualResults");

        if (sheet == null) return sharesByDate;

        Row dateRow = sheet.getRow(2);     // index 2 = 3rd row
        Row epsRow = sheet.getRow(12);     // EPS
        Row patRow = sheet.getRow(42);     // PAT

        if (dateRow == null || epsRow == null || patRow == null) return sharesByDate;

        for (int col = 1; col <= 14; col++) {
            Cell dateCell = dateRow.getCell(col);
            Cell epsCell = epsRow.getCell(col);
            Cell patCell = patRow.getCell(col);

            java.sql.Date date = getSqlDateFromCell(dateCell);
            Double eps = getNumericFromCell(epsCell);
            Double pat = getNumericFromCell(patCell);

            if (date != null && eps != null && eps != 0 && pat != null) {
                sharesByDate.put(date, pat / eps);
            }
        }

        return sharesByDate;
    }

    private static java.sql.Date getSqlDateFromCell(Cell cell) {
        if (cell == null) return null;
        try {
            int type = (cell.getCellType() == Cell.CELL_TYPE_FORMULA)
                    ? cell.getCachedFormulaResultType()
                    : cell.getCellType();

            if (type == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return new java.sql.Date(cell.getDateCellValue().getTime());
            } else if (type == Cell.CELL_TYPE_STRING) {
                return java.sql.Date.valueOf(cell.getStringCellValue().trim()); // expects yyyy-MM-dd
            }
        } catch (Exception e) {
            // Optional: log error
        }
        return null;
    }

    private static Double getNumericFromCell(Cell cell) {
        if (cell == null) return null;
        try {
            int type = (cell.getCellType() == Cell.CELL_TYPE_FORMULA)
                    ? cell.getCachedFormulaResultType()
                    : cell.getCellType();

            switch (type) {
                case Cell.CELL_TYPE_NUMERIC:
                    return cell.getNumericCellValue();
                case Cell.CELL_TYPE_STRING:
                    String text = cell.getStringCellValue().trim();
                    return text.isEmpty() ? null : Double.parseDouble(text);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static List<ResultRow> extractQuarterlyResults(Workbook workbook) {
        List<ResultRow> results = new ArrayList<>();
        Sheet sheet = workbook.getSheet("QuarterP&L");

        if (sheet == null) return results;

        Row quarterEndRow = sheet.getRow(2);
        Row announcementRow = sheet.getRow(14);
        Row ttmPatRow = sheet.getRow(34);

        if (quarterEndRow == null || announcementRow == null || ttmPatRow == null) return results;

        for (int col = 1; col < 25; col++) {
            Cell resultEndCell = quarterEndRow.getCell(col);
            Cell announceCell = announcementRow.getCell(col);
            Cell patCell = ttmPatRow.getCell(col);

            java.sql.Date resultEnd = getSqlDateFromCell(resultEndCell);
            java.sql.Date announce = getSqlDateFromCell(announceCell);
            Double ttmPat = getNumericFromCell(patCell);

            if (resultEnd == null) continue;

            // fallback: if announce date not available, use result end date
            if (announce == null) announce = resultEnd;

            ResultRow row = new ResultRow();
            row.setResultEndDate(resultEnd);
            row.setAnnouncementDate(announce);
            row.setTtmPat(ttmPat);
            // shares & ttm_eps to be filled later
            results.add(row);
        }

        return results;
    }

    public static List<PricePoint> getPriceSeriesFromDB(String ticker, String rangeOrDate) {
        return getPriceSeriesFromDB(ticker, rangeOrDate, null, null);
    }

    public static List<PricePoint> getPriceSeriesFromDB(String ticker, String rangeOrDate, String from, String to) {
        List<PricePoint> result = new ArrayList<>();

        String sql = "SELECT date, close_price FROM nse_price_history WHERE nse_ticker = ?";
        List<Object> params = new ArrayList<>();
        params.add(ticker);

        if ("custom".equalsIgnoreCase(rangeOrDate)) {
            if (from != null && !from.isEmpty()) {
                sql += " AND date >= ?";
                params.add(java.sql.Date.valueOf(from));
            }
            if (to != null && !to.isEmpty()) {
                sql += " AND date <= ?";
                params.add(java.sql.Date.valueOf(to));
            }
        } else if (!"ALL".equalsIgnoreCase(rangeOrDate)) {
            if (rangeOrDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sql += " AND date >= ?";
                params.add(java.sql.Date.valueOf(rangeOrDate));
            } else {
                String interval = null;
                switch (rangeOrDate.toUpperCase()) {
                    case "30D": interval = "INTERVAL 30 DAY"; break;
                    case "90D": interval = "INTERVAL 90 DAY"; break;
                    case "1Y": interval = "INTERVAL 1 YEAR"; break;
                    case "3Y": interval = "INTERVAL 3 YEAR"; break;
                    case "5Y": interval = "INTERVAL 5 YEAR"; break;
                    default: interval = null; break;
                }
                if (interval != null) {
                    sql += " AND date >= CURDATE() - " + interval;
                }
            }
        }

        sql += " ORDER BY date";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            java.sql.Date date = rs.getDate("date");
            double closePrice = rs.getDouble("close_price");
            if (rs.wasNull()) closePrice = 0.0;  // or skip the row if needed
            return new PricePoint(date, closePrice);
        });

    }


}
