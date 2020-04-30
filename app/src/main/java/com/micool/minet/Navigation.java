package com.micool.minet;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.micool.minet.Fragments.Connector;
import com.micool.minet.Fragments.Sensors;
import com.micool.minet.Helpers.Mapper;
import com.micool.minet.Models.Coordinate;
import com.micool.minet.Views.GraphView;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.List;

public class Navigation extends MainActivity
    implements Sensors.SensorsListener, Connector.ConnectorListener {

    final String TAG = "nav";
    private Button stepBtn;
    Sensors sensors;
    Connector connector;
    private float stepId = 0.0f;
    TextView subscribeText;
    GraphView graphView;
    List<Coordinate> map;
    Mapper mapper = new Mapper();
    String dang;

    private final String test = "[[0, 1], [0, 0], [0, 6], [0, 5], [0, 4], [0, 3], [0, 2]]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //fragment stuff
        sensors = new Sensors();
        connector = new Connector();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.navConnectorContainer, connector)
                .add(0, sensors, "sens")
                .commit();

        FragmentManager fm = getSupportFragmentManager();

        stepBtn = findViewById(R.id.navStepBtn);
        stepBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                connector.easyPublish(dang, "realtime");
            }
        });

        subscribeText = findViewById(R.id.subscribeText);
        graphView = findViewById(R.id.myview);
    }

    @Override
    public void onInputSensorsSent(String data) {
        //Log.d(TAG, "onInputSensorsSent: " + data);
        dang = data;
    }

    @Override
    public void onMagDataSent(float[] mag) {

    }

    @Override
    public void onGravDataSent(float[] grav) {

    }

    @Override
    public void onDirectionSent(String direction) {

    }

    @Override
    public void onTeslaSent(double tesla) {

    }

    @Override
    public void onConnectionSent(Boolean connection) {
        if (connection) {
            stepBtn.setEnabled(true);
        } else {
            stepBtn.setEnabled(false);
        }
    }

    @Override
    public void onSubscriptionDataSent(String data){
        if(data != null) {
            subscribeText.setText(data);

            graphView.setActiveLandmark(data);
            graphView.invalidate();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        stepBtn.setEnabled(false);
    }

    @Override
    public void onMapSent(String data) {
        if (data != null && data != ""){
            Log.d(TAG, "data: " + data);
            graphView.buildMapFromData(data);
            graphView.invalidate();
            try {
                connector.unSubscribe(connector.client, "map");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
