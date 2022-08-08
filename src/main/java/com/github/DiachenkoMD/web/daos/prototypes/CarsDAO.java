package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface CarsDAO {
    List<Car> getAll() throws DBException;
}
