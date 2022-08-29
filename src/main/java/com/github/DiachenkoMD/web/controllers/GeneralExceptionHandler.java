package com.github.DiachenkoMD.web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebServlet("/exception")
public class GeneralExceptionHandler extends HttpServlet {
    private final static Logger logger = LogManager.getLogger(GeneralExceptionHandler.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processError(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processError(req, resp);
    }

    private void processError(HttpServletRequest req, HttpServletResponse resp){
        try{
            Throwable throwable = (Throwable) req.getAttribute("jakarta.servlet.error.exception");

            logger.info("Attributes {}", req.getAttributeNames().toString());

            if(throwable == null) {
                req.getRequestDispatcher("/views/exceptions/404.jsp").forward(req, resp);
                return;
            }

            Integer statusCode = (Integer) req.getAttribute("jakarta.servlet.error.status_code");
            String servletName = (String) req.getAttribute("jakarta.servlet.error.servlet_name");

            if (servletName == null)
                servletName = "Unknown";

            String requestUri = (String) req.getAttribute("jakarta.servlet.error.request_uri");
            if (requestUri == null)
                requestUri = "Unknown";

            logger.fatal("Status code: {}", statusCode);
            logger.fatal("Servlet name: {}", servletName);
            logger.fatal("Request uri: {}", requestUri);
            logger.fatal(throwable);

            req.getRequestDispatcher("/views/exceptions/500.jsp").forward(req, resp);
        }catch (Exception e){
            logger.error(e);
        }
    }
}
