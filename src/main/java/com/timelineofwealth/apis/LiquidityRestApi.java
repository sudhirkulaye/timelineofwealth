package com.timelineofwealth.apis;

import com.timelineofwealth.entities.Liquidity;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.LiquidityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(value = "user/api/")
public class LiquidityRestApi {
    private final Logger logger = LoggerFactory.getLogger(LiquidityRestApi.class);
    private final CommonService commonService;
    private final LiquidityService liquidityService;

    public LiquidityRestApi(CommonService commonService,
                            LiquidityService liquidityService){
        this.commonService = commonService;
        this.liquidityService = liquidityService;
    }

    @RequestMapping(value = "/getliquidities", method = RequestMethod.GET)
    public List<Liquidity> getLiquidities() {
        logger.debug(String.format("Call user/api/getliquidities/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        return liquidityService.getLiquidityRecords(user.getEmail());
    }

    @RequestMapping(value = "/updateliquidity", method = RequestMethod.PUT)
    public List<Liquidity> updateLiquidity(@RequestBody Liquidity editedRecord) {
        logger.debug("Call user/api/updateliquidity/ " + editedRecord.getKey().getMemberid());
        liquidityService.updateLiquidityRecord(editedRecord);
        return getLiquidities();
    }

    @RequestMapping(value = "/addliquidity", method = RequestMethod.POST)
    public List<Liquidity> addLiquidity(@RequestBody Liquidity newRecord) {
        logger.debug("Call user/api/addliquidity/ " + newRecord.getKey().getMemberid());
        liquidityService.addLiquidityRecord(newRecord);
        return getLiquidities();
    }

    @RequestMapping(value = "/deleteliquidity", method = RequestMethod.POST)
    public List<Liquidity> deleteLiquidity(@RequestBody Liquidity deleteRecord) {
        logger.debug("Call user/api/deleteliquidity/ " + deleteRecord.getKey().getMemberid());
        liquidityService.deleteLiquidityRecord(deleteRecord);
        return getLiquidities();
    }
    
}
