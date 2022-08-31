package com.github.DiachenkoMD.web.controllers.restore;

import com.github.DiachenkoMD.entities.dto.JWTAnalysis;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.JWTManager;
import com.github.DiachenkoMD.web.utils.middlewares.warden.UseWards;
import com.github.DiachenkoMD.web.utils.middlewares.warden.wards.JWTWard;
import io.fusionauth.jwt.InvalidJWTException;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.domain.JWT;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.*;

@UseWards(JWTWard.class)
@WebServlet("/restore")
public class PasswordRestoreController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(PasswordRestoreController.class);

    private UsersService usersService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.usersService = (UsersService) config.getServletContext().getAttribute("users_service");
    }

    /**
     * Serves /restore GET queries and forwards to restoration page with email if JWT token was not found or has any problems (they will be displayed on the page).
     * If JWT verified without any issues, forward will be done to restoration page with "new password" input. <br/>
     * Note: JWT token is being processed by {@link JWTWard}.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JWTAnalysis jwtAnalysis = (JWTAnalysis) req.getAttribute("jwtAnalysis");

        logger.info("JWT token analysis: {}", jwtAnalysis);

        if(jwtAnalysis.isValid() && jwtAnalysis.containsFields("email")){
            JWT token = jwtAnalysis.getToken();
            req.setAttribute("targetEmail", token.getString("email"));
            req.getRequestDispatcher("/views/restore/new_password_step.jsp").forward(req, resp);
        }else{
            if(jwtAnalysis.isDisabled()){
                req.setAttribute("tokenErrorMessage", new StatusText("restore.token_has_been_disabled"));
            }else if(jwtAnalysis.isExpired()){
                req.setAttribute("tokenErrorMessage", new StatusText("restore.token_has_expired"));
            }

            req.getRequestDispatcher("/views/restore/email_step.jsp").forward(req, resp);
        }
    }

    /**
     * Serves /restore POST queries and designed to send restoration email to user.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        try{
            JSONObject jsonObject = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            usersService.sendRestorationLink(jsonObject.getString("email"));

            sendSuccess(new StatusText("restore.link_sent_successfully", true, StatusStates.SUCCESS).convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.ACQUIRING_ERROR, () -> exceptionToClient.set(new StatusText("restore.email_not_found").convert(getLang(req))));
            }

            if(exceptionToClient.get() == null)
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.debug(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }

    /**
     * Serves /restore PUT queries and designed for updating user`s password (setting it to newly inputted on the restoration page). <br/>
     * If flow is successful, redirects to /login page.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try{
            JSONObject jsonObject = new JSONObject(req.getReader().lines().collect(Collectors.joining()));

            usersService.updatePasswordForAccount(jsonObject.getString("token"), jsonObject.getString("password"));

            sendSuccess(new StatusText("restore.password_changed_successfully", true, StatusStates.SUCCESS).convert(getLang(req)), resp);
        }catch (Exception e){
            AtomicReference<String> exceptionToClient = new AtomicReference<>();

            logger.error(e);

            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.ACQUIRING_ERROR, () -> exceptionToClient.set(new StatusText("restore.unable_to_validate_token").convert(getLang(req))));
                descExc.execute(ExceptionReason.TOKEN_ALREADY_USED, () -> exceptionToClient.set(new StatusText("restore.token_has_been_disabled").convert(getLang(req))));
                descExc.execute(ExceptionReason.TOKEN_ALREADY_EXPIRED, () -> exceptionToClient.set(new StatusText("restore.token_has_expired").convert(getLang(req))));
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("restore.password_validation_failed").convert(getLang(req))));
            }

            if(exceptionToClient.get() == null)
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            logger.debug(exceptionToClient.get());

            sendException(exceptionToClient.get(), resp);
        }
    }
}
