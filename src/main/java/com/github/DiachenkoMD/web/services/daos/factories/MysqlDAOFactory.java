package com.github.DiachenkoMD.web.services.daos.factories;

import com.github.DiachenkoMD.web.services.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.services.daos.prototypes.UsersDAO;

import javax.sql.DataSource;

public class MysqlDAOFactory extends DAOFactory{
    private final DataSource ds;

    public MysqlDAOFactory(DataSource ds){
        this.ds = ds;
    }

    @Override
    public UsersDAO getUsersDAO() {
        return new MysqlUsersDAO(ds);
    }
}
