package com.github.DiachenkoMD.web.controllers.renting;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.controllers.RentController;
import com.github.DiachenkoMD.web.services.IntroService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.roles.ClientRGuard;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;
import static com.github.DiachenkoMD.web.utils.Utils.getLang;
@UseGuards({AuthGuard.class, ClientRGuard.class})
@WebServlet("/rent/data")
public class RentDataController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(RentDataController.class);

    private IntroService introService;

    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.introService = (IntroService) config.getServletContext().getAttribute("intro_service");
        this.gson = (Gson) config.getServletContext().getAttribute("gson");
    }

    /** Route for checking the availability of drivers on a certain dates range. Awaiting <strong>start</strong> and <strong>end</strong> parameters, each of them should have format like 2022-08-16.
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            String start = req.getParameter("start");
            String end = req.getParameter("end");
            int cityId = Integer.parseInt(req.getParameter("city"));

            List<Integer> foundDrivers =  introService.getAvailableDriversOnRange(start, end, cityId);

            sendSuccess(gson.toJson(
                    Map.of(
                            "value", foundDrivers.size() > 0,
                            "message", foundDrivers.size() > 0 ? "" : new StatusText("rent.for_client.driver_not_found").convert(getLang(req))
                    )
            ), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{

            String incomingJSON = new BufferedReader(req.getReader()).lines().collect(Collectors.joining());
            AuthUser currentUser = (AuthUser) req.getSession().getAttribute("auth");

            logger.info(incomingJSON);
            introService.createRent(incomingJSON, currentUser);

            sendSuccess(new StatusText("rent.payment.successful", true, StatusStates.SUCCESS).convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if(e instanceof DescriptiveException desExc){
                desExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("rent.payment.validation_error").convert(getLang(req))));
                desExc.execute(ExceptionReason.PASSPORT_VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("rent.payment.passport_validation_error").convert(getLang(req))));
                desExc.execute(ExceptionReason.DRIVER_NOT_ALLOWED, () -> exceptionToClient.set(new StatusText("rent.payment.driver_not_allowed").convert(getLang(req))));
                desExc.execute(ExceptionReason.NOT_ENOUGH_MONEY, () -> exceptionToClient.set(new StatusText("rent.payment.not_enough_money").convert(getLang(req))));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.error(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }
}
