package com.github.DiachenkoMD.web.services;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.invoices.NewRent;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.github.DiachenkoMD.web.utils.Utils;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.emailNotify;


public class IntroService {
    private static final Logger logger = LogManager.getLogger(IntroService.class);
    private static final Marker DB_MARKER = MarkerManager.getMarker("DB");
    private final CarsDAO carsDAO;
    private final UsersDAO usersDAO;
    private final InvoicesDAO invoicesDAO;

    private final ServletContext ctx;

    public IntroService(CarsDAO carsDAO, UsersDAO usersDAO, InvoicesDAO invoicesDAO, ServletContext ctx) {
        this.carsDAO = carsDAO;
        this.usersDAO = usersDAO;
        this.invoicesDAO = invoicesDAO;
        this.ctx = ctx;
    }

    /**
     * Method for getting all cars from db for showing them to client as "available cars for rent". May work in pair with TODO:: add
     * @return
     */
    public List<Car> getAllCars() throws DBException {
       return carsDAO.getAll();
    }

    public List<String> getCarsNotRentedInDatesRange(DatesRange datesRange) throws DBException {
        LocalDate start = datesRange.getStart();
        LocalDate end = datesRange.getEnd();

        logger.debug(datesRange);

        if(start == null || end == null) {
            throw new IllegalArgumentException("Date start or Date end is null");
        }


        return carsDAO.getIdsOfCarsNotRentedInRange(start, end)
                .parallelStream()
                .map(x -> {
                    try {
                        return CryptoStore.encrypt(String.valueOf(x));
                    } catch (DescriptiveException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Method for getting renting info for a specific car. Provides car data and list of already rented dates. Further, those dates are used to block ability of selecting such ranges in the datepicker.
     * @param carId
     * @return Car info and already rented dates.
     * @throws DBException may be thrown from {@link CarsDAO#get(int)}, {@link CarsDAO#getRentedDatesOnCar(int, LocalDate)}
     * @throws DescriptiveException may be thrown with reason {@link ExceptionReason#ACQUIRING_ERROR ACQUIRING_ERROR};
     */
    public Map.Entry<Car, List<LocalDate>> getRentingInfo(int carId) throws DBException, DescriptiveException {
        Car car = carsDAO.get(carId).orElseThrow(() -> new DescriptiveException("Couldn`t obtain car with such id", ExceptionReason.ACQUIRING_ERROR));

        List<DatesRange> rentedDatesRanges = carsDAO.getRentedDatesOnCar((Integer) car.getId(), LocalDate.now());

        List<LocalDate> rentedDates = rentedDatesRanges
                .parallelStream()
                .map(x -> {
                            List<LocalDate> localDates = new LinkedList<>(x.getStart()
                                    .datesUntil(x.getEnd()).toList());
                            localDates.add(x.getEnd());
                            return localDates;
                        }
                )
                .flatMap(List::stream).toList();

        return Map.entry(car, rentedDates);
    }


    /**
     * Method for getting available driver ids on a specified range.
     * @param dateStart
     * @param dateEnd
     * @return List of driver ids
     * @throws DBException may be thrown from {@link UsersDAO#getAvailableDriversOnRange(LocalDate, LocalDate, int)}
     */
    public List<Integer> getAvailableDriversOnRange(String dateStart, String dateEnd, int cityId) throws DBException {
        LocalDate start = LocalDate.parse(dateStart, Utils.localDateFormatter);
        LocalDate end = LocalDate.parse(dateEnd, Utils.localDateFormatter);

        return getAvailableDriversOnRange(start, end, cityId);
    }

    private List<Integer> getAvailableDriversOnRange(LocalDate start, LocalDate end, int cityId) throws DBException {
        return usersDAO.getAvailableDriversOnRange(start, end, cityId);
    }


    /**
     * Method for creating new rent invoices. Awaits for json structure similar to {@link NewRent}, because inside is parsed with GSON.
     * @param invoiceDataJSON
     * @throws DescriptiveException may be thrown with reasons VALIDATION_ERROR, PASSPORT_VALIDATION_ERROR (from {@link Passport#validate()}), DRIVER_NOT_ALLOWED, ACQUIRING_ERROR (if not car by that id was found), NOT_ENOUGH_MONEY
     * @throws DBException may be thrown by {@link CarsDAO#get(int)}, {@link UsersDAO#getBalance(int)}, {@link InvoicesDAO#createInvoice(int, int, DatesRange, Passport, BigDecimal, Integer) createInvoice(...)}, {@link UsersDAO#getFromDriver(int)}.
     */
    public void createRent(String invoiceDataJSON, AuthUser currentUser) throws DescriptiveException, DBException {
        Gson gson = (Gson) ctx.getAttribute("gson");

        NewRent rentData = gson.fromJson(invoiceDataJSON, NewRent.class);

        // Validating rent data
        // --> Passport data
        Passport passport = rentData.getPassport();
        passport.validate();
        // --> Minimal available date to be selected is today and maximum is +2 months
        if(rentData.getDatesRange() == null ||  rentData.getDatesRange().getStart() == null || rentData.getDatesRange().getEnd() == null)
            throw new DescriptiveException("Some dates data is null", ExceptionReason.VALIDATION_ERROR);

        LocalDate start = rentData.getDatesRange().getStart();
        LocalDate end = rentData.getDatesRange().getEnd();

        if(!(start.isAfter(LocalDate.now().minusDays(1)) && end.isBefore(LocalDate.now().plusMonths(2).plusDays(1))))
            throw new DescriptiveException("Dates are not in allowed range", ExceptionReason.VALIDATION_ERROR);

        // Getting car to count its invoice`s final price and to obtain connected drivers
        Car car = carsDAO.get((Integer) rentData.getCarId()).orElseThrow(() -> new DescriptiveException("Couldn`t obtain car id from incoming object", ExceptionReason.ACQUIRING_ERROR));

        // --> Validating driver
        List<Integer> availableDrivers = null;
        if(rentData.isWithDriver()){
            availableDrivers = getAvailableDriversOnRange(start, end, (Integer) car.getId());

            if(availableDrivers.size() == 0)
                throw new DescriptiveException("User selected [With driver: true] but there is no drivers available", ExceptionReason.DRIVER_NOT_ALLOWED);
        }

        // --> Getting final price
        BigDecimal price = BigDecimal.valueOf(Math.abs(Period.between(start, end).getDays()) * car.getPrice());
        BigDecimal clientBalance = BigDecimal.valueOf(usersDAO.getBalance((Integer) currentUser.getId()));

        // Validating user balance (does he have enough money to pay)
        if(clientBalance.compareTo(price) < 0)
            throw new DescriptiveException("Client`s balance is too low", ExceptionReason.NOT_ENOUGH_MONEY);

        // Creating invoice and deducting the money from the user's account (all in one, because under the hood dao`s method uses transactions)
        Integer driverId = availableDrivers == null ? null : availableDrivers.get(new Random().nextInt(availableDrivers.size()));


        invoicesDAO.createInvoice(
                (Integer) car.getId(),
                (Integer) currentUser.getId(),
                new DatesRange(start, end),
                passport,
                price,
                driverId
        );

        currentUser.setBalance(clientBalance.subtract(price).doubleValue()); // The problem of using double instead of BigDecimal keeps catching up with me...

        String rentStartFormatted = start.format(Utils.localDateFormatter);
        String rentEndFormatted = end.format(Utils.localDateFormatter);

        // Getting drivers id (if we had isWithDriver: true) and notifying him, that he had new rent connected
        if(rentData.isWithDriver() && driverId != null){
            LimitedUser driver = usersDAO.getFromDriver(driverId).get();
            String driverEmail = driver.getEmail();

            emailNotify(driverEmail, "New rent was added to your list", String.format("Hello, driver. New rent, scheduled from <strong>%s</strong> to <strong>%s</strong> on <strong>%s</strong> has been added to your list!", rentStartFormatted, rentEndFormatted, car.getBrand() + car.getModel()));
        }

        logger.info(DB_MARKER, "User [{}] rented car [{}] for date range [{}] to [{}]. Rent price was [{}]. With driver -> [{}]",
                currentUser.getId(),
                car.getId(),
                rentStartFormatted,
                rentEndFormatted,
                price,
                driverId);

        emailNotify(
                currentUser.getEmail(),
                String.format("Thank you for renting <strong>%s %s</strong>!", car.getBrand(), car.getModel()),
                String.format("Good afternoon. Thank you for using our services. Your order for <strong>%s %s</strong> has been successfully paid and added to the list. We are waiting for you in our office in %s on %s. \n" +
                        "<p>Price: %s</p>\n" +
                        "<p>Auto: %s %s</p>\n" +
                        "<p>With driver: %s</p>"+
                        "<p>Rent start: %s</p>"+
                        "<p>Rent end: %s</p>",
                        car.getBrand(), car.getModel(),
                        ResourceBundle.getBundle("langs.i18n_en_US").getString("cities."+car.getCity().keyword()),
                        rentStartFormatted,
                        price,
                        car.getBrand(), car.getModel(),
                        driverId == null ? "no" : "yes",
                        rentStartFormatted,
                        rentEndFormatted
                )
        );
    }

}