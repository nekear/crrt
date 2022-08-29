package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.PageGuard;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;

@UseGuards({PageGuard.class, AuthGuard.class})
@WebServlet("/exit")
public class ExitController extends HttpServlet {

    private final static Logger logger = LogManager.getLogger(ExitController.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            req.getSession().removeAttribute(SESSION_AUTH);
            resp.sendRedirect("login");
        }catch (Exception e){
            logger.error(e);
        }
    }
}
