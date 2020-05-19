package com.physi.pac.setter.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.physi.pac.setter.data.DeviceInfo;

import java.util.LinkedList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = DBHelper.class.getName();

    public static final String DEVICE_TABLE= "PACDevice";

    private static final String DATABASE = "PAC_Device.db";
    private static final int VERSION = 1;

    public static final String COL_DEVICE_ID = "did";
    public static final String COL_NAME = "name";

    public DBHelper(Context context){
        super(context, DATABASE, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DEVICE_TABLE + " (" +
                COL_DEVICE_ID + " TEXT UNIQUE NOT NULL, " +
                COL_NAME + " TEXT NOT NULL )";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DEVICE_TABLE);
        onCreate(db);
    }

    public boolean insertData(ContentValues values){
        return getWritableDatabase().insert(DEVICE_TABLE, null, values) > 0;
    }

    public boolean updateData(ContentValues values, String targetColumn, String targetValue){
        return getWritableDatabase().update(DEVICE_TABLE, values, targetColumn + " = '" + targetValue + "'",null) > 0;
    }

    public boolean deleteData(String targetColumn, String targetValue){
        return getWritableDatabase().delete(DEVICE_TABLE, targetColumn + " = '" + targetValue + "'",null) > 0;
    }

    public List<DeviceInfo> getDeviceList(){
        List<DeviceInfo> devices = new LinkedList<>();
        @SuppressLint("Recycle")
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + DEVICE_TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                devices.add(new DeviceInfo(
                        cursor.getString(cursor.getColumnIndex(COL_DEVICE_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_NAME))
                ));
            } while (cursor.moveToNext());
        }
        return devices;
    }
}