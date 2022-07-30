package com.github.DiachenkoMD.daos;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private final DataSource ds;

    public Database(String lookupName){
        try{
            Context initContext = new InitialContext();
            ds = (DataSource) initContext.lookup(lookupName);
        }catch (NamingException e){
            throw new IllegalArgumentException(String.format("Cannot find %s at JNDI!", lookupName), e);
        }
    }

    public Connection con(){
        try{
            return ds.getConnection();
        }catch (SQLException e){
            throw new IllegalStateException("Cannot establish connection with DataSource!", e);
        }
    }
}
