package com.physi.pac.setter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.physi.pac.setter.http.HttpPacket;
import com.physi.pac.setter.http.HttpRequestActivity;
import com.physi.pac.setter.utils.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class RegisterActivity extends HttpRequestActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private EditText etDeviceID, etDeviceName;
    private Button btnRegister;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_register){
            checkDeviceID();
        }
    }

    @Override
    protected void onHttpResponse(String url, JSONObject resObj) {
        super.onHttpResponse(url, resObj);
        if(url.equals(HttpPacket.EXIST_ID_URL))
            registerDevice(resObj);
    }

    private void checkDeviceID(){
        if(etDeviceID.getText().length() == 0 || etDeviceName.getText().length() == 0)
            return;
        JSONObject paramsObj = new JSONObject();
        try {
            paramsObj.put(HttpPacket.PARAMS_DEVICE_ID, etDeviceID.getText().toString());
            requestAPI(HttpPacket.EXIST_ID_URL, paramsObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void registerDevice(JSONObject resObj){
        try {
            if(resObj.getBoolean(HttpPacket.PARAMS_ID_EXIST)){
                ContentValues params = new ContentValues();
                params.put(DBHelper.COL_DEVICE_ID, etDeviceID.getText().toString());
                params.put(DBHelper.COL_NAME, etDeviceName.getText().toString());
                boolean result = dbHelper.insertData(params);
                Toast.makeText(getApplicationContext(), "REGISTER RESULT : " + result, Toast.LENGTH_SHORT).show();
                if(result)
                    finish();
            }else{
                Toast.makeText(getApplicationContext(), "등록되지 않은 시리얼번호 입니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void init() {      
        etDeviceID = findViewById(R.id.et_device_id);
        etDeviceName = findViewById(R.id.et_device_name);

        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);

        dbHelper = new DBHelper(getApplicationContext());
    }

}
