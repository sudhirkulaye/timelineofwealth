package com.timelineofwealth.apis;

import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.dto.ConsolidatedAssetsDTO;
import com.timelineofwealth.entities.Composite;
import com.timelineofwealth.entities.CompositeConstituents;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.AdviserService;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.GeneratePDFReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@RestController
@RequestMapping(value = "adviser/api/")
public class AdviserRestApi {
    private static final Logger logger = LoggerFactory.getLogger(AdviserRestApi.class);
    @Autowired
    private ServletContext context;

    @RequestMapping(value = "/getclients", method = RequestMethod.GET)
    public List<ClientDTO> getClients() {
        logger.debug(String.format("Call adviser/api/getusermembers/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return AdviserService.getClients(user.getEmail());
    }

    @RequestMapping(value = "/getpmsclients", method = RequestMethod.GET)
    public List<ClientDTO> getPMSClients() {
        logger.debug(String.format("Call adviser/api/getpmsclients/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return AdviserService.getPMSClients(user.getEmail());
    }

    @RequestMapping(value = "/getconsolidatedassetsofclient", method = RequestMethod.POST)
    public List<ConsolidatedAssetsDTO> getConsolidatedAssets(@RequestBody String clientemail) {
        logger.debug(String.format("Call adviser/api/getconsolidatedassetsofclient for {%s}", clientemail));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);
        return AdviserService.getConsolidatedAssets(user.getEmail(),clientemail);
    }

    @RequestMapping(value = "/getcomposites", method = RequestMethod.GET)
    public List<Composite> getComposites() {
        logger.debug(String.format("Call adviser/api/getcomposites "));

        return AdviserService.getComposites();
    }

    @RequestMapping(value = "/getcompositedetails", method = RequestMethod.GET)
    public List<CompositeConstituents> getCompositeDetails() {
        logger.debug(String.format("Call adviser/api/getcompositedetails "));

        return AdviserService.getCompositeDetails();
    }

    @RequestMapping(value = "/deletecompositedetail", method = RequestMethod.POST)
    public List<CompositeConstituents> deleteCompositeDetails(@RequestBody CompositeConstituents deleteRecord) {
        logger.debug(String.format("Call adviser/api/deletecompositedetails " + deleteRecord.getShortName()));
        AdviserService.deleteCompositeDetails(deleteRecord);
        return getCompositeDetails();
    }

    @RequestMapping(value = "/updatewcompositedetail", method = RequestMethod.PUT)
    public List<CompositeConstituents> updateWealthDetailsRecord(@RequestBody CompositeConstituents editedRecord) {
        logger.debug("Call user/api/updatecompositedetails/ " + editedRecord.getShortName());
        AdviserService.updateCompositeDetails(editedRecord);
        return getCompositeDetails();
    }

    @RequestMapping(value = "/addcompositedetail", method = RequestMethod.POST)
    public List<CompositeConstituents> addWealthDetailsRecord(@RequestBody CompositeConstituents newRecord) {
        logger.debug("Call user/api/addcompositedetail/ " + newRecord.getShortName());
        AdviserService.addCompositeDetails(newRecord);
        return getCompositeDetails();
    }

}
