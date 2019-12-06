package com.micool.minet;

import java.sql.Timestamp;

public class Data {
    private float x, y, z;
    private float azimuth, pitch, roll;
    private double tesla;
    private String id;

    public Data() {}

    public Data(float[] mag, double tesla, float[] orientation) {
        this.x = mag[0];
        this.y = mag[1];
        this.z = mag[2];
        this.tesla = tesla;
        this.azimuth = orientation[0];
        this.pitch = orientation[1];
        this.roll = orientation[2];
        this.id = new Timestamp(System.currentTimeMillis()).toString();
    }

    public Data(float[] mag, double tesla, float[] orientation, String room) {
        this.x = mag[0];
        this.y = mag[1];
        this.z = mag[2];
        this.tesla = tesla;
        this.azimuth = orientation[0];
        this.pitch = orientation[1];
        this.roll = orientation[2];
        this.id = room;
    }

    public String getID() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public double getTesla() {
        return tesla;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

}
