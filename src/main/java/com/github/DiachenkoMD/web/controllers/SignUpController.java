package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.entities.dto.Status;
import com.github.DiachenkoMD.entities.dto.StatusStates;
import com.github.DiachenkoMD.entities.exceptions.DBException;
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


@WebServlet("/register")
public class SignUpController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(SignUpController.class);

    private UsersService usersService;
    @Override
    public void init(ServletConfig config) throws ServletException {
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
            usersService.registerUser(req, resp);
        }catch (DescriptiveException e){
            // DescriptiveException class has execute() method which accepts execution condition and action to be executed (which is Runnable by its nature)
            e.execute(ExceptionReason.EMAIL_EXISTS, () -> req.getSession().setAttribute("login_prg_message", new Status("sign_up.email_exists", true, e.getArguments(), StatusStates.ERROR)));
            e.execute(ExceptionReason.VALIDATION_ERROR, () -> req.getSession().setAttribute("login_prg_message", new Status("sign.validation_failed", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.REGISTRATION_PROCESS_ERROR, () -> req.getSession().setAttribute("login_prg_message", new Status("global.unexpectedError", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.CONFIRMATION_CODE_ERROR, () -> {
                logger.debug("Error setting code {} to {}", e.getArg("code"), e.getArg("email"));

                req.getSession().setAttribute("login_prg_message", new Status("global.unexpectedError", true, StatusStates.ERROR));
            });
        }catch (Exception e){
            logger.error("Unexpected error", e);
            req.getSession().setAttribute("login_prg_message", new Status("global.unexpectedError", true, StatusStates.ERROR));
        }

        try {
            resp.sendRedirect("status");
        }catch (IOException ignored){}
    }
}
