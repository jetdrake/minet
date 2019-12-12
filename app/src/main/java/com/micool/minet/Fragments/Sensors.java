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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micool.minet.Data;
import com.micool.minet.Helpers.SOTWFormatter;
import com.micool.minet.R;
import com.micool.minet.Helpers.Tools;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

public class Sensors extends Fragment implements SensorEventListener {

    private SensorsListener listener;

    public interface SensorsListener {
        void onInputSensorsSent(String data);
    }

    //sensors
    private static SensorManager sensorManager;
    private static Sensor magSensor;
    private static Sensor gravSensor;
    //private static Sensor stepSensor;

    //value storage
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] Rm = new float[9];
    private float[] Im = new float[9];
    float orientation[] = new float[3];
    float azimuth;
    float lazimuth;
    String direction;
    double tesla;
    int stepCount = 1;

    TextView reading;
    TextView x;
    TextView y;
    TextView z;
    TextView accreading;
    TextView accx;
    TextView accy;
    TextView accz;
    Button stepBtn;

    RadioGroup dataSelect;
    int activeDataID = 0;

    String [] rooms;
    int activeRoomID;
    boolean start = false;
    Data currentData;
    //persistent storage
    ArrayList<String> dataJson = new ArrayList<String>();
    //temp storage
    ArrayList<Data> tempData = new ArrayList<Data>();



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_sensors, container, false);
        //get SensorManager and create sensors (on every creation)
        sensorManager = (SensorManager)this.getActivity().getSystemService(SENSOR_SERVICE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //get buttons and views for magnet and acc data
        reading = view.findViewById(R.id.reading);
        x = view.findViewById(R.id.x);
        y = view.findViewById(R.id.y);
        z = view.findViewById(R.id.z);

        accreading = view.findViewById(R.id.accreading);
        accx = view.findViewById(R.id.accx);
        accy = view.findViewById(R.id.accy);
        accz = view.findViewById(R.id.accz);

        stepBtn = view.findViewById(R.id.stepBtn);
        dataSelect = view.findViewById(R.id.radioDataTypes);

        dataSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                activeDataID = checkedId;
            }
        });

        stepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataJson.add(Tools.dataToJSON(averageTempData()));
                tempData.clear();
                stepBtn.setText(""+ stepCount++);
            }
        });

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
            Toast.makeText(this.getActivity(), "Accelerometer Sensor Not supported", Toast.LENGTH_SHORT).show();
        }

        /*
        if(stepSensor != null){
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this.getActivity(), "Step Sensor Not supported", Toast.LENGTH_SHORT).show();
        }

         */

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

                float lazimuth = (float) Math.toDegrees(azimuth);
                lazimuth = (lazimuth + 360) % 360;
                SOTWFormatter formatter = new SOTWFormatter();

                direction = formatter.formatNum(lazimuth);

                accreading.setText(formatter.format(lazimuth));
            }

            currentData = createCurrentData(activeDataID);
            listener.onInputSensorsSent(getCurrentDataJSON());

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Toast.makeText(getActivity(), "wow that's bad", Toast.LENGTH_SHORT);
    }

    private void createData(boolean start, int dataID) {
        if(start == true){
            String data = Tools.dataToJSON(currentData);
            Log.d("data", data);
            dataJson.add(data);
        }
    }

    private Data createCurrentData (int dataID) {
        Data data = null;
        switch (dataID){
            case 0:
                data = new Data(mGeomagnetic, tesla, orientation, rooms != null ? rooms[activeRoomID] : "Room not set", direction);
                break;

            case 1:
                data = new Data(mGeomagnetic, tesla, orientation, rooms != null ? rooms[activeRoomID] : "Room not set");
                break;

            case 2:
                data = new Data(tesla, rooms != null ? rooms[activeRoomID] : "Room not set", direction);
                break;

            default:
                break;
        }

        if(start) tempData.add(data);

        return data;
    }

    private Data averageTempData (){
        float x = 0, y = 0, z = 0, azimuth = 0, pitch = 0, roll = 0;
        double tesla = 0;
        for (Data data : tempData) {
            x += data.getX();
            y += data.getY();
            z += data.getZ();
            azimuth += data.getAzimuth();
            pitch += data.getPitch();
            roll += data.getRoll();
            tesla += data.getTesla();
        }

        x /= tempData.size();
        y /= tempData.size();
        z /= tempData.size();
        azimuth /= tempData.size();
        pitch /= tempData.size();
        roll /= tempData.size();
        tesla /= tempData.size();


        return new Data(x,y,z,azimuth,pitch,roll,tesla,tempData.get(0).getID(), tempData.get(0).getDirection());

    }

    public String getCurrentDataJSON() {
        return Tools.dataToJSON(currentData);
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
