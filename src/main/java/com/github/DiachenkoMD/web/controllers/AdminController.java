package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.AdminRGuard;
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

@UseGuards({PageGuard.class, AuthGuard.class, AdminRGuard.class})
@WebServlet("/admin")
public class AdminController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(AdminController.class);

    /**
     * GET route to open admin page.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            getServletContext().getRequestDispatcher("/views/admin.jsp").forward(req, resp);
        }catch (ServletException | IOException e){
            logger.error(e);
        }
    }
}
