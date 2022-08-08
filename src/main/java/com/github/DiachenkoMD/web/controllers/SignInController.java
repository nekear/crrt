package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.entities.enums.StatusStates;
import com.github.DiachenkoMD.entities.dto.StatusText;
import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.pinger.Pinger;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;
import static com.github.DiachenkoMD.web.utils.Utils.getLang;

@WebServlet("/login")
public class SignInController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(SignInController.class);
    private UsersService usersService;
    private Pinger pinger;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        usersService = ((UsersService) config.getServletContext().getAttribute("users_service"));
        pinger = ((Pinger) config.getServletContext().getAttribute("pinger"));
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
            User logged_in = usersService.loginUser(req, resp);

            req.getSession().setAttribute(SESSION_AUTH, logged_in);

            resp.setStatus(200);
        }catch (Exception e) { // including DescriptiveException
            AtomicReference<String> exceptionToClient = new AtomicReference<>("");

            logger.error(e);
            if (e instanceof DescriptiveException descExc) {
                descExc.execute(ExceptionReason.VALIDATION_ERROR, () -> exceptionToClient.set(new StatusText("login.validation_failed", true, StatusStates.ERROR).convert("en")));
                descExc.execute(ExceptionReason.LOGIN_USER_NOT_FOUND, () -> exceptionToClient.set(new StatusText("login.incorrect_data", true, StatusStates.ERROR).convert("en")));
                descExc.execute(ExceptionReason.LOGIN_NOT_CONFIRMED, () -> exceptionToClient.set(new StatusText("login.account_not_confirmed", true, StatusStates.ERROR).convert("en")));
                descExc.execute(ExceptionReason.LOGIN_WRONG_PASSWORD, () -> exceptionToClient.set(new StatusText("login.incorrect_data", true, StatusStates.ERROR).convert("en")));
            }

            if(exceptionToClient.get() == null)
                exceptionToClient.set(new StatusText("global.unexpectedError").convert(getLang(req)));

            try {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(exceptionToClient.get());
                resp.getWriter().flush();
            } catch (IOException ioExc) {
                pinger.omit(ioExc);
            }
        }
    }
}
