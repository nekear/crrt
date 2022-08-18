package com.github.DiachenkoMD.web.daos.prototypes;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.Image;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.exceptions.DBException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CarsDAO {

    /**
     * Method for getting all car entities in the db.
     * @return list of {@link Car} but inside omits {@link Car#images} field.
     * @throws DBException
     */
    List<Car> getAll() throws DBException;

    /**
     * Method for getting car entity by car id.
     * @param carId
     * @return
     * @throws DBException
     */
    Optional<Car> get(int carId) throws DBException;


    /**
     * Method for creating Car. Waiting for filled car entity and returns newly generated id. <br/>
     * Unlike {@link UsersDAO#register(LimitedUser, String) register} or {@link UsersDAO#completeRegister(AuthUser, String) completeRegister} doesn`t place newly generated id inside incoming {@link Car} object.
     * @param car entity with filled data that will be inserted into db.
     * @return newly generated car id.
     * @throws DBException
     */
    int create(Car car) throws DBException;


    /**
     * Method for adding images. Inside works with batching.
     * @param carId
     * @param images list of images` file names.
     * @throws DBException
     */
    void addImages(int carId, List<String> images) throws DBException;


    /**
     * Method for adding images to specified car with specified name.
     * @param carId
     * @param image image file name
     * @return newly generated image id
     * @throws DBException
     */
    int addImage(int carId, String image) throws DBException;


    /**
     * Method for getting {@link Image} entity by image id in db.
     * @param imageId
     * @return
     * @throws DBException
     */
    Optional<Image> getImage(int imageId) throws DBException;

    /**
     * Method for deleting images by id.
     * @param imageId
     * @return boolean value depending on affected rows number. If this number > 0, than it will return "true" otherwise it is false.
     * @throws DBException
     */
    boolean deleteImage(int imageId) throws DBException;

    /**
     * Method for updating car`s data. Accepts car with id an updated car data. Every field from {@link Car} is rewritten in db row with specified id in the incoming object.
     * @param car
     * @return boolean value depending on whether was car updated or not.
     * @throws DBException
     */
    boolean update(Car car) throws DBException;

    /**
     * Method for deleting car from db.
     * @param carId
     * @return boolean value depending on whether the entity has been updated or not
     * @throws DBException
     */
    boolean delete(int carId) throws DBException;

    /**
     * Method for getting ids of cars that are <strong>not rented</strong> in a specified dates range. <br>
     * <hr>
     * Logic of the query is that some invoice renting date end should be greater or equals to specified date start and
     * at the same time, invoice`s date start should be less or equals than specified date end. Rejections and cancellations should be empty (or 0, if we are talking about db representation). <br> Then we count
     * entries that satisfy our conditions and if some car have number of such entries more than 0 -> we don`t need that car in resulting list.
     * <hr>
     * <pre>Note: we returning only ids because it is expected to use some constraints on the client side that will just hide rented cars.
     * Trying to take advantage of reactive frameworks as more as possible.</pre>
     * @param start
     * @param end
     * @return List of cars integers that are not rented in a specific dates range.
     * @throws DBException
     */
    List<Integer> getIdsOfCarsNotRentedInRange(LocalDate start, LocalDate end) throws DBException;

    /**
     * Returns rented dates periods on specified car. This is necessary to block the possibility of renting vehicles on already occupied date intervals. Used on /views/rent.jsp.
     * @param carId
     * @param searchStart only dates that are going after that date will be returned
     * @return
     * @throws DBException
     */
    List<DatesRange> getRentedDatesOnCar(int carId, LocalDate searchStart) throws DBException;
}
