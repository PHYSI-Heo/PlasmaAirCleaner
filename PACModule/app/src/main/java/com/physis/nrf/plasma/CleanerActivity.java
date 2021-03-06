package com.physis.nrf.plasma;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.physis.nrf.plasma.ble.BluetoothLEManager;
import com.physis.nrf.plasma.ble.GattAttributes;
import com.physis.nrf.plasma.custom.OnSingleClickListener;

public class CleanerActivity extends AppCompatActivity {

    private static final String TAG = CleanerActivity.class.getSimpleName();

    private static final float VOC_LIMIT_GOOD_STATE = 1;
    private static final float VOC_LIMIT_NORMAL_STATE = 6;
    private static final float VOC_LIMIT_BAD_STATE = 9;


    private static final int VOC_STATE_GOOD = 0;
    private static final int VOC_STATE_NORMAL = 1;
    private static final int VOC_STATE_BAD = 2;
    private static final int VOC_STATE_VERY_BAD = 3;

    private TextView tvVocValue, tvCleanerName, tvVocStateMsg, tvVocStateBar;
    private ImageView ivBtnBack;
    private Button btnAuto, btnFanPower;

    private BluetoothLEManager bleManager;

    private boolean autoState, fanState;
    private int vocState = -1;

    private int verticalPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaner);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.disconnectDevice();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothLEManager.BLE_DATA_AVAILABLE:
                    showFanState((String) msg.obj);
                    break;
                case BluetoothLEManager.BLE_DISCONNECT_DEVICE:
                    Toast.makeText(getApplicationContext(), "디바이스와 연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };



    private OnSingleClickListener clickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()){
                case R.id.iv_btn_back:
                    finish();
                    break;
                case R.id.btn_auto:
                    sendControlMessage("A", autoState ? "0" : "1");
                    break;
                case R.id.btn_fan_power:
                    controlFanState();
                    break;
            }
        }
    };


    private void controlFanState(){
        if(autoState){
            Toast.makeText(getApplicationContext(), "AUTO 모드에서는 수동 제어가 불가능합니다.\n먼저 AUTO 모드를 비활성화 해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        sendControlMessage("F", fanState ? "0" : "1");
    }


    private void sendControlMessage(String key, String data){
        bleManager.writeCharacteristic(GattAttributes.NRF5_SERVICE, GattAttributes.NRF5_RX, key + data);
    }

    private void showFanState(String data){
        Log.e(TAG, "> Receive Message : " + data);
        if(data.length() < 3)
            return;
        showAutoState(data.charAt(0) == '1');
        showFanState(data.charAt(1) == '1');
        String vocValue = data.substring(2);
        setVocState(vocValue);
    }

    @SuppressLint("SetTextI18n")
    private void setVocState(String value){
        float voc = Float.parseFloat(value);

        if(voc < 0)
            return;
        tvVocValue.setText(value);

        int state;
        if(voc < VOC_LIMIT_GOOD_STATE){
            state = VOC_STATE_GOOD;
        }else if(voc < VOC_LIMIT_NORMAL_STATE){
            state = VOC_STATE_NORMAL;
        }else if(voc < VOC_LIMIT_BAD_STATE){
            state = VOC_STATE_BAD;
        }else{
            state = VOC_STATE_VERY_BAD;
        }
        showVocEffect(state);
    }

    private void showVocEffect(int state){
        if(vocState == state)
            return;
        vocState = state;
        switch (vocState){
            case VOC_STATE_GOOD:
                tvVocStateMsg.setText("GOOD");
                tvVocStateMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFanState0));
                tvVocStateBar.setBackgroundResource(R.color.colorFanState0);
                break;
            case VOC_STATE_NORMAL:
                tvVocStateMsg.setText("NORMAL");
                tvVocStateMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFanState1));
                tvVocStateBar.setBackgroundResource(R.color.colorFanState1);
                break;
            case VOC_STATE_BAD:
                tvVocStateMsg.setText("BAD");
                tvVocStateMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFanState2));
                tvVocStateBar.setBackgroundResource(R.color.colorFanState2);
                break;
            case VOC_STATE_VERY_BAD:
                tvVocStateMsg.setText("VERY BAD");
                tvVocStateMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFanState3));
                tvVocStateBar.setBackgroundResource(R.color.colorFanState3);
                break;
        }
    }

    private void showAutoState(boolean state){
        if(state == autoState)
            return;
        autoState = state;
        if(autoState){
            btnAuto.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_auto_on, 0, 0);
            btnAuto.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorButtonEffect));
            btnAuto.setBackgroundResource(R.drawable.btn_circle_effect);
        }else{
            btnAuto.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_auto_off, 0, 0);
            btnAuto.setTextColor(Color.BLACK);
            btnAuto.setBackgroundResource(R.drawable.btn_circle_basic);
        }
        btnAuto.setPadding(0, verticalPadding, 0, verticalPadding);
    }

    private void showFanState(boolean state){
        if(state == fanState)
            return;

        fanState = state;

        if(fanState){
            btnFanPower.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_power_on, 0, 0);
            btnFanPower.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorButtonEffect));
            btnFanPower.setBackgroundResource(R.drawable.btn_circle_effect);
        }else{
            btnFanPower.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_power_off, 0, 0);
            btnFanPower.setTextColor(Color.BLACK);
            btnFanPower.setBackgroundResource(R.drawable.btn_circle_basic);
        }
        btnFanPower.setPadding(0, verticalPadding, 0, verticalPadding);
    }


    private void init() {
        AnimationDrawable animDrawable = (AnimationDrawable) findViewById(R.id.layout_frame).getBackground();
        animDrawable.setEnterFadeDuration(4500);
        animDrawable.setExitFadeDuration(4500);
        animDrawable.start();

        tvVocValue = findViewById(R.id.tv_voc_value);
        tvVocValue.setText("0.0");

        tvCleanerName = findViewById(R.id.tv_cleaner_name);
        tvCleanerName.setText(getIntent().getStringExtra("NAME"));

        tvVocStateMsg = findViewById(R.id.tv_state_msg);
        tvVocStateBar = findViewById(R.id.tv_state_bar);

        ivBtnBack = findViewById(R.id.iv_btn_back);
        ivBtnBack.setOnClickListener(clickListener);

        btnAuto = findViewById(R.id.btn_auto);
        btnAuto.setOnClickListener(clickListener);
        btnFanPower = findViewById(R.id.btn_fan_power);
        btnFanPower.setOnClickListener(clickListener);

        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        bleManager.setHandler(handler);

        verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getApplicationContext().getResources().getDisplayMetrics());
    }

}
