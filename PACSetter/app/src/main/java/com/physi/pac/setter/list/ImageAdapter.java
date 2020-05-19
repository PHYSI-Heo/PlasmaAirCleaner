package com.physi.pac.setter.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.physi.pac.setter.R;
import com.physi.pac.setter.data.IMGInfo;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {

    public interface OnDeleteItemListener{
        void onDelete(int position);
    }

    private OnDeleteItemListener onDeleteItemListener;

    public void setOnDeleteItemListener(OnDeleteItemListener listener){
        onDeleteItemListener = listener;
    }

    private List<IMGInfo> filePaths = new LinkedList<>();
    private Context context;

    public ImageAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_image_item, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageHolder holder, final int position) {
        IMGInfo info = filePaths.get(position);

        if(info.getThumbnailPath() == null){
            holder.pbLoading.setVisibility(View.GONE);
            Glide.with(context)
                    .asBitmap()
                    .load(Uri.fromFile(new File(info.getFilePath())))
                    .into(holder.ivShowImg);
        }else{
            Glide.with(context)
                    .asBitmap()
                    .load(info.getThumbnailPath())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            holder.pbLoading.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.ivShowImg);
        }

        if(info.getFileName().endsWith(".jpg") || info.getFileName().endsWith(".png")){
            holder.ivFileType.setImageResource(R.drawable.ic_img_type);
        }else{
            holder.ivFileType.setImageResource(R.drawable.ic_video_type);
        }

        holder.ivBntDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDeleteItemListener != null)
                    onDeleteItemListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    public void setItems(List<IMGInfo> filePaths){
        this.filePaths = filePaths;
        notifyDataSetChanged();
    }
}
