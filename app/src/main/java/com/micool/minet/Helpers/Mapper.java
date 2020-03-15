package com.micool.minet.Helpers;

import android.util.Log;

import com.micool.minet.Models.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    private final String TAG = "Mapper";
    List<Coordinate> map = new ArrayList<Coordinate>();

    public void convertPythonListToJava(String list){
        List<Coordinate> itemList = new ArrayList<Coordinate>();
        int[] ints = new int[2];

        //Log.d(TAG, "convertPythonListToJava: " + itemList.toArray());
        int counter = 0;
        for (char item : list.toCharArray()){
            if (Character.isDigit(item)){
                Log.d(TAG, "convertPythonListToJava: " + item);
                ints[counter] = Integer.parseInt(String.valueOf(item));
                counter++;
                if (counter == 2){
                    itemList.add(new Coordinate(ints[0], ints[1]));
                    counter = 0;
                }
            }
        }

        for (Coordinate coord : itemList){
            Log.d(TAG, "convertPythonListToJava: " + coord.toString());
        }

        map = itemList;
    }

    public void setActiveLandmark(String landmark){
        int counter = 0;
        int[] ints = new int[2];

        for (char item : landmark.toCharArray()){
            if (Character.isDigit(item)){
                //Log.d(TAG, "convertPythonListToJava: " + item);
                ints[counter] = Integer.parseInt(String.valueOf(item));
                counter++;
            }
        }

        for (Coordinate c : map){
            if ((c.getX() == ints[0]) && (c.getY() == ints[1])){
                c.setActive(true);
            } else {
                c.setActive(false);
            }
        }
    }

    public List<Coordinate> getMap() {
        return map;
    }
}

