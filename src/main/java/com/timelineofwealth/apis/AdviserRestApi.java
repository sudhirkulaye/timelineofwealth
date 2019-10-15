package com.timelineofwealth.apis;

import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.dto.ConsolidatedAssetsDTO;
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

    @RequestMapping(value = "/generatepmspdf/{memberid}", method = RequestMethod.GET)
    public void generatePmsPdf(@PathVariable long memberid, HttpServletRequest request, HttpServletResponse response) {
        logger.debug(String.format("/adviser/generatePmsPdf/ %d", memberid));
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = CommonService.getLoggedInUser(userDetails);

        boolean isSuccess = GeneratePDFReport.generatePMSPDFForMember(user, memberid, context, request, response);
        String fullPath = request.getServletContext().getRealPath("/resources/reports/"+memberid+".pdf");
        String fileName = ""+memberid+".pdf";
        if(isSuccess){
            downloadFile(fullPath,response,fileName);
        }

    }

    private void downloadFile(String fullPath, HttpServletResponse response, String fileName) {
        logger.debug(String.format("Downloading file %s", fileName));
        File file = new File(fullPath);
        final int BUFFER_SIZE = 4095;
        if(file.exists()){
            try {
                FileInputStream inputStream = new FileInputStream(file);
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("content-disposition","attachment: filename="+fileName);
                OutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int byteRead = -1;
                while ((byteRead = inputStream.read()) != -1) {
                    outputStream.write(buffer, 0, byteRead);
                }
                inputStream.close();
                outputStream.close();
                file.delete();
            } catch (Exception e) {
                logger.debug(String.format("Error in downloading file %s", fileName));
            }
        }
    }

}
