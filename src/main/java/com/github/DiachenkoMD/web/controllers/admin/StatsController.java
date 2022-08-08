package com.github.DiachenkoMD.web.controllers.admin;

import com.github.DiachenkoMD.web.services.AdminService;
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

@WebServlet("/admin/stats")
public class StatsController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(StatsController.class);
    private AdminService adminService;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            List<Double> statsList = adminService.getStats();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(new Gson().toJson(statsList));
            resp.getWriter().flush();
        }catch (Exception e){

            logger.error(e);

            try {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().flush();
            } catch (IOException ioExc) {
                logger.error(ioExc);
            }
        }
    }
}
