package com.physis.nrf.plasma.list;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.nrf.plasma.R;

public class ScanHolder extends RecyclerView.ViewHolder {

    LinearLayout llScanDevice;
    TextView tvName, tvAddress;


    public ScanHolder(@NonNull View itemView) {
        super(itemView);

        llScanDevice = itemView.findViewById(R.id.ll_scan_device);
        tvName = itemView.findViewById(R.id.tv_device_name);
        tvAddress = itemView.findViewById(R.id.tv_device_address);
    }
}
