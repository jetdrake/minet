package com.micool.minet;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.firestore.*;

import java.sql.Timestamp;
import java.util.Locale;

public class Cartographer extends MainActivity implements SensorEventListener {
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

    //buttons, views, and control variables
    TextView reading;
    TextView x;
    TextView y;
    TextView z;
    TextView accreading;
    TextView accx;
    TextView accy;
    TextView accz;
    Boolean start = false;
    ToggleButton startSending;
    TextView input;
    TextView calc_room;
    Button calc;
    Boolean calcStart = false;
    Button roomdb;
    int counter = 0;

    //database
    FirebaseFirestore db;
    String root = "/Localization/Apartment/";
    String Collection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cartographer);

        //get SensorManager and create sensors (on every creation)
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //get buttons and views
        input = findViewById(R.id.room);
        reading = findViewById(R.id.reading);
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);

        accreading = findViewById(R.id.accreading);
        accx = findViewById(R.id.accx);
        accy = findViewById(R.id.accy);
        accz = findViewById(R.id.accz);

        //controls the data sending
        startSending = findViewById(R.id.startButton);
        startSending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String text = input.getText().toString();
                    Collection = text.isEmpty() ? "default" : text;

                    Toast.makeText(Cartographer.this, "sending data to: " + Collection ,Toast.LENGTH_LONG).show();
                    start = true;
                } else {
                    Toast.makeText(Cartographer.this, "stopping sending data" ,Toast.LENGTH_LONG).show();
                    start = false;
                }
            }
        });

        //text
        calc_room = findViewById(R.id.calc_room);
        //button
        calc = findViewById(R.id.calc);

        db = FirebaseFirestore.getInstance();

        roomdb = findViewById(R.id.db);
        roomdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Cartographer.this, RoomDB.class);
                String text = input.getText().toString();
                intent.putExtra("room", text.isEmpty() ? "default" : text);
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(magSensor != null){
            sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(Cartographer.this, "Magnetic Field Sensor Not supported", Toast.LENGTH_SHORT).show();
        }

        if(gravSensor != null){
            sensorManager.registerListener(this, gravSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(Cartographer.this, "Magnetic Field Sensor Not supported", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
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

            //send to db
            if (counter <= 25) sendToDB(start);
            counter++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Toast.makeText(Cartographer.this, "wow that's bad", Toast.LENGTH_SHORT);
    }

    //https://stackoverflow.com/questions/50035752/how-to-get-list-of-documents-from-a-collection-in-firestore-android

    private String calcRoom(){




        return "";
    }

    private void sendToDB(Boolean start){
        if(start){
            String text = input.getText().toString();
            Collection = text.isEmpty() ? "default" : text;

            //send to db
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            //creates new document (timestamp) with current data
            db.collection(root + Collection)
                    .document(timestamp.toString())
                    .set(new Data(mGeomagnetic, tesla, orientation));
        }
    }

}
