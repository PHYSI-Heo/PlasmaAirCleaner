package com.physi.pac.setter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.physi.pac.setter.data.DeviceInfo;
import com.physi.pac.setter.list.DeviceAdapter;
import com.physi.pac.setter.mqtt.MQTTClient;
import com.physi.pac.setter.utils.DBHelper;
import com.physi.pac.setter.utils.SystemEnv;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DeviceActivity extends AppCompatActivity implements View.OnClickListener, DeviceAdapter.OnSelectedListener {

    private static final String TAG = DeviceActivity.class.getSimpleName();

    private Button btnRegister;
    private RecyclerView rcvDevice;
    private TextView tvNoItem;

    private DBHelper dbHelper;
    private DeviceAdapter deviceAdapter;
    private MQTTClient mqttClient;

    private List<DeviceInfo> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        init();
        mqttClient.connect(getApplicationContext(), SystemEnv.BROKER_IP, SystemEnv.BROKER_PORT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        devices = dbHelper.getDeviceList();
        if(devices.size() == 0){
            tvNoItem.setVisibility(View.VISIBLE);
            rcvDevice.setVisibility(View.GONE);
        }else{
            tvNoItem.setVisibility(View.GONE);
            rcvDevice.setVisibility(View.VISIBLE);
        }
        deviceAdapter.setItems(devices);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttClient.disconnect();
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_register){
            startActivity(new Intent(DeviceActivity.this, RegisterActivity.class));
        }
    }

    @Override
    public void onDataSetup(int index) {
        startActivity(new Intent(DeviceActivity.this, SetupActivity.class)
                .putExtra("INFO", devices.get(index)));
    }

    @Override
    public void onImageSetup(int index) {
        startActivity(new Intent(DeviceActivity.this, IMGUploadActivity.class)
                .putExtra("ID", devices.get(index).getId()));
    }

    private void init() {
        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);

        rcvDevice = findViewById(R.id.rcv_devices);
        // Set Layout Manager
        LinearLayoutManager itemLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemLayoutManager.setItemPrefetchEnabled(true);
        rcvDevice.setLayoutManager(itemLayoutManager);
        // Set Adapter
        rcvDevice.setAdapter(deviceAdapter = new DeviceAdapter());
        deviceAdapter.setOnSelectedListener(this);

        tvNoItem = findViewById(R.id.tv_no_device);

        dbHelper = new DBHelper(getApplicationContext());
        mqttClient = MQTTClient.getInstance();
    }

}
