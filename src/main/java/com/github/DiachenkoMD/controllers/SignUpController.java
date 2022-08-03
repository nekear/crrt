package com.github.DiachenkoMD.controllers;

import com.github.DiachenkoMD.sevices.UsersService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/register")
public class SignUpController extends HttpServlet {

    /**
     * Serves /register request and returns registration page in response
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/views/register.jsp").forward(req, resp);
    }

    /**
     * Serves /register with post data and at the end redirects to /status with status message (saved to session)
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        new UsersService().registerUser(req, resp);
    }
}
