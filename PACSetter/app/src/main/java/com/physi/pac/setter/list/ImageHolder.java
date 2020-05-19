package com.physi.pac.setter.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physi.pac.setter.R;

public class ImageHolder extends RecyclerView.ViewHolder {

    ImageView ivShowImg, ivFileType, ivBntDel;
    ProgressBar pbLoading;

    public ImageHolder(@NonNull View itemView) {
        super(itemView);

        ivShowImg = itemView.findViewById(R.id.iv_show_image);
        ivFileType = itemView.findViewById(R.id.iv_file_type);
        ivBntDel = itemView.findViewById(R.id.iv_btn_delete);

        pbLoading = itemView.findViewById(R.id.pb_loading);
    }
}
