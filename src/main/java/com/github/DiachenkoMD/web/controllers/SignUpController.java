package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.entities.Constants;
import com.github.DiachenkoMD.entities.dto.Status;
import com.github.DiachenkoMD.entities.enums.StatusStates;
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
import java.util.HashMap;
import java.util.Map;


@WebServlet("/register")
public class SignUpController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(SignUpController.class);

    private UsersService usersService;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        usersService = ((UsersService) getServletContext().getAttribute("users_service"));
    }

    /**
     * Serves /register request and returns registration page in response
     * @param req
     * @param resp
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            getServletContext().getRequestDispatcher("/views/register.jsp").forward(req, resp);
        }catch (ServletException | IOException e){
            logger.error(e);
        }
    }

    /**
     * Serves /register with post data and at the end redirects to /status with status message (saved to session)
     * @param req
     * @param resp
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String registeredEmail = usersService.registerUser(req, resp);

            req.getSession().setAttribute(Constants.END_PRG_STATUS, new Status("sign_up.verify_email", true, new HashMap<>(Map.of("email", registeredEmail)), StatusStates.SUCCESS));
        }catch (DescriptiveException e){
            // DescriptiveException class has execute() method which accepts execution condition and action to be executed (which is Runnable by its nature)
            e.execute(ExceptionReason.VALIDATION_ERROR, () -> req.getSession().setAttribute(Constants.END_PRG_STATUS, new Status("sign.validation_failed")));
            e.execute(ExceptionReason.EMAIL_EXISTS, () -> req.getSession().setAttribute(Constants.END_PRG_STATUS, new Status("sign_up.email_exists")));
            e.execute(ExceptionReason.RECAPTCHA_VERIFICATION_ERROR, () -> req.getSession().setAttribute(Constants.END_PRG_STATUS, new Status("sign_up.recaptcha_verification_error")));
            e.execute(ExceptionReason.REGISTRATION_PROCESS_ERROR, () -> req.getSession().setAttribute(Constants.END_PRG_STATUS, new Status("global.unexpectedError")));
        }catch (Exception e){
            logger.error("Unexpected error", e);
            req.getSession().setAttribute(Constants.END_PRG_STATUS, new Status("global.unexpectedError"));
        }

        try {
            resp.sendRedirect("status");
        }catch (IOException e){
            logger.error("Strange redirect to /status error: {}", e.getMessage());
        }
    }
}
