package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.DB_Constants;
import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.entities.adapters.DBCoupledAdapter;
import com.github.DiachenkoMD.entities.enums.CarSegments;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.DiachenkoMD.web.utils.Utils.containsColumn;

public class Car {
    @JsonAdapter(CryptoAdapter.class)
    @SerializedName("id")
    private Object id;
    private String brand;
    private String model;
    @JsonAdapter(DBCoupledAdapter.class)
    @SerializedName("segment")
    private CarSegments segment;
    private Double price;

    // This field is omitted on getAll()
    private List<Image> images;

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

          if(containsColumn(rs, "photos") && rs.getString("photos") != null){
              String[] photos = rs.getString("photos").split("\\?");

              car.setImages(Arrays.stream(photos).parallel().map(i -> Image.of(i).orElse(null)).collect(Collectors.toList()));
          }

          return car;
    }


    public boolean encrypt() throws DescriptiveException {
        if(this.id instanceof Integer decryptedId){
            this.id = CryptoStore.encrypt(String.valueOf(decryptedId));
            return true;
        }

        return false;
    }

    public boolean decrypt() throws DescriptiveException {
        if(this.id instanceof String encryptedId) {
            this.id = CryptoStore.decrypt(encryptedId);
            return true;
        }

        return false;
    }

    public Optional<Integer> getCleanId() throws DescriptiveException {
        if(this.id == null)
            return Optional.empty();

        if(this.id instanceof String encryptedId)
            return Optional.of(Integer.valueOf(CryptoStore.decrypt(encryptedId)));

        return Optional.of((Integer) this.id);
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


    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
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
