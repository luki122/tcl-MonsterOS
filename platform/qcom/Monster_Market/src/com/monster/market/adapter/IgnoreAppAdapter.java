package com.monster.market.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.activity.AppIgnoreActivity;
import com.monster.market.bean.AppUpgradeInfo;
import com.monster.market.utils.FileUtil;
import com.monster.market.utils.SystemUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xiaobin on 16-8-24.
 */
public class IgnoreAppAdapter extends BaseAdapter {

    private AppIgnoreActivity appIgnoreActivity;
    private List<AppUpgradeInfo> appList;

    private LayoutInflater inflater;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    // 图片加载工具
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions optionsImage;

    private boolean loadImage = true;

    public IgnoreAppAdapter(AppIgnoreActivity appIgnoreActivity, List<AppUpgradeInfo> appList) {
        this.appIgnoreActivity = appIgnoreActivity;
        this.appList = appList;
        inflater = LayoutInflater.from(appIgnoreActivity);

        optionsImage = SystemUtil.buildAppListDisplayImageOptions(appIgnoreActivity);
    }

    @Override
    public int getCount() {
        return appList != null ? appList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        AppUpgradeInfo info = appList.get(i);
        Holder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_ignore, null);
            holder = new Holder();
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_version = (TextView) convertView.findViewById(R.id.tv_version);
            holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            holder.btn = (Button) convertView.findViewById(R.id.btn_recovery_update);

            holder.btn.setBackgroundResource(R.drawable.button_default_selector);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        changeViewData(holder, info);

        return convertView;
    }

    private void changeViewData(Holder holder, final AppUpgradeInfo info) {
        // 开始头像图片异步加载
        imageLoader.displayImage(info.getAppIcon(),
                holder.iv_icon, optionsImage, animateFirstListener);

        final ImageView view = holder.iv_icon;
        view.setDrawingCacheEnabled(true);

        holder.tv_name.setText(info.getAppName());
        holder.tv_size.setText(SystemUtil.bytes2kb(info.getAppSizeNew()));
        holder.tv_version.setText(String.valueOf(info.getVersionNameNew()));

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appIgnoreActivity.recoveryUpdate(info);
            }
        });
    }

    static class Holder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_version;
        TextView tv_size;
        Button btn;
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

}
