package com.micool.minet;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.micool.minet.Fragments.Connector;
import com.micool.minet.Fragments.Sensors;

public class Navigation extends AppCompatActivity
    implements Sensors.SensorsListener, Connector.ConnectorListener {

    Sensors sensors;
    Connector connector;
    CheckBox useLive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //fragment stuff
        sensors = new Sensors();
        connector = new Connector();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.navConnectorContainer, connector)
                .replace(R.id.navSensorContainer, sensors)
                .commit();

        useLive = findViewById(R.id.useLive);
    }

    @Override
    public void onInputSensorsSent(String data) {
        if(useLive.isChecked()) connector.easyPublish(data, "realtime");
        //Log.d("realtime", data);
    }
}
