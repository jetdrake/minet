package com.micool.minet.Helpers;

import com.micool.minet.DataClasses.Data;
import com.micool.minet.DataClasses.MetaData;

import org.json.JSONException;
import org.json.JSONObject;


public class Tools {


    public static JSONObject dataToJSON(Data data){
        try {
            JSONObject obj = new JSONObject();
            obj.put("x", data.getX());
            obj.put("y", data.getY());
            obj.put("z", data.getZ());


            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Data JSONToData (String json) {
        try {
            JSONObject reader = new JSONObject(json);
            float [] mag = new float[3];

            mag[0] = Float.parseFloat(reader.getString("x"));
            mag[1] = Float.parseFloat(reader.getString("y"));
            mag[2] = Float.parseFloat(reader.getString("z"));

            Data data = new Data(mag);
            //Log.d("convert", data.toString());

            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject metaToJSON(MetaData meta) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("direction", meta.getDirection());
            obj.put("stepId", meta.getStepId());
            obj.put("room", meta.getRoom());


            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static JSONObject dataPackToJSON(MetaData meta, Data data) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("direction", meta.getDirection());
            obj.put("stepId", meta.getStepId());
            obj.put("room", meta.getRoom());
            obj.put("x", data.getX());
            obj.put("y", data.getY());
            obj.put("z", data.getZ());
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }


}
