package com.github.DiachenkoMD.daos.factories;


import com.github.DiachenkoMD.daos.*;
import com.github.DiachenkoMD.daos.prototypes.UsersDAO;

public abstract class DAOFactory {
    private static String mysqlLookup = "java:comp/env/jdbc/mysql/crrt";

    public abstract UsersDAO getUsersDAO();

    private static DAOFactory mysql_factory;

    public static synchronized DAOFactory getFactory(DBTypes dbType){
        switch (dbType){
            case MYSQL -> {
                if(mysql_factory == null)
                    mysql_factory = new MysqlDAOFactory(new Database(mysqlLookup));

                return mysql_factory;
            }
            default -> {return null;}
        }
    }
}
