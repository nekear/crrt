package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.PageGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.roles.ClientRGuard;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@UseGuards({PageGuard.class, AuthGuard.class, ClientRGuard.class})
@WebServlet("/client")
public class ClientController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ClientController.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            getServletContext().getRequestDispatcher("/views/client.jsp").forward(req, resp);
        }catch (Exception e){
            logger.error(e);
        }
    }

}
