package com.github.DiachenkoMD.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Provides a bunch of methods to create better readability of JUnit DAO Extensions. <br />
 * Extensions use <ul>
 *     <li>{@link #openConnection() openConnection} to return available connection</li>
 *     <li>{@link #getConnection() getConnection} to open new connection. (if not closed, previous connection will be lost)</li>
 *     <li>{@link #closeConnection() closeConnection}to close existing connection.</li>
 */
public class TConnectionManager {
    private static Connection con;

    public static Connection getConnection(){
        return con;
    }

    public static Connection openConnection(){
        try{
            Class.forName("org.h2.Driver");

            ResourceBundle appPropsBundle = ResourceBundle.getBundle("app");

            String url = appPropsBundle.getString("test.db.url");
            String username = appPropsBundle.getString("test.db.username");
            String password = appPropsBundle.getString("test.db.password");

            con = DriverManager.getConnection(
                    url,
                    username,
                    password
            );

            return con;
        }catch (ClassNotFoundException | SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(){
        try{
            con.close();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
