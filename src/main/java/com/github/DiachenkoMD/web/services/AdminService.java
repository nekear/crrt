package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AdminService {

    private static final Logger logger = LogManager.getLogger(AdminService.class);
    private final UsersDAO usersDAO;
    private final CarsDAO carsDAO;

    public AdminService(UsersDAO usersDAO, CarsDAO carsDAO){
        this.usersDAO = usersDAO;
        this.carsDAO = carsDAO;
    }

    // TODO: add stats
    public List<Double> getStats(){
        return List.of(1000d, 1001d, 10d);
    }

    public List<Car> getCars() throws DBException {
        List<Car> cars = carsDAO.getAll();

        cars.parallelStream().forEach(c -> {
            try {
                c.encrypt();
            } catch (DescriptiveException e) {
                logger.error(e);
            }
        });

        return cars;
    }
}
