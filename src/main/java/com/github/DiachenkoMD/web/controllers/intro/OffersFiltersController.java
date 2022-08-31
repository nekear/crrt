package com.github.DiachenkoMD.web.controllers.intro;

import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.web.services.IntroService;
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

    /**
     * Route for getting date range and return id`s of available cars for renting on that dates range.
     * @param req > "start": String (yyyy-mm-dd) and "end": String (yyyy-mm-dd).
     * @param resp > id`s of cars available on specified dates range.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            DatesRange datesRange = new DatesRange();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if(req.getParameter("start") != null)
                datesRange.setStart(LocalDate.parse(req.getParameter("start"), formatter));

            if(req.getParameter("end") != null)
                datesRange.setEnd(LocalDate.parse(req.getParameter("end"), formatter));

            Utils.sendSuccess(gson.toJson(introService.getCarsNotRentedInDatesRange(datesRange)), resp);

        }catch (Exception e){
            logger.error(e);

            Utils.sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
