package com.micool.minet;

import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import org.json.*;

import io.grpc.okhttp.internal.framed.Header;

public class RoomDB extends MainActivity {

    String root = "/";

    private TextView header;
    String Collection;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.roomdb);

        header = findViewById(R.id.room_header);

        Intent intent = getIntent();

        if (!intent.getBooleanExtra("useDB", false)){
            try{
                header.setText("local");
                ArrayList<String> local = intent.getStringArrayListExtra("local");

                if (!local.isEmpty()) Toast.makeText(this, "local", Toast.LENGTH_LONG).show();

                List<Data> dataArrayList = new ArrayList<>();

                for ( String data : local) {
                    dataArrayList.add(tools.JSONToData(data));
                }

                ListView dataList = findViewById(R.id.dataList);
                DataAdapter dataAdapter = new DataAdapter(RoomDB.this, dataArrayList);
                dataList.setAdapter(dataAdapter);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                Collection = intent.getStringExtra("room");
                header.setText(Collection);
                root += Collection;

                Toast.makeText(this, "searching: " + root, Toast.LENGTH_LONG).show();

                db = FirebaseFirestore.getInstance();

                db.collection(root).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Data> dataArrayList = new ArrayList<>();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                Data data = document.toObject(Data.class);
                                dataArrayList.add(data);
                            }
                            ListView dataList = findViewById(R.id.dataList);
                            DataAdapter dataAdapter = new DataAdapter(RoomDB.this, dataArrayList);
                            dataList.setAdapter(dataAdapter);
                        } else {
                            Log.d("RoomDB", "Error getting documents: ", task.getException());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
            }
        }
    }

}
