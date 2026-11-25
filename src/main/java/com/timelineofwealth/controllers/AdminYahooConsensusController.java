package com.timelineofwealth.controllers;

import com.timelineofwealth.service.ConfigFiles;
import com.timelineofwealth.service.consensus.YahooConsensusProcessService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Admin > Process Yahoo Consensus: after 1_Download_YahooConsensus.bat (and copying the WeeklyConsensus folder to
 * this laptop), compute the weekly scores and load all consensus tables. "Compare only" changes nothing.
 */
@Controller
public class AdminYahooConsensusController {
    private static final String VIEW = "admin/processyahooconsensus";
    private final YahooConsensusProcessService service;
    private final JdbcTemplate jdbc;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AdminYahooConsensusController(YahooConsensusProcessService service, JdbcTemplate jdbc) {
        this.service = service;
        this.jdbc = jdbc;
    }

    @RequestMapping(value = "/admin/processyahooconsensus", method = RequestMethod.GET)
    public String page(Model model) {
        status(model);
        return VIEW;
    }

    @RequestMapping(value = "/admin/processyahooconsensus", method = RequestMethod.POST)
    public String run(Model model, @RequestParam("mode") String mode) {
        boolean compareOnly = !"process".equals(mode);
        if (!running.compareAndSet(false, true)) {
            model.addAttribute("error", "A run is already in progress. Wait for it to finish and reload the page.");
        } else {
            try {
                YahooConsensusProcessService.Result r = service.run(compareOnly);
                model.addAttribute("result", r);
                model.addAttribute("compareOnly", compareOnly);
                boolean allMatch = r.comparison.stream().allMatch(c -> Boolean.TRUE.equals(c.get("match")) || Boolean.TRUE.equals(c.get("skipped")));
                model.addAttribute("allMatch", allMatch);
            } finally {
                running.set(false);
            }
        }
        status(model);
        return VIEW;
    }

    /** what the page shows before a run: the input files and what the database holds now */
    private void status(Model model) {
        SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        File folder = new File(ConfigFiles.yahooConsensusFolder());
        File[] files = folder.listFiles((dir, name) -> name.matches("(?i)\\d{8}_YahooConsensus\\.csv"));
        String latest = "";
        if (files != null) for (File x : files) if (x.getName().compareToIgnoreCase(latest) > 0) latest = x.getName();
        File history = new File(folder, "YahooConsensusHistory.csv");
        File tracker = new File(ConfigFiles.analystViewTracker());
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Weekly consensus files", folder.getPath(), files == null ? "folder not found" : files.length + " files, latest " + latest});
        rows.add(new String[]{"History file", history.getName(), history.isFile() ? "updated " + f.format(new Date(history.lastModified())) : "missing"});
        rows.add(new String[]{"Broker reports", tracker.getPath(), tracker.isFile() ? "updated " + f.format(new Date(tracker.lastModified())) : "missing (broker views will be left as they are)"});
        rows.add(new String[]{"Watchlists", ConfigFiles.watchlists(), new File(ConfigFiles.watchlists()).isFile() ? "found" : "missing"});
        model.addAttribute("inputs", rows);
        List<String[]> db = new ArrayList<>();
        db.add(new String[]{"yahoo_consensus", one("SELECT CONCAT(COUNT(*), ' rows, ', COUNT(DISTINCT date), ' weeks, latest ', IFNULL(MAX(date),'-')) FROM yahoo_consensus")});
        db.add(new String[]{"consensus_weekly_score", one("SELECT CONCAT(COUNT(*), ' rows, ', COUNT(DISTINCT week_date), ' weeks, latest ', IFNULL(MAX(week_date),'-')) FROM consensus_weekly_score")});
        db.add(new String[]{"consensus_broker_view", one("SELECT CONCAT(COUNT(*), ' broker views, latest report ', IFNULL(MAX(report_date),'-')) FROM consensus_broker_view")});
        db.add(new String[]{"nse_price_history", one("SELECT CONCAT('prices up to ', MAX(date)) FROM nse_price_history")});
        model.addAttribute("dbstatus", db);
        model.addAttribute("title", "TimelineOfWealth");
    }

    private String one(String sql) {
        try {
            return jdbc.queryForObject(sql, String.class);
        } catch (Exception e) {
            return "not available (" + e.getClass().getSimpleName() + ")";
        }
    }
}
