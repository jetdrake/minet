package com.micool.minet;

import android.content.Intent;
import android.os.Bundle;
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
import com.micool.minet.DataClasses.Data;
import com.micool.minet.DataClasses.MetaData;
import com.micool.minet.Helpers.DataAdapter;
import com.micool.minet.Helpers.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomDB extends MainActivity {

    String root = "/";

    private TextView header;
    String Collection;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_roomdb);

        header = findViewById(R.id.room_header);

        Intent intent = getIntent();

        if (!intent.getBooleanExtra("useDB", false)){
            try{
                header.setText("local");
                ArrayList<String> local = intent.getStringArrayListExtra("local");

                if (!local.isEmpty()) Toast.makeText(this, "local", Toast.LENGTH_LONG).show();

                List<Map<String, Object>> dataArrayList = new ArrayList<>();

//                for ( String data : local) {
//                    dataArrayList.add(Tools.JSONToData(data));
//                }

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
                        List<Map<String, Object>> dataArrayList = new ArrayList<>();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
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
