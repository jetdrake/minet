package com.micool.minet.Fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micool.minet.Data;
import com.micool.minet.R;
import com.micool.minet.tools;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

public class Sensors extends Fragment implements SensorEventListener {

    private SensorsListener listener;

    public interface SensorsListener {
        void onInputSensorsSent(Data data);
    }

    //sensors
    private static SensorManager sensorManager;
    private static Sensor magSensor;
    private static Sensor gravSensor;

    //value storage
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] Rm = new float[9];
    private float[] Im = new float[9];
    float orientation[] = new float[3];
    float azimuth;
    double tesla;

    TextView reading;
    TextView x;
    TextView y;
    TextView z;
    TextView accreading;
    TextView accx;
    TextView accy;
    TextView accz;

    String [] rooms;
    int activeRoomID;
    ArrayList<String> dataJson = new ArrayList<String>();
    boolean start = false;
    Data currentData;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_sensors, container, false);
        //get SensorManager and create sensors (on every creation)
        sensorManager = (SensorManager)this.getActivity().getSystemService(SENSOR_SERVICE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //get buttons and views for magnet and acc data
        reading = view.findViewById(R.id.reading);
        x = view.findViewById(R.id.x);
        y = view.findViewById(R.id.y);
        z = view.findViewById(R.id.z);

        accreading = view.findViewById(R.id.accreading);
        accx = view.findViewById(R.id.accx);
        accy = view.findViewById(R.id.accy);
        accz = view.findViewById(R.id.accz);

        listener.onInputSensorsSent(currentData);

        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof SensorsListener) {
            listener = (SensorsListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SensorsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(magSensor != null){
            sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this.getActivity(), "Magnetic Field Sensor Not supported", Toast.LENGTH_SHORT).show();
        }

        if(gravSensor != null){
            sensorManager.registerListener(this, gravSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this.getActivity(), "Magnetic Field Sensor Not supported", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, gravSensor);
        sensorManager.unregisterListener(this, magSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                        * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                        * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                        * event.values[2];

                // mGravity = event.values;

            }


            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                //get adjusted values
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                        * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                        * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                        * event.values[2];

                x.setText(String.format(Locale.ENGLISH, "x: %.2f", mGeomagnetic[0]));
                y.setText(String.format(Locale.ENGLISH, "y: %.2f", mGeomagnetic[1]));
                z.setText(String.format(Locale.ENGLISH, "z: %.2f", mGeomagnetic[2]));

                tesla = Math.sqrt((mGeomagnetic[0] * mGeomagnetic[0]) + (mGeomagnetic[1] * mGeomagnetic[1]) + (mGeomagnetic[2] * mGeomagnetic[2]));

                String text = String.format(Locale.ENGLISH, "%.2f μT", tesla);
                reading.setText(text);
            }

            //rotation matrix
            boolean success = SensorManager.getRotationMatrix(Rm, Im, mGravity,
                    mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(Rm, orientation);
                azimuth = orientation[0];

                accx.setText(String.format(Locale.ENGLISH, "azimuth: %.2f", orientation[0]));
                accy.setText(String.format(Locale.ENGLISH, "pitch: %.2f", orientation[1]));
                accz.setText(String.format(Locale.ENGLISH, "roll: %.2f", orientation[2]));
                accreading.setText(String.format(Locale.ENGLISH, "%.2f", azimuth));
            }

            currentData = new Data(mGeomagnetic, tesla, orientation);
            createData(start);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Toast.makeText(getActivity(), "wow that's bad", Toast.LENGTH_SHORT);
    }

    private void createData(boolean start) {
        if(start == true){
            String data = tools.dataToJSON(new Data(mGeomagnetic, tesla, orientation, rooms[activeRoomID]));
            Log.d("data", data);
            dataJson.add(data);
        }
    }

    public String getCurrentDataJSON() {
        return tools.dataToJSON(currentData);
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public String[] getRooms() {
        return rooms;
    }

    public void setRooms(String[] rooms) {
        this.rooms = rooms;
    }

    public int getActiveRoomID() {
        return activeRoomID;
    }

    public void setActiveRoomID(int activeRoomID) {
        this.activeRoomID = activeRoomID;
    }

    public ArrayList<String> getDataJson() {
        return dataJson;
    }

    public void setDataJson(ArrayList<String> dataJson) {
        this.dataJson = dataJson;
    }


}
