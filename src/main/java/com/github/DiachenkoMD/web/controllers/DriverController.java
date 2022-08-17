package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.drivers.ExtendedDriver;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.web.services.DriverService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.github.DiachenkoMD.web.utils.Utils.getLang;


@WebServlet("/driver")
public class DriverController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(DriverController.class);

    private DriverService driverService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.driverService = (DriverService) config.getServletContext().getAttribute("driver_service");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        Cities city;
        try{
            // Getting city id to further use it on the page stats panel
            int userId = (Integer) ((AuthUser) req.getSession().getAttribute("auth")).getId();

            city = driverService.getCity(userId);

            req.setAttribute("cityId", city.id());
        }catch (Exception e){
            req.setAttribute("cityId", -1);
            logger.error(e);
        }

        try{
            getServletContext().getRequestDispatcher("/views/driver.jsp").forward(req, resp);
        }catch (Exception e){
            logger.error(e);
        }
    }

}
