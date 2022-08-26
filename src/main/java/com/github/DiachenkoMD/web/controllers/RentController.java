package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.IntroService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.PageGuard;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.DiachenkoMD.web.utils.Utils.getLang;

@UseGuards({PageGuard.class, AuthGuard.class})
@WebServlet("/rent")
public class RentController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(RentController.class);

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
            String carRefId = req.getParameter("ref");

            if(carRefId != null){
                int decryptedCarId = Integer.parseInt(CryptoStore.decrypt(carRefId));
                Map.Entry<Car, List<LocalDate>> carData = introService.getRentingInfo(decryptedCarId);

                logger.debug(carData);

                req.setAttribute("carData", carData);

                req.getRequestDispatcher("/views/rent.jsp").forward(req, resp);
            }else{
                resp.sendRedirect(req.getContextPath());
            }
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.ACQUIRING_ERROR, () -> exceptionToClient.set(new StatusText("rent.no_car_found").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());

            req.setAttribute("carError", exceptionToClient.get());
            req.getRequestDispatcher("/views/rent.jsp").forward(req, resp);
        }
    }
}
