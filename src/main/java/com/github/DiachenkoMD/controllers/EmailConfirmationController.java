package com.github.DiachenkoMD.controllers;

import com.github.DiachenkoMD.sevices.UsersService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/confirmation")
public class EmailConfirmationController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        new UsersService().confirmUserEmail(req, resp);
    }
}
