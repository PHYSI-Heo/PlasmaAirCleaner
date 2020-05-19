package com.physi.pac.setter.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physi.pac.setter.R;
import com.physi.pac.setter.data.DeviceInfo;

import java.util.LinkedList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder> {

    public interface OnSelectedListener{
        void onDataSetup(int index);
        void onImageSetup(int index);
    }

    private OnSelectedListener onSelectedListener;

    public void setOnSelectedListener(OnSelectedListener onSelectedListener){
        this.onSelectedListener = onSelectedListener;
    }

    private List<DeviceInfo> deviceInfos = new LinkedList<>();

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_device_item, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, final int position) {
        holder.rlDevice.setClipToOutline(true);
        DeviceInfo info = deviceInfos.get(position);
        holder.tvName.setText(info.getName());

        holder.ivBtnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSelectedListener != null)
                    onSelectedListener.onDataSetup(position);
            }
        });

        holder.ivBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSelectedListener != null)
                    onSelectedListener.onImageSetup(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceInfos.size();
    }

    public void setItems(List<DeviceInfo> infos){
        deviceInfos = infos;
        notifyDataSetChanged();
    }
}
