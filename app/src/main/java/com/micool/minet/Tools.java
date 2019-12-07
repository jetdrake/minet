package com.micool.minet;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

            mag[0] = Float.parseFloat(reader.getString("x"));
            mag[1] = Float.parseFloat(reader.getString("y"));
            mag[2] = Float.parseFloat(reader.getString("z"));

            orientation[0] = Float.parseFloat(reader.getString("azimuth"));
            orientation[1] = Float.parseFloat(reader.getString("pitch"));
            orientation[2] = Float.parseFloat(reader.getString("roll"));

            tesla = reader.getDouble("tesla");
            ID = reader.getString("ID");

            Data data = new Data(mag, tesla, orientation, ID);
            //Log.d("convert", data.toString());

            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
