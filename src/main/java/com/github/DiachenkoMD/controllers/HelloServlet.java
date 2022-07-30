package com.github.DiachenkoMD.controllers;

import com.github.DiachenkoMD.daos.DBTypes;
import com.github.DiachenkoMD.daos.factories.DAOFactory;
import com.github.DiachenkoMD.daos.factories.MysqlDAOFactory;
import com.github.DiachenkoMD.dto.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       DAOFactory mysqlDAOFactory = DAOFactory.getFactory(DBTypes.MYSQL);

       List<User> foundUsers = mysqlDAOFactory.getUsersDAO().getAll();

       req.setAttribute("usersList", foundUsers);

       System.out.println(foundUsers);
       getServletContext().getRequestDispatcher("/views/users.jsp").forward(req, resp);
    }
}
