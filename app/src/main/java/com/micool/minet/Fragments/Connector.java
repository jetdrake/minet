package com.micool.minet.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    public String broker;
    private ConnectorListener listener;
    boolean connected = false;
    TextView brokerAddressView;
    TextView connection;
    Button connect;

    public interface ConnectorListener{
        void onConnectionSent(Boolean connection);

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_connector, container, false);

        //bad naming -
        //  connect = button, connected = boolean, connection = text representation of connected
        GetIPFromFirebase();
        brokerAddressView = view.findViewById(R.id.brokerAddress);
        connection = view.findViewById(R.id.connection);
        connect = view.findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(connected) {
                    try {
                        disconnect(client);
                    } catch (MqttException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Error disconnecting", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String clientId = MqttClient.generateClientId();
                    client = getMqttClient(getActivity(), broker, clientId);
                    //Toast.makeText(getActivity(), broker, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if(connected) disconnect(client);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(connected){
            String clientId = MqttClient.generateClientId();
            client = getMqttClient(getActivity(), broker, clientId);
        }

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
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                    onConnect();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                    onDisconnect();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return client;
    }

    public MqttAndroidClient getMqttClient(Context context, String brokerUrl, String clientId) {
        client = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                    onConnect();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                    onDisconnect();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return client;
    }

    public void disconnect(@NonNull MqttAndroidClient client) throws MqttException {
        listener.onConnectionSent(false);
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Successfully disconnected");
                onDisconnect();
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString());
                onConnect();
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

    public void easyPublish(@NonNull String msg, @NonNull String topic){
        try {
            publishMessage(client, msg, 0, topic);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
        }
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

    public void initBroker (String ip){
        broker = "tcp://" + ip + ":1883";
        connect.setEnabled(true);
        brokerAddressView.setText(broker);
    }

    public void GetIPFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ip").getRef();
        ValueEventListener ipListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // IP address
                String ip = dataSnapshot.getValue().toString();
                initBroker(ip);
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting IP failed, log a message
                Log.w("ip", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        ref.addListenerForSingleValueEvent(ipListener);
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

    public void onConnect () {
        connection.setTextColor(Color.GREEN);
        connect.setText("Disconnect");
        connected = true;
        listener.onConnectionSent(true);
    }

    public void onDisconnect () {
        connection.setTextColor(Color.RED);
        connect.setText("Connect");
        connected = false;
        listener.onConnectionSent(false);
    }
}
