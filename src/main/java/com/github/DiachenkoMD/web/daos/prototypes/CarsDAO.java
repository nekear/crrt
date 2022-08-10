package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.Image;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface CarsDAO {
    List<Car> getAll() throws DBException;

    Optional<Car> get(int carId) throws DBException;

    int create(Car car) throws DBException;

    void addImages(int carId, List<String> images) throws DBException;

    int addImage(int carId, String image) throws DBException;

    Optional<Image> getImage(int imageId) throws DBException;

    boolean deleteImage(int imageId) throws DBException;

    boolean update(Car car) throws DBException;

    boolean delete(int carId) throws DBException;
}
