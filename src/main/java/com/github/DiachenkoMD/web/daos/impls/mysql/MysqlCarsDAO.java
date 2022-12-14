package com.github.DiachenkoMD.web.daos.impls.mysql;

import com.github.DiachenkoMD.entities.dto.Car;
import com.github.DiachenkoMD.entities.dto.DatesRange;
import com.github.DiachenkoMD.entities.dto.Image;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.factories.DAOFactory;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Car> get(int car_id) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT *, glueCarPhotos(?) AS photos FROM tbl_cars WHERE id=? LIMIT 0,1");
        ){

            stmt.setInt(1, car_id);
            stmt.setInt(2, car_id);


            Optional<Car> car = Optional.empty();

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next())
                    car = Optional.of(Car.of(rs));
            }

            return car;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public int create(Car car) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_cars (brand, model, segment_id, price, city_id) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ){

            int index = 0;
            stmt.setString(++index, car.getBrand());
            stmt.setString(++index, car.getModel());
            stmt.setInt(++index, car.getSegment().id());
            stmt.setDouble(++index, car.getPrice());
            stmt.setInt(++index, car.getCity().id());

            int affectedRows = stmt.executeUpdate();

            if(affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }else{
                return -1;
            }

        } catch (SQLException e) {
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public void addImages(int car_id, List<String> images) throws DBException {
        if(images.size() == 0)
            return;

        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_cars_photos (car_id, photo) VALUES (?, ?)");
        ){
            for(String image : images){
                stmt.setInt(1, car_id);
                stmt.setString(2, image);
                stmt.addBatch();
            }

            stmt.executeBatch();
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public int addImage(int car_id, String image) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("INSERT INTO tbl_cars_photos (car_id, photo) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        ){
            stmt.setInt(1, car_id);
            stmt.setString(2, image);

            int affectedRows = stmt.executeUpdate();

            if(affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }else{
                return -1;
            }
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public Optional<Image> getImage(int image_id) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT id, photo  FROM tbl_cars_photos WHERE id=? LIMIT 0,1");
        ){
            stmt.setInt(1, image_id);

            Optional<Image> image = Optional.empty();

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next())
                    image = Optional.of(Image.of(rs));
            }

            return image;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean deleteImage(int image_id) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM tbl_cars_photos WHERE id=?");
        ){
            stmt.setInt(1, image_id);

           return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean update(Car car) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE tbl_cars SET brand=?, model=?, segment_id=?, price=?, city_id=? WHERE id=?");
        ){
            int index = 0;
            stmt.setString(++index, car.getBrand());
            stmt.setString(++index, car.getModel());
            stmt.setInt(++index, car.getSegment().id());
            stmt.setDouble(++index, car.getPrice());
            stmt.setInt(++index, car.getCity().id());
            stmt.setInt(++index, (Integer) car.getId());

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public boolean delete(int carId) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM tbl_cars WHERE id=?");
        ){
            stmt.setInt(1, carId);

            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }


    @Override
    public List<Integer> getIdsOfCarsNotRentedInRange(LocalDate start, LocalDate end) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT tbl_cars.id FROM tbl_cars WHERE " +
                        "(SELECT COUNT(id) FROM tbl_invoices " +
                        "WHERE date_end >= ? AND date_start <= ?  " +
                        "AND tbl_invoices.car_id =  tbl_cars.id " +
                        "AND tbl_invoices.is_canceled = 0 AND tbl_invoices.is_rejected = 0 " +
                        ") = 0");
        ){

            stmt.setObject(1, start);
            stmt.setObject(2, end);

            List<Integer> foundCarsIds = new LinkedList<>();
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next())
                    foundCarsIds.add(rs.getInt("id"));
            }

            return foundCarsIds;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }

    @Override
    public List<DatesRange> getRentedDatesOnCar(int carId, LocalDate searchStart) throws DBException {
        try(
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT date_start, date_end FROM tbl_invoices WHERE date_start >= ? AND car_id = ?");
        ){

            stmt.setObject(1, searchStart);
            stmt.setInt(2, carId);

            List<DatesRange> foundRanges = new LinkedList<>();
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next())
                    foundRanges.add(
                            new DatesRange(
                                rs.getObject("date_start", LocalDate.class),
                                rs.getObject("date_end", LocalDate.class)
                            )
                    );
            }

            return foundRanges;
        }catch (SQLException e){
            logger.error(e);
            throw new DBException(e);
        }
    }
}
