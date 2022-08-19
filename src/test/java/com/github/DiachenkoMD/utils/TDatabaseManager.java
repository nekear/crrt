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
        initCarsTable();
        initCarsPhotosTable();
        initDriversTable();
        initPassportTable();
        initInvoicesTable();
        initRepairInvoicesTable();
        initGlueCarPhotosFunction();
        initGetActiveRepairsByInvoiceIdFunction();
        initGetExpiredRepairsByInvoiceIdFunction();
    }

    public static void destroy(){
        dropGlueCarPhotosFunction();
        dropGetActiveRepairsByInvoiceIdFunction();
        dropGetExpiredRepairsByInvoiceIdFunction();
        dropRepairInvoicesTable();
        dropInvoicesTable();
        dropDriversTable();
        dropPassportTable();
        dropUsersTable();
        dropCarsPhotosTable();
        dropCarsTable();
    }

    private static void initUsersTable(){
        exec(
                "create table tbl_users\n" +
                        "(\n" +
                        "    id         int auto_increment\n" +
                        "        primary key,\n" +
                        "    email      varchar(100)                        not null,\n" +
                        "    password   varchar(255)                        not null,\n" +
                        "    firstname  varchar(255)                        null,\n" +
                        "    surname    varchar(255)                        null,\n" +
                        "    patronymic varchar(255)                        null,\n" +
                        "    avatar     varchar(255)                        null,\n" +
                        "    balance    float     default 0                 not null,\n" +
                        "    role_id    tinyint   default 1                 not null,\n" +
                        "    is_blocked tinyint   default 2                 null,\n" +
                        "    conf_code  tinytext                            null,\n" +
                        "    ts_created timestamp default CURRENT_TIMESTAMP not null,\n" +
                        "    constraint email\n" +
                        "        unique (email)\n" +
                        ");\n",
                "initUsersTable"
        );
    }

    private static void initCarsTable(){
        exec(
                "CREATE TABLE `tbl_cars` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`brand` VARCHAR(100) NOT NULL,\n" +
                        "\t`model` VARCHAR(100) NOT NULL,\n" +
                        "\t`segment_id` TINYINT(4) NOT NULL,\n" +
                        "\t`price` DOUBLE NOT NULL,\n" +
                        "\t`city_id` TINYINT(4) NOT NULL,\n" +
                        "\t`ts_edited` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`id`)\n"+
                        ");",
                "initCarsTable"
        );
    }

    private static void initCarsPhotosTable(){
        exec(
                "CREATE TABLE `tbl_cars_photos` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`car_id` INT(11) NOT NULL,\n" +
                        "\t`photo` CHAR(255) NOT NULL,\n" +
                        "\t`ts_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`id`),\n" +
                        "\tINDEX `FK_tbl_cars_photos_tbl_cars` (`car_id`),\n" +
                        "\tCONSTRAINT `FK_tbl_cars_photos_tbl_cars` FOREIGN KEY (`car_id`) REFERENCES `tbl_cars` (`id`) ON DELETE CASCADE\n" +
                        ");",
                "initCarsPhotosTable"
        );
    }

    private static void initDriversTable(){
        exec(
                "CREATE TABLE `tbl_drivers` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`user_id` INT(11) NOT NULL,\n" +
                        "\t`city_id` INT(11) NOT NULL,\n" +
                        "\tPRIMARY KEY (`id`),\n" +
                        "\tUNIQUE INDEX `user_id` (`user_id`),\n" +
                        "\tCONSTRAINT `FK_tbl_drivers_tbl_users` FOREIGN KEY (`user_id`) REFERENCES `tbl_users` (`id`) ON UPDATE CASCADE\n" +
                        ");",
                "initDriversTable"
        );
    }

    private static void initPassportTable(){
        exec(
                "CREATE TABLE `tbl_passport` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`firstname` VARCHAR(100) NULL DEFAULT NULL,\n" +
                        "\t`surname` VARCHAR(100) NULL DEFAULT NULL,\n" +
                        "\t`patronymic` VARCHAR(100) NULL DEFAULT NULL,\n" +
                        "\t`date_of_birth` DATE NULL DEFAULT NULL,\n" +
                        "\t`date_of_issue` DATE NULL DEFAULT NULL,\n" +
                        "\t`doc_number` BIGINT(20) NULL DEFAULT NULL,\n" +
                        "\t`rntrc` BIGINT(20) NULL DEFAULT NULL,\n" +
                        "\t`authority` INT(11) NULL DEFAULT NULL,\n" +
                        "\t`ts_created` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`id`)\n" +
                        ");",
                "initPassportTable"
        );
    }

    private static void initInvoicesTable(){
        exec(
                "CREATE TABLE `tbl_invoices` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`code` CHAR(12) NOT NULL,\n" +
                        "\t`car_id` INT(11) NOT NULL,\n" +
                        "\t`driver_id` INT(11) NULL DEFAULT NULL,\n" +
                        "\t`client_id` INT(11) NOT NULL,\n" +
                        "\t`exp_price` INT(11) NOT NULL,\n" +
                        "\t`date_start` DATE NOT NULL,\n" +
                        "\t`date_end` DATE NOT NULL,\n" +
                        "\t`is_canceled` TINYINT(4) NOT NULL DEFAULT '0',\n" +
                        "\t`is_rejected` TINYINT(4) NOT NULL DEFAULT '0',\n" +
                        "\t`reject_reason` VARCHAR(50) NULL DEFAULT NULL,\n" +
                        "\t`passport_id` INT(11) NOT NULL,\n" +
                        "\t`ts_created` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`id`),\n" +
                        "\tUNIQUE INDEX `code` (`code`),\n" +
                        "\tINDEX `FK_tbl_invoices_tbl_users` (`client_id`),\n" +
                        "\tINDEX `FK_tbl_invoices_tbl_cars` (`car_id`),\n" +
                        "\tINDEX `FK_tbl_invoices_tbl_drivers` (`driver_id`),\n" +
                        "\tINDEX `FK_tbl_invoices_tbl_passport` (`passport_id`),\n" +
                        "\tCONSTRAINT `FK_tbl_invoices_tbl_cars` FOREIGN KEY (`car_id`) REFERENCES `tbl_cars` (`id`) ON UPDATE CASCADE,\n" +
                        "\tCONSTRAINT `FK_tbl_invoices_tbl_drivers` FOREIGN KEY (`driver_id`) REFERENCES `tbl_drivers` (`id`) ON UPDATE CASCADE ON DELETE SET NULL,\n" +
                        "\tCONSTRAINT `FK_tbl_invoices_tbl_passport` FOREIGN KEY (`passport_id`) REFERENCES `tbl_passport` (`id`),\n" +
                        "\tCONSTRAINT `FK_tbl_invoices_tbl_users` FOREIGN KEY (`client_id`) REFERENCES `tbl_users` (`id`) ON UPDATE CASCADE\n" +
                        ");",
                "initInvoicesTable"
        );
    }

    private static void initRepairInvoicesTable(){
        exec(
                "CREATE TABLE `tbl_repair_invoices` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`invoice_id` INT(11) NOT NULL,\n" +
                        "\t`price` DOUBLE NOT NULL,\n" +
                        "\t`expiration_date` DATE NOT NULL,\n" +
                        "\t`comment` VARCHAR(1000) NULL DEFAULT NULL,\n" +
                        "\t`is_paid` TINYINT(1) NULL DEFAULT '0',\n" +
                        "\t`ts_created` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\t`ts_edited` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`id`),\n" +
                        "\tINDEX `FK_tbl_repair_invoices_tbl_invoices` (`invoice_id`),\n" +
                        "\tCONSTRAINT `FK_tbl_repair_invoices_tbl_invoices` FOREIGN KEY (`invoice_id`) REFERENCES `tbl_invoices` (`id`) ON UPDATE CASCADE\n" +
                        ");",
                "initRepairInvoicesTable"
        );
    }

    private static void initGlueCarPhotosFunction(){
        exec(
                "CREATE ALIAS glueCarPhotos FOR \"com.github.DiachenkoMD.utils.TH2Functions.glueCarPhotos\";",
                "initGlueCarPhotosFunction"
        );
    }

    private static void initGetActiveRepairsByInvoiceIdFunction(){
        exec(
                "CREATE ALIAS getActiveRepairsByInvoiceId FOR \"com.github.DiachenkoMD.utils.TH2Functions.getActiveRepairsByInvoiceId\";",
                "initGetActiveRepairsByInvoiceIdFunction"
        );
    }

    private static void initGetExpiredRepairsByInvoiceIdFunction(){
        exec(
                "CREATE ALIAS getExpiredRepairsByInvoiceId FOR \"com.github.DiachenkoMD.utils.TH2Functions.getExpiredRepairsByInvoiceId\";",
                "initGetExpiredRepairsByInvoiceIdFunction"
        );
    }

    private static void dropUsersTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_users;",
                "dropUsersTable"
        );
    }

    private static void dropCarsPhotosTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_cars_photos;",
                "dropCarsPhotosTable"
        );
    }

    private static void dropCarsTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_cars;",
                "dropCarsTable"
        );
    }

    private static void dropDriversTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_drivers;",
                "dropDriversTable"
        );
    }

    private static void dropPassportTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_passport;",
                "dropPassportTable"
        );
    }

    private static void dropInvoicesTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_invoices;",
                "dropPassportTable"
        );
    }

    private static void dropRepairInvoicesTable(){
        exec(
                "DROP TABLE IF EXISTS tbl_repair_invoices;",
                "dropRepairInvoicesTable"
        );
    }

    private static void dropGlueCarPhotosFunction(){
        exec(
                "DROP ALIAS IF EXISTS glueCarPhotos;",
                "dropGlueCarPhotosFunction"
        );
    }

    private static void dropGetActiveRepairsByInvoiceIdFunction(){
        exec(
                "DROP ALIAS IF EXISTS getActiveRepairsByInvoiceId;",
                "dropGetActiveRepairsByInvoiceIdFunction"
        );
    }
    private static void dropGetExpiredRepairsByInvoiceIdFunction(){
        exec(
                "DROP ALIAS IF EXISTS getExpiredRepairsByInvoiceId;",
                "dropGetExpiredRepairsByInvoiceIdFunction"
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
