package com.github.DiachenkoMD.tests.database;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.Image;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.CarSegments;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.extensions.ConnectionParameterResolverExtension;
import com.github.DiachenkoMD.extensions.DatabaseOperationsExtension;
import com.github.DiachenkoMD.utils.TGenerators;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlCarsDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlInvoicesDAO;
import com.github.DiachenkoMD.web.daos.impls.mysql.MysqlUsersDAO;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        DatabaseOperationsExtension.class,
        ConnectionParameterResolverExtension.class
})
class CarsDAOTest {

    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;
    private final CarsDAO carsDAO;

    public CarsDAOTest(DataSource ds){
        this.usersDAO = new MysqlUsersDAO(ds);
        this.invoicesDAO = new MysqlInvoicesDAO(ds);
        this.carsDAO = new MysqlCarsDAO(ds);
    }

    @Test
    void getAll() throws Exception{
        Car car = TGenerators.genCar();

        Car car2 = new Car();

        car2.setBrand("Mercedes");
        car2.setModel("Benz");
        car2.setPrice(102.2);
        car2.setCity(Cities.LVIV);
        car2.setSegment(CarSegments.D_SEGMENT);

        carsDAO.create(car);
        carsDAO.create(car2);

        assertEquals(2, carsDAO.getAll().size());
    }

    @Test
    void get() throws Exception{
        Car car = TGenerators.genCar();

        // Creating new car in db
        car.setId(carsDAO.create(car));

        // Getting inserted car info
        Car foundCar = carsDAO.get((Integer) car.getId()).orElse(null);

        // Car shouldn`t be null
        assertThat(foundCar).isNotNull();

        // Car should be fully equal to original object
        assertThat(foundCar)
                .usingRecursiveComparison()
                .isEqualTo(car);
    }

    @Test
    void create() throws DBException {
        Car car = TGenerators.genCar();

        assertThat(carsDAO.create(car)).isGreaterThan(0); // in reality newly inserted object to clean db will always result in 1
    }

    @Test
    void addImages() throws DBException {
        Car car = TGenerators.genCar();

        // Creating new car in db
        int carId = carsDAO.create(car);
        car.setId(carId);

        // Preparing images array
        List<String> images = new LinkedList<>(List.of("1.png", "someFile.jpg"));

        // Inserting images to db
        carsDAO.addImages((Integer) car.getId(), images);

        // Verifying that insert worked correctly by comparing two lists
        Car carFromDB = carsDAO.get(carId).get();

        AtomicInteger theSameImages = new AtomicInteger();

        carFromDB.getImages()
                .parallelStream()
                .forEach(image -> {
                    if(images.contains(image.getFileName()))
                        theSameImages.getAndIncrement();
                });

        assertThat(theSameImages.get()).isEqualTo(images.size());
    }

    @DisplayName("addImage / getImage")
    @Test
    void addAndGetImage() throws DBException {
        Car car = TGenerators.genCar();

        // Creating new car in db
        int carId = carsDAO.create(car);
        car.setId(carId);

        // Inserting new image into db
        int newImageId = carsDAO.addImage(carId, "someImage.jpg");

        // Getting image by identifier
        Image carImage = carsDAO.getImage(newImageId).orElse(null);

        assertThat(carImage).isNotNull();
        assertThat(carImage.getFileName()).isEqualTo("someImage.jpg");
    }

    @Test
    void deleteImage() throws DBException {
        Car car = TGenerators.genCar();

        // Creating new car in db
        int carId = carsDAO.create(car);
        car.setId(carId);

        // Inserting new image into db
        int newImageId = carsDAO.addImage(carId, "someImage.jpg");

        // Deleting image
        assertTrue(carsDAO.deleteImage(newImageId));
        assertTrue(carsDAO.getImage(newImageId).isEmpty());
    }

    @Test
    void update() throws DBException {
        Car car = TGenerators.genCar();

        // Creating new car in db
        int carId = carsDAO.create(car);
        car.setId(carId);

        // Updating car
        car.setBrand("Ferrari");
        car.setCity(Cities.LVIV);
        assertTrue(carsDAO.update(car));

        // Getting car from db and comparing
        Car carFromDB = carsDAO.get(carId).get();

        assertThat(carFromDB)
                .usingRecursiveComparison()
                .isEqualTo(car);
    }

    @Test
    void delete() throws DBException {
        Car car = TGenerators.genCar();

        // Creating new car in db
        int carId = carsDAO.create(car);
        car.setId(carId);

        // Deleting car
        assertTrue(carsDAO.delete(carId));
        assertTrue(carsDAO.get(carId).isEmpty());
    }

    @Test
    void getIdsOfCarsNotRentedInRange() throws DBException {
        // Forming needed entities to create invoices
        Car car1 = TGenerators.genCar();
        int car1id = carsDAO.create(car1);
        car1.setId(car1id);

        Car car2 = TGenerators.genCar();
        int car2id = carsDAO.create(car2);
        car2.setId(car2id);

        LimitedUser client = TGenerators.genUser();

        Passport passport = TGenerators.genPassport();

        int clientId = usersDAO.insertUser(client);
        client.setId(clientId);

        DatesRange rentedDatesRange1 = new DatesRange(
                LocalDate.of(2022, 10, 10),
                LocalDate.of(2022, 10, 15)
        );

        DatesRange rentedDatesRange2 = new DatesRange(
                LocalDate.of(2022, 10, 20),
                LocalDate.of(2022, 10, 22)
        );


        // Generating two invoices: first is 2022-10-10 to 2022-10-15 and another one is 2022-10-20 to 2022-10-22
        int invoice1 = invoicesDAO.createInvoice(
                car1id,
                clientId,
                rentedDatesRange1,
                passport,
                BigDecimal.valueOf(1000),
                null
        );

        int invoice2 = invoicesDAO.createInvoice(
                car2id,
                clientId,
                rentedDatesRange2,
                passport,
                BigDecimal.valueOf(2000),
                null
        );

        // This range is from 2022-12-12 to 2022-12-14 and should not touch any cars. So expecting ids of 2 cars returned.
        List<Integer> idsOfCarsNotInRange1 = carsDAO.getIdsOfCarsNotRentedInRange(
                LocalDate.of(2022, 12, 12),
                LocalDate.of(2022, 12, 14)
        );

        assertThat(idsOfCarsNotInRange1.size()).isEqualTo(2);
        assertTrue(idsOfCarsNotInRange1.contains(car1id));
        assertTrue(idsOfCarsNotInRange1.contains(car2id));

        // This range is from 2022-10-08 to 2022-10-12 and collides with invoice connected to first car. So expecting id of the second car returned.
        List<Integer> idsOfCarsNotInRange2 = carsDAO.getIdsOfCarsNotRentedInRange(
                LocalDate.of(2022, 10, 8),
                LocalDate.of(2022, 10, 12)
        );

        assertThat(idsOfCarsNotInRange2.size()).isEqualTo(1);
        assertTrue(idsOfCarsNotInRange2.contains(car2id));
    }

    @Test
    void getRentedDatesOnCar() throws DBException {
        // Forming needed entities to create invoices
        Car car = TGenerators.genCar();
        int carId = carsDAO.create(car);
        car.setId(carId);

        LimitedUser client = TGenerators.genUser();

        Passport passport = TGenerators.genPassport();

        int clientId = usersDAO.insertUser(client);
        client.setId(clientId);

        DatesRange rentedDatesRange1 = new DatesRange(
                LocalDate.of(2022, 9, 10),
                LocalDate.of(2022, 9, 15)
        );

        DatesRange rentedDatesRange2 = new DatesRange(
                LocalDate.of(2022, 10, 10),
                LocalDate.of(2022, 10, 15)
        );


        // Generating invoice from 2022-09-10 to 2022-09-15
        invoicesDAO.createInvoice(
                carId,
                clientId,
                rentedDatesRange1,
                passport,
                BigDecimal.valueOf(1000),
                null
        );

        // Generating invoice from 2022-10-10 to 2022-10-15
        invoicesDAO.createInvoice(
                carId,
                clientId,
                rentedDatesRange2,
                passport,
                BigDecimal.valueOf(1000),
                null
        );

        // Getting dates on car (start searching from 2022-08-10)
        List<DatesRange> foundDates1 = carsDAO.getRentedDatesOnCar(carId, LocalDate.of(2022, 8, 10));
        assertEquals(2, foundDates1.size());

        // Getting dates on car (start searching from 2022-10-09)
        List<DatesRange> foundDates2 = carsDAO.getRentedDatesOnCar(carId, LocalDate.of(2022, 10, 9));
        assertEquals(1, foundDates2.size());

        // Getting dates on car (start searching from 2022-10-10) (check for expected inclusive search)
        List<DatesRange> foundDates3 = carsDAO.getRentedDatesOnCar(carId, LocalDate.of(2022, 10, 10));
        assertEquals(1, foundDates3.size());
    }
}