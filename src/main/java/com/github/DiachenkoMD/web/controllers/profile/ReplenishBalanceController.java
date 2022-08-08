package com.github.DiachenkoMD.web.controllers.profile;

import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.UsersService;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.DiachenkoMD.web.utils.Utils.getLang;

@WebServlet("/profile/replenish")
public class ReplenishBalanceController extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(UpdatePasswordController.class);
    private UsersService usersService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.usersService = (UsersService) config.getServletContext().getAttribute("users_service");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            double newBalance = this.usersService.replenishBalance(req, resp);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(
                    new Gson().toJson(
                            Map.of(
                            "newBalance", newBalance,
                            "message", new StatusText("profile.replenishment_successful", true, StatusStates.SUCCESS).convert(getLang(req))
                            )
                    )
            );

            resp.getWriter().flush();

        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("profile.replenishment_validation_error").convert(getLang(req))));
            }

            if(exceptionToClient.get() == null)
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.debug(exceptionToClient.get());

            try {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(exceptionToClient.get());
                resp.getWriter().flush();
            } catch (IOException ioExc) {
                logger.error(ioExc);
            }
        }
    }
}
