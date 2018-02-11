package com.monster.market.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.bean.AppTypeInfo;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mst.widget.recycleview.RecyclerView;

/**
 * Created by xiaobin on 16-8-11.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private Context mContext;

    private List<AppTypeInfo> appTypeInfoList;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    // 图片加载工具
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions optionsImage;

    private OnItemClickListener onItemClickListener;

    public CategoryAdapter(Context context, List<AppTypeInfo> appTypeInfoList) {
        mContext = context;
        this.appTypeInfoList =  appTypeInfoList;
        optionsImage = SystemUtil.buildAppListDisplayImageOptions(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_category, viewGroup,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        if (appTypeInfoList != null) {
            myViewHolder.bindData(appTypeInfoList, position);
        }
    }

    @Override
    public int getItemCount() {
        return appTypeInfoList == null ? 0 : appTypeInfoList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout item;
        private ImageView iv_icon;
        private TextView tv_name;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }

        public void bindData(List<AppTypeInfo> appTypeInfoList,int position){
            tv_name.setText(appTypeInfoList.get(position).getTypeName());
            // 开始头像图片异步加载
            imageLoader.displayImage(appTypeInfoList.get(position).getTypeIcon(),
                    iv_icon, optionsImage, animateFirstListener);
        }

    }

    private static class AnimateFirstDisplayListener extends
            SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(int position);
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
