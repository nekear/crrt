package com.github.DiachenkoMD.web.daos.impls.mysql;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.generateRandomString;

public class MysqlCarsDAO implements CarsDAO {
    private static final Logger logger = LogManager.getLogger(MysqlCarsDAO.class);
    private final DataSource ds;

    public MysqlCarsDAO(DataSource ds){
        this.ds = ds;
    }

    @Override
    public List<Car> getAll() throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM tbl_cars");
        ){
            List<Car> foundCars = new ArrayList<>();
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next())
                    foundCars.add(Car.of(rs));
            }
            return foundCars;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }
}
