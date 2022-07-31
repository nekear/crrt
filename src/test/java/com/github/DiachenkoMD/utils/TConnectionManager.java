package com.github.DiachenkoMD.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TConnectionManager {
    private static Connection con;

    public static Connection getConnection(){
        return con;
    }

    public static Connection openConnection(){
        try{
            Class.forName("org.h2.Driver");

            System.out.println(
                    PropertiesManager.get("test.db.url") + " - " + PropertiesManager.get("test.db.username") + " - " + PropertiesManager.get("test.db.password")
            );

            con = DriverManager.getConnection(
                    PropertiesManager.get("test.db.url"),
                    PropertiesManager.get("test.db.username"),
                    PropertiesManager.get("test.db.password")
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
