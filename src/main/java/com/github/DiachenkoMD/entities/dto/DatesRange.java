package com.github.DiachenkoMD.entities.dto;

import java.time.LocalDate;

public class DatesRange {
    private LocalDate start;
    private LocalDate end;

    public DatesRange(){}

    public DatesRange(LocalDate start, LocalDate end){
        this.start = start;
        this.end = end;
    }
}
