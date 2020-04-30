package com.micool.minet;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.micool.minet.Models.Data;
import com.micool.minet.Models.MetaData;
import com.micool.minet.Fragments.Sensors;
import com.micool.minet.Helpers.Serializer;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


public class Cartographer extends MainActivity implements Sensors.SensorsListener {
    ToggleButton startSending;
    TextView input;
    Button senddb;

    int stepCount = 1;

    //Radio Buttons and Dynamic adding
    TableLayout layout;
    RadioGroup radioGroup;
    Button enter;
    TextView roomsText;
    String [] rooms;

    TextView reading;
    TextView x;
    TextView y;
    TextView z;
    TextView accreading;
    TextView accx;
    TextView accy;
    TextView accz;

    //database
    FirebaseFirestore db;
    String Collection;

    Sensors sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartographer);

        //fragment stuff
        sensors = new Sensors();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.carSensorContainer, sensors)
                .commit();

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
        input = findViewById(R.id.dbtext);
        roomsText = findViewById(R.id.roomsText);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //need to make this handle more cases
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

                    sensors.setRooms(rooms);

                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            sensors.setActiveRoomID(checkedId);
                            //Toast.makeText(getApplicationContext(), rooms[activeRoomID], Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {
                    Toast.makeText(Cartographer.this, "No RadioGroup or Rooms Provided", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //controls the data sending
        startSending = findViewById(R.id.startButton);
        startSending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(Cartographer.this, "reading data" ,Toast.LENGTH_SHORT).show();
                    if (rooms == null && !enter.getText().toString().isEmpty()) enter.performClick();
                    sensors.onSetStart(true);
                } else {
                    Toast.makeText(Cartographer.this, "stopping data" ,Toast.LENGTH_SHORT).show();
                    sensors.onSetStart(false);
                }
            }
        });

        db = FirebaseFirestore.getInstance();

        senddb = findViewById(R.id.send);
        senddb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToDB();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void sendToDB() {
        startSending.setChecked(false);
        String text = input.getText().toString();
        Collection = text.isEmpty() ? "default" : text;

        //String json = sensors.getDataPackJson();

        //send to db with Timestamp
        LinkedHashMap<MetaData, Data> map = sensors.getDataPack();

        Iterator it = map.entrySet().iterator();

        while(it.hasNext()){
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Map.Entry pair = (Map.Entry)it.next();

            Map<String, Object> data = new HashMap<>();
            data.put("meta", pair.getKey());
            data.put("data", pair.getValue());

            db.collection("/" + Collection)
                    .document(timestamp.toString())
                    .set(data)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("document", "Error adding document", e);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(Cartographer.this, "Sent to: " + Collection, Toast.LENGTH_SHORT).show();
                }
            });


        }


    }


    @Override
    public void onInputSensorsSent(String data) {

    }

    @Override
    public void onMagDataSent(float[] mag) {
        x.setText(String.format(Locale.ENGLISH, "x: %.2f", mag[0]));
        y.setText(String.format(Locale.ENGLISH, "y: %.2f", mag[1]));
        z.setText(String.format(Locale.ENGLISH, "z: %.2f", mag[2]));
    }

    @Override
    public void onGravDataSent(float[] grav) {
        accx.setText(String.format(Locale.ENGLISH, "azimuth: %.2f", grav[0]));
        accy.setText(String.format(Locale.ENGLISH, "pitch: %.2f", grav[1]));
        accz.setText(String.format(Locale.ENGLISH, "roll: %.2f", grav[2]));
    }

    @Override
    public void onDirectionSent(String direction) {
        accreading.setText(direction);
    }

    @Override
    public void onTeslaSent(double tesla) {
        reading.setText(String.format(Locale.ENGLISH, "%.2f μT", tesla));
    }
}
