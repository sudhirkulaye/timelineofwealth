package com.timelineofwealth.service;

import com.timelineofwealth.dto.IndexMonthlyReturnsDTO;
import com.timelineofwealth.dto.IndexReturnsDTO;
import com.timelineofwealth.entities.IndexMonthlyReturns;
import com.timelineofwealth.entities.IndexStatistics;
import com.timelineofwealth.entities.IndexValuation;
import com.timelineofwealth.repositories.IndexMonthlyReturnsRepository;
import com.timelineofwealth.repositories.IndexStatisticsRepository;
import com.timelineofwealth.repositories.IndexValuationRepository;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service("IndexService")
@EnableCaching
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private static IndexValuationRepository indexValuationRepository;
    @Autowired
    public void setIndexValuationRepository(IndexValuationRepository indexValuationRepository){
        IndexService.indexValuationRepository = indexValuationRepository;
    }

    @Autowired
    private static IndexMonthlyReturnsRepository indexMonthlyReturnsRepository;
    @Autowired
    public void setIndexMonthlyReturnsRepository(IndexMonthlyReturnsRepository indexMonthlyReturnsRepository){
        IndexService.indexMonthlyReturnsRepository = indexMonthlyReturnsRepository;
    }

    @Autowired
    private static IndexStatisticsRepository indexStatisticsRepository;
    @Autowired
    public void setIndexStatisticsRepository(IndexStatisticsRepository indexStatisticsRepository){
        IndexService.indexStatisticsRepository = indexStatisticsRepository;
    }

    public static void saveMonthlyReturns(IndexMonthlyReturns indexMonthlyReturns) {
        indexMonthlyReturnsRepository.save(indexMonthlyReturns);
    }

    public static List<IndexMonthlyReturnsDTO> getMonthlyReturns(String ticker) {
        List<IndexMonthlyReturns> indexMonthlyReturnsList = indexMonthlyReturnsRepository.findAllByKeyTickerOrderByKeyYearDesc(ticker);
        List<IndexMonthlyReturnsDTO> monthlyReturnsDTOList = new ArrayList<>();

        if (indexMonthlyReturnsList.isEmpty()) {
            // Compute and save monthly returns if the list is empty
            computeAndSaveIndexMonthlyReturns(ticker);
            // Retrieve the data from the repository again
            indexMonthlyReturnsList = indexMonthlyReturnsRepository.findAllByKeyTickerOrderByKeyYearDesc(ticker);
        }

        for (IndexMonthlyReturns indexMonthlyReturns : indexMonthlyReturnsList) {
            IndexMonthlyReturnsDTO monthlyReturnsDTO = new IndexMonthlyReturnsDTO();

            // Set properties in DTO from the entity
            monthlyReturnsDTO.setTicker(indexMonthlyReturns.getKey().getTicker());
            monthlyReturnsDTO.setYear(indexMonthlyReturns.getKey().getYear());
            monthlyReturnsDTO.setJanReturn(indexMonthlyReturns.getJanReturn());
            monthlyReturnsDTO.setFebReturn(indexMonthlyReturns.getFebReturn());
            monthlyReturnsDTO.setMarReturn(indexMonthlyReturns.getMarReturn());
            monthlyReturnsDTO.setAprReturn(indexMonthlyReturns.getAprReturn());
            monthlyReturnsDTO.setMayReturn(indexMonthlyReturns.getMayReturn());
            monthlyReturnsDTO.setJunReturn(indexMonthlyReturns.getJunReturn());
            monthlyReturnsDTO.setJulReturn(indexMonthlyReturns.getJulReturn());
            monthlyReturnsDTO.setAugReturn(indexMonthlyReturns.getAugReturn());
            monthlyReturnsDTO.setSepReturn(indexMonthlyReturns.getSepReturn());
            monthlyReturnsDTO.setOctReturn(indexMonthlyReturns.getOctReturn());
            monthlyReturnsDTO.setNovReturn(indexMonthlyReturns.getNovReturn());
            monthlyReturnsDTO.setDecReturn(indexMonthlyReturns.getDecReturn());
            monthlyReturnsDTO.setAnnualReturn(indexMonthlyReturns.getAnnualReturn());

            monthlyReturnsDTOList.add(monthlyReturnsDTO);
        }

        return monthlyReturnsDTOList;
    }

    public static void computeAndSaveIndexMonthlyReturns(String ticker, boolean... isCurrentYear) {
        List<IndexMonthlyReturnsDTO> monthlyReturnsList = new ArrayList<>();

        // Find the maximum available date for the ticker
        Date maxDateForTicker = indexValuationRepository.findMaxKeyDateForKeyTicker(ticker);

        Calendar calendar = Calendar.getInstance();
        int currentYear;
        int currentMonth;

        if (maxDateForTicker != null) {
            calendar.setTime(maxDateForTicker);
            currentYear = calendar.get(Calendar.YEAR);
            currentMonth = calendar.get(Calendar.MONTH) + 1;
        } else {
            // Set default values if no data is available
            currentYear = Calendar.getInstance().get(Calendar.YEAR);
            currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        }

        int minYear = 2000;
        if(isCurrentYear.length > 0)
            minYear = currentYear;
        else
            minYear = findMinYear(ticker);

        for (int year = currentYear; year >= minYear; year--) {
            IndexMonthlyReturnsDTO monthlyReturns = new IndexMonthlyReturnsDTO();
            monthlyReturns.setTicker(ticker);
            monthlyReturns.setYear(BigDecimal.valueOf(year));

            for (int month = currentMonth; month >= 1; month--) {
                BigDecimal monthendValue = findLastValueOfMonth(ticker, year, month);

                // Handle January by getting the last year's December value
                if (month == 1) {
                    BigDecimal lastYearDecemberValue = findLastValueOfMonth(ticker, year - 1, 12);
                    if (monthendValue == null) {
                        setMonthReturnToZero(monthlyReturns, month);
                    } else if (lastYearDecemberValue != null) {
                        BigDecimal monthlyReturn = monthendValue.divide(lastYearDecemberValue, 4, BigDecimal.ROUND_HALF_UP).subtract(BigDecimal.ONE);
                        setMonthReturn(monthlyReturns, month, monthlyReturn);
                    } else {
                        setMonthReturnToZero(monthlyReturns, month);
                    }
                } else if (monthendValue != null) {
                    BigDecimal lastMonthendValue = findLastValueOfMonth(ticker, year, month - 1);
                    if (lastMonthendValue != null) {
                        BigDecimal monthlyReturn = monthendValue.divide(lastMonthendValue, 4, BigDecimal.ROUND_HALF_UP).subtract(BigDecimal.ONE);
                        setMonthReturn(monthlyReturns, month, monthlyReturn);
                    } else {
                        setMonthReturnToZero(monthlyReturns, month);
                    }
                } else {
                    // No data for this month
                    setMonthReturnToZero(monthlyReturns, month);
                }
            }

            BigDecimal annualReturn = calculateAnnualReturn(ticker, year);
            monthlyReturns.setAnnualReturn(annualReturn.setScale(4, BigDecimal.ROUND_HALF_UP));

            monthlyReturnsList.add(monthlyReturns);

            // Reset currentMonth to 12 after the first iteration
            if (year == currentYear) {
                currentMonth = 12;
            }
        }

        for (IndexMonthlyReturnsDTO monthlyReturnsDTO : monthlyReturnsList) {
            IndexMonthlyReturns indexMonthlyReturns = new IndexMonthlyReturns();

            // Set the key (ticker and year)
            IndexMonthlyReturns.IndexMonthlyReturnsKey key = new IndexMonthlyReturns.IndexMonthlyReturnsKey();
            key.setTicker(monthlyReturnsDTO.getTicker());
            key.setYear(monthlyReturnsDTO.getYear());
            indexMonthlyReturns.setKey(key);

            // Set monthly returns and annual return
            indexMonthlyReturns.setJanReturn(monthlyReturnsDTO.getJanReturn());
            indexMonthlyReturns.setFebReturn(monthlyReturnsDTO.getFebReturn());
            indexMonthlyReturns.setMarReturn(monthlyReturnsDTO.getMarReturn());
            indexMonthlyReturns.setAprReturn(monthlyReturnsDTO.getAprReturn());
            indexMonthlyReturns.setMayReturn(monthlyReturnsDTO.getMayReturn());
            indexMonthlyReturns.setJunReturn(monthlyReturnsDTO.getJunReturn());
            indexMonthlyReturns.setJulReturn(monthlyReturnsDTO.getJulReturn());
            indexMonthlyReturns.setAugReturn(monthlyReturnsDTO.getAugReturn());
            indexMonthlyReturns.setSepReturn(monthlyReturnsDTO.getSepReturn());
            indexMonthlyReturns.setOctReturn(monthlyReturnsDTO.getOctReturn());
            indexMonthlyReturns.setNovReturn(monthlyReturnsDTO.getNovReturn());
            indexMonthlyReturns.setDecReturn(monthlyReturnsDTO.getDecReturn());
            indexMonthlyReturns.setAnnualReturn(monthlyReturnsDTO.getAnnualReturn());

            // Save the data to the database
            saveMonthlyReturns(indexMonthlyReturns);
        }
    }

    private static void setMonthReturn(IndexMonthlyReturnsDTO monthlyReturns, int month, BigDecimal returnVal) {
        switch (month) {
            case 1:
                monthlyReturns.setJanReturn(returnVal);
                break;
            case 2:
                monthlyReturns.setFebReturn(returnVal);
                break;
            case 3:
                monthlyReturns.setMarReturn(returnVal);
                break;
            case 4:
                monthlyReturns.setAprReturn(returnVal);
                break;
            case 5:
                monthlyReturns.setMayReturn(returnVal);
                break;
            case 6:
                monthlyReturns.setJunReturn(returnVal);
                break;
            case 7:
                monthlyReturns.setJulReturn(returnVal);
                break;
            case 8:
                monthlyReturns.setAugReturn(returnVal);
                break;
            case 9:
                monthlyReturns.setSepReturn(returnVal);
                break;
            case 10:
                monthlyReturns.setOctReturn(returnVal);
                break;
            case 11:
                monthlyReturns.setNovReturn(returnVal);
                break;
            case 12:
                monthlyReturns.setDecReturn(returnVal);
                break;
            default:
                // Handle invalid month values here if needed
                break;
        }
    }

    private static void setMonthReturnToZero(IndexMonthlyReturnsDTO monthlyReturns, int month) {
        setMonthReturn(monthlyReturns, month, BigDecimal.ZERO);
    }

    private static BigDecimal findLastValueOfMonth(String ticker, int year, int month) {
        Date maxDate = indexValuationRepository.findMaxDateForMonth(ticker, year, month);

        if (maxDate != null) {
            // Now that we have the max date, get the value for that date and ticker
            IndexValuation valuation = indexValuationRepository.findByKeyTickerAndKeyDate(ticker, maxDate);
            if (valuation != null) {
                return valuation.getValue();
            }
        }

        return null; // No data for this month
    }

    private static int findMinYear(String ticker) {
        Integer minYear = indexValuationRepository.findMinYearForTicker(ticker);
        return minYear != null ? minYear : Calendar.getInstance().get(Calendar.YEAR);
    }

    private static BigDecimal calculateAnnualReturn(String ticker, int year) {
        int previousYear = year - 1;

        BigDecimal initialValue = findLastValueOfMonth(ticker, previousYear, 12);
        BigDecimal finalValue = findLastValueOfMonth(ticker, year, 12);

        if (initialValue != null && finalValue != null) {
            return finalValue.divide(initialValue, 4, BigDecimal.ROUND_HALF_UP).subtract(BigDecimal.ONE);
        }

        return BigDecimal.ZERO;
    }

    public static void computeAndSavePeriodReturnStatistics(String ticker, int period) {
        List<IndexReturnsDTO> periodReturnsList = getPeriodReturns(ticker, period);

        if (periodReturnsList.isEmpty()) {
            // Handle the case when there are no one-year returns data.
            return;
        }

        // Calculate mean, median, standard deviation, minimum, and maximum returns
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (IndexReturnsDTO returns : periodReturnsList) {
            stats.addValue(returns.getPeriodReturns().doubleValue());
        }

        BigDecimal meanReturns = BigDecimal.valueOf(stats.getMean()).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal medianReturns = BigDecimal.valueOf(stats.getPercentile(50)).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal standardDeviation = BigDecimal.valueOf(stats.getStandardDeviation()).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal minimumReturns = BigDecimal.valueOf(stats.getMin()).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal maximumReturns = BigDecimal.valueOf(stats.getMax()).setScale(4, BigDecimal.ROUND_HALF_UP);

        // Find the date range for minimum and maximum returns
        String minReturnsDuration = findDateRangeForReturns(periodReturnsList, stats.getMin(), true);
        String maxReturnsDuration = findDateRangeForReturns(periodReturnsList, stats.getMax(), false);

        // Create and save the IndexStatistics entity
        IndexStatistics statistics = new IndexStatistics();
        statistics.setTicker(ticker);
        if(period == 1) {
            statistics.setMeanReturns1yr(meanReturns);
            statistics.setMedianReturns1yr(medianReturns);
            statistics.setStandardDeviation1yr(standardDeviation);
            statistics.setMinimumReturns1yr(minimumReturns);
            statistics.setMinimumReturns1yrDuration(minReturnsDuration);
            statistics.setMaximumReturns1yr(maximumReturns);
            statistics.setMaximumReturns1yrDuration(maxReturnsDuration);
        }
        if(period == 3) {
            statistics.setMeanReturns3yr(meanReturns);
            statistics.setMedianReturns3yr(medianReturns);
            statistics.setStandardDeviation3yr(standardDeviation);
            statistics.setMinimumReturns3yr(minimumReturns);
            statistics.setMinimumReturns3yrDuration(minReturnsDuration);
            statistics.setMaximumReturns3yr(maximumReturns);
            statistics.setMaximumReturns3yrDuration(maxReturnsDuration);
        }
        if(period == 5) {
            statistics.setMeanReturns5yr(meanReturns);
            statistics.setMedianReturns5yr(medianReturns);
            statistics.setStandardDeviation5yr(standardDeviation);
            statistics.setMinimumReturns5yr(minimumReturns);
            statistics.setMinimumReturns5yrDuration(minReturnsDuration);
            statistics.setMaximumReturns5yr(maximumReturns);
            statistics.setMaximumReturns5yrDuration(maxReturnsDuration);
        }
        if(period == 10) {
            statistics.setMeanReturns10yr(meanReturns);
            statistics.setMedianReturns10yr(medianReturns);
            statistics.setStandardDeviation10yr(standardDeviation);
            statistics.setMinimumReturns10yr(minimumReturns);
            statistics.setMinimumReturns10yrDuration(minReturnsDuration);
            statistics.setMaximumReturns10yr(maximumReturns);
            statistics.setMaximumReturns10yrDuration(maxReturnsDuration);
        }

        // Save the statistics to the database
        saveIndexStatisticsForPeriod(ticker, statistics,period);
    }

    private static List<IndexReturnsDTO> getPeriodReturns(String ticker, int period) {
        List<IndexReturnsDTO> oneYearReturnsList = new ArrayList<>();

        // Get all data for the given ticker ordered by date in descending order
        List<IndexValuation> indexDataList = indexValuationRepository.findAllByKeyTickerOrderByKeyDateDesc(ticker);

        // Initialize variables for tracking one-year return computation
        BigDecimal currentPeriodBeforeValue = null;
        Date currentPeriodBeforeDate = null;

        for (IndexValuation indexData : indexDataList) {
            BigDecimal currentDateValue = indexData.getValue();
            Date currentDate = indexData.getKey().getDate();

            // Update current year-before values for the next iteration
            currentPeriodBeforeDate = calculatePeriodBeforeDate(ticker, currentDate, period);
            currentPeriodBeforeValue = findValueForDate(ticker, currentPeriodBeforeDate);

            // Calculate one-year return when we have both current and one-year-before values
            if (currentPeriodBeforeValue != null) {

                BigDecimal periodReturn = calculateAnnualizedReturn(currentPeriodBeforeValue, currentDateValue, period);

                // Create and add the IndexReturnsDTO object to the list
                IndexReturnsDTO oneYearReturns = new IndexReturnsDTO();
                oneYearReturns.setTicker(ticker);
                oneYearReturns.setFromDate(currentPeriodBeforeDate);
                oneYearReturns.setToDate(currentDate);
                oneYearReturns.setPeriodReturns(periodReturn);

                oneYearReturnsList.add(oneYearReturns);
            }
        }

        return oneYearReturnsList;
    }

    public static BigDecimal calculateAnnualizedReturn(BigDecimal beginningValue, BigDecimal endValue, int n) {
        BigDecimal returnRatio = endValue.divide(beginningValue, MathContext.DECIMAL128);
        double exponent = 1.0 / n;
        BigDecimal annualizedReturn = new BigDecimal(Math.pow(returnRatio.doubleValue(), exponent), MathContext.DECIMAL128)
                .subtract(BigDecimal.ONE)
                .setScale(4, RoundingMode.HALF_UP); // Adjust the scale as needed (2 decimal places in this example)
        return annualizedReturn;
    }

    private static String findDateRangeForReturns(List<IndexReturnsDTO> returnsList, double returns, boolean isMinimumFlag) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = null;
        String toDateStr = null;

        // Sort the list based on periodReturns using Java 8
        returnsList = returnsList.stream()
                .sorted(Comparator.comparingDouble(dto -> dto.getPeriodReturns().doubleValue()))
                .collect(Collectors.toList());

        if (isMinimumFlag) {
            // Search in ascending order
            for (IndexReturnsDTO returnsDTO : returnsList) {
                if (returnsDTO.getPeriodReturns().doubleValue() >= returns) {
                    if (fromDateStr == null) {
                        fromDateStr = dateFormat.format(returnsDTO.getFromDate());
                    }
                    toDateStr = dateFormat.format(returnsDTO.getToDate());
                    break;
                }
            }
        } else {
            // Search in descending order (reverse)
            for (int i = returnsList.size() - 1; i >= 0; i--) {
                IndexReturnsDTO returnsDTO = returnsList.get(i);
                if (returnsDTO.getPeriodReturns().doubleValue() <= returns) {
                    if (fromDateStr == null) {
                        fromDateStr = dateFormat.format(returnsDTO.getFromDate());
                    }
                    toDateStr = dateFormat.format(returnsDTO.getToDate());
                    break;
                }
            }
        }

        if (fromDateStr != null && toDateStr != null) {
            return fromDateStr + "-to-" + toDateStr;
        } else {
            return "-to-";
        }
    }

    public static void setDateLastUpdatedForIndexStats(String ticker, Date date){
        List<IndexStatistics> existingRecords = indexStatisticsRepository.findOneByTicker(ticker);
        if (existingRecords != null && !existingRecords.isEmpty()) {
            existingRecords.get(0).setLastUpdated(date);
            indexStatisticsRepository.save(existingRecords.get(0));
        }
    }

    private static void saveIndexStatisticsForPeriod(String ticker, IndexStatistics statistics, int period) {
        // Save the statistics entity to the index_statistics table
        // You can implement this method based on your data access logic.
        List<IndexStatistics> existingRecords = indexStatisticsRepository.findOneByTicker(ticker);
        if(period == 1) {
            if (existingRecords != null && !existingRecords.isEmpty()) {
                existingRecords.get(0).setMaximumReturns1yr(statistics.getMaximumReturns1yr());
                existingRecords.get(0).setMaximumReturns1yrDuration(statistics.getMaximumReturns1yrDuration());
                existingRecords.get(0).setMinimumReturns1yr(statistics.getMinimumReturns1yr());
                existingRecords.get(0).setMinimumReturns1yrDuration(statistics.getMinimumReturns1yrDuration());
                existingRecords.get(0).setMeanReturns1yr(statistics.getMeanReturns1yr());
                existingRecords.get(0).setMedianReturns1yr(statistics.getMedianReturns1yr());
                existingRecords.get(0).setStandardDeviation1yr(statistics.getStandardDeviation1yr());
                indexStatisticsRepository.save(existingRecords.get(0));
            } else {
                indexStatisticsRepository.save(statistics);
            }
        }
        if(period == 3){
            if (existingRecords != null && !existingRecords.isEmpty()) {
                existingRecords.get(0).setMaximumReturns3yr(statistics.getMaximumReturns3yr());
                existingRecords.get(0).setMaximumReturns3yrDuration(statistics.getMaximumReturns3yrDuration());
                existingRecords.get(0).setMinimumReturns3yr(statistics.getMinimumReturns3yr());
                existingRecords.get(0).setMinimumReturns3yrDuration(statistics.getMinimumReturns3yrDuration());
                existingRecords.get(0).setMeanReturns3yr(statistics.getMeanReturns3yr());
                existingRecords.get(0).setMedianReturns3yr(statistics.getMedianReturns3yr());
                existingRecords.get(0).setStandardDeviation3yr(statistics.getStandardDeviation3yr());
                indexStatisticsRepository.save(existingRecords.get(0));
            } else {
                indexStatisticsRepository.save(statistics);
            }
        }
        if(period == 5){
            if (existingRecords != null && !existingRecords.isEmpty()) {
                existingRecords.get(0).setMaximumReturns5yr(statistics.getMaximumReturns5yr());
                existingRecords.get(0).setMaximumReturns5yrDuration(statistics.getMaximumReturns5yrDuration());
                existingRecords.get(0).setMinimumReturns5yr(statistics.getMinimumReturns5yr());
                existingRecords.get(0).setMinimumReturns5yrDuration(statistics.getMinimumReturns5yrDuration());
                existingRecords.get(0).setMeanReturns5yr(statistics.getMeanReturns5yr());
                existingRecords.get(0).setMedianReturns5yr(statistics.getMedianReturns5yr());
                existingRecords.get(0).setStandardDeviation5yr(statistics.getStandardDeviation5yr());
                indexStatisticsRepository.save(existingRecords.get(0));
            } else {
                indexStatisticsRepository.save(statistics);
            }
        }
        if(period == 10){
            if (existingRecords != null && !existingRecords.isEmpty()) {
                existingRecords.get(0).setMaximumReturns10yr(statistics.getMaximumReturns10yr());
                existingRecords.get(0).setMaximumReturns10yrDuration(statistics.getMaximumReturns10yrDuration());
                existingRecords.get(0).setMinimumReturns10yr(statistics.getMinimumReturns10yr());
                existingRecords.get(0).setMinimumReturns10yrDuration(statistics.getMinimumReturns10yrDuration());
                existingRecords.get(0).setMeanReturns10yr(statistics.getMeanReturns10yr());
                existingRecords.get(0).setMedianReturns10yr(statistics.getMedianReturns10yr());
                existingRecords.get(0).setStandardDeviation10yr(statistics.getStandardDeviation10yr());
                indexStatisticsRepository.save(existingRecords.get(0));
            } else {
                indexStatisticsRepository.save(statistics);
            }
        }
    }

    private static Date calculatePeriodBeforeDate(String ticker, Date currentDate, int period) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.YEAR, -period);
        IndexValuation indexData = indexValuationRepository.findByKeyTickerAndKeyDate(ticker, currentDate);
        if(indexData != null)
            return new Date(calendar.getTimeInMillis());
        else {
            Date date = indexValuationRepository.findMaxKeyDateForKeyTickerBeforeKeyDate(ticker, currentDate);
            return date;
        }
    }

    private static BigDecimal findValueForDate(String ticker, Date date) {
        IndexValuation indexData = indexValuationRepository.findByKeyTickerAndKeyDate(ticker, date);
        if(indexData!=null)
            return indexData.getValue();
        else
            return null;
    }
}

