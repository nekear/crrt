package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.Transversal;
import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.enums.CarSegments;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Car extends Transversal {
    @JsonAdapter(CryptoAdapter.class)
    @SerializedName("id")
    private Object id;
    private String brand;
    private String model;
    @JsonAdapter(DBCoupledAdapter.class)
    @SerializedName("segment")
    private CarSegments segment;
    private Double price;

    @JsonAdapter(DBCoupledAdapter.class)
    @SerializedName("city")
    private Cities city;


    public static Car of(ResultSet rs) throws SQLException {
          int id = rs.getInt(DB_Constants.TBL_CARS_ID);
          String brand = rs.getString(DB_Constants.TBL_CARS_BRAND);
          String model = rs.getString(DB_Constants.TBL_CARS_MODEL);
          CarSegments segment = CarSegments.getById(rs.getInt(DB_Constants.TBL_CARS_SEGMENT_ID));
          Double price = rs.getDouble(DB_Constants.TBL_CARS_PRICE);
          Cities city = Cities.getById(rs.getInt(DB_Constants.TBL_CARS_CITY_ID));

          Car car = new Car();

          car.setId(id);
          car.setBrand(brand);
          car.setModel(model);
          car.setSegment(segment);
          car.setPrice(price);
          car.setCity(city);

          return car;
    }


    @Override
    public boolean encrypt() throws DescriptiveException {
        super.setObject(this.id);
        if(super.encrypt()){
            this.id = super.getObject();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean decrypt() throws DescriptiveException {
        super.setObject(this.id);
        if(super.decrypt()){
            this.id = super.getObject();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Optional<Integer> getCleanId() throws DescriptiveException {
        super.setObject(this.id);
        return super.getCleanId();
    }


    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public CarSegments getSegment() {
        return segment;
    }

    public void setSegment(CarSegments segment) {
        this.segment = segment;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %s, %s, %4.3f, %s}", this.id, this.model, this.brand, this.segment, this.price, this.city);
    }
}
