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

public class CleanerAdapter extends RecyclerView.Adapter<CleanerHolder> {

    public static final int MENU_TYPE_CONTROL = 22;
    public static final int MENU_TYPE_DELETE = 23;
    public static final int MENU_TYPE_EDIT_NAME = 24;

    public interface OnSelectedItemListener{
        void onSelected(int position, int menu);
    }

    private OnSelectedItemListener listener;

    public void setOnSelectedItemListener(OnSelectedItemListener listener){
        this.listener = listener;
    }

    private List<CleanerData> devices = new LinkedList<>();

    @NonNull
    @Override
    public CleanerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cleaner_device, parent, false);
        return new CleanerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CleanerHolder holder, final int position) {
        CleanerData device = devices.get(position);

        holder.tvName.setText(device.getName());
        holder.ivBtnControl.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(listener != null)
                    listener.onSelected(position, MENU_TYPE_CONTROL);
            }
        });

        holder.ivBtnDelete.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(listener != null)
                    listener.onSelected(position, MENU_TYPE_DELETE);
            }
        });

        holder.ivBtnEditName.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(listener != null)
                    listener.onSelected(position, MENU_TYPE_EDIT_NAME);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setItems(List<CleanerData> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }
}
