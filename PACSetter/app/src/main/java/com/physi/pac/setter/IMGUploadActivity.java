package com.physi.pac.setter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.physi.pac.setter.data.IMGInfo;
import com.physi.pac.setter.http.HttpPacket;
import com.physi.pac.setter.http.HttpRequestActivity;
import com.physi.pac.setter.list.ImageAdapter;
import com.physi.pac.setter.mqtt.MQTTClient;
import com.physi.pac.setter.utils.FileUploader;
import com.physi.pac.setter.utils.FormatConverter;
import com.physi.pac.setter.utils.LoadingDialog;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class IMGUploadActivity extends HttpRequestActivity implements View.OnClickListener {

    private static final String TAG = IMGUploadActivity.class.getSimpleName();
    private static final int REQ_MEDIA_FILE_SELECTOR = 11;

    private Button btnImgSelector, btnFileUpload;
    private RecyclerView rcvImages;
    private TextView tvNoImg;

    private FormatConverter converter;
    private ImageAdapter imageAdapter;
    private FileUploader fileUploader;

    private List<IMGInfo> filePaths = new LinkedList<>();
    private List<String> savedFileNames = new LinkedList<>();
    private String deviceId;
    private boolean isImageLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_upload);

        init();
        isImageLoad = true;
        getSetupImageList();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_MEDIA_FILE_SELECTOR) {
                assert data != null;
                if(data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for(int i = 0; i < count; i++) {
                        Uri dataUri = data.getClipData().getItemAt(i).getUri();
                        appendFilePath(converter.uriToFilePath(getApplicationContext(), dataUri));
                    }
                }
                else if(data.getData() != null) {
                    appendFilePath(converter.uriToFilePath(getApplicationContext(), data.getData()));
                }
                showImageList();
            }
        }
    }

    @Override
    protected void onHttpResponse(String url, JSONObject resObj) {
        super.onHttpResponse(url, resObj);
        try {
            if(url.equals(HttpPacket.GET_IMGs_URL)){
                JSONArray rowsObj = resObj.getJSONArray(HttpPacket.PARAMS_ROW_DATA);
                setThumbnailPaths(rowsObj);
                if(isImageLoad)
                    showImageList();
            }else if(url.equals(HttpPacket.UPDATE_IMGs_URL)){
                LoadingDialog.dismiss();
                isImageLoad = false;
                getSetupImageList();
                MQTTClient.getInstance().publish(deviceId, "IMG");
                Toast.makeText(getApplicationContext(), "UPDATE SUCCESSFUL.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_img_selector:
                showMediaSelector();
                break;
            case R.id.btn_file_upload:
                startFileUpload();
                break;
        }
    }

    private ImageAdapter.OnDeleteItemListener onDeleteItemListener = new ImageAdapter.OnDeleteItemListener() {
        @Override
        public void onDelete(int position) {
            filePaths.remove(position);
            showImageList();
        }
    };

    private void startFileUpload(){
        if(filePaths.size() == 0)
            return;

        List<String> uploadPaths = new LinkedList<>();
        for(IMGInfo info : filePaths){
            if(info.getThumbnailPath() == null && !savedFileNames.contains(info.getFileName())){
                Log.e(TAG, "# Upload File Path : " + info.getFilePath());
                uploadPaths.add(info.getFilePath());
            }
        }

        LoadingDialog.show(IMGUploadActivity.this, "IMAGE UPLOADING...");

        if(uploadPaths.size() > 0){
            fileUploader.setUploadFile(deviceId, uploadPaths);
            fileUploader.sendToServer(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e(TAG, "# File Upload Failed.");
                    LoadingDialog.dismiss();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.e(TAG, "# File Upload Successful. (" + response.code() + ")");
                    updateImageFiles();
                }
            });
        }else{
            updateImageFiles();
        }
    }


    private void updateImageFiles(){
        JSONObject paramsObj = new JSONObject();
        JSONArray imgArray = new JSONArray();
        try {
            for(int i = 0; i < filePaths.size(); i++){
                String fileName = filePaths.get(i).getFileName();
                String fileType = fileName.substring(fileName.lastIndexOf("."));
                String thumbnailPath;
                if(fileType.equals(".jpg") || fileType.equals(".png")){
                    thumbnailPath = HttpPacket.THUMBNAIL_BASE + deviceId + "/"  + fileName;
                }else{
                    thumbnailPath = HttpPacket.THUMBNAIL_BASE + deviceId + "/"  +
                            fileName.substring(0, fileName.lastIndexOf(".")) + ".png";
                }
                Log.e(TAG, "# Thumbnail Url : " + thumbnailPath);
                JSONObject obj = new JSONObject();
                obj.put(HttpPacket.PARAMS_IMG_ORDER, i);
                obj.put(HttpPacket.PARAMS_IMG_FILE_PATH, thumbnailPath);
                obj.put(HttpPacket.PARAMS_IMG_FILE_NAME, fileName);
                imgArray.put(obj);
            }
            paramsObj.put(HttpPacket.PARAMS_DEVICE_ID, deviceId);
            paramsObj.put(HttpPacket.PARAMS_IMG_INFOs, imgArray);
            requestAPI(HttpPacket.UPDATE_IMGs_URL, paramsObj);
        } catch (JSONException e) {
            LoadingDialog.dismiss();
            e.printStackTrace();
        }
    }


    private void getSetupImageList(){
        JSONObject paramsObj = new JSONObject();
        try {
            paramsObj.put(HttpPacket.PARAMS_DEVICE_ID, deviceId);
            requestAPI(HttpPacket.GET_IMGs_URL, paramsObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setThumbnailPaths(JSONArray rowsObj){
        try {
            filePaths.clear();
            savedFileNames.clear();
            for(int i = 0; i < rowsObj.length(); i++){
                JSONObject obj = rowsObj.getJSONObject(i);
                IMGInfo imgInfo = new IMGInfo();
                imgInfo.setThumbnailPath(obj.getString(HttpPacket.PARAMS_IMG_FILE_PATH), obj.getString(HttpPacket.PARAMS_IMG_FILE_NAME));
                filePaths.add(imgInfo);
                savedFileNames.add(imgInfo.getFileName());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showImageList(){
        if(filePaths.size() == 0){
            rcvImages.setVisibility(View.GONE);
            tvNoImg.setVisibility(View.VISIBLE);
        }else{
            rcvImages.setVisibility(View.VISIBLE);
            tvNoImg.setVisibility(View.GONE);
        }
        imageAdapter.setItems(filePaths);
    }

    private void appendFilePath(String path){
        IMGInfo imgInfo = new IMGInfo();
        imgInfo.setFilePath(path);
        if(!filePaths.contains(imgInfo)){
            filePaths.add(imgInfo);
        }
    }

    private void showMediaSelector(){
        Intent intent = new Intent();
        intent.setType("image/* video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), REQ_MEDIA_FILE_SELECTOR);
    }

    private void init() {
//        ActionBar actionBar = getSupportActionBar();
//        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_back);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle("SETUP IMAGEs");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        btnImgSelector = findViewById(R.id.btn_img_selector);
        btnImgSelector.setOnClickListener(this);
        btnFileUpload = findViewById(R.id.btn_file_upload);
        btnFileUpload.setOnClickListener(this);

        rcvImages = findViewById(R.id.rcv_images);
        // Set Layout Manager
        LinearLayoutManager itemLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemLayoutManager.setItemPrefetchEnabled(true);
        rcvImages.setLayoutManager(itemLayoutManager);
        // Set Adapter
        rcvImages.setAdapter(imageAdapter = new ImageAdapter(getApplicationContext()));
        imageAdapter.setOnDeleteItemListener(onDeleteItemListener);

        tvNoImg = findViewById(R.id.tv_no_image);

        deviceId = getIntent().getStringExtra("ID");
        converter = new FormatConverter();

        fileUploader = new FileUploader();
        fileUploader.setServerUrl(HttpPacket.UPLOAD_IMGs_URL);
    }
}
