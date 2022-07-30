package com.github.DiachenkoMD.controllers;

import com.github.DiachenkoMD.daos.DBTypes;
import com.github.DiachenkoMD.daos.factories.DAOFactory;
import com.github.DiachenkoMD.dto.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/h2")
public class h2TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try(Connection connection = DriverManager.getConnection("jdbc:h2:~/crrt_test2")){
           System.out.println(connection);
       }catch (SQLException e){
           throw new IllegalArgumentException(e);
       }
    }
}
