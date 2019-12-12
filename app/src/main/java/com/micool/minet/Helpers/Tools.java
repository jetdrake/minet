package com.micool.minet.Helpers;

import android.util.Log;

import com.micool.minet.Data;

import org.json.JSONException;
import org.json.JSONObject;


public class Tools {


    public static String dataToJSON(Data data){
        try {
            JSONObject obj = new JSONObject();
            obj.put("x", ""+data.getX());
            obj.put("y", ""+data.getY());
            obj.put("z", ""+data.getZ());
            obj.put("tesla", ""+data.getTesla());
            obj.put("azimuth", ""+data.getAzimuth());
            obj.put("pitch", ""+data.getPitch());
            obj.put("roll", ""+data.getRoll());
            obj.put("ID", ""+data.getID());
            obj.put("direction",data.getDirection());

            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Data JSONToData (String json) {
        try {
            JSONObject reader = new JSONObject(json);
            float [] mag = new float[3];
            float [] orientation = new float[3];
            double tesla;
            String ID;
            String direction;

            mag[0] = Float.parseFloat(reader.getString("x"));
            mag[1] = Float.parseFloat(reader.getString("y"));
            mag[2] = Float.parseFloat(reader.getString("z"));

            orientation[0] = Float.parseFloat(reader.getString("azimuth"));
            orientation[1] = Float.parseFloat(reader.getString("pitch"));
            orientation[2] = Float.parseFloat(reader.getString("roll"));

            tesla = reader.getDouble("tesla");
            ID = reader.getString("ID");
            direction = reader.getString("direction");

            Data data = new Data(mag, tesla, orientation, ID, direction);
            //Log.d("convert", data.toString());

            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
