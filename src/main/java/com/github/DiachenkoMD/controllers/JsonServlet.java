package com.github.DiachenkoMD.controllers;

import com.github.DiachenkoMD.daos.DBTypes;
import com.github.DiachenkoMD.daos.factories.DAOFactory;
import com.github.DiachenkoMD.dto.User;
import com.github.DiachenkoMD.sevices.UsersService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/json")
public class JsonServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        new UsersService().registerUser(req, resp);
    }
}
