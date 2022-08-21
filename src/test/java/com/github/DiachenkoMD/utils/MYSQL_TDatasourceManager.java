package com.github.DiachenkoMD.utils;

import com.github.DiachenkoMD.extensions.StateStore;
import com.github.DiachenkoMD.extensions.TestDisableReason;
import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Provides single method to return Mysql Datasource instance. Uses Singleton pattern. <br />
 * To get Datasource from that class use {@link #getDataSource() getDataSource()} method. <br/>
 * Unlike {@link H2_TDatasourceManager}, this datasource executes queries is a lot slower and used only some times where H2 is not supported,
 * for example at {@link com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO#getPanelInvoicesWithFilters(HashMap, List, int, int) here}.
 * H2 lacks of normal support for FULL TEXT search and even in mysql compatibility mode it doesn`t support mysql-like MATCH AGAINST syntax.<br/>
 * I know I could just that few functions without tests but wanna try make this thing possible).
 */
public class MYSQL_TDatasourceManager {
    private static DataSource ds;
    public static synchronized DataSource getDataSource(){
            try{
                if(ds == null){
                    ResourceBundle appPropsBundle = ResourceBundle.getBundle("app");

                    String url = appPropsBundle.getString("test.db.mysql.url");
                    String username = appPropsBundle.getString("test.db.mysql.username");
                    String password = appPropsBundle.getString("test.db.mysql.password");

                    MysqlDataSource dataSource = new MysqlDataSource();
                    dataSource.setUrl(url);
                    dataSource.setUser(username);
                    dataSource.setPassword(password);

                    ds = dataSource;
                }

                // Testing connection to disable tests if needed
                try(Connection con = ds.getConnection()){}
            }catch (Exception e){
                StateStore.mysqlTestsBlocked = new TestDisableReason(e.getMessage());
            }

        return ds;
    }
}
