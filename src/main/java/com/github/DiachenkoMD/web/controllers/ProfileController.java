package com.github.DiachenkoMD.web.controllers;

import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.pinger.Pinger;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebServlet("/profile")
public class ProfileController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ProfileController.class);
    private UsersService usersService;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        usersService = ((UsersService) config.getServletContext().getAttribute("users_service"));
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try{
            getServletContext().getRequestDispatcher("/views/profile.jsp").forward(req, resp);
        }catch (ServletException | IOException e){
            logger.error(e);
        }
    }
}
