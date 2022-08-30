package com.github.DiachenkoMD.entities.dto;

import java.time.LocalDate;
import java.time.Period;

/**
 * Class designed to easily store dates ranges together.
 */
public class DatesRange {
    private LocalDate start;
    private LocalDate end;

    public DatesRange(){}

    public DatesRange(LocalDate start, LocalDate end){
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public Period getLength(){
        return Period.between(this.getStart(), this.getEnd());
    }

    @Override
    public String toString() {
        return String.format("[%s -> %s]", this.start.toString(), this.end.toString());
    }
}
