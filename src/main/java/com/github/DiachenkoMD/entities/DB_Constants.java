package com.github.DiachenkoMD.entities;

public class DB_Constants {
    public static final String TBL_USERS_USER_ID = "id";
    public static final String TBL_USERS_EMAIL = "email";
    public static final String TBL_USERS_PASSWORD = "password";
    public static final String TBL_USERS_FIRSTNAME = "firstname";
    public static final String TBL_USERS_SURNAME = "surname";
    public static final String TBL_USERS_PATRONYMIC = "patronymic";
    public static final String TBL_USERS_AVATAR = "avatar";
    public static final String TBL_USERS_BALANCE = "balance";
    public static final String TBL_USERS_ROLE_ID = "role_id";
    public static final String TBL_USERS_CONF_CODE = "conf_code";
    public static final String TBL_USERS_TS_CREATED = "ts_created";
    public static final String TBL_USERS_IS_BLOCKED = "is_blocked";


    public static final String TBL_CARS_ID = "id";
    public static final String TBL_CARS_BRAND = "brand";
    public static final String TBL_CARS_MODEL = "model";
    public static final String TBL_CARS_SEGMENT_ID = "segment_id";
    public static final String TBL_CARS_PRICE = "price";
    public static final String TBL_CARS_CITY_ID = "city_id";
    public static final String TBL_CARS_TS_EDITED = "ts_edited";

    public static final String TBL_CARS_PHOTOS_ID = "id";
    public static final String TBL_CARS_PHOTOS_CAR_ID = "car_id";
    public static final String TBL_CARS_PHOTOS_PHOTO = "photo";


    public static final String TBL_INVOICES_ID = "id";
    public static final String TBL_INVOICES_CODE = "code";
    public static final String TBL_INVOICES_CAR_ID = "car_id";
    public static final String TBL_INVOICES_DRIVER_ID = "driver_id";
    public static final String TBL_INVOICES_CLIENT_ID = "client_id";
    public static final String TBL_INVOICES_EXP_PRICE = "exp_price";
    public static final String TBL_INVOICES_DATE_START = "date_start";
    public static final String TBL_INVOICES_DATE_END = "date_end";
    public static final String TBL_INVOICES_IS_PAID = "is_paid";
    public static final String TBL_INVOICES_IS_CANCELED = "is_canceled";
    public static final String TBL_INVOICES_IS_REJECTED = "is_rejected";
    public static final String TBL_INVOICES_REJECT_REASON = "reject_reason";
    public static final String TBL_INVOICES_PASSPORT_ID = "passport_id";
    public static final String TBL_INVOICES_TS_CREATED = "ts_created";


    public static final String TBL_DRIVERS_ID="id";
    public static final String TBL_DRIVERS_CODE="code";
    public static final String TBL_DRIVERS_USER_ID="user_id";
    public static final String TBL_DRIVERS_CITY_ID="city_id";
}
