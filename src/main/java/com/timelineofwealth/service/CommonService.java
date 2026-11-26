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

    public final String ADMIN_ROLE = "ROLE_ADMIN";
    public final String ADVISER_ROLE = "ROLE_ADVISER";
    private final Logger logger = LoggerFactory.getLogger(CommonService.class);
    private final String BASE_EXCEL_PATH = "C:/MyDocuments/03Business/05ResearchAndAnalysis/StockInvestments/QuarterResultsScreenerExcels/";
    /** watchlists.properties in 11Property\website */
    private String watchlistFile() { return ConfigFiles.watchlists(); }
    private final UserRepository userRepository;
    private final AssetClassificationRepository assetClassificationRepository;
    private final SubindustryRepository subindustryRepository;
    private final MutualFundUniverseRepository mutualFundUniverseRepository;
    private final MutualFundStatsRepository mutualFundStatsRepository;
    private final StockUniverseRepository stockUniverseRepository;
    private final StockAnalystRecoRepository stockAnalystRecoRepository;
    private final StockValuationRepository stockValuationRepository;
    private final StockPnlRepository stockPnlRepository;
    private final StockQuarterRepository stockQuarterRepository;
    private final DailyDataSRepository dailyDataSRepository;
    private final DailyDataBRepository dailyDataBRepository;
    private final StockPriceMovementRepository stockPriceMovementRepository;
    private final SetupDatesRepository setupDatesRepository;
    private final IndexValuationRepository indexValuationRepository;
    private final IndexStatisticsRepository indexStatisticsRepository;
    private final StockPriceMovementHistoryRepository stockPriceMovementHistoryRepository;
    private final BenchmarkTwrrMonthlyRepository benchmarkTwrrMonthlyRepository;
    private final BenchmarkTwrrSummaryRepository benchmarkTwrrSummaryRepository;
    private final IndexService indexService;
    public final JdbcTemplate jdbcTemplate;
    private List<StockUniverse> nseBse500BasicList;
    private List<NseBse500> nseBse500List;

    @Autowired
    public CommonService(UserRepository userRepository,
                         AssetClassificationRepository assetClassificationRepository,
                         SubindustryRepository subindustryRepository,
                         MutualFundUniverseRepository mutualFundUniverseRepository,
                         MutualFundStatsRepository mutualFundStatsRepository,
                         StockUniverseRepository stockUniverseRepository,
                         StockAnalystRecoRepository stockAnalystRecoRepository,
                         StockValuationRepository stockValuationRepository,
                         StockPnlRepository stockPnlRepository,
                         StockQuarterRepository stockQuarterRepository,
                         DailyDataSRepository dailyDataSRepository,
                         DailyDataBRepository dailyDataBRepository,
                         StockPriceMovementRepository stockPriceMovementRepository,
                         SetupDatesRepository setupDatesRepository,
                         IndexValuationRepository indexValuationRepository,
                         IndexStatisticsRepository indexStatisticsRepository,
                         StockPriceMovementHistoryRepository stockPriceMovementHistoryRepository,
                         BenchmarkTwrrMonthlyRepository benchmarkTwrrMonthlyRepository,
                         BenchmarkTwrrSummaryRepository benchmarkTwrrSummaryRepository,
                         IndexService indexService,
                         JdbcTemplate jdbcTemplate){
        this.userRepository = userRepository;
        this.assetClassificationRepository = assetClassificationRepository;
        this.subindustryRepository = subindustryRepository;
        this.mutualFundUniverseRepository = mutualFundUniverseRepository;
        this.mutualFundStatsRepository = mutualFundStatsRepository;
        this.stockUniverseRepository = stockUniverseRepository;
        this.stockAnalystRecoRepository = stockAnalystRecoRepository;
        this.stockValuationRepository = stockValuationRepository;
        this.stockPnlRepository = stockPnlRepository;
        this.stockQuarterRepository = stockQuarterRepository;
        this.dailyDataSRepository = dailyDataSRepository;
        this.dailyDataBRepository = dailyDataBRepository;
        this.stockPriceMovementRepository = stockPriceMovementRepository;
        this.setupDatesRepository = setupDatesRepository;
        this.indexValuationRepository = indexValuationRepository;
        this.indexStatisticsRepository = indexStatisticsRepository;
        this.stockPriceMovementHistoryRepository = stockPriceMovementHistoryRepository;
        this.benchmarkTwrrMonthlyRepository = benchmarkTwrrMonthlyRepository;
        this.benchmarkTwrrSummaryRepository = benchmarkTwrrSummaryRepository;
        this.indexService = indexService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns true if SignIn User is Admin
     * @param userDetails
     * @return
     */
    public boolean isAdmin(@AuthenticationPrincipal UserDetails userDetails){
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
    public boolean isAdviser(@AuthenticationPrincipal UserDetails userDetails){
        boolean isAdviser = false;
        String roleName = getLoggedInUser(userDetails).getRoleName();
        if (roleName.equals(ADVISER_ROLE)) {
            isAdviser = true;
        }
        return isAdviser;
    }

    public void updateLastLoginStatus(@AuthenticationPrincipal UserDetails userDetails){
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
    public User getLoggedInUser(UserDetails userDetails) throws  UsernameNotFoundException {
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
    public String getWelcomeMessage(User signInUser){
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
    public SetupDates getSetupDates() {
        List<SetupDates> setupDatesList;
        SetupDates setupDates = new SetupDates();
        if (this.setupDatesRepository != null) {
            setupDatesList = this.setupDatesRepository.findAll();
            setupDates = setupDatesList.get(0);
        }

        return setupDates;
    }

    /**
     * Returns Asset Classifications
     * @return
     */
    @Cacheable("AssetClassfication")
    public List<AssetClassification> getAssetClassfication() {
//        if (this.assetClassificationRepository != null) {
//            return  this.assetClassificationRepository.findAll();
//        } else {
//            return new ArrayList<>();
//        }
        return  this.assetClassificationRepository.findAll();
    }

    /**
     * Return All Sub Industries
     * @return
     */
    @Cacheable("Subindustries")
    public List<Subindustry> getSubindustries() {
        return this.subindustryRepository.findAll();
    }

    /**
     * Returns all Fund Houses
     * @return
     */
    @Cacheable("FundHouses")
    public List<String> getDistinctFundHouse() {
        return this.mutualFundUniverseRepository.findDistinctFundHouse();
    }

    /**
     * Returns all Fund Houses by
     * @param fundHouse
     * @return
     */
    @Cacheable(value = "SchemeNamesByFundHouse")
    public List<MutualFundDTO> getSchemeNames(String fundHouse){
        List<MutualFundUniverse> funds = this.mutualFundUniverseRepository.findSchemeNamesByFundHouse(fundHouse,new Sort("schemeNamePart"));
        List<MutualFundDTO> fundsDTO = new ArrayList<>();
        setFundsDTO(funds, fundsDTO);
        return  fundsDTO;
    }

    private final void setFundsDTO(List<MutualFundUniverse> funds, List<MutualFundDTO> fundsDTO) {
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
    public List<MutualFundDTO> getSchemeNames(String fundHouse,String directRegular){
        List<MutualFundUniverse> funds = this.mutualFundUniverseRepository.findSchemeNamesByFundHouse(fundHouse,directRegular, new Sort("schemeNamePart"));
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
    public List<MutualFundDTO> getSchemeNames(String fundHouse,String directRegular,String dividendGrowth){
        List<MutualFundUniverse> funds = this.mutualFundUniverseRepository.findSchemeNamesByFundHouse(fundHouse,directRegular, dividendGrowth, new Sort("schemeNamePart"));
        List<MutualFundDTO> fundsDTO = new ArrayList<>();
        setFundsDTO(funds, fundsDTO);
        return  fundsDTO;
    }

    @Cacheable(value = "GetSchemeDetails")
    public List<MutualFundDTO> getSchemeDetails(String fundHouse, String category) {

        List<MutualFundUniverse> funds = this.mutualFundUniverseRepository.findSchemeNamesByFundHouseAndCategory(fundHouse, category, new Sort("schemeNamePart"));
        //List<MutualFundUniverse> funds = this.mutualFundUniverseRepository.findAllByFundHouseAndCategory(fundHouse, category);
        List<MutualFundDTO> fundsDTO = new ArrayList<>();
        setFundsDTO(funds, fundsDTO);
        return fundsDTO;
    }

    @Cacheable(value = "SelectedMFStats")
    public List<MutualFundStats> getSelectedMF() {
        //return null;
        List<MutualFundStats> mfStats =  this.mutualFundStatsRepository.findAll();
        mfStats.sort(Comparator.comparing(l->l.getSchemeNamePart()));
        mfStats.sort(Comparator.comparing(l->l.getSchemeType()));
        return mfStats;
    }

    /**
     * Returns all stocks
     * @return
     */
    @Cacheable(value = "StockUniverse")
    public List<StockUniverse> getAllStocks(){
        return this.stockUniverseRepository.findAll();
    }


    /**
     * Returns all stocks
     * @return
     */
    @Cacheable(value = "NseBse500Basic")
    public List<NseBse500> getNseBse500() {
        nseBse500BasicList = this.stockUniverseRepository.findAllByIsNse500OrIsBse500OrderByMarketcapDesc(1,1);
        List<DailyDataS> dailyDataSList = this.dailyDataSRepository.findAllByKeyDate(getSetupDates().getDateToday());
        List<StockPriceMovement> stockPriceMovementList = this.stockPriceMovementRepository.findAll();
        nseBse500List = new ArrayList<>();
        List<Subindustry> subindustries = this.getSubindustries();
        Map<String, List<String>> watchlistsByTicker = loadWatchlistsByTicker();
        Set<String> existingTickers = nseBse500BasicList.stream()
                .map(StockUniverse::getTicker)
                .filter(Objects::nonNull)
                .map(this::normalizeTicker)
                .collect(Collectors.toSet());

        for (String watchlistTicker : watchlistsByTicker.keySet()) {
            if (!existingTickers.contains(watchlistTicker)) {
                StockUniverse stock = this.stockUniverseRepository.findByTicker(watchlistTicker);
                if (stock != null) {
                    nseBse500BasicList.add(stock);
                    existingTickers.add(watchlistTicker);
                } else {
                    logger.warn("Watchlist ticker not found in stock_universe: {}", watchlistTicker);
                }
            }
        }

        nseBse500BasicList.sort((a, b) -> {
            if (a.getMarketcap() == null && b.getMarketcap() == null) return 0;
            if (a.getMarketcap() == null) return 1;
            if (b.getMarketcap() == null) return -1;
            return b.getMarketcap().compareTo(a.getMarketcap());
        });

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
            // Broker-named recommendations (latestMOSLReco ...) and the DCF valuation range are no longer sent to
            // the public stock list: it shows the Yahoo consensus with broker targets labelled A / B instead
            // (attached per request by ConsensusService, so a new week shows without a restart).

            // only Focus Five, the long-term shortlists and the sector groups (101+) are public
            List<String> watchlistNames = new ArrayList<>();
            for (String section : watchlistsByTicker.getOrDefault(normalizeTicker(stockUniverse.getTicker()), new ArrayList<>())) {
                if (WatchlistNames.isPublic(section)) watchlistNames.add(WatchlistNames.display(section));
            }
            nseBse500.setWatchlistNames(watchlistNames);
            nseBse500.setWatchlistsDisplay(String.join(", ", watchlistNames));

            nseBse500List.add(nseBse500);
        }
        return nseBse500List;
    }

    private final String getLatestFourRecommendations(String ticker, String broker, double cmp) {
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

    private final void getLatestFourValuation(String ticker, double cmp, NseBse500 nseBse500) {
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
    public NseBse500 getStockDetails(String ticker) {
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
            StockUniverse stock = this.stockUniverseRepository.findByTicker(ticker);
            dto = new NseBse500(stock);
        }

        // === ADD THIS LINE ===
        dto.setExcelCharts(loadExcelChartsForTicker(ticker));
        return dto;
    }

    /**
     * Get consolidated Sales and Profits, if consolidated not available then standalone
     * @param ticker
     * @return
     */
    public List<StockQuarter> getStockQuarter(String ticker){
        List<java.sql.Date> dates = this.stockQuarterRepository.findDistinctDatesForTicker(ticker);
        List<StockQuarter> stockQuarterList = new ArrayList<>();
        for(java.sql.Date date: dates){
            StockQuarter stockQuarter = this.stockQuarterRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"C", date);
            if (stockQuarter != null) {
                stockQuarterList.add(stockQuarter);
            } else {
                stockQuarter = this.stockQuarterRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"S", date);
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
    public List<StockPnl> getStockPnl(String ticker){
        List<java.sql.Date> dates = this.stockPnlRepository.findDistinctDatesForTicker(ticker);
        List<StockPnl> stockPnlList = new ArrayList<>();
        for(java.sql.Date date: dates){
            StockPnl stockPnl = this.stockPnlRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"C", date);
            if (stockPnl != null) {
                stockPnlList.add(stockPnl);
            } else {
                stockPnl = this.stockPnlRepository.findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(ticker,"S", date);
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
    public List<StockValuationHistory> getStockValuationHistory(String ticker){
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
    public List<StockPriceMovementHistory> getPriceMovements(String ticker) {
        List<StockPriceMovementHistory> stockPriceMovementHistoryList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate oneYearOld = now.minusDays(365);

        stockPriceMovementHistoryList = this.stockPriceMovementHistoryRepository.findAllByKeyTickerAndKeyDateGreaterThanOrderByKeyDateAsc(ticker,java.sql.Date.valueOf(oneYearOld));

        return stockPriceMovementHistoryList;
    }



    /**
     * Get Index Valuation
     * @return
     */
    //@Cacheable(value = "IndexValuation")
    public List<IndexValuation> getIndexValuation(){
        return this.indexValuationRepository.findAllByKeyTickerOrderByKeyDate("NIFTY");
        //List<IndexValuation> list = this.indexValuationRepository.findAllByKeyTickerAndKeyDateBetweenOrderByKeyDate("NIFTY", getSetupDates().getDateStart4Quarter() , getSetupDates().getDateToday());
        //System.out.println("list.size()::"+ list.size());
        //return list;

    }

    /**
     * Get monthly returns of the Index
     * @param ticker
     * @return
     */
    @Cacheable(value = "IndexMonthlyReturnsDTO")
    public List<IndexMonthlyReturnsDTO> getIndexMonthlyReturns(String ticker){
        return indexService.getMonthlyReturns(ticker);
    }

    /**
     * Get Index Statistics
     * @return
     */
    public List<IndexStatistics> getIndexReturnStatistics(String index){
        if (index.equalsIgnoreCase("NIFTY")) {
            return this.indexStatisticsRepository.findOneByTicker("NIFTY");
        } else {
            List<IndexStatistics> indexStatistics = this.indexStatisticsRepository.findOneByTicker("NIFTY");
            indexStatistics.addAll(this.indexStatisticsRepository.findOneByTicker("BSEMidCap"));
            indexStatistics.addAll(this.indexStatisticsRepository.findOneByTicker("BSESmallCap"));
            return indexStatistics;
        }
    }

    /**
     * Get Benchmark Returns
     */
    public List<BenchmarkTwrrMonthlyDTO> getBenchmarkTwrrMonthly() {
        List<Object[]> objects;
        List<BenchmarkTwrrMonthlyDTO> dtos = new ArrayList<>();
        objects = this.benchmarkTwrrMonthlyRepository.findAllBenchmarks();
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
    public List<BenchmarkTwrrSummaryDTO> getBenchmarkTwrrSummary() {
        List<Object[]> objects;
        List<BenchmarkTwrrSummaryDTO> dtos = new ArrayList<>();
        objects = this.benchmarkTwrrSummaryRepository.findAllBenchmarks();
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

    /** Public watchlists in file order (001, 003-005, then 101+): [{name, group}] for the stock list filter. */
    public List<Map<String, String>> getPublicWatchlists() {
        List<String> sections = new ArrayList<>(loadSectionedProperties(watchlistFile()).keySet());
        sections.removeIf(s -> !WatchlistNames.isPublic(s));
        sections.sort(Comparator.comparingInt(WatchlistNames::code));
        List<Map<String, String>> out = new ArrayList<>();
        for (String s : sections) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("name", WatchlistNames.display(s));
            m.put("group", WatchlistNames.group(s));
            out.add(m);
        }
        return out;
    }

    private Map<String, List<String>> loadWatchlistsByTicker() {
        Map<String, List<String>> watchlistsByTicker = new HashMap<>();
        try {
            Map<String, Map<String, String>> watchlistSections = loadSectionedProperties(watchlistFile());

            for (Map.Entry<String, Map<String, String>> sectionEntry : watchlistSections.entrySet()) {
                String watchlistName = sectionEntry.getKey();
                String tickersCsv = sectionEntry.getValue().get("tickers");

                if (tickersCsv == null || tickersCsv.trim().isEmpty()) {
                    continue;
                }

                String[] tickers = tickersCsv.split(",");
                for (String ticker : tickers) {
                    String normalizedTicker = normalizeTicker(ticker);
                    if (normalizedTicker.isEmpty()) {
                        continue;
                    }
                    watchlistsByTicker
                            .computeIfAbsent(normalizedTicker, key -> new ArrayList<>())
                            .add(watchlistName);
                }
            }
        } catch (Exception ex) {
            logger.error("Unable to load watchlists from {}", watchlistFile(), ex);
        }
        return watchlistsByTicker;
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null) {
            return "";
        }
        return ticker.trim().toUpperCase(Locale.ROOT);
    }

    private final List<CustomChartData> loadExcelChartsForTicker(String ticker) {
        List<CustomChartData> chartDataList = new ArrayList<>();
        try {
            Map<String, Map<String, String>> chartProperty = loadSectionedProperties(ConfigFiles.chartForSheetAndRow());

            Properties tickerCombination = new Properties();
            tickerCombination.load(new FileInputStream(ConfigFiles.tickerChartCombination()));

            File excelFile = getLatestExcelFileForTicker(ticker);
            if (excelFile == null) return chartDataList;

            Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFile));
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

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
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[range.length > 1 ? 1 : 0].trim());   // "[3]" = just 3
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
                // row-number definitions; label-based ones (block / row_label) leave these out
                int dateRow = Integer.parseInt(sectionProps.getOrDefault("date_row", "0"));
                int paramRow = Integer.parseInt(sectionProps.getOrDefault("parameter_row", "0"));
                String startCol = sectionProps.getOrDefault("date_column_start", "A");
                String endCol = sectionProps.getOrDefault("date_column_end", "A");
                String title = sectionProps.get("name");
                String chartType = sectionProps.getOrDefault("chart_type", "line");
                String fieldNameColumn = sectionProps.getOrDefault("name_column", "A");
                boolean stacked = Boolean.parseBoolean(sectionProps.getOrDefault("stacked", "false"));
                boolean showLegend = Boolean.parseBoolean(sectionProps.getOrDefault("show_legend", "false"));

                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) continue;

                if (sectionProps.containsKey("block")) {          // label-based definition (HistoricalData / ModelBFSI)
                    CustomChartData c = labelChart(sheet, evaluator, sectionProps, comboId, title, chartType, stacked, showLegend);
                    if (c != null) chartDataList.add(c);
                    continue;
                }

                Row dateRowObj = sheet.getRow(dateRow - 1);
                Row paramRowObj = sheet.getRow(paramRow - 1);
                if (dateRowObj == null || paramRowObj == null) continue;

                String fieldName = getCellAsString(paramRowObj.getCell(CellReference.convertColStringToIndex(fieldNameColumn)), evaluator);

                int startIdx = CellReference.convertColStringToIndex(startCol);
                int endIdx = CellReference.convertColStringToIndex(endCol);

                List<String> dates = new ArrayList<>();
                List<Double> values = new ArrayList<>();

                for (int col = startIdx; col <= endIdx; col++) {
                    java.sql.Date parsedDate = extractSqlDate(dateRowObj.getCell(col), evaluator);
                    if (parsedDate != null) {
                        dates.add(parsedDate.toString());
                    } else {
                        String textDate = getCellAsString(dateRowObj.getCell(col), evaluator);
                        dates.add(textDate.isEmpty() ? null : textDate);
                    }
                }
                String seriesStart = sectionProps.get("series_row_start");
                String seriesEnd = sectionProps.get("series_row_end");
                List<ChartSeriesData> series = new ArrayList<>();

                if (seriesStart != null && seriesEnd != null) {
                    int sRow = Integer.parseInt(seriesStart);
                    int eRow = Integer.parseInt(seriesEnd);
                    for (int rowNo = sRow; rowNo <= eRow; rowNo++) {
                        Row seriesRow = sheet.getRow(rowNo - 1);
                        if (seriesRow == null) continue;

                        String label = getCellAsString(seriesRow.getCell(CellReference.convertColStringToIndex(fieldNameColumn)), evaluator);
                        if (label == null || label.trim().isEmpty()) continue;

                        List<Double> seriesValues = extractValues(seriesRow, startIdx, endIdx, evaluator);
                        boolean hasData = seriesValues.stream().anyMatch(Objects::nonNull);
                        if (!hasData) continue;

                        ChartSeriesData chartSeriesData = new ChartSeriesData();
                        chartSeriesData.setLabel(label.trim());
                        chartSeriesData.setValues(seriesValues);
                        chartSeriesData.setChartType(sectionProps.getOrDefault("series_chart_type", chartType));
                        series.add(chartSeriesData);
                    }
                }

                if (series.isEmpty()) {
                    values = extractValues(paramRowObj, startIdx, endIdx, evaluator);
                    ChartSeriesData defaultSeries = new ChartSeriesData();
                    defaultSeries.setLabel((fieldName == null || fieldName.trim().isEmpty()) ? title : fieldName.trim());
                    defaultSeries.setValues(values);
                    defaultSeries.setChartType(chartType);
                    series.add(defaultSeries);
                }

                CustomChartData chartData = new CustomChartData();
                chartData.setCombinationId(comboId);
                chartData.setTitle(title);
                chartData.setLabels(dates);
                chartData.setValues(values);
                chartData.setFieldName(fieldName);
                chartData.setSeries(series);
                chartData.setChartType(chartType);
                chartData.setStacked(stacked);
                chartData.setShowLegend(showLegend);
                chartDataList.add(chartData);
            }
            workbook.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return chartDataList;
    }

    /**
     * Label-based chart definition, so charts survive row moves in the new sheets:
     *   sheet_name    = HistoricalData (or ModelBFSI)
     *   block         = Quarterly Income Statement      title of the block in column B (prefix match)
     *   row_label     = Revenue                          one series: the row with this label (column C or D)
     *   row_under     = BORROWING & INTEREST EXPENSES    optional: look for row_label only below this label
     *   series_under  = Segmental Revenue                instead of row_label: one series per row below this
     *                                                    sub-header, up to the next column-C row (the subtotal
     *                                                    row repeating the header label and empty rows skipped)
     *   series_label  = optional legend name for a row_label series
     *   transform     = optional ttm / ttm_growth / ttm_weighted / noplat / ttm_noplat (see transformed())
     * Columns are the block's period labels (FY26 / FY27Q2, header "FIN Years" / "Quarters" in column C) whose
     * period has ended; trailing periods with no value yet are dropped. Returns null when nothing is found.
     */
    private CustomChartData labelChart(Sheet sheet, FormulaEvaluator ev, Map<String, String> p, int comboId,
                                       String title, String chartType, boolean stacked, boolean showLegend) {
        String block = p.get("block").trim();
        int last = sheet.getLastRowNum();
        int r0 = -1;
        for (int r = 0; r <= last && r0 < 0; r++) {
            String t = safeText(cellAt(sheet, r, 1), ev);
            if (t != null && t.trim().startsWith(block)) r0 = r;
        }
        if (r0 < 0) return null;
        int hdr = -1;
        for (int r = r0; r <= Math.min(last, r0 + 6) && hdr < 0; r++) {
            String t = safeText(cellAt(sheet, r, 2), ev).trim();
            if (t.startsWith("FIN Year") || t.startsWith("Quarter")) hdr = r;
        }
        if (hdr < 0) return null;
        int end = last + 1;                       // block ends at the next text in column B
        for (int r = hdr + 1; r <= last; r++) {
            if (!safeText(cellAt(sheet, r, 1), ev).trim().isEmpty()) { end = r; break; }
        }

        // actual periods only
        Date today = new Date();
        List<Integer> cols = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Row hr = sheet.getRow(hdr);
        for (int c = 3; hr != null && c < hr.getLastCellNum(); c++) {
            String l = safeText(hr.getCell(c), ev).trim();
            Date pe = StockFinancialsService.periodEnd(l);
            if (pe == null || pe.after(today)) continue;
            cols.add(c);
            labels.add(l);
        }
        if (cols.isEmpty()) return null;

        // rows: label in column C (main rows) or D (sub-rows); remember which column held it
        List<Integer> rows = new ArrayList<>();
        List<String> rowLabels = new ArrayList<>();
        List<Boolean> mainRow = new ArrayList<>();
        for (int r = hdr + 1; r < end; r++) {
            String c = safeText(cellAt(sheet, r, 2), ev).trim();
            String d = safeText(cellAt(sheet, r, 3), ev).trim();
            if (c.isEmpty() && d.isEmpty()) continue;
            rows.add(r);
            rowLabels.add(c.isEmpty() ? d : c);
            mainRow.add(!c.isEmpty());
        }

        List<ChartSeriesData> series = new ArrayList<>();
        String seriesType = p.getOrDefault("series_chart_type", chartType);
        String under = p.get("series_under");
        if (under != null) {
            int i = rowLabels.indexOf(under.trim());
            if (i < 0) return null;
            for (int j = i + 1; j < rows.size() && !mainRow.get(j); j++) {
                if (rowLabels.get(j).equals(under.trim())) continue;          // subtotal row
                List<Double> v = valuesAt(sheet, rows.get(j), cols, ev);
                if (v.stream().noneMatch(x -> x != null && x != 0)) continue;
                ChartSeriesData s = new ChartSeriesData();
                s.setLabel(rowLabels.get(j));
                s.setValues(v);
                s.setChartType(seriesType);
                series.add(s);
            }
        } else {
            String label = p.getOrDefault("row_label", "").trim();
            int from = 0;
            if (p.containsKey("row_under")) {
                from = rowLabels.indexOf(p.get("row_under").trim());
                if (from < 0) return null;
            }
            final int start = from;
            java.util.function.Function<String, List<Double>> row = l -> {
                for (int j = start; j < rowLabels.size(); j++)
                    if (rowLabels.get(j).equals(l.trim())) return valuesAt(sheet, rows.get(j), cols, ev);
                return null;
            };
            List<Double> v = transformed(p.getOrDefault("transform", "").trim(), label, p, row);
            if (v == null || v.stream().noneMatch(x -> x != null && x != 0)) return null;
            ChartSeriesData s = new ChartSeriesData();
            s.setLabel(p.getOrDefault("series_label", label.isEmpty() ? title : label));
            s.setValues(v);
            s.setChartType(chartType);
            series.add(s);
        }
        if (series.isEmpty()) return null;

        // drop leading and trailing periods with no value in any series (years before the history starts,
        // the latest quarter not filled in yet)
        int n = labels.size(), first = 0;
        while (n > 0 && !hasValue(series, n - 1)) n--;
        while (first < n && !hasValue(series, first)) first++;
        if (first >= n) return null;
        for (ChartSeriesData s : series) s.setValues(new ArrayList<>(s.getValues().subList(first, n)));

        CustomChartData chartData = new CustomChartData();
        chartData.setCombinationId(comboId);
        chartData.setTitle(title);
        chartData.setLabels(new ArrayList<>(labels.subList(first, n)));
        chartData.setValues(series.get(0).getValues());
        chartData.setFieldName(series.size() == 1 ? series.get(0).getLabel() : title);
        chartData.setSeries(series);
        chartData.setChartType(chartType);
        chartData.setStacked(stacked);
        chartData.setShowLegend(showLegend);
        return chartData;
    }

    /**
     * Optional "transform" of a row_label chart (quarterly blocks; quarters must be consecutive columns):
     *   ttm          sum of the last 4 quarters of row_label
     *   ttm_growth   ttm vs ttm 4 quarters earlier (fraction)
     *   ttm_weighted margin over the last 4 quarters: sum(row_label x weight_label) / sum(weight_label)
     *                (weight_label defaults to Revenue), e.g. TTM OPM from the quarterly margin row
     *   noplat       Revenue x (Ex-Other Inc.) EBIT Margin% x (1 - Tax Rate%)   (row_label not used)
     *   ttm_noplat   ttm of noplat
     * A quarter whose own value is missing or 0 (not reported yet) gets no TTM value.
     */
    private static List<Double> transformed(String t, String label, Map<String, String> p,
                                            java.util.function.Function<String, List<Double>> row) {
        if (t.isEmpty()) return row.apply(label);
        List<Double> base;
        if (t.endsWith("noplat")) {
            List<Double> rev = row.apply("Revenue"), m = row.apply("(Ex-Other Inc.) EBIT Margin%"), tax = row.apply("Tax Rate%");
            if (rev == null || m == null || tax == null) return null;
            base = new ArrayList<>();
            for (int k = 0; k < rev.size(); k++) {
                Double r = rev.get(k), mm = m.get(k), tx = tax.get(k);
                base.add(r == null || r == 0 || mm == null ? null : r * mm * (1 - (tx == null ? 0 : tx)));
            }
            if (t.equals("noplat")) return base;
            return ttm(base);
        }
        base = row.apply(label);
        if (base == null) return null;
        switch (t) {
            case "ttm":
                return ttm(base);
            case "ttm_growth": {
                List<Double> s = ttm(base), out = new ArrayList<>();
                for (int k = 0; k < s.size(); k++) {
                    Double a = s.get(k), b = k >= 4 ? s.get(k - 4) : null;
                    out.add(a == null || b == null || b <= 0 ? null : a / b - 1);
                }
                return out;
            }
            case "ttm_weighted": {
                List<Double> w = row.apply(p.getOrDefault("weight_label", "Revenue"));
                if (w == null) return null;
                List<Double> prod = new ArrayList<>();
                for (int k = 0; k < base.size(); k++) {
                    Double a = base.get(k), b = w.get(k);
                    prod.add(a == null || b == null || b == 0 ? null : a * b);
                }
                List<Double> num = ttm(prod), den = ttm(w), out = new ArrayList<>();
                for (int k = 0; k < num.size(); k++) {
                    Double a = num.get(k), b = den.get(k);
                    out.add(a == null || b == null || b == 0 ? null : a / b);
                }
                return out;
            }
            default:
                return base;
        }
    }

    private static List<Double> ttm(List<Double> v) {
        List<Double> out = new ArrayList<>();
        for (int k = 0; k < v.size(); k++) {
            Double sum = null;
            if (k >= 3 && v.get(k) != null && v.get(k) != 0) {
                sum = 0.0;
                for (int j = k - 3; j <= k && sum != null; j++) sum = v.get(j) == null ? null : sum + v.get(j);
            }
            out.add(sum);
        }
        return out;
    }

    private String safeText(Cell cell, FormulaEvaluator ev) {
        try {
            return getCellAsString(cell, ev);
        } catch (Exception e) {
            try { return cell != null && cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING ? cell.getStringCellValue() : ""; }
            catch (Exception ignored) { return ""; }
        }
    }

    private static boolean hasValue(List<ChartSeriesData> series, int k) {
        for (ChartSeriesData s : series) {
            Double v = s.getValues().get(k);
            if (v != null && v != 0) return true;
        }
        return false;
    }

    private static Cell cellAt(Sheet sheet, int row, int col) {
        Row r = sheet.getRow(row);
        return r == null ? null : r.getCell(col);
    }

    private List<Double> valuesAt(Sheet sheet, int row, List<Integer> cols, FormulaEvaluator ev) {
        List<Double> out = new ArrayList<>();
        Row r = sheet.getRow(row);
        for (int c : cols) {
            Double v = null;
            Cell cell = r == null ? null : r.getCell(c);
            if (cell != null) {
                try {
                    CellValue cv = ev.evaluate(cell);
                    if (cv != null && cv.getCellType() == Cell.CELL_TYPE_NUMERIC) v = cv.getNumberValue();
                } catch (Exception e) {
                    if (cell.getCellType() == Cell.CELL_TYPE_FORMULA && cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) v = cell.getNumericCellValue();
                }
            }
            out.add(v);
        }
        return out;
    }

    private List<Double> extractValues(Row valueRow, int startIdx, int endIdx, FormulaEvaluator evaluator) {
        List<Double> values = new ArrayList<>();
        for (int col = startIdx; col <= endIdx; col++) {
            Cell valueCell = valueRow.getCell(col);
            if (valueCell == null) {
                values.add(null);
                continue;
            }
            CellValue evaluatedValue = evaluator.evaluate(valueCell);
            if (evaluatedValue != null && evaluatedValue.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                values.add(evaluatedValue.getNumberValue());
            } else {
                values.add(null);
            }
        }
        return values;
    }

    private String getCellAsString(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }

        CellValue evaluated = evaluator.evaluate(cell);
        if (evaluated == null) {
            return "";
        }

        if (evaluated.getCellType() == Cell.CELL_TYPE_STRING) {
            return evaluated.getStringValue();
        }
        if (evaluated.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isValidExcelDate(evaluated.getNumberValue())) {
                return new DataFormatter().formatRawCellContents(
                        evaluated.getNumberValue(),
                        -1,
                        DateFormatConverter.convert(Locale.ENGLISH, "yyyy-MM-dd")
                );
            }
            return String.valueOf(evaluated.getNumberValue());
        }
        if (evaluated.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(evaluated.getBooleanValue());
        }

        return "";
    }

    private java.sql.Date extractSqlDate(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }

        CellValue evaluated = evaluator.evaluate(cell);
        if (evaluated == null) {
            return null;
        }

        try {
            if (evaluated.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isValidExcelDate(evaluated.getNumberValue())) {
                return new java.sql.Date(DateUtil.getJavaDate(evaluated.getNumberValue()).getTime());
            }
            if (evaluated.getCellType() == Cell.CELL_TYPE_STRING) {
                String value = evaluated.getStringValue();
                if (value != null && !value.trim().isEmpty()) {
                    return java.sql.Date.valueOf(value.trim());
                }
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    public File getLatestExcelFileForTicker(String ticker) {
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

    private final List<String> getAvailableQuarters(String basePath) {
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

    private final Map<String, Map<String, String>> loadSectionedProperties(String filePath) {
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

    private final String getCellString(Cell cell) {
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

    public List<Map<String, Object>> getPriceSeries(String ticker, String rangeOrDate) {
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

    public List<Map<String, Object>> getPriceSeries(String ticker, String from, String to) {
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

    public List<Map<String, Object>> getMarketCapSeries(String ticker, String rangeOrDate, String from, String to) {
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

    public List<Map<String, Object>> computeIndexTtmPeSeries(String indexTicker, String from, String to) {
        return computeIndexTtmPeSeriesInternal(indexTicker, "custom", from, to);
    }

    public List<Map<String, Object>> computeIndexTtmPeSeries(String indexTicker, String range) {
        return computeIndexTtmPeSeriesInternal(indexTicker, range, null, null);
    }

    public List<Map<String, Object>> computeIndexTtmPeSeriesInternal(String ticker, String rangeOrDate, String from, String to) {
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

    public List<Map<String, Object>> computeStockTtmPeSeries(String ticker, String from, String to) {
        return computeStockTtmPeSeriesInternal(ticker, "custom", from, to);
    }

    public List<Map<String, Object>> computeStockTtmPeSeries(String ticker, String range) {
        return computeStockTtmPeSeriesInternal(ticker, range, null, null);
    }

    private final List<Map<String, Object>> computeStockTtmPeSeriesInternal(String ticker, String rangeOrDate, String from, String to) {
        List<Map<String, Object>> output = new ArrayList<>();

        try {
            File excelFile = getLatestExcelFileForTicker(ticker);
            if (excelFile == null || !excelFile.exists()) return output;

            try (FileInputStream fis = new FileInputStream(excelFile)) {
                Workbook workbook = WorkbookFactory.create(fis);
                List<ResultRow> quarterlyResults = extractQuarterlyResults(workbook);
                quarterlyResults.sort(Comparator.comparing(ResultRow::getResultEndDate));

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

    /**
     * One row per reported quarter from the HistoricalData "Quarterly Income Statement" block (rows found by
     * label): quarter end from the period label (FY27Q2), announcement = "Result Date" (quarter end when blank),
     * TTM EPS = the sheet's "TTM EPS" row, else sum of the last 4 quarters' "Net Income" / "Wt. Avg. Shares".
     * Quarters not reported yet (period not ended, Revenue 0, or Result Date in the future) are skipped.
     */
    private List<ResultRow> extractQuarterlyResults(Workbook workbook) {
        List<ResultRow> results = new ArrayList<>();
        Sheet sheet = workbook.getSheet("HistoricalData");
        if (sheet == null) return results;
        FormulaEvaluator ev = workbook.getCreationHelper().createFormulaEvaluator();

        int last = sheet.getLastRowNum(), r0 = -1, hdr = -1, end = last + 1;
        for (int r = 0; r <= last && r0 < 0; r++)
            if (safeText(cellAt(sheet, r, 1), ev).trim().startsWith("Quarterly Income Statement")) r0 = r;
        if (r0 < 0) return results;
        for (int r = r0; r <= Math.min(last, r0 + 6) && hdr < 0; r++)
            if (safeText(cellAt(sheet, r, 2), ev).trim().startsWith("Quarter")) hdr = r;
        if (hdr < 0) return results;
        for (int r = hdr + 1; r <= last; r++)
            if (!safeText(cellAt(sheet, r, 1), ev).trim().isEmpty()) { end = r; break; }

        Map<String, Integer> rowOf = new HashMap<>();
        for (int r = hdr + 1; r < end; r++) {
            String c = safeText(cellAt(sheet, r, 2), ev).trim();
            String d = safeText(cellAt(sheet, r, 3), ev).trim();
            String l = c.isEmpty() ? d : c;
            if (!l.isEmpty()) rowOf.putIfAbsent(l, r);
        }
        Integer revRow = rowOf.get("Revenue"), npRow = rowOf.get("Net Income"), shRow = rowOf.get("Wt. Avg. Shares"),
                ttmEpsRow = rowOf.get("TTM EPS"), dateRow = rowOf.get("Result Date");
        if (revRow == null) return results;

        Date today = new Date();
        List<Integer> cols = new ArrayList<>();
        List<Date> ends = new ArrayList<>();
        Row hr = sheet.getRow(hdr);
        for (int c = 3; hr != null && c < hr.getLastCellNum(); c++) {
            Date pe = StockFinancialsService.periodEnd(safeText(hr.getCell(c), ev).trim());
            if (pe == null || pe.after(today)) continue;
            cols.add(c);
            ends.add(pe);
        }
        List<Double> rev = valuesAt(sheet, revRow, cols, ev);
        List<Double> np = npRow == null ? null : valuesAt(sheet, npRow, cols, ev);
        List<Double> sh = shRow == null ? null : valuesAt(sheet, shRow, cols, ev);
        List<Double> ttmEps = ttmEpsRow == null ? null : valuesAt(sheet, ttmEpsRow, cols, ev);
        List<Double> ttmNp = np == null ? null : ttm(np);

        for (int k = 0; k < cols.size(); k++) {
            if (rev.get(k) == null || rev.get(k) == 0) continue;
            java.sql.Date quarterEnd = new java.sql.Date(ends.get(k).getTime());
            java.sql.Date announce = null;
            if (dateRow != null) {
                try {
                    Cell dc = cellAt(sheet, dateRow, cols.get(k));
                    if (dc != null) {
                        CellValue cv = ev.evaluate(dc);
                        if (cv != null && cv.getCellType() == Cell.CELL_TYPE_NUMERIC && cv.getNumberValue() > 0)
                            announce = new java.sql.Date(DateUtil.getJavaDate(cv.getNumberValue()).getTime());
                    }
                } catch (Exception ignored) { }
            }
            if (announce != null && announce.after(today)) continue;
            if (announce == null) announce = quarterEnd;

            Double eps = ttmEps == null ? null : ttmEps.get(k);
            if ((eps == null || eps <= 0) && ttmNp != null && sh != null && ttmNp.get(k) != null
                    && sh.get(k) != null && sh.get(k) > 0) eps = ttmNp.get(k) / sh.get(k);

            ResultRow row = new ResultRow();
            row.setResultEndDate(quarterEnd);
            row.setAnnouncementDate(announce);
            row.setTtmPat(ttmNp == null ? null : ttmNp.get(k));
            row.setShares(sh == null ? null : sh.get(k));
            row.setTtmEps(eps);
            results.add(row);
        }
        return results;
    }

    public List<PricePoint> getPriceSeriesFromDB(String ticker, String rangeOrDate) {
        return getPriceSeriesFromDB(ticker, rangeOrDate, null, null);
    }

    public List<PricePoint> getPriceSeriesFromDB(String ticker, String rangeOrDate, String from, String to) {
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

    private Set<String> getWatchlistTickers(Map<String, List<String>> watchlistsByTicker) {
        return new HashSet<>(watchlistsByTicker.keySet());
    }

}
