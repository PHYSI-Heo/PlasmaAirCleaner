package com.physi.pac.setter.http;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.physi.pac.setter.utils.LoadingDialog;

import org.json.JSONObject;

public class HttpRequestActivity extends AppCompatActivity {

    protected void requestAPI(String url, JSONObject params){
        HttpRequester requester = new HttpRequester("POST", url, params);
        requester.setOnResponseListener(responseListener);
        requester.execute();
    }

    final HttpRequester.OnResponseListener responseListener = new HttpRequester.OnResponseListener() {
        @Override
        public void onResponseListener(String url, String responseData) {
            try{
                if(responseData == null){
                    Toast.makeText(getApplicationContext(), "서버와 통신이 불안정합니다.", Toast.LENGTH_SHORT).show();
                }else{
                    JSONObject resObj = new JSONObject(responseData);
                    if(resObj.getString(HttpPacket.PARAMS_RES_CODE).equals(REQ_SUCCESS)){
                        onHttpResponse(url, resObj);
                    }else {
                        showErrorToast(resObj.getString(HttpPacket.PARAMS_RES_CODE));
                        LoadingDialog.dismiss();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                LoadingDialog.dismiss();
            }
        }
    };

    protected void onHttpResponse(String url, JSONObject resObj){

    }

    private static final String REQ_SUCCESS  = "1001";
    private static final String ERR_DB_CONNECT = "1002";
    private static final String ERR_DB_QUERY = "1003";

    private void showErrorToast(String errCode){
        String errMsg;
        switch (errCode){
            case  ERR_DB_CONNECT:
                errMsg = "서버 DB와 연결에 실패하였습니다.\n잠시 후 다시 시도해주세요.";
                break;
            case  ERR_DB_QUERY:
                errMsg = "올바른 입력 정보가 아닙니다.";
                break;
            default:
                errMsg = "알수없는 오류가 발생하였습니다.";
        }
        Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
    }
}
