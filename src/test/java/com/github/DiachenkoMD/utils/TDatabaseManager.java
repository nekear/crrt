package com.github.DiachenkoMD.utils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides methods to set up database before tests. <br />
 * For outside use available only {@link #init(DataSource) init(DataSource)} and {@link #setup() setup()} + {@link #destroy() destroy()} methods.
 */
public class TDatabaseManager {
    private static DataSource ds;
    public static void init(DataSource ds){
        TDatabaseManager.ds = ds;
    }
    public static void setup(){
        initUsersTable();
    }

    public static void destroy(){
        dropUsersTable();
    }

    private static void initUsersTable(){
        exec(
        "CREATE TABLE IF NOT EXISTS `tbl_users` (\n" +
                "`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "`email` VARCHAR(100) NOT NULL,\n" +
                "`password` VARCHAR(255) NOT NULL,\n" +
                "`firstname` VARCHAR(255) NULL,\n" +
                "`surname` VARCHAR(255) NULL,\n" +
                "`patronymic` VARCHAR(255) NULL,\n" +
                "`avatar_path` VARCHAR(255) NULL DEFAULT NULL,\n" +
                "`balance` MEDIUMINT(9) NOT NULL DEFAULT '0',\n" +
                "`role_id` TINYINT(4) NOT NULL DEFAULT '1',\n" +
                "`conf_code` TINYTEXT NULL,\n"+
                "`ts_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "PRIMARY KEY (`id`),\n" +
                "UNIQUE INDEX `email` (`email`)\n" +
                ");",
                "initUsersTable"
        );
    }

    private static void dropUsersTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_users;",
                "dropUsersTable"
        );
    }

    private static void exec(String query, String callerName){
        try(
            Connection con = ds.getConnection();
            Statement stmt = con.createStatement()){
            stmt.executeUpdate(query);
        }catch(SQLException e){
            throw new IllegalArgumentException(String.format("Exception when executing query from %s...", callerName), e);
        }
    }
}
