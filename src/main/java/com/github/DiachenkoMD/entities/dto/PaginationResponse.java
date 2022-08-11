package com.github.DiachenkoMD.entities.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginationResponse <T>{
    private int totalElements;

    @SerializedName("entities")
    private List<T> responseData;

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public List<T> getResponseData() {
        return responseData;
    }

    public void setResponseData(List<T> responseData) {
        this.responseData = responseData;
    }
}
