package com.timelineofwealth.apis;

import com.timelineofwealth.dto.ResultExcelDTO;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.service.AdminService;
import com.timelineofwealth.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import java.util.List;

@RestController
@RequestMapping(value = "admin/api/")
public class AdminRestApi {
    private final Logger logger = LoggerFactory.getLogger(AdminRestApi.class);
    private final ServletContext context;
    private final CommonService commonService;
    private final AdminService adminService;

    @Autowired
    public AdminRestApi(ServletContext context,
                        CommonService commonService,
                        AdminService adminService){
        this.context = context;
        this.commonService = commonService;
        this.adminService = adminService;
    }

    @RequestMapping(value = "/getlatestresultexcels", method = RequestMethod.GET)
    public List<ResultExcelDTO> getLatestResultExcels() {
        logger.debug(String.format("Call admin/api/getlatestresultexcels/"));

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = commonService.getLoggedInUser(userDetails);
        return adminService.getLatestResultExcels();
    }

}
