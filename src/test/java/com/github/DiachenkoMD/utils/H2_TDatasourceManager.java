package com.github.DiachenkoMD.utils;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.ResourceBundle;

/**
 * Provides single method to return H2 Datasource instance. Uses Singleton pattern. <br />
 * To get Datasource from that class use {@link #getDataSource() getDataSource()} method.
 */
public class H2_TDatasourceManager {
    private static DataSource ds;
    public static synchronized DataSource getDataSource(){

        if(ds == null){
            ResourceBundle appPropsBundle = ResourceBundle.getBundle("app");

            String url = appPropsBundle.getString("test.db.h2.url");
            String username = appPropsBundle.getString("test.db.h2.username");
            String password = appPropsBundle.getString("test.db.h2.password");

            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setUrl(url);
            dataSource.setUser(username);
            dataSource.setPassword(password);

            ds = dataSource;
        }

        return ds;
    }
}
