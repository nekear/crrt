package com.github.DiachenkoMD.web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;

@WebServlet("")
public class IntroController extends HttpServlet {

    private final static Logger logger = LogManager.getLogger(IntroController.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            req.getRequestDispatcher("/views/index.jsp").forward(req, resp);
        }catch (Exception e){
            logger.error(e);
        }
    }
}
