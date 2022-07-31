package com.github.DiachenkoMD.sevices;

import com.github.DiachenkoMD.daos.DBTypes;
import com.github.DiachenkoMD.daos.factories.DAOFactory;
import com.github.DiachenkoMD.daos.factories.MysqlDAOFactory;
import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.dto.User;
import com.github.DiachenkoMD.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.stream.Collectors;

public class UsersService {

    private final UsersDAO usersDAO;

    public UsersService(){
        DAOFactory factory = DAOFactory.getFactory(DBTypes.MYSQL);
        usersDAO = factory.getUsersDAO();
    }

    public UsersService(UsersDAO usersDAO){
        this.usersDAO = usersDAO;
    }

    public boolean registerUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(Collectors.joining("\n"));

        User registeringUser = Utils.flatJsonParser(jsonBody, User.class);

        return true;
    }
}
