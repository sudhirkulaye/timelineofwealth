package com.timelineofwealth.controllers;

import com.timelineofwealth.apis.PublicApi;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.GeneratePDFReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class AdviserViewController {


    private static final Logger logger = LoggerFactory.getLogger(AdviserViewController.class);
    private java.sql.Date dateToday; // = new PublicApi().getSetupDates().getDateToday();
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ServletContext context;

    @Autowired
    public AdviserViewController(Environment environment){}

    @RequestMapping(value = "/adviser/listofclients")
    public String listOfClients(Model model, @AuthenticationPrincipal UserDetails userDetails){
        Date dateToday = new Date();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "TimelineOfWealth-Clients");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/listofclients";
    }

    @RequestMapping(value = "/adviser/consolidatedassets")
    public String consolidatedAssets(Model model, @AuthenticationPrincipal UserDetails userDetails){
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "Consolidated Assets");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/consolidatedassets";
    }

    @RequestMapping(value = "/adviser/generatepdfreport", method = RequestMethod.GET)
    public String generatePDFReport(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, HttpServletResponse response) throws IOException {
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "Generate PDF Reports");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/generatepdfreport";

    }

    @RequestMapping(value = "/adviser/modelportfolios", method = RequestMethod.GET)
    public String getModelPortfolios(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, HttpServletResponse response) throws IOException {
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "Composites/Model Portfolios");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        return "adviser/modelportfolios";

    }

    @RequestMapping(value = "/adviser/generatepmspdf/{memberid}")
    public void generatePMSPDF(@PathVariable long memberid, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        dateToday = new PublicApi().getSetupDates().getDateToday();
//        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
//        model.addAttribute("title", "Generate PDF Reports");
//        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));
        logger.debug(String.format("/adviser/generatepmspdf/ %d", memberid));
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
        File file = new File(fullPath );
        final int BUFFER_SIZE = 4095;
        if(file.exists()){
            try {
                //FileInputStream inputStream = new FileInputStream(file);
                InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

                String mimeType = URLConnection.guessContentTypeFromName(file.getName());// context.getMimeType(fullPath);
                if (mimeType == null) {
                    //unknown mimetype so set the mimetype to application/octet-stream
                    mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                //response.setHeader("content-disposition","inline; filename="+fileName);
                response.setHeader("content-disposition", String.format("attachment; filename=\"" + file.getName() + "\""));
                response.setContentLength((int) file.length());

                OutputStream outputStream = response.getOutputStream();
                FileCopyUtils.copy(inputStream, response.getOutputStream());

//                byte[] buffer = new byte[BUFFER_SIZE];
//                int byteRead = -1;
//                while ((byteRead = inputStream.read()) != -1) {
//                    outputStream.write(buffer, 0, byteRead);
//                }
//                inputStream.close();
//                outputStream.close();
                file.delete();
            } catch (Exception e) {
                logger.debug(String.format("Error in downloading file %s", fileName));
            }
        }
    }
}
