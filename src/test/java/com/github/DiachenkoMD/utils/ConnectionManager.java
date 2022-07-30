package com.github.DiachenkoMD.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static Connection con;

    public static Connection getConnection(){
        return con;
    }

    public static Connection openConnection(){
        try{
            Class.forName("org.h2.Driver");

            con = DriverManager.getConnection("jdbc:h2:~/crrt_test", "tester", "");

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
