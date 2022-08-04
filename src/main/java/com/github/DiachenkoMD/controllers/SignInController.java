package com.github.DiachenkoMD.controllers;

import com.github.DiachenkoMD.dto.ValidationParameters;
import com.github.DiachenkoMD.exceptions.DescriptiveException;
import com.github.DiachenkoMD.exceptions.ExceptionReason;
import com.github.DiachenkoMD.sevices.UsersService;
import com.sun.mail.util.DecodingException;
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

import static com.github.DiachenkoMD.utils.Utils.validate;

@WebServlet("/login")
public class SignInController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        new UsersService().loginUser(req, resp);
    }
}
