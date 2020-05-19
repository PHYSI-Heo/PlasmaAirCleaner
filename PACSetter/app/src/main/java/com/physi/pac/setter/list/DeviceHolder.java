package com.physi.pac.setter.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physi.pac.setter.R;

public class DeviceHolder extends RecyclerView.ViewHolder {

    RelativeLayout rlDevice;
    ImageView ivBtnSetup, ivBtnImage;
    TextView tvName;

    public DeviceHolder(@NonNull View itemView) {
        super(itemView);

        rlDevice = itemView.findViewById(R.id.rl_device_item);
        ivBtnImage = itemView.findViewById(R.id.iv_btn_image);
        ivBtnSetup = itemView.findViewById(R.id.iv_btn_setup);
        tvName = itemView.findViewById(R.id.tv_name);
    }
}
