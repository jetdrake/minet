package com.micool.minet.Helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.micool.minet.DataClasses.Data;
import com.micool.minet.DataClasses.MetaData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataManager {

    private Data currentData = new Data(0.0f, 0.0f, 0.0f);
    private MetaData currentMeta = new MetaData("N", "0", "N/A"); //initialized to defaults
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
    }

    private Data averageTempData (){
        float x = 0, y = 0, z = 0;

        for (Data data : tempData) {
            x += data.getX();
            y += data.getY();
            z += data.getZ();

        }

        x /= tempData.size();
        y /= tempData.size();
        z /= tempData.size();


        return new Data(x,y,z);

    }

    public void createCurrentData(boolean start, MetaData meta, Data data) {
        if (start){
            setCurrentMeta(meta);
            setCurrentData(data);
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
        return Tools.dataToJSON(currentData).toString();
    }

    public String getCurrentMetaJSON() {
        return Tools.metaToJSON(currentMeta).toString();
    }

    public String getCurrentDataPackJSON() {
        return Tools.dataPackToJSON(currentMeta, currentData).toString();
    }

    public String getDataPackJson() {
        Gson gson = new Gson();
        String json = gson.toJson(dataPack);
        Log.d("json", "getDataPackJson: "+ json);
        return json;
    }
}
