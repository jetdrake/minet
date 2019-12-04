package com.micool.minet;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import org.json.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
    Button senddb;
    int counter = 0;

    //Radio Buttons and Dynamic adding
    TableLayout layout;
    RadioGroup radioGroup;
    Button enter;
    TextView roomsText;
    String [] rooms;
    CheckBox useDB;

    //database
    FirebaseFirestore db;
    String root = "/";
    String Collection;

    //Data and timer
    Timer timer = new Timer();
    int activeRoomID;
    ArrayList<String> dataJson = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cartographer);

        //get SensorManager and create sensors (on every creation)
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //get buttons and views for magnet and acc data
        input = findViewById(R.id.room);
        reading = findViewById(R.id.reading);
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);

        accreading = findViewById(R.id.accreading);
        accx = findViewById(R.id.accx);
        accy = findViewById(R.id.accy);
        accz = findViewById(R.id.accz);

        //radio button set up
        layout = findViewById(R.id.rootContainer);
        radioGroup = findViewById(R.id.radioGroup);
        enter = findViewById(R.id.enter);
        roomsText = findViewById(R.id.roomsText);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(radioGroup != null && !roomsText.getText().toString().isEmpty()){
                    //create radio buttons programmatically
                    String rawText = roomsText.getText().toString();
                    rooms = rawText.split(",");

                    int counter = 0;
                    for (String room : rooms) {
                        RadioButton radioButton = new RadioButton(Cartographer.this);
                        if (counter == 0) radioButton.setChecked(true);
                        radioButton.setLayoutParams(new TableLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
                        radioButton.setText(room);
                        radioButton.setId(counter++);
                        radioGroup.addView(radioButton);
                    }

                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            activeRoomID = checkedId;
                            //Toast.makeText(getApplicationContext(), rooms[activeRoomID], Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {
                    Toast.makeText(Cartographer.this, "No RadioGroup or Rooms Provided", Toast.LENGTH_SHORT).show();
                }

            }
        });

        useDB = findViewById(R.id.use);

        //controls the data sending
        startSending = findViewById(R.id.startButton);
        startSending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(Cartographer.this, "reading data: " + Collection ,Toast.LENGTH_SHORT).show();
                    start = true;
                } else {
                    Toast.makeText(Cartographer.this, "stopping data" ,Toast.LENGTH_SHORT).show();
                    start = false;
                }
            }
        });

        db = FirebaseFirestore.getInstance();

        roomdb = findViewById(R.id.db);
        roomdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Cartographer.this, RoomDB.class);
                String text = input.getText().toString();
                boolean db = false;
                //controls whether gets local data or
                if (useDB.isChecked()) db = true;
                intent.putExtra("useDB", db);
                intent.putExtra("local", dataJson);
                intent.putExtra("room", text.isEmpty() ? "default" : text);
                view.getContext().startActivity(intent);
            }
        });

        senddb = findViewById(R.id.send);
        senddb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToDB();
                Toast.makeText(Cartographer.this, "Sent to: " + Collection, Toast.LENGTH_SHORT).show();
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

            //begin collection or send to database
            //todo: add database control, so that this can be sent to firebase
            createData(start);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Toast.makeText(Cartographer.this, "wow that's bad", Toast.LENGTH_SHORT);
    }

    //https://stackoverflow.com/questions/50035752/how-to-get-list-of-documents-from-a-collection-in-firestore-android

    private void sendToDB() {
        String text = input.getText().toString();
        Collection = text.isEmpty() ? "default" : text;

        //send to db with Timestamp
        for (String data : dataJson) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            db.collection(root + Collection)
                    .document(timestamp.toString())
                    .set(tools.JSONToData(data));
        }

    }



    private void createData(boolean start) {
        if(start == true){
            String data = tools.dataToJSON(new Data(mGeomagnetic, tesla, orientation, rooms[activeRoomID]));
            Log.d("data", data);
            dataJson.add(data);
        }
    }

}
