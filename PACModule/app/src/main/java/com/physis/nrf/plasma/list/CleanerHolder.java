package com.physis.nrf.plasma.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.nrf.plasma.R;

public class CleanerHolder extends RecyclerView.ViewHolder {

    TextView tvName;
    ImageView ivBtnControl, ivBtnDelete, ivBtnEditName;

    public CleanerHolder(@NonNull View itemView) {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_cleaner_name);
        ivBtnControl = itemView.findViewById(R.id.iv_btn_control);
        ivBtnDelete = itemView.findViewById(R.id.iv_btn_delete);
        ivBtnEditName = itemView.findViewById(R.id.iv_btn_edit_name);
    }
}
