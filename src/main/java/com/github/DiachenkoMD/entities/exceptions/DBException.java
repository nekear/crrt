package com.github.DiachenkoMD.entities.exceptions;

import java.sql.SQLException;

public class DBException extends SQLException {

    public DBException(String message){
        super(message);
    }
    public DBException(SQLException e) {
        super(e);
    }
}
