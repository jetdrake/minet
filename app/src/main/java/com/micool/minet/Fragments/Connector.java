package com.micool.minet.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micool.minet.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;



public class Connector extends Fragment {

    private static final String TAG = "PahoMqttClient";
    public MqttAndroidClient client;

    private ConnectorListener listener;
    boolean connected = false;
    TextView connection;
    ToggleButton connect;

    public interface ConnectorListener{

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_connector, container, false);

        connection = view.findViewById(R.id.connection);

        connect = view.findViewById(R.id.connect);
        connect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String clientId = MqttClient.generateClientId();
                    getMqttClient(getActivity(), getString(R.string.broker), clientId, "micool", "sophie");
                    connection.setTextColor(Color.GREEN);
                } else {
                    try {
                        disconnect(client);
                        connection.setTextColor(Color.RED);
                    } catch (MqttException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Error disconnecting", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    public MqttAndroidClient getMqttClient(Context context, String brokerUrl, String clientId, String clientUn, String clientPw) {
        client = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            MqttConnectOptions myMqttcnxoptions = getMqttConnectionOption();
            if(clientUn.trim().length() > 0) {
                myMqttcnxoptions.setUserName(clientUn);
            }
            if(clientPw.trim().length() > 0) {
                myMqttcnxoptions.setPassword(clientPw.toCharArray());
            }
            IMqttToken token = client.connect(myMqttcnxoptions);

            //IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption(clientUn,clientPw));
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return client;
    }

    public void disconnect(@NonNull MqttAndroidClient client) throws MqttException {
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Successfully disconnected");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString());
            }
        });
    }

    @NonNull
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    @NonNull
    private MqttConnectOptions getMqttConnectionOption() {
        //private MqttConnectOptions getMqttConnectionOption(String clientUn, String clientPw) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setWill(Constants.PUBLISH_TOPIC, "I am going offline".getBytes(), 1, true);
        //mqttConnectOptions.setUserName("dave_test1");
        //mqttConnectOptions.setPassword("dave_test123".toCharArray());
        //mqttConnectOptions.setUserName(un);
        //mqttConnectOptions.setPassword(clientPw.toCharArray());
        return mqttConnectOptions;
    }


    public void publishMessage(@NonNull MqttAndroidClient client, @NonNull String msg, int qos, @NonNull String topic)
            throws MqttException, UnsupportedEncodingException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(320);
        message.setRetained(true);
        message.setQos(qos);
        client.publish(topic, message);
    }

    public void subscribe(@NonNull MqttAndroidClient client, @NonNull final String topic, int qos) throws MqttException {
        IMqttToken token = client.subscribe(topic, qos);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Subscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "Subscribe Failed " + topic);

            }
        });
    }

    public void unSubscribe(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {

        IMqttToken token = client.unsubscribe(topic);

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "UnSubscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "UnSubscribe Failed " + topic);
            }
        });
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof ConnectorListener) {
            listener = (ConnectorListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MQTTListner");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public boolean isConnected() {
        return connected;
    }
}
