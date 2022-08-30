package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.UsersService;
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

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;
import static com.github.DiachenkoMD.web.utils.Utils.getLang;
import static com.github.DiachenkoMD.web.utils.Utils.sendException;


@WebServlet("/login")
public class SignInController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(SignInController.class);
    private UsersService usersService;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        usersService = ((UsersService) config.getServletContext().getAttribute("users_service"));
    }

    /**
     * Serves /login get-queries and redirects to login page
     * @param req
     * @param resp
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try {
            getServletContext().getRequestDispatcher("/views/login.jsp").forward(req, resp);
        }catch (ServletException | IOException e){
            logger.error(e);
        }
    }

    /**
     * Serves /login post-queries (from js) and at the end logins user (redirection is held by js-side). <br/>
     * @param req - should contain {@link com.github.DiachenkoMD.entities.Constants#REQ_EMAIL REQ_EMAIL} and {@link com.github.DiachenkoMD.entities.Constants#REQ_PASSWORD REQ_PASSWORD} parameters.
     * @param resp
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        try {
            Map.Entry<AuthUser, Boolean> loginResponse = usersService.loginUser(req, resp);

            req.getSession().setAttribute(SESSION_AUTH, loginResponse.getKey());

            if(loginResponse.getValue()) // if we should remember current session
                req.getSession().setMaxInactiveInterval(31536000); // 60 x 60 x 24 x 365

            resp.setStatus(HttpServletResponse.SC_OK);
        }catch (Exception e) { // including DescriptiveException
            String lang = getLang(req);
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);
            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("login.validation_failed", true, StatusStates.ERROR).convert(lang)));
                descExc.execute(ExceptionReason.LOGIN_USER_NOT_FOUND, () -> exceptionToClient.set(new StatusText("login.incorrect_data", true, StatusStates.ERROR).convert(lang)));
                descExc.execute(ExceptionReason.LOGIN_NOT_CONFIRMED, () -> exceptionToClient.set(new StatusText("login.account_not_confirmed", true, StatusStates.ERROR).convert(lang)));
                descExc.execute(ExceptionReason.LOGIN_WRONG_PASSWORD, () -> exceptionToClient.set(new StatusText("login.incorrect_data", true, StatusStates.ERROR).convert(lang)));
            }

            if(exceptionToClient.get().isEmpty())
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(lang));

            sendException(exceptionToClient.get(), resp);
        }
    }
}
