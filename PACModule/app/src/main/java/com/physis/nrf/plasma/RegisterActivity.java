package com.physis.nrf.plasma;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.physis.nrf.plasma.ble.BluetoothLEManager;
import com.physis.nrf.plasma.ble.data.BLEDevice;
import com.physis.nrf.plasma.custom.OnSingleClickListener;
import com.physis.nrf.plasma.data.CleanerData;
import com.physis.nrf.plasma.list.ScanAdapter;
import com.physis.nrf.plasma.utils.DBHelper;
import com.physis.nrf.plasma.utils.LoadingDialog;

import java.util.LinkedList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private RecyclerView rcvDevice;
    private EditText etName;
    private Button btnScan;
    private ImageView ivBtnBack;
    private TextView tvDeviceState;

    private BluetoothLEManager bleManager;
    private ScanAdapter scanAdapter;
    private DBHelper dbHelper;

    private List<CleanerData> savedDataList = new LinkedList<>();
    private List<BluetoothDevice> bleDevices = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
    }

    private OnSingleClickListener clickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if(v.getId() == R.id.btn_scan){
                bleManager.scan(true, true);
            }else if(v.getId() == R.id.iv_btn_back){
                finish();
            }
        }
    };


    private ScanAdapter.OnSelectedDeviceListener onSelectedDeviceListener = new ScanAdapter.OnSelectedDeviceListener() {
        @Override
        public void onSelectedAddress(String address) {
            registerDevice(address);
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLEManager.BLE_SCAN_START:
                    bleDevices.clear();
                    LoadingDialog.show(RegisterActivity.this, "주변 디바시스를 검색합니다.");
                    break;
                case BluetoothLEManager.BLE_SCAN_STOP:
                    scanAdapter.setItems(bleDevices);
                    if(bleDevices.size() == 0){
                        tvDeviceState.setVisibility(View.VISIBLE);
                        tvDeviceState.setText("검색된 디바이스가 없습니다.");
                    }else{
                        tvDeviceState.setVisibility(View.GONE);
                    }
                    LoadingDialog.dismiss();
                    break;
                case BluetoothLEManager.BLE_SCAN_DEVICE:
                    BLEDevice device = (BLEDevice) msg.obj;
                    setScanDevices(device.getDevice());
                    break;
            }
        }
    };

    private void registerDevice(String address){
        if (etName.length() == 0){
            Toast.makeText(getApplicationContext(), "디바이스 명칭을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceName = etName.getText().toString();

        boolean isExistName = false, isExistAddress = false;
        for(CleanerData data : savedDataList){
            if(data.getName().equals(deviceName)){
                isExistName = true;
                break;
            }

            if(data.getAddress().equals(address)){
                isExistAddress = true;
                break;
            }
        }

        if (isExistName){
            Toast.makeText(getApplicationContext(), "등록된 디바이스 명칭입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isExistAddress){
            Toast.makeText(getApplicationContext(), "이미 등록된 디바이스입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

         insertCleaner(deviceName, address);
    }

    private void insertCleaner(String name, String address){
        ContentValues params = new ContentValues();
        params.put(DBHelper.COL_NAME, name);
        params.put(DBHelper.COL_ADDRESS, address);
        boolean result = dbHelper.insertData(params);
        if(result){
            Toast.makeText(getApplicationContext(), "디바이스가 등록되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(getApplicationContext(), "디바이스 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setScanDevices(BluetoothDevice device){
        if(device.getName() == null)
            return;

        if(bleDevices.contains(device))
            return;

        bleDevices.add(device);
    }


    private void init() {
        AnimationDrawable animDrawable = (AnimationDrawable) findViewById(R.id.layout_frame).getBackground();
        animDrawable.setEnterFadeDuration(4500);
        animDrawable.setExitFadeDuration(4500);
        animDrawable.start();

        rcvDevice = findViewById(R.id.rcv_device);
        // Set Recycler Division Line
//        DividerItemDecoration decoration
//                = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
//        decoration.setDrawable(getResources().getDrawable(R.drawable.list_item_division_line,null));
//        rcvDevice.addItemDecoration(decoration);
        // Set Layout Manager
        LinearLayoutManager itemLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemLayoutManager.setItemPrefetchEnabled(true);
        rcvDevice.setLayoutManager(itemLayoutManager);
        // Set Adapter
        rcvDevice.setAdapter(scanAdapter = new ScanAdapter());
        scanAdapter.setOnSelectedDeviceListener(onSelectedDeviceListener);

        etName = findViewById(R.id.et_name);

        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(clickListener);

        ivBtnBack = findViewById(R.id.iv_btn_back);
        ivBtnBack.setOnClickListener(clickListener);

        tvDeviceState = findViewById(R.id.tv_list_state);
        tvDeviceState.setText("[찾기] 버튼을 통해 주변 디바이스를 검색하세요.");

        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        bleManager.setHandler(handler);

        dbHelper = new DBHelper(getApplicationContext());
        savedDataList = dbHelper.selectData();
    }
}
