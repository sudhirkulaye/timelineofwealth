package com.timelineofwealth.controllers;

import com.timelineofwealth.apis.PublicApi;
import com.timelineofwealth.dto.ClientDTO;
import com.timelineofwealth.entities.Member;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.MemberRepository;
import com.timelineofwealth.service.AdviserService;
import com.timelineofwealth.service.CommonService;
import com.timelineofwealth.service.GeneratePDFReport;
import com.timelineofwealth.service.MemberService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
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

    @RequestMapping(value = "/adviser/generateallpdfreports", method = RequestMethod.POST)
    public String generateAllPDFReports(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, HttpServletResponse response) throws IOException {
        dateToday = new PublicApi().getSetupDates().getDateToday();
        model.addAttribute("processingDate", dateToday.toString()/*CommonService.getProcessingDate()*/);
        model.addAttribute("title", "Generate PDF Reports");
        model.addAttribute("welcomeMessage", CommonService.getWelcomeMessage(CommonService.getLoggedInUser(userDetails)));

        logger.debug(String.format("/adviser/generateallpdfreports"));
        User user = CommonService.getLoggedInUser(userDetails);

        String message = "";

        List<ClientDTO> clients = AdviserService.getPMSClients(user.getEmail());
        for (ClientDTO client : clients){
            try {
                logger.debug(String.format("Processing Client %s", client.getMemberName()));
                boolean isSuccess = GeneratePDFReport.generatePMSPDFForMember(user, client.getMemberid(), context, request, response);
                if (isSuccess) {
                    String fullPath = request.getServletContext().getRealPath("/resources/reports/" + client.getMemberid() + ".pdf");
                    String fileName = "" + client.getMemberid() + ".pdf";
                    moveAndRenamePMSReportFile(fullPath, dateToday, client.getMemberid(), client.getMoslCode(), client.getFirstName());
                    logger.debug(String.format("Successfully moved report to PMS folder for Client %s", client.getMemberName()));
                } else {
                    logger.debug(String.format("Failed to generate report for Client %s", client.getMemberName()));
                    if(message.equals(""))
                        message = "Failed to generate PDF for client " + client.getMemberid() + "_" + client.getMoslCode() + "_" + client.getMemberName();
                    else
                        message = message + "\n" + "Failed to generate PDF for client " + client.getMemberid() + "_" + client.getMoslCode() + "_" + client.getMemberName();
                }
                if(message.equals(""))
                    message = "Successfully generated PDF for client " + client.getMemberid() + "_" + client.getMoslCode() + "_" + client.getMemberName();
                else
                    message = message + "\n" + "Successfully generated PDF for client " + client.getMemberid() + "_" + client.getMoslCode() + "_" + client.getMemberName();
            } catch (Exception e) {
                logger.debug(String.format("Failed to generate report for Client %s", client.getMemberName()));
                if(message.equals(""))
                    message = "Failed to generate PDF for client " + client.getMemberid() + "_" + client.getMoslCode() + "_" + client.getMemberName();
                else
                    message = message + "\n" + "Failed to generate PDF for client " + client.getMemberid() + "_" + client.getMoslCode() + "_" + client.getMemberName();
                e.printStackTrace();
            }
        }
        // finally generate Model Portfolio Report
        boolean isSuccess = GeneratePDFReport.generatePMSPDFForMember(user, 1, context, request, response);
        if (isSuccess) {
            String fullPath = request.getServletContext().getRealPath("/resources/reports/" + 1 + ".pdf");
            String fileName = "" + 1 + ".pdf";
            moveAndRenamePMSReportFile(fullPath, dateToday, 1, "H1", "Focus-Five_ModelFolio");

        } else {
            logger.debug(String.format("Failed to generate report for Model Portfolio"));
            if(message.equals(""))
                message = "Failed to generate PDF for Model Portfolio ";
            else
                message = message + "\n" + "Failed to generate PDF for Model Portfolio ";
        }
        model.addAttribute("message", message);
        return "adviser/generatepdfreport";
    }

    public static void moveAndRenamePMSReportFile(String sourceFolderPath, Date dateToday, long memberId, String moslCode, String clientName) throws IOException {

        String targetBasePath = "C:\\MyDocuments\\03Business\\06ClientData\\PMS\\ClientReports";
        // Format the date as YYYYMMDD
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateFormatted = dateFormat.format(dateToday);

        // Create the target directory structure
        String year = dateFormatted.substring(0, 4);
        String monthDay = dateFormatted.substring(4);

        String targetYearPath = targetBasePath + File.separator + year;
        String targetDatePath = targetYearPath + File.separator + year + monthDay;

        // Create the target directories if they don't exist
        Files.createDirectories(Paths.get(targetYearPath)); // Use Paths.get here
        Files.createDirectories(Paths.get(targetDatePath)); // Use Paths.get here

        // Get the original file name
        File sourceFile = new File(sourceFolderPath);
        String originalFileName = sourceFile.getName();

        // Generate the new file name based on the specified format
        String newFileName = "";
        if (memberId != 1)
            newFileName = dateFormatted + "_" + memberId + "_" + moslCode + "_" + clientName + "_TOW.pdf";
        else
            newFileName = dateFormatted + "_Focus-Five_ModelFolio.pdf";


        // Create the target file path with the new name
        Path targetPath = Paths.get(targetDatePath, newFileName); // Use Paths.get here

        // Move the PDF file to the target folder with the new name, replacing it if it already exists
        Files.move(Paths.get(sourceFolderPath), targetPath, StandardCopyOption.REPLACE_EXISTING); // Use Paths.get here
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
