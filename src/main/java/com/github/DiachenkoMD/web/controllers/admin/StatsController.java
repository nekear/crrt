package com.github.DiachenkoMD.web.controllers.admin;

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

@WebServlet("/admin/stats")
public class StatsController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(StatsController.class);
    private AdminService adminService;
    private Gson gson;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminService = ((AdminService) config.getServletContext().getAttribute("admin_service"));
        gson = (Gson) config.getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            List<Double> statsList = adminService.getStats();
            Utils.sendSuccess(gson.toJson(statsList), resp);
        }catch (Exception e){

            logger.error(e);

            Utils.sendException("", resp);
        }
    }
}
