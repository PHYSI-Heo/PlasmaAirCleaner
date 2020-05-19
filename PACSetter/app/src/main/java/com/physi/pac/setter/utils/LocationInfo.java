package com.physi.pac.setter.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class LocationInfo {

    private static final String TAG = LocationInfo.class.getSimpleName();

    private JSONArray locationObjs;
    private List<String> cityNames = new LinkedList<>();
    private List<String> provinceNames = new LinkedList<>();

    private Context context;

    public LocationInfo(Context context){
        this.context = context;
        setCityList(context);
    }

    private void setCityList(Context context){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("CityList.txt"), StandardCharsets.UTF_8));
            String data;
            while ((data = reader.readLine()) != null) {
                builder.append(data);
            }
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }

        try {
            locationObjs = new JSONArray(builder.toString());
            cityNames.add("시/도");
            for(int i = 0; i < locationObjs.length(); i++){
                cityNames.add(locationObjs.getJSONObject(i).getString("name").trim());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<String> getCityList(){
        return cityNames;
    }

    public int getCityPosition(String city){
        return cityNames.indexOf(city);
    }

    public void setProvince(int cityIndex){
        provinceNames.clear();
        try {
            if(cityIndex == 0)
                provinceNames.add("시/군/구");
            else{
                JSONArray arrayObj = locationObjs.getJSONObject(cityIndex - 1).getJSONArray("gugun");
                for(int i = 0; i < arrayObj.length(); i++){
                    provinceNames.add(arrayObj.getJSONObject(i).getString("name").trim());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<String> getProvinceNames(){
        return provinceNames;
    }

    public int getProvincePosition(String province){
        return provinceNames.indexOf(province);
    }

}
