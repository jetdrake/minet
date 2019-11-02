package com.micool.minet;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.*;

import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;

public class Cartographer extends MainActivity implements SensorEventListener {

    private static SensorManager sensorManager;
    private static Sensor magSensor;
    private static Sensor accSensor;
    TextView reading;
    TextView x;
    TextView y;
    TextView z;
    Boolean start = false;

    FirebaseFirestore db;
    String root = "/Localization/Apartment/";
    String Collection;
    ToggleButton startSending;
    TextView input;
    TextView calc_room;
    Button calc;
    Boolean calcStart = false;
    Button roomdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cartographer);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        input = findViewById(R.id.room);
        reading = findViewById(R.id.reading);
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);
        startSending = findViewById(R.id.startButton);
        startSending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String text = input.getText().toString();
                    Toast.makeText(Cartographer.this, text ,Toast.LENGTH_LONG).show();
                    Collection = text.isEmpty() ? "default" : text;
                    startSending();
                } else {
                    stopSending();
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

    public void stopSending() {
        Toast.makeText(Cartographer.this, "stop sending data",Toast.LENGTH_LONG).show();
        start = false;
    }
    public void startSending() {
        Toast.makeText(Cartographer.this, "start sending",Toast.LENGTH_LONG).show();
        start = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        int slow = 400000000;

        if(magSensor != null){
            sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(Cartographer.this, "Magnetic Field Sensor Not supported", Toast.LENGTH_SHORT).show();
        }

        if(accSensor != null){
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        } else {
            Toast.makeText(Cartographer.this, "Accelerometer Not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    /*
    double previousTesla = 0.0;
    int runCounter = 0;
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        double azimuth = event.values[0];
        double pitch = event.values[1];
        double roll = event.values[2];

        x.setText(String.format(Locale.ENGLISH, "x: %.2f", azimuth));
        y.setText(String.format(Locale.ENGLISH, "y: %.2f", pitch));
        z.setText(String.format(Locale.ENGLISH, "z: %.2f", roll));


        double tesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));
        /*
        if (runCounter < 1){
            previousTesla = tesla;
        }
        runCounter++;
        //and accelerometer has not moved (should help with accuracy)
        if(tesla <= 1.5 * previousTesla){
            String text = String.format(Locale.ENGLISH, "%.2f + μT", tesla);
            reading.setText(text);
        }
        previousTesla = tesla;

         */

        String text = String.format(Locale.ENGLISH, "%.2f μT", tesla);
        reading.setText(text);

        //send to db
        if(start == true){
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            //creates new document (timestamp) with current data
            db.collection(root + Collection)
                    .document(timestamp.toString())
                    .set(new Data(azimuth, pitch, roll, tesla));
        }

        if(calcStart == true) {
            CollectionReference ref = db.collection(root);

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

}
