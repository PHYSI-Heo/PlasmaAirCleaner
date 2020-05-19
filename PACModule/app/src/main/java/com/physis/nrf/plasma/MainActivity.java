package com.physis.nrf.plasma;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.nrf.plasma.ble.BluetoothLEManager;
import com.physis.nrf.plasma.ble.GattAttributes;
import com.physis.nrf.plasma.ble.data.BLEDevice;
import com.physis.nrf.plasma.custom.OnSingleClickListener;
import com.physis.nrf.plasma.data.CleanerData;
import com.physis.nrf.plasma.list.CleanerAdapter;
import com.physis.nrf.plasma.utils.DBHelper;
import com.physis.nrf.plasma.utils.LoadingDialog;
import com.physis.nrf.plasma.utils.NotifyDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView rcvDevice;
    private Button btnRegister;
    private TextView tvDeviceState;

    private CleanerAdapter cleanerAdapter;
    private DBHelper dbHelper;
    private BluetoothLEManager bleManager;

    private List<CleanerData> cleanerDataList = new LinkedList<>();
//    private String connectAddress, connectDeviceName;
    private CleanerData selectedDevice;
    private BluetoothDevice connectDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        bleManager.bindService();
        bleManager.registerReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bleManager.setHandler(handler);
        setCleanerItem();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.disconnectDevice();
        bleManager.unregisterReceiver();
        bleManager.unBindService();
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLEManager.BLE_SCAN_START:
                    connectDevice = null;
                    LoadingDialog.show(MainActivity.this, "디바이스와 연결합니다.");
                    break;
                case BluetoothLEManager.BLE_SCAN_STOP:
                    if(connectDevice != null){
                        bleManager.connectDevice(connectDevice);
                    }else{
                        Toast.makeText(getApplicationContext(), "The device does not exist.", Toast.LENGTH_SHORT).show();
                        LoadingDialog.dismiss();
                    }
                    break;
                case BluetoothLEManager.BLE_SCAN_DEVICE:
                    BLEDevice device = (BLEDevice) msg.obj;
                    if(device.getDevice().getAddress().equals(selectedDevice.getAddress())){
                        connectDevice = device.getDevice();
                        bleManager.scan(false, false);
                    }
                    break;
                case BluetoothLEManager.BLE_CONNECT_DEVICE:
//                    Toast.makeText(getApplicationContext(), "Connected device.", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothLEManager.BLE_SERVICES_DISCOVERED:
                    if(bleManager.notifyCharacteristic(GattAttributes.NRF5_SERVICE, GattAttributes.NRF5_TX)){
                        startActivity(new Intent(MainActivity.this, CleanerActivity.class)
                                .putExtra("NAME", selectedDevice.getName()));
                    }else{
                        Toast.makeText(getApplicationContext(), "Service discovery error.", Toast.LENGTH_SHORT).show();
                        bleManager.disconnectDevice();
                    }
                    LoadingDialog.dismiss();
                    break;
                case BluetoothLEManager.BLE_DISCONNECT_DEVICE:
                    break;
            }
        }
    };

    private OnSingleClickListener clickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if(v.getId() == R.id.btn_register){
                startRegisterActivity();
            }
        }
    };


    private CleanerAdapter.OnSelectedItemListener onSelectedItemListener = new CleanerAdapter.OnSelectedItemListener() {
        @Override
        public void onSelected(int position, int menu) {
            selectedDevice = cleanerDataList.get(position);
            if(CleanerAdapter.MENU_TYPE_CONTROL == menu){
                connectCleaner();
            }else if(CleanerAdapter.MENU_TYPE_EDIT_NAME == menu){
                showEditNameDialog();
            }else{
                showDeleteDialog();
            }
        }
    };


    private void showEditNameDialog(){
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
        final EditText etName = view.findViewById(R.id.et_name);
        etName.setText(selectedDevice.getName());
        new NotifyDialog().show(MainActivity.this, null, view,
                "변경", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editDeviceName(etName.getText().toString());
                    }
                });
    }

    private void showDeleteDialog(){
        new NotifyDialog().show(MainActivity.this, null,
                "[ " + selectedDevice.getName() + " ]을 삭제하시겠습니까?",
                "삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDevice();
                    }
                });
    }

    private void editDeviceName(String name){
        if(name == null || name.length() == 0){
            Toast.makeText(getApplicationContext(), "디바이스 명칭을 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            ContentValues values = new ContentValues();
            values.put(DBHelper.COL_NAME, name);
            boolean result = dbHelper.updateData(values, DBHelper.COL_ADDRESS, selectedDevice.getAddress());
            if(result){
                Toast.makeText(getApplicationContext(), "디바이스 명칭이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                onStart();
            }else{
                Toast.makeText(getApplicationContext(), "디바이스 명칭 변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteDevice(){
        boolean result = dbHelper.deleteData(DBHelper.COL_ADDRESS, selectedDevice.getAddress());
        if(result){
            Toast.makeText(getApplicationContext(), "디바이스를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
            onStart();
        }else{
            Toast.makeText(getApplicationContext(), "디바이스 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectCleaner(){
        bleManager.scan(true, true);
    }

    private void setCleanerItem(){
        cleanerDataList = dbHelper.selectData();
        cleanerAdapter.setItems(cleanerDataList);
        tvDeviceState.setVisibility(cleanerDataList.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private void startRegisterActivity(){
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
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
        rcvDevice.setAdapter(cleanerAdapter = new CleanerAdapter());
        cleanerAdapter.setOnSelectedItemListener(onSelectedItemListener);


        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(clickListener);

        tvDeviceState = findViewById(R.id.tv_list_state);

        dbHelper = new DBHelper(getApplicationContext());
        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
    }
}
