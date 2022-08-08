package com.github.DiachenkoMD.web.daos.factories;

import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;

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
    public CarsDAO getCarsDAO() {
        return new MysqlCarsDAO(ds);
    }
}
