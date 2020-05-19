package com.physis.nrf.plasma.ble.data;

import android.bluetooth.BluetoothDevice;

public class BLEDevice {
    private BluetoothDevice device;
    private int rssi;

    public BLEDevice(BluetoothDevice device, int rssi){
        this.device = device;
        this.rssi = rssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRssi() {
        return rssi;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
