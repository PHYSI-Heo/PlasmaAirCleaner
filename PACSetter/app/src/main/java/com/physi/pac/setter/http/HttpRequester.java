package com.physi.pac.setter.http;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;


public class HttpRequester extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "HttpRequester";
    private static final int connectTime = 4000;

    private String requestURL;
    private String requestParams = null;
    private String requestMethod;
    private String responseData = null;

    private OnResponseListener onResponseListener = null;

    public interface OnResponseListener{
        void onResponseListener(String url, String responseData);
    }

    public void setOnResponseListener(OnResponseListener listener){
        onResponseListener = listener;
    }

    public HttpRequester(String reqMethod, String url, JSONObject paramsObj){
        requestMethod = reqMethod;
        if(requestMethod.equals("POST")){
            requestURL = url;
            if(paramsObj != null)
                requestParams = paramsObj.toString();
        }else{
            requestURL = createGetUrl(url, paramsObj);
        }
    }


    private String createGetUrl(String reqURL, JSONObject paramsObj){
        StringBuilder paramsBuilder = new StringBuilder();
        paramsBuilder.append(reqURL).append("?");

        try {
            Iterator<?> keys = paramsObj.keys();
            while(keys.hasNext()) {
                String key = (String) keys.next();
                paramsBuilder.append(key).append("=").append(paramsObj.get(key)).append("&");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.e(TAG, "## Create URL : " + paramsBuilder.toString());
        return paramsBuilder.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            if(conn == null)
                return null;

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod(requestMethod);
            conn.setConnectTimeout(connectTime);
            conn.setDoInput(true);

            if(requestMethod.equals("POST") && requestParams != null){
                conn.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
                wr.write(requestParams);
                wr.flush();
            }

            Log.e(TAG, " # HTTP Request : " +  requestURL);

            int resCode = conn.getResponseCode();

            if(resCode != HttpURLConnection.HTTP_OK)
                return null;

            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] byteBuffer = new byte[1536];
            byte[] byteData;
            int nLength;
            while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                baos.write(byteBuffer, 0, nLength);
            }

            byteData = baos.toByteArray();
            responseData = new String(byteData);
            Log.e(TAG, " # HTTP Response : " +  responseData);

            baos.close();
            is.close();
            conn.disconnect();

        } catch(Exception e) {
            Log.e(TAG, " # HTTP Error : " +  e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(onResponseListener != null)
            onResponseListener.onResponseListener(requestURL, responseData);
    }
}
