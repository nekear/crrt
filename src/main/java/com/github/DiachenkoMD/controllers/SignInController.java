package com.github.DiachenkoMD.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/login")
public class SignInController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(SignInController.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestData = req.getReader().lines().collect(Collectors.joining());

        JSONObject acquiredData = new JSONObject(requestData);

        String email = acquiredData.getString("email");
        String password = acquiredData.getString("password");

        resp.setContentType("text/plain");
        resp.setStatus(500);
        resp.getWriter().write("This is an exception!");
    }
}
