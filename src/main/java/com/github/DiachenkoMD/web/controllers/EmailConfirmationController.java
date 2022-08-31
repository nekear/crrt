package com.github.DiachenkoMD.web.controllers;

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

import static com.github.DiachenkoMD.entities.Constants.END_CONFIRMATION_RESPONSE;


@WebServlet("/confirmation")
public class EmailConfirmationController extends HttpServlet {

    private final static Logger logger = LogManager.getLogger(EmailConfirmationController.class);

    private UsersService usersService;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        usersService = ((UsersService) getServletContext().getAttribute("users_service"));
    }

    /**
     * Serves /confirmation get-queries. Awaiting url to contain "code" (it is taken from {@link com.github.DiachenkoMD.entities.Constants#REQ_CODE REQ_CODE} parameter. At the end forwards to /confirmation.jsp.
     * @see UsersService#confirmUserEmail(HttpServletRequest, HttpServletResponse)
     * @param req
     * @param resp
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            usersService.confirmUserEmail(req, resp);

            req.setAttribute(END_CONFIRMATION_RESPONSE, new Status("email_conf.conf_success", true, StatusStates.SUCCESS));
        }catch (DescriptiveException e){
            e.execute(ExceptionReason.CONFIRMATION_CODE_EMPTY, () -> req.setAttribute(END_CONFIRMATION_RESPONSE, new Status("email_conf.code_empty", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.CONFIRMATION_NO_SUCH_CODE, () -> req.setAttribute(END_CONFIRMATION_RESPONSE, new Status("email_conf.no_such_code", true, StatusStates.ERROR)));
            e.execute(ExceptionReason.CONFIRMATION_PROCESS_ERROR, () -> req.setAttribute(END_CONFIRMATION_RESPONSE, new Status("email_conf.process_error", true, StatusStates.ERROR)));
        }catch (Exception e){
             req.setAttribute(END_CONFIRMATION_RESPONSE, new Status("global.unexpectedError", true, StatusStates.ERROR));
        }

        try{
            req.getServletContext().getRequestDispatcher("/views/confirmation.jsp").forward(req, resp);
        }catch (IOException | ServletException e){
            logger.error(e);
        }
    }
}
