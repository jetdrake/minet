package com.micool.minet;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class DataAdapter extends ArrayAdapter<Data>{
    public DataAdapter(Context context, List<Data> object){
        super(context, 0, object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView =  ((Activity)getContext()).getLayoutInflater().inflate(R.layout.data_view, parent,false);
        }

        TextView ID = convertView.findViewById(R.id.ID);
        TextView x_value = convertView.findViewById(R.id.x_value);
        TextView y_value = convertView.findViewById(R.id.y_value);
        TextView z_value = convertView.findViewById(R.id.z_value);
        TextView tesla = convertView.findViewById(R.id.tesla);
        TextView azimuth = convertView.findViewById(R.id.azimuth);
        TextView pitch = convertView.findViewById(R.id.pitch);
        TextView roll = convertView.findViewById(R.id.roll);


        Data data = getItem(position);

        ID.setText(data != null ? data.getID().toString() : "Id not found");
        x_value.setText(String.format(Locale.ENGLISH, "x: %.2f", data.getX()));
        y_value.setText(String.format(Locale.ENGLISH, "y: %.2f", data.getY()));
        z_value.setText(String.format(Locale.ENGLISH, "z: %.2f", data.getZ()));
        tesla.setText(String.format(Locale.ENGLISH, "%.2f μT", data.getTesla()));
        azimuth.setText(String.format(Locale.ENGLISH, "Azimuth: %.2f", data.getAzimuth()));
        pitch.setText(String.format(Locale.ENGLISH, "Pitch: %.2f", data.getPitch()));
        roll.setText(String.format(Locale.ENGLISH, "Roll: %.2f", data.getRoll()));

        return convertView;
    }


}
