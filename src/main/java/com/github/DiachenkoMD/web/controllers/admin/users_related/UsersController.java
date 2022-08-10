package com.github.DiachenkoMD.web.controllers.admin.users_related;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/users")
public class UsersController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(UsersController.class);
    private AdminService adminService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
        gson = ((Gson) config.getServletContext().getAttribute("gson"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String paginationWrapperJSON = req.getParameter("data");
            logger.info("Param: {}", paginationWrapperJSON);
            logger.info("Success {} ", adminService.getUsers(paginationWrapperJSON));
        }catch (Exception e){
            logger.error(e);

            try {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(new StatusText("global.unexpectedError").convert(Utils.getLang(req)));
                resp.getWriter().flush();
            } catch (IOException ioExc) {
                logger.error(ioExc);
            }
        }
    }
}
