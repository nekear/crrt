package com.github.DiachenkoMD.utils;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.dto.users.Passport;
import com.github.DiachenkoMD.entities.enums.CarSegments;
import com.github.DiachenkoMD.entities.enums.Cities;

import java.time.LocalDate;
import java.util.Random;

import static com.github.DiachenkoMD.web.utils.Utils.generateRandomString;

public class TGenerators {
    public final static Random random = new Random();
    private final static int carSegmentsAmount = CarSegments.values().length;
    private final static int citiesAmount = Cities.values().length;

    public static Passport genPassport(){
        Passport passport = new Passport();

        passport.setFirstname(generateRandomString(6));
        passport.setSurname(generateRandomString(4));
        passport.setPatronymic(generateRandomString(8));

        passport.setDateOfBirth(LocalDate.of(
                2022,
                random.nextInt(11)+1,
                random.nextInt(27)+1
        ));

        passport.setDateOfIssue(LocalDate.of(
                2022,
                random.nextInt(11)+1,
                random.nextInt(27)+1
        ));

        passport.setDocNumber(111111111L);
        passport.setRntrc(2222222222L);
        passport.setAuthority(3333);

        return passport;
    }

    public static Car genCar(){
        Car car = new Car();

        car.setBrand("Brand ".concat(generateRandomString(4)));
        car.setModel("Model ".concat(generateRandomString(6)));
        car.setPrice(random.nextDouble() * 100);

        car.setSegment(CarSegments.getById(random.nextInt(carSegmentsAmount-1)+1));
        car.setCity(Cities.getById(random.nextInt(citiesAmount-1)+1));

        return car;
    }

    public static LimitedUser genUser(){
        LimitedUser user = new LimitedUser();
        user.setFirstname(generateRandomString(4));
        user.setSurname(generateRandomString(5));
        user.setPatronymic(generateRandomString(6));

        user.setEmail(generateRandomString(4).concat("@").concat("mail.ua"));

        return user;
    }

    public static DatesRange genDatesRange(){
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().plusMonths(random.nextInt(10)).getMonthValue();
        int day = LocalDate.of(year, month, LocalDate.now().getDayOfMonth()).plusDays(random.nextInt(10) + 1).getDayOfMonth(); // random day is before 11 (inclusive) to later add random days number in range of 1-11.

        LocalDate start = LocalDate.of(year, month, day);
        LocalDate end = start.plusDays(random.nextInt(22)+1);

        return new DatesRange(start, end);
    }
}
