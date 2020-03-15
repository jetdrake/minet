package com.micool.minet.Models;

import android.util.Log;

import androidx.annotation.NonNull;

public class Coordinate {
    private int x;
    private int y;
    private String id;
    boolean active;


    public Coordinate(int nx, int ny){
        x = nx;
        y = ny;
        this.active = false;
    }

    public Coordinate(int nx, int ny, String id){
        x = nx;
        y = ny;
        this.id = id;
        this.active = false;
    }


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + x + ", " + y + ") : " + active;
    }
}
