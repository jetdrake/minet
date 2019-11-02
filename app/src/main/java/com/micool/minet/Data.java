package com.micool.minet;

import java.sql.Timestamp;

public class Data {
    private Double x, y, z, tesla, orientation;
    private String ID = new Timestamp(System.currentTimeMillis()).toString();

    public Data() {}

    public Data(Double x, Double y, Double z, Double tesla, Double orientation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tesla = tesla;
        this.orientation = orientation;
    }

    // no orientation data
    public Data(Double x, Double y, Double z, Double tesla) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tesla = tesla;
    }

    public String getID() {
        return ID;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public Double getTesla() {
        return tesla;
    }

    public void setTesla(Double tesla) {
        this.tesla = tesla;
    }

    public Double getOrientation() {
        return orientation;
    }

    public void setOrientation(Double orientation) {
        this.orientation = orientation;
    }
}
