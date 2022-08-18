package com.github.DiachenkoMD.web.controllers.admin.cars_related;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.roles.AdminRGuard;
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
@UseGuards({AuthGuard.class, AdminRGuard.class})
@WebServlet("/admin/cars")
public class CarsController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(CarsController.class);
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
            List<Car> carsList = adminService.getCars();

            Utils.sendSuccess(gson.toJson(carsList), resp);
        }catch (Exception e){

            logger.error(e);

           Utils.sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
