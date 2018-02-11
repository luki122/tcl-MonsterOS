package com.monster.market.adapter;

import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.activity.AppUpgradeActivity;
import com.monster.market.bean.AppUpgradeInfo;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.download.AppDownloadData;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.ReportDownloadInfoRequestData;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.ProgressBtnUtil;
import com.monster.market.utils.SystemUtil;
import com.monster.market.views.ProgressBtn;
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
public class UpgradeAppAdapter extends BaseAdapter {

    private AppUpgradeActivity appUpgradeActivity;

    private List<AppUpgradeInfo> appList;
    private List<AppDownloadData> downloadDataList;

    private LayoutInflater inflater;
    private ProgressBtnUtil progressBtnUtil;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    // 图片加载工具
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions optionsImage;

    private boolean loadImage = true;

    public UpgradeAppAdapter(AppUpgradeActivity appUpgradeActivity, List<AppUpgradeInfo> appList, List<AppDownloadData> downloadDataList) {
        this.appUpgradeActivity = appUpgradeActivity;
        this.appList = appList;
        this.downloadDataList = downloadDataList;
        inflater = LayoutInflater.from(appUpgradeActivity);

        progressBtnUtil = new ProgressBtnUtil();
        optionsImage = SystemUtil.buildAppListDisplayImageOptions(appUpgradeActivity);
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
            convertView = inflater.inflate(R.layout.item_upgrade, null);
            holder = new Holder();
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_version = (TextView) convertView.findViewById(R.id.tv_version);
            holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            holder.progressBtn = (ProgressBtn) convertView.findViewById(R.id.progressBtn);
            holder.tv_operation = (TextView) convertView.findViewById(R.id.tv_operation);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        changeViewData(holder, info, downloadDataList.get(i));

        return convertView;
    }

    private void changeViewData(Holder holder, final AppUpgradeInfo info, final AppDownloadData data) {
        // 开始头像图片异步加载
        imageLoader.displayImage(info.getAppIcon(),
                holder.iv_icon, optionsImage, animateFirstListener);

        final ImageView view = holder.iv_icon;
        view.setDrawingCacheEnabled(true);

        holder.tv_name.setText(Html.fromHtml(info.getAppName()));
        holder.tv_size.setText(SystemUtil.bytes2kb(info.getAppSizeNew()));
        holder.tv_version.setText(String.valueOf(info.getVersionNameNew()));

        progressBtnUtil.updateProgressBtn(holder.progressBtn, data);

        holder.tv_operation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpgradeActivity.ignoreApp(info, data);
            }
        });

    }

    static class Holder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_version;
        TextView tv_size;
        ProgressBtn progressBtn;
        TextView tv_operation;
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

    public void updateView(ListView listView) {
        if (listView == null) {
            return;
        }

        int headerCount = listView.getHeaderViewsCount();
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int offset = headerCount - firstVisiblePosition;
        boolean containerHeader = false;
        if (headerCount > 0) {
            if (firstVisiblePosition < headerCount) {
                containerHeader = true;
            }
        }
        int count = listView.getChildCount();

        for (int i = 0; i < count; i++) {
            int position = 0;
            if (containerHeader) {
                if (i < offset) {
                    continue;
                }
                position = i - offset;
            } else {
                position = i + firstVisiblePosition - headerCount;
            }

            if (position >= downloadDataList.size()) {
                continue;
            }
            AppDownloadData data = downloadDataList.get(position);
            AppUpgradeInfo appInfo = appList.get(position);

            View view = listView.getChildAt(i);
            Holder holder = (Holder)view.getTag();

            if (holder == null) {
                continue;
            }

            changeViewData(holder, appInfo, data);
        }
    }

    public void setLoadImage(boolean loadImage) {
        this.loadImage = loadImage;
    }

    public void clearProgressBtnTag(ListView listView) {
        if (listView == null) {
            return;
        }

        int headerCount = listView.getHeaderViewsCount();
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int offset = headerCount - firstVisiblePosition;
        boolean containerHeader = false;
        if (headerCount > 0) {
            if (firstVisiblePosition < headerCount) {
                containerHeader = true;
            }
        }
        int count = listView.getChildCount();

        for (int i = 0; i < count; i++) {
            int position = 0;
            if (containerHeader) {
                if (i < offset) {
                    continue;
                }
                position = i - offset;
            } else {
                position = i + firstVisiblePosition - headerCount;
            }

            if (position >= downloadDataList.size()) {
                continue;
            }

            AppDownloadData data = downloadDataList.get(position);
            View view = listView.getChildAt(i);
            Holder holder = (Holder)view.getTag();

            if (holder == null) {
                continue;
            }

            holder.progressBtn.setTag(0);
        }
    }

}
