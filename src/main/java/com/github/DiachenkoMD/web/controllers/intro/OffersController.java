package com.github.DiachenkoMD.web.controllers.intro;

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
@WebServlet("/offers")
public class OffersController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(OffersController.class);

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
            Utils.sendSuccess(gson.toJson(introService.getAllCars()), resp);
        }catch (Exception e){
            logger.error(e);

            Utils.sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
