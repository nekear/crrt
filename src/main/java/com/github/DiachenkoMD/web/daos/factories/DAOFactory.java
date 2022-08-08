package com.github.DiachenkoMD.web.daos.factories;


import com.github.DiachenkoMD.web.daos.DBTypes;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Factory for producing specific DAOFactories. Firstly, use {@link #init(DBTypes, String) init(DBTypes, String)} to specify <i>type of db</i> and <i>lookup</i> for that db is JNDI.
 * Then freely use {@link #getFactory() getFactory()} to get appropriate factory. This class uses Singleton pattern to create factories. <br/>
 * {@link #getFactory()} can be extended to produce other types of factories (for example Postgresql of MariaDB). <br/>
 * Note: I am generating one (currently mysql) factory on Servet startup at {@link com.github.DiachenkoMD.web.listeners.ContextListener ContextListener}.
 */
public abstract class DAOFactory {
    private static DBTypes dbType;
    private static String lookup;

    public abstract UsersDAO getUsersDAO();
    public abstract CarsDAO getCarsDAO();

    private static DAOFactory activeFactory;

    public static synchronized DAOFactory getFactory(){
        if(activeFactory == null){
            DataSource ds;
            try{
                Context initContext = new InitialContext();
                ds = (DataSource) initContext.lookup(lookup);
            }catch (NamingException e){
                throw new IllegalArgumentException(String.format("Cannot find %s at JNDI!", lookup), e);
            }

            switch (dbType){
                case MYSQL -> activeFactory = new MysqlDAOFactory(ds);
            }
        }

        return activeFactory;
    }

    public static void init(DBTypes type, String lookup){
        DAOFactory.dbType = type;
        DAOFactory.lookup = lookup;
    }

}
