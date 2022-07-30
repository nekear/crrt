package com.github.DiachenkoMD.daos.factories;

import com.github.DiachenkoMD.daos.Database;
import com.github.DiachenkoMD.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.daos.prototypes.UsersDAO;

public class MysqlDAOFactory extends DAOFactory{
    private final Database db;

    public MysqlDAOFactory(Database db){
        this.db = db;
    }

    @Override
    public UsersDAO getUsersDAO() {
        return new MysqlUsersDAO(db.con());
    }
}
