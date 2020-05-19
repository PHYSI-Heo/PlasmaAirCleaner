package com.physis.nrf.plasma.list;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.nrf.plasma.R;
import com.physis.nrf.plasma.custom.OnSingleClickListener;
import com.physis.nrf.plasma.data.CleanerData;

import java.util.LinkedList;
import java.util.List;

public class ScanAdapter extends RecyclerView.Adapter<ScanHolder> {

    public interface OnSelectedDeviceListener{
        void onSelectedAddress(String address);
    }

    public OnSelectedDeviceListener listener;

    public void setOnSelectedDeviceListener(OnSelectedDeviceListener listener){
        this.listener = listener;
    }

    private List<BluetoothDevice> devices = new LinkedList<>();

    @NonNull
    @Override
    public ScanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_device, parent, false);
        return new ScanHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanHolder holder, int position) {
        final BluetoothDevice device = devices.get(position);
        holder.tvName.setText(device.getName());
        holder.tvAddress.setText(device.getAddress());
        holder.llScanDevice.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(listener != null)
                    listener.onSelectedAddress(device.getAddress());
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setItems(List<BluetoothDevice> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }
}
