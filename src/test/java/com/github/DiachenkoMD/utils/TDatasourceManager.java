package com.github.DiachenkoMD.utils;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.ResourceBundle;

/**
 * Provides single method to return H2 Datasource instance. Uses Singleton pattern. <br />
 * To get Datasource from that class use {@link #getDataSource() getDataSource()} method.
 */
public class TDatasourceManager {
    private static DataSource ds;
    public static synchronized DataSource getDataSource(){

        if(ds == null){
            ResourceBundle appPropsBundle = ResourceBundle.getBundle("app");

            String url = appPropsBundle.getString("test.db.url");
            String username = appPropsBundle.getString("test.db.username");
            String password = appPropsBundle.getString("test.db.password");

            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setUrl(url);
            dataSource.setUser(username);
            dataSource.setPassword(password);

            ds = dataSource;
        }

        return ds;
    }
}
