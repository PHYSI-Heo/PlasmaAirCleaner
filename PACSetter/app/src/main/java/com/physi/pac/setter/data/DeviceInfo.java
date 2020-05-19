package com.physi.pac.setter.data;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceInfo implements Parcelable {

    private String id, name;

    public DeviceInfo(String id, String name){
        this.id = id;
        this.name = name;
    }

    protected DeviceInfo(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
    }
}
