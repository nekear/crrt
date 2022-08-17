package com.github.DiachenkoMD.web.controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
