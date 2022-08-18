package com.github.DiachenkoMD.entities.dto;

import static com.github.DiachenkoMD.entities.DB_Constants.*;
import com.github.DiachenkoMD.entities.adapters.CryptoAdapter;
import com.github.DiachenkoMD.web.utils.CryptoStore;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Image {
    @JsonAdapter(CryptoAdapter.class)
    private Object id;

    private String fileName;

    public static Optional<Image> of(String imageData){ // string should be in format {id}#{imageFileName}
        String[] split = imageData.split("#");
        if(split.length == 2){
            Image image = new Image();
            image.setId(split[0]);
            image.setFileName(split[1]);
            return Optional.of(image);
        }else{
            return Optional.empty();
        }
    }

    public static Image of(ResultSet rs) throws SQLException {
        int id = rs.getInt(TBL_CARS_PHOTOS_ID);
        String fileName = rs.getString(TBL_CARS_PHOTOS_PHOTO);

        Image image = new Image();
        image.setId(id);
        image.setFileName(fileName);

        return image;
    }


    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
