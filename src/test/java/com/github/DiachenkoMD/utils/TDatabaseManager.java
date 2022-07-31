package com.github.DiachenkoMD.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TDatabaseManager {
    public static void init(Connection con){
        initUsersTable(con);
    }

    public static void destroy(Connection con){
        dropUsersTable(con);
    }

    private static void initUsersTable(Connection con){
        exec(
        "CREATE TABLE IF NOT EXISTS `tbl_users` (\n" +
                "`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "`email` VARCHAR(100) NOT NULL,\n" +
                "`password` VARCHAR(255) NOT NULL,\n" +
                "`username` VARCHAR(255) NULL,\n" +
                "`surname` VARCHAR(255) NULL,\n" +
                "`patronymic` VARCHAR(255) NULL,\n" +
                "`avatar_path` VARCHAR(255) NULL DEFAULT NULL,\n" +
                "`balance` MEDIUMINT(9) NOT NULL DEFAULT '0',\n" +
                "`role_id` TINYINT(4) NOT NULL DEFAULT '1',\n" +
                "`ts_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "PRIMARY KEY (`id`),\n" +
                "UNIQUE INDEX `email` (`email`)\n" +
                ");",
                con,
                "initUsersTable"
        );
    }

    private static void dropUsersTable(Connection con){
        exec(
                "DROP TABLE IF EXISTS tbl_users;",
                con,
                "dropUsersTable"
        );
    }

    private static void exec(String query, Connection con, String callerName){
        try(Statement stmt = con.createStatement()){
            stmt.executeUpdate(query);
        }catch(SQLException e){
            throw new IllegalArgumentException(String.format("Exception when executing query from %s...", callerName), e);
        }
    }
}
