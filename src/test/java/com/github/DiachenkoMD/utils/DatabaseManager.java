package com.github.DiachenkoMD.utils;

import java.sql.Connection;

public class DatabaseManager {

    public static void init(Connection con){

    }

    private static void initUsersTable(Connection con){
        exec(
        "CREATE TABLE IF NOT EXISTS `tbl_users` (\n" +
                "`id` int NOT NULL AUTO_INCREMENT,\n" +
                "`email` varchar(100) DEFAULT NULL,\n" +
                "`password` varchar(100) DEFAULT NULL,\n" +
                "  `username` varchar(255) DEFAULT NULL,\n" +
                "  `surname` varchar(255) DEFAULT NULL,\n" +
                "  `patronymic` varchar(255) DEFAULT NULL,\n" +
                "  `avatar_path` varchar(255) DEFAULT NULL,\n" +
                "  `balance` mediumint DEFAULT '0',\n" +
                "  `role_id` tinyint DEFAULT '1',\n" +
                "  `ts_created` timestamp NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `email` (`email`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;",
                con
        );
    }

    private static void exec(String query, Connection con){

    }
}
