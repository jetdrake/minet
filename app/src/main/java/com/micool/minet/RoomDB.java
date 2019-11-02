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

import java.util.ArrayList;
import java.util.List;

import io.grpc.okhttp.internal.framed.Header;

public class RoomDB extends MainActivity {

    String root = "/Localization/Apartment/";

    private TextView header;
    String HeaderText = "default";

    private FirebaseFirestore db;

    private DataAdapter dataAdapter;
    private ArrayList<Data> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.roomdb);

        header = findViewById(R.id.room_header);

        Intent intent = getIntent();
        HeaderText = intent.getStringExtra("room");
        header.setText(HeaderText);
        root += HeaderText;

        Toast.makeText(this, "searching: " + root, Toast.LENGTH_LONG);

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
                    DataAdapter mMissionAdapter = new DataAdapter(RoomDB.this, dataArrayList);
                    dataList.setAdapter(mMissionAdapter);
                } else {
                    Log.d("RoomDB", "Error getting documents: ", task.getException());
                }
            }
        });

    }
}
