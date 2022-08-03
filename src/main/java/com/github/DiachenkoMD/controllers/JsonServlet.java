package com.github.DiachenkoMD.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebServlet("/json")
public class JsonServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(JsonServlet.class);
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("loggy");
        logger.debug("Request on /json doPost method!");
        logger.warn("Request on /json doPost method!");
        logger.info("Request on /json doPost method!");
        logger.error("Request on /json doPost method!");
    }
}
