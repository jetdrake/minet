package com.micool.minet.Helpers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.micool.minet.DataClasses.Data;
import com.micool.minet.DataClasses.MetaData;
import com.micool.minet.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataAdapter extends ArrayAdapter<Map<String, Object>>{
    public DataAdapter(Context context, List<Map<String, Object>> object){
        super(context, 0, object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView =  ((Activity)getContext()).getLayoutInflater().inflate(R.layout.view_data_view, parent,false);
        }

        TextView ID = convertView.findViewById(R.id.ID);
        TextView x_value = convertView.findViewById(R.id.x_value);
        TextView y_value = convertView.findViewById(R.id.y_value);
        TextView z_value = convertView.findViewById(R.id.z_value);
        TextView tesla = convertView.findViewById(R.id.tesla);
        TextView azimuth = convertView.findViewById(R.id.azimuth);
        TextView pitch = convertView.findViewById(R.id.pitch);
        TextView roll = convertView.findViewById(R.id.roll);


        Map<String, Object> map = getItem(position);
        List<String> l = new ArrayList<String>(map.keySet());
        String meta = l.get(0);


        ID.setText(meta != null ? meta : "Meta not found");


        List<Object> dataObjects = (List<Object>) map.values();

        for (Object data : dataObjects) {
            Data thisData = (Data) data;

            x_value.setText(String.format(Locale.ENGLISH, "x: %.2f", thisData.getX()));
            y_value.setText(String.format(Locale.ENGLISH, "y: %.2f", thisData.getY()));
            z_value.setText(String.format(Locale.ENGLISH, "z: %.2f", thisData.getZ()));

        }

        return convertView;
    }


}
