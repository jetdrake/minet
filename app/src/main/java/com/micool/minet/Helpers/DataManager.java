package com.micool.minet.Helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.micool.minet.Models.Data;
import com.micool.minet.Models.MetaData;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataManager {

    private Data currentData;
    private MetaData currentMeta;
    //persistent storage
    private ArrayList<String> dataJson = new ArrayList<String>();
    //temp storage
    private ArrayList<Data> tempData = new ArrayList<Data>();

    private LinkedHashMap<MetaData, Data> dataPack = new LinkedHashMap<>();

    public DataManager() {

    }

    private void AddToDataPack(MetaData currentMeta, Data currentData) {
        dataPack.put(currentMeta, currentData);
    }

    public LinkedHashMap<MetaData, Data> getDataPack(){
        Log.d("dataPack", "getDataPack: "+ dataPack);
        return dataPack;
    }

    public void AddToDataPackFromCurrent () {
        AddToDataPack(currentMeta, currentData);
        Log.d("dataPack", "getDataPack: "+ dataPack);
    }

    public void createCurrentData(boolean start, MetaData meta, Data data) {
        if (start){
            if (meta != null) setCurrentMeta(meta);
            if (data != null) setCurrentData(data);
        }
    }

    public Data getCurrentData() {
        return currentData;
    }

    public void setCurrentData(Data currentData) {
        this.currentData = currentData;
    }

    public MetaData getCurrentMeta() {
        return currentMeta;
    }

    public void setCurrentMeta(MetaData currentMeta) {
        this.currentMeta = currentMeta;
    }

    public String getCurrentDataJSON() {
        return Serializer.dataToJSON(currentData).toString();
    }

    public String getCurrentMetaJSON() {
        return Serializer.metaToJSON(currentMeta).toString();
    }

    public String getCurrentDataPackJSON() {
        return Serializer.dataPackToJSON(currentMeta, currentData).toString();
    }

    public String getDataPackJson() {
        Gson gson = new Gson();
        String json = gson.toJson(dataPack);
        Log.d("json", "getDataPackJson: "+ json);
        return json;
    }
}
