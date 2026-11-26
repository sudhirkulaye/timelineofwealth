package com.timelineofwealth.service.consensus;

/** Table definitions, identical to the Python weekly_lib (consensus_db.DDL and dashboard_db.DDL / NEW_SCORE_COLS). */
final class YcSchema {
    private YcSchema() {}

    /** consensus_broker_view columns added after the Python version: added to an existing table on write */
    static final String[][] NEW_BROKER_COLS = {
            {"fwd_multiple", "varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'valuation basis of the target (AnalystsReco Fwd Multiple)'"},
    };

    static final String[][] NEW_SCORE_COLS = {
            {"eps_rev_4w", "decimal(12,6) DEFAULT NULL COMMENT 'EPS 4W revision, both FYs averaged (dashboard EPS 4W)'"},
            {"eps_rev_13w", "decimal(12,6) DEFAULT NULL COMMENT 'EPS 13W revision, both FYs averaged'"},
            {"score_pct", "decimal(8,4) DEFAULT NULL COMMENT 'score percentile, 100 = best'"},
            {"z_eps", "decimal(10,6) DEFAULT NULL COMMENT 'score component: EPS revision rank z'"},
            {"z_tp", "decimal(10,6) DEFAULT NULL COMMENT 'score component: TP revision rank z'"},
            {"z_rev", "decimal(10,6) DEFAULT NULL COMMENT 'score component: minus 4W price rank z (reversal)'"},
            {"z_mom12", "decimal(10,6) DEFAULT NULL COMMENT 'score component: 12-1 momentum rank z'"},
            {"count_chg_13w", "int(5) DEFAULT NULL COMMENT 'change in analyst count over 13 weeks'"},
            {"consensus_age_weeks", "int(5) DEFAULT NULL COMMENT 'weeks since the Yahoo TP or EPS last changed'"},
    };

    private static String newCols() {
        StringBuilder sb = new StringBuilder();
        for (String[] c : NEW_SCORE_COLS) sb.append(",\n  ").append(c[0]).append(' ').append(c[1]);
        return sb.toString();
    }

    static final String[] DDL = {
            "CREATE TABLE IF NOT EXISTS yahoo_consensus (\n" +
                    "  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK stock_universe ticker (mapped from yahoo_ticker)',\n" +
                    "  date date NOT NULL COMMENT 'PK weekly snapshot date',\n" +
                    "  fin_year varchar(6) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK estimate fiscal year e.g. FY27',\n" +
                    "  yahoo_ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'symbol as downloaded from Yahoo Finance',\n" +
                    "  tp_avg decimal(20,3) DEFAULT NULL COMMENT 'consensus target price average',\n" +
                    "  tp_min decimal(20,3) DEFAULT NULL COMMENT 'consensus target price lowest',\n" +
                    "  tp_max decimal(20,3) DEFAULT NULL COMMENT 'consensus target price highest',\n" +
                    "  rev_avg decimal(20,3) DEFAULT NULL COMMENT 'consensus revenue average (Cr)',\n" +
                    "  rev_min decimal(20,3) DEFAULT NULL COMMENT 'consensus revenue lowest (Cr)',\n" +
                    "  rev_max decimal(20,3) DEFAULT NULL COMMENT 'consensus revenue highest (Cr)',\n" +
                    "  eps_avg decimal(20,3) DEFAULT NULL COMMENT 'consensus EPS average',\n" +
                    "  eps_min decimal(20,3) DEFAULT NULL COMMENT 'consensus EPS lowest',\n" +
                    "  eps_max decimal(20,3) DEFAULT NULL COMMENT 'consensus EPS highest',\n" +
                    "  analyst_count int(5) DEFAULT NULL COMMENT 'number of analysts',\n" +
                    "  year_ago_rev decimal(20,3) DEFAULT NULL COMMENT 'revenue of the year before fin_year',\n" +
                    "  year_ago_eps decimal(20,3) DEFAULT NULL COMMENT 'EPS of the year before fin_year',\n" +
                    "  current_price decimal(20,3) DEFAULT NULL COMMENT 'price on the snapshot date',\n" +
                    "  PRIMARY KEY (ticker, date, fin_year),\n" +
                    "  KEY index_yahoo_consensus_date (date)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                    "  COMMENT='Weekly Yahoo Finance analyst consensus (Admin > Process Yahoo Consensus)'",
            "CREATE TABLE IF NOT EXISTS consensus_weekly_score (\n" +
                    "  week_date date NOT NULL COMMENT 'PK consensus week (Friday)',\n" +
                    "  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK stock_universe ticker',\n" +
                    "  analyst_count int(5) DEFAULT NULL,\n" +
                    "  watchlist_group varchar(80) COLLATE utf8_unicode_ci NOT NULL COMMENT 'first 101+ section of watchlists.properties, else Other',\n" +
                    "  tp_rev_1w decimal(12,6) DEFAULT NULL COMMENT 'consensus TP change vs 1 week before (0.05 = +5%)',\n" +
                    "  tp_rev_4w decimal(12,6) DEFAULT NULL,\n" +
                    "  eps_fy1_rev_4w decimal(12,6) DEFAULT NULL COMMENT 'nearer fiscal year, same FY label on both dates',\n" +
                    "  eps_fy2_rev_4w decimal(12,6) DEFAULT NULL,\n" +
                    "  rev_fy1_rev_4w decimal(12,6) DEFAULT NULL COMMENT 'revenue estimate, nearer fiscal year',\n" +
                    "  persistence_13w int(4) DEFAULT NULL COMMENT 'weeks with EPS upgrade minus weeks with downgrade, last 13',\n" +
                    "  mom_4w decimal(12,6) DEFAULT NULL COMMENT 'price return over the 4 weeks to week_date',\n" +
                    "  mom_12_1 decimal(12,6) DEFAULT NULL COMMENT 'price return from 12 months to 1 month before week_date',\n" +
                    "  score decimal(12,6) DEFAULT NULL COMMENT 'composite score (mean rank z); NULL if not scored',\n" +
                    "  score_rank int(5) DEFAULT NULL COMMENT '1 = best among the week''s scored stocks',\n" +
                    "  is_long tinyint(1) NOT NULL DEFAULT 0,\n" +
                    "  is_short tinyint(1) NOT NULL DEFAULT 0,\n" +
                    "  is_divergence tinyint(1) NOT NULL DEFAULT 0,\n" +
                    "  is_emerging tinyint(1) NOT NULL DEFAULT 0,\n" +
                    "  price_at_score decimal(20,3) DEFAULT NULL COMMENT 'nse_price_history close on or before week_date'" + newCols() + ",\n" +
                    "  PRIMARY KEY (week_date, ticker),\n" +
                    "  KEY index_consensus_weekly_score_ticker (ticker)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Weekly composite score per stock'",
            "CREATE TABLE IF NOT EXISTS consensus_weekly_group (\n" +
                    "  week_date date NOT NULL COMMENT 'PK',\n" +
                    "  watchlist_group varchar(80) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK',\n" +
                    "  stock_count int(5) NOT NULL COMMENT 'stocks with consensus that week',\n" +
                    "  avg_eps_rev_4w decimal(12,6) DEFAULT NULL,\n" +
                    "  up_count int(5) NOT NULL DEFAULT 0 COMMENT 'EPS 4W revision above +0.5%',\n" +
                    "  down_count int(5) NOT NULL DEFAULT 0 COMMENT 'EPS 4W revision below -0.5%',\n" +
                    "  avg_return_1m decimal(12,6) DEFAULT NULL COMMENT 'mean price return over the month to week_date',\n" +
                    "  rel_return_1m_vs_nifty decimal(12,6) DEFAULT NULL COMMENT 'avg_return_1m minus NIFTY over the same dates',\n" +
                    "  PRIMARY KEY (week_date, watchlist_group)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Weekly revisions by watchlist group'",
            "CREATE TABLE IF NOT EXISTS consensus_scoreboard (\n" +
                    "  week_date date NOT NULL COMMENT 'PK',\n" +
                    "  ic_spearman decimal(8,6) DEFAULT NULL COMMENT 'rank corr of score with next 4-week return; NULL until known',\n" +
                    "  long_fwd_ret decimal(12,6) DEFAULT NULL COMMENT 'long list next 4-week return minus the scored-stock average',\n" +
                    "  short_fwd_ret decimal(12,6) DEFAULT NULL COMMENT 'short list next 4-week return minus the scored-stock average',\n" +
                    "  spread_fwd_ret decimal(12,6) DEFAULT NULL COMMENT 'long_fwd_ret minus short_fwd_ret',\n" +
                    "  hit_rate decimal(8,6) DEFAULT NULL COMMENT 'share of long and short picks that beat / lagged the average',\n" +
                    "  breadth_up decimal(8,6) DEFAULT NULL COMMENT 'share of scored stocks with EPS 4W revision above +0.5%',\n" +
                    "  breadth_down decimal(8,6) DEFAULT NULL COMMENT 'share below -0.5%',\n" +
                    "  PRIMARY KEY (week_date)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Weekly score track record'",
            "CREATE TABLE IF NOT EXISTS consensus_broker_view (\n" +
                    "  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK stock_universe ticker',\n" +
                    "  broker varchar(10) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK MOSL / AMBIT / PL / AXIS',\n" +
                    "  reco varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                    "  target decimal(20,3) DEFAULT NULL,\n" +
                    "  report_date date DEFAULT NULL,\n" +
                    "  quarter varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                    "  fwd_multiple varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'valuation basis of the target (AnalystsReco Fwd Multiple)',\n" +
                    "  PRIMARY KEY (ticker, broker)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                    "  COMMENT='Latest verified AnalystsReco report per ticker and broker'",
    };
}
