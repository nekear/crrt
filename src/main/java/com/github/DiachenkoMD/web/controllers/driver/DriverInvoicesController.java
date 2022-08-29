package com.github.DiachenkoMD.web.controllers.driver;

import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.web.services.DriverService;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.DriverRGuard;
import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.Map;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;
import static com.github.DiachenkoMD.web.utils.Utils.*;

@UseGuards({AuthGuard.class, DriverRGuard.class})
@WebServlet("/driver/invoices")
public class DriverInvoicesController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(DriverInvoicesController.class);
    private DriverService driverService;

    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        driverService = ((DriverService) config.getServletContext().getAttribute("driver_service"));
        gson = (Gson) config.getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            int userId = ((AuthUser) req.getSession().getAttribute(SESSION_AUTH)).getCleanId().get();

            sendSuccess(gson.toJson(driverService.getInvoices(userId)), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp){
        try{
            JSONObject jsonBody = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            int cityId = Integer.parseInt(jsonBody.getString("cityId"));
            int userId = (Integer) ((AuthUser) req.getSession().getAttribute(SESSION_AUTH)).getId();

            driverService.changeCity(userId, Cities.getById(cityId));
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try{
            JSONObject jsonBody = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            int invoiceId = Integer.parseInt(CryptoStore.decrypt(jsonBody.getString("id")));

            AuthUser user = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

            boolean skipOperationResult = driverService.skipInvoice(invoiceId, user);

            String message = skipOperationResult ? new StatusText("driver.skip.successful").convert(getLang(req)) : new StatusText("driver.skip.no_suitable_found").convert(getLang(req));

            sendSuccess(gson.toJson(
                    Map.of(
                        "status", skipOperationResult,
                        "message", message
                    )
            ), resp);
        }catch (Exception e){
            logger.error(e);

            sendException(new StatusText("global.unexpectedError").convert(Utils.getLang(req)), resp);
        }
    }
}
