package com.timelineofwealth.apis;

import com.timelineofwealth.entities.YahooConsensus;
import com.timelineofwealth.repositories.StockUniverseRepository;
import com.timelineofwealth.repositories.YahooConsensusRepository;
import com.timelineofwealth.service.StockFinancialsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Lightweight JSON endpoints for the chart-first stock page and the top-bar stock search.
 * Responses are column arrays (not one object per row) to keep long price histories small.
 */
@RestController
@RequestMapping(value = "public/api/")
public class StockChartApi {
    private final StockUniverseRepository stockUniverseRepository;
    private final YahooConsensusRepository yahooConsensusRepository;
    private final JdbcTemplate jdbcTemplate;
    private final StockFinancialsService stockFinancialsService;

    public StockChartApi(StockUniverseRepository stockUniverseRepository,
                         YahooConsensusRepository yahooConsensusRepository, JdbcTemplate jdbcTemplate,
                         StockFinancialsService stockFinancialsService) {
        this.stockUniverseRepository = stockUniverseRepository;
        this.yahooConsensusRepository = yahooConsensusRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.stockFinancialsService = stockFinancialsService;
    }

    /** Annual + quarterly revenue, EBITDA, net profit: latest result Excel, else stock_pnl / stock_quarter. */
    @RequestMapping(value = "/getstockfinancials/{ticker}", method = RequestMethod.GET)
    public Map<String, Object> getStockFinancials(@PathVariable String ticker) {
        return stockFinancialsService.getFinancials(ticker);
    }

    /** Top-bar search. Returns up to 10 {t: ticker, n: short name}. */
    @RequestMapping(value = "/searchstocks", method = RequestMethod.GET)
    public List<Map<String, String>> searchStocks(@RequestParam("q") String q) {
        List<Map<String, String>> out = new ArrayList<>();
        String query = q == null ? "" : q.trim().toUpperCase();
        if (query.isEmpty() || query.length() > 40) return out;
        for (Object[] row : stockUniverseRepository.searchByTickerOrName(query, PageRequest.of(0, 10))) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("t", (String) row[0]);
            m.put("n", (String) row[1]);
            out.add(m);
        }
        return out;
    }

    /**
     * Full daily OHLCV history from nse_price_history as parallel arrays:
     * {d:[dates], o:[], h:[], l:[], c:[], v:[]}. Prices are as stored (split/bonus adjusted in the DB).
     */
    @RequestMapping(value = "/getstockohlc/{ticker}", method = RequestMethod.GET)
    public Map<String, Object> getStockOhlc(@PathVariable String ticker) {
        List<String> d = new ArrayList<>();
        List<Double> o = new ArrayList<>(), h = new ArrayList<>(), l = new ArrayList<>(), c = new ArrayList<>();
        List<Long> v = new ArrayList<>();
        jdbcTemplate.query(
                "SELECT date, open_price, high_price, low_price, close_price, total_traded_quantity " +
                "FROM nse_price_history WHERE nse_ticker = ? AND close_price > 0 ORDER BY date",
                new Object[]{ticker},
                rs -> {
                    double close = rs.getDouble(5);
                    double open = rs.getDouble(2), high = rs.getDouble(3), low = rs.getDouble(4);
                    d.add(rs.getDate(1).toString());
                    o.add(open > 0 ? open : close);
                    h.add(high > 0 ? high : close);
                    l.add(low > 0 ? low : close);
                    c.add(close);
                    v.add(rs.getLong(6));
                });
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ticker", ticker);
        out.put("d", d);
        out.put("o", o);
        out.put("h", h);
        out.put("l", l);
        out.put("c", c);
        out.put("v", v);
        return out;
    }

    /**
     * Weekly Yahoo consensus for one stock_universe ticker, shaped for charts:
     * {dates:[], price:[], tpAvg:[], tpMin:[], tpMax:[], analysts:[],
     *  fy:{"FY27": {epsAvg:[], epsMin:[], epsMax:[], revAvg:[]}, ...}}  (every array aligned to dates; null = no estimate).
     * Empty dates means no coverage.
     */
    @RequestMapping(value = "/getconsensus/{ticker}", method = RequestMethod.GET)
    public Map<String, Object> getConsensus(@PathVariable String ticker) {
        List<YahooConsensus> rows = yahooConsensusRepository.findAllByKeyTickerOrderByKeyDateAscKeyFinYearAsc(ticker);
        List<String> dates = new ArrayList<>();
        Map<String, Integer> idx = new HashMap<>();
        for (YahooConsensus r : rows) {
            String d = r.getKey().getDate().toString();
            if (!idx.containsKey(d)) { idx.put(d, dates.size()); dates.add(d); }
        }
        int n = dates.size();
        Double[] price = new Double[n], tpAvg = new Double[n], tpMin = new Double[n], tpMax = new Double[n];
        Integer[] analysts = new Integer[n];
        Map<String, Map<String, Double[]>> fy = new TreeMap<>();
        for (YahooConsensus r : rows) {
            int i = idx.get(r.getKey().getDate().toString());
            price[i] = first(price[i], r.getCurrentPrice());
            tpAvg[i] = first(tpAvg[i], r.getTpAvg());
            tpMin[i] = first(tpMin[i], r.getTpMin());
            tpMax[i] = first(tpMax[i], r.getTpMax());
            if (analysts[i] == null) analysts[i] = r.getAnalystCount();
            Map<String, Double[]> f = fy.computeIfAbsent(r.getKey().getFinYear(), k -> {
                Map<String, Double[]> m = new LinkedHashMap<>();
                for (String s : new String[]{"epsAvg", "epsMin", "epsMax", "revAvg"}) m.put(s, new Double[n]);
                return m;
            });
            f.get("epsAvg")[i] = num(r.getEpsAvg());
            f.get("epsMin")[i] = num(r.getEpsMin());
            f.get("epsMax")[i] = num(r.getEpsMax());
            f.get("revAvg")[i] = num(r.getRevAvg());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ticker", ticker);
        out.put("dates", dates);
        out.put("price", price);
        out.put("tpAvg", tpAvg);
        out.put("tpMin", tpMin);
        out.put("tpMax", tpMax);
        out.put("analysts", analysts);
        out.put("fy", fy);
        return out;
    }

    private static Double num(BigDecimal b) { return b == null ? null : b.doubleValue(); }
    private static Double first(Double current, BigDecimal b) { return current != null ? current : num(b); }
}
