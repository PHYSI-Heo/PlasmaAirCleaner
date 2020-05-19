package com.physi.pac.setter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.physi.pac.setter.data.DeviceInfo;
import com.physi.pac.setter.http.HttpPacket;
import com.physi.pac.setter.http.HttpRequestActivity;
import com.physi.pac.setter.mqtt.MQTTClient;
import com.physi.pac.setter.utils.DBHelper;
import com.physi.pac.setter.utils.LocationInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SetupActivity extends HttpRequestActivity implements View.OnClickListener {

    private static final String TAG = SetupActivity.class.getSimpleName();

    private Button btnEditName, btnDeleteDevice, btnSetup;
    private EditText etName, etDisplayTime;
    private Switch swcCCTVEnable;
    private Spinner spnCity, spnProvince;

    private DBHelper dbHelper;
    private LocationInfo locationInfo;

    private DeviceInfo deviceInfo;
    private String savedCity, savedProvince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        init();
        getSetupInformation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        etName.setText(deviceInfo.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_edit_name:
                editDeviceName();
                break;
            case R.id.btn_delete:
                deleteDevice();
                break;
            case R.id.btn_setup:
                updateSetupInformation();
                break;
        }
    }

    @Override
    protected void onHttpResponse(String url, JSONObject resObj) {
        super.onHttpResponse(url, resObj);
        try {
            switch (url) {
                case HttpPacket.GET_INFO_URL:
                    JSONObject dataObj = resObj.getJSONArray(HttpPacket.PARAMS_ROW_DATA).getJSONObject(0);
                    showSetupInformation(dataObj);
                    break;
                case HttpPacket.UPDATE_INFO_URL:
                    Toast.makeText(getApplicationContext(), "UPDATE SUCCESSFUL.", Toast.LENGTH_SHORT).show();
                    MQTTClient.getInstance().publish(deviceInfo.getId(), "SETUP");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(position == -1)
                return;
            locationInfo.setProvince(position);
            spnProvince.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.view_spinner_item,
                    locationInfo.getProvinceNames()));
            if(savedProvince != null && !savedProvince.equals("null")){
                spnProvince.setSelection(locationInfo.getProvincePosition(savedProvince));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    public LatLng getLocationPoint(String city, String province){
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            List<Address> list = geocoder.getFromLocationName(city + " " + province, 1);
            if (list != null && list.size() != 0) {
                Address addr = list.get(0);
                double lat = addr.getLatitude();
                double lon = addr.getLongitude();
                Log.e(TAG, "> Latitude : " + lat + ", Longitude : " + lon);
                return new LatLng(lat, lon);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateSetupInformation() {
        if(spnCity.getSelectedItemPosition() == 0){
            Toast.makeText(getApplicationContext(), "날씨 지역 정보를 설정하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject paramsObj = new JSONObject();
        try {
            String cityName = spnCity.getSelectedItem().toString();
            String provinceName = spnProvince.getSelectedItem().toString();
            LatLng latLng = getLocationPoint(cityName, provinceName);
            if(latLng == null){
                Toast.makeText(getApplicationContext(), "지역 설정 오류가 발생하였습니다.\n잠시 후 다시 시도해주세요. .", Toast.LENGTH_SHORT).show();
                return;
            }
            paramsObj.put(HttpPacket.PARAMS_DEVICE_ID, deviceInfo.getId());
            paramsObj.put(HttpPacket.PARAMS_CITY, spnCity.getSelectedItem().toString());
            paramsObj.put(HttpPacket.PARAMS_PROVINCE, spnProvince.getSelectedItem().toString());
            paramsObj.put(HttpPacket.PARAMS_LOCATION_LAT, String.valueOf(latLng.latitude));
            paramsObj.put(HttpPacket.PARAMS_LOCATION_LON, String.valueOf(latLng.longitude));
            paramsObj.put(HttpPacket.PARAMS_DISPLAY_TIME, etDisplayTime.getText().toString());
            paramsObj.put(HttpPacket.PARAMS_CCTV_ENABLE, swcCCTVEnable.isChecked() ? "1" : "0");
            requestAPI(HttpPacket.UPDATE_INFO_URL, paramsObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showSetupInformation(JSONObject dataObj) {
        try {
            swcCCTVEnable.setChecked(dataObj.getString(HttpPacket.PARAMS_CCTV_ENABLE).equals("1"));
            etDisplayTime.setText(dataObj.getString(HttpPacket.PARAMS_DISPLAY_TIME));
            savedCity = dataObj.getString(HttpPacket.PARAMS_CITY);
            savedProvince = dataObj.getString(HttpPacket.PARAMS_PROVINCE);
            if(savedCity != null && !savedCity.equals("null")){
                spnCity.setSelection(locationInfo.getCityPosition(savedCity));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getSetupInformation(){
        JSONObject paramsObj = new JSONObject();
        try {
            paramsObj.put(HttpPacket.PARAMS_DEVICE_ID, deviceInfo.getId());
            requestAPI(HttpPacket.GET_INFO_URL, paramsObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void editDeviceName(){
        if(etName.getText().length() == 0)
            return;
        ContentValues param = new ContentValues();
        param.put(DBHelper.COL_NAME, etName.getText().toString());
        boolean result = dbHelper.updateData(param, DBHelper.COL_DEVICE_ID, deviceInfo.getId());
        Toast.makeText(getApplicationContext(), "UPDATE RESULT : " + result, Toast.LENGTH_SHORT).show();
        if(result)
            deviceInfo.setName(etName.getText().toString());
    }

    private void deleteDevice(){
        boolean result = dbHelper.deleteData(DBHelper.COL_DEVICE_ID, deviceInfo.getId());
        Toast.makeText(getApplicationContext(), "DELETE RESULT : " + result, Toast.LENGTH_SHORT).show();
        if(result) {
            finish();
        }
    }

    private void init() {
//        ActionBar actionBar = getSupportActionBar();
//        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_back);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle("DEVICE SETUP");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        dbHelper = new DBHelper(getApplicationContext());
        deviceInfo = getIntent().getParcelableExtra("INFO");
        locationInfo = new LocationInfo(getApplicationContext());

        btnEditName = findViewById(R.id.btn_edit_name);
        btnEditName.setOnClickListener(this);
        btnDeleteDevice = findViewById(R.id.btn_delete);
        btnDeleteDevice.setOnClickListener(this);
        btnSetup = findViewById(R.id.btn_setup);
        btnSetup.setOnClickListener(this);

        etName = findViewById(R.id.et_name);
        etDisplayTime = findViewById(R.id.et_display_time);

        swcCCTVEnable = findViewById(R.id.swc_cctv_enable);

        spnCity = findViewById(R.id.spn_city);
        spnProvince = findViewById(R.id.spn_province);

        spnCity.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                R.layout.view_spinner_item,
                locationInfo.getCityList()));
        spnCity.setOnItemSelectedListener(itemSelectedListener);
    }

}
