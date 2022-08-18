package com.github.DiachenkoMD.web.controllers.intro;

import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.IntroService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.roles.DriverRGuard;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@WebServlet("/filtered_offers")
public class OffersFiltersController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(OffersFiltersController.class);

    private IntroService introService;

    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.introService = (IntroService) config.getServletContext().getAttribute("intro_service");
        this.gson = (Gson) config.getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            DatesRange datesRange = new DatesRange();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if(req.getParameter("start") != null)
                datesRange.setStart(LocalDate.parse(req.getParameter("start"), formatter));

            if(req.getParameter("end") != null)
                datesRange.setEnd(LocalDate.parse(req.getParameter("end"), formatter));

            Utils.sendSuccess(gson.toJson(introService.getCarsWithDatesRange(datesRange)), resp);

        }catch (Exception e){
            logger.error(e);

            Utils.sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
