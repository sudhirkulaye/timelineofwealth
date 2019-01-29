package com.timelineofwealth.apis;

import com.timelineofwealth.dto.MutualFundDTO;
import com.timelineofwealth.dto.NseBse500;
import com.timelineofwealth.entities.*;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "public/api/")
public class PublicApi {
    private static final Logger logger = LoggerFactory.getLogger(PublicApi.class);

    @RequestMapping(value = "/getDates", method = RequestMethod.GET)
    public SetupDates getSetupDates() {
        logger.debug(String.format("Call public/api/getassetclassifications/"));

        return CommonService.getSetupDates();
    }

    @RequestMapping(value = "/getassetclassifications", method = RequestMethod.GET)
    public List<AssetClassification> getAssetClassfication() {
        logger.debug(String.format("Call public/api/getassetclassifications/"));

        return CommonService.getAssetClassfication();
    }

    @RequestMapping(value = "/getsubindustries", method = RequestMethod.GET)
    public List<Subindustry> getSubindusties() {
        logger.debug(String.format("Call public/api/getsubindustries/"));

        return CommonService.getSubindustries();
    }

    @RequestMapping(value = "/getdistinctfundhouse", method = RequestMethod.GET)
    public List<String> getDistinctFundHouse() {
        logger.debug(String.format("Call public/api/getdistinctfundhouse/"));

        return CommonService.getDistinctFundHouse();
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse) {
        logger.debug(String.format("Call public/api/getschemenames/" + fundHouse));

        return CommonService.getSchemeNames(fundHouse);
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}/{directRegular}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse, @PathVariable String directRegular) {
        logger.debug(String.format("Call public/api/getschemenames/"+fundHouse+"/"+directRegular));

        return CommonService.getSchemeNames(fundHouse,directRegular);
    }

    @RequestMapping(value = "/getschemenames/{fundHouse}/{directRegular}/{dividendGrowth}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeNames(@PathVariable String fundHouse, @PathVariable String directRegular, @PathVariable String dividendGrowth) {
        logger.debug(String.format("Call public/api/getschemenames/"+fundHouse+"/"+directRegular+"/"+dividendGrowth));

        return CommonService.getSchemeNames(fundHouse,directRegular,dividendGrowth);
    }

    @RequestMapping(value = "/getschemedetails/{fundHouse}/{category}", method = RequestMethod.GET)
    public List<MutualFundDTO> getSchemeDetails(@PathVariable String fundHouse, @PathVariable String category) {
        logger.debug(String.format("Call public/api/getschemedetails/"+fundHouse+"/"+category));

        return CommonService.getSchemeDetails(fundHouse,category);
    }

    @RequestMapping(value = "/getallstocks", method = RequestMethod.GET)
    public List<StockUniverse> getAllStocks(){
        logger.debug(String.format("Call public/api/getallstocks/"));

        return CommonService.getAllStocks();
    }

    @RequestMapping(value = "/getnsebse500", method = RequestMethod.GET)
    public List<NseBse500> getNseBse500(){
        logger.debug(String.format("Call public/api/getnsebse500/"));

        return CommonService.getNseBse500();
    }

    @RequestMapping(value = "/getindexvaluation", method = RequestMethod.GET)
    public List<IndexValuation> getIndexValutiaon(){
        logger.debug(String.format("Call public/api/getindexvaluation"));
        return CommonService.getIndexValuation();
    }

    @RequestMapping(value = "/getindexstatistics", method = RequestMethod.GET)
    public List<IndexStatistics> getIndexStatistics(){
        logger.debug(String.format("Call public/api/getindexstatistics"));
        return CommonService.getIndexStatistics("NIFTY");
    }

    @RequestMapping(value = "/getmidandsmallcapindexstatistics", method = RequestMethod.GET)
    public List<IndexStatistics> getMidAndSmallCapIndexStatistics(){
        logger.debug(String.format("Call public/api/getmidandsmallcapindexstatistics"));
        return CommonService.getIndexStatistics("Mid&Small");
    }
}
