package com.micool.minet.DataClasses;

import java.sql.Timestamp;

public class Data {
    private float x, y, z;
    private float azimuth, pitch, roll;
    private double tesla;
//    private String direction;
//    private String id;

    public Data() {}

//    public Data(double tesla, String direction, String room) {
//
//        this.tesla = tesla;
//        this.direction = direction;
//        this.id = room;
//
//        //set these defaults to not break any other code
//        this.x = (float)0.0;
//        this.y = (float)0.0;
//        this.z = (float)0.0;
//        this.pitch = (float)0.0;
//        this.azimuth = (float)0.0;
//        this.roll = (float)0.0;
//    }
//
//    public Data(float[] mag, double tesla, float[] orientation) {
//        this.x = mag[0];
//        this.y = mag[1];
//        this.z = mag[2];
//        this.tesla = tesla;
//        this.azimuth = orientation[0];
//        this.pitch = orientation[1];
//        this.roll = orientation[2];
//        this.id = new Timestamp(System.currentTimeMillis()).toString();
//        this.direction = "Not Formatted";
//    }
//
//    public Data(float[] mag, double tesla, float[] orientation, String id) {
//        this.x = mag[0];
//        this.y = mag[1];
//        this.z = mag[2];
//        this.tesla = tesla;
//        this.azimuth = orientation[0];
//        this.pitch = orientation[1];
//        this.roll = orientation[2];
//        this.id = id;
//        this.direction = "Not Formatted";
//    }

    public Data(float[] mag, double tesla, float[] orientation) {
        this.x = mag[0];
        this.y = mag[1];
        this.z = mag[2];
        this.tesla = tesla;
        this.azimuth = orientation[0];
        this.pitch = orientation[1];
        this.roll = orientation[2];
//        this.id = id;
//        this.direction = direction;
    }

    public Data(float x, float y, float z, float azimuth, float pitch, float roll, double tesla) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
        this.tesla = tesla;
//        this.id = id;
//        this.direction = direction;
    }

//    public String getID() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

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

//    public String getDirection() {
//        return direction;
//    }
//
//    public void setDirection(String direction) {
//        this.direction = direction;
//    }
}
