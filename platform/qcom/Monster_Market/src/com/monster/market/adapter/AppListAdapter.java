package com.monster.market.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.bean.AppListInfo;
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
 * Created by xiaobin on 16-7-26.
 */
public class AppListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ProgressBtnUtil progressBtnUtil;

    private List<AppListInfo> list;
    private List<AppDownloadData> downloadDataList;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    // 图片加载工具
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions optionsImage;

    private boolean loadImage = true;

    private String downloadCountFormatStr = "";

    public AppListAdapter(Context context, List<AppListInfo> list, List<AppDownloadData> downloadDataList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.list = list;
        this.downloadDataList = downloadDataList;
        progressBtnUtil = new ProgressBtnUtil();
        optionsImage = SystemUtil.buildAppListDisplayImageOptions(context);

        downloadCountFormatStr = context.getString(R.string.item_download_count_str);
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
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
        AppListInfo info = list.get(i);
        Holder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_main_app, null);
            holder = new Holder();
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_version = (TextView) convertView.findViewById(R.id.tv_version);
            holder.rb_score = (RatingBar) convertView.findViewById(R.id.rb_score);
            holder.progressBtn = (ProgressBtn) convertView.findViewById(R.id.progressBtn);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        changeViewData(holder, info, downloadDataList.get(i), true);

        return convertView;
    }

    private static class Holder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_version;
        RatingBar rb_score;
        ProgressBtn progressBtn;
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

    private void changeViewData(Holder holder, AppListInfo info, final AppDownloadData data, boolean refreshImage) {
        if (refreshImage) {
            // 开始头像图片异步加载
            imageLoader.displayImage(info.getBigAppIcon(),
                    holder.iv_icon, optionsImage, animateFirstListener);


            final ImageView view = holder.iv_icon;
            view.setDrawingCacheEnabled(true);
//        holder.progressBtn.setOnButtonClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                LogUtil.i("AppListAdapter", "progressBtn onclick(), reportDownload apkId: " + data.getApkId());
//
//                RequestHelper.reportDownloadInfo(context,
//                        SystemUtil.buildReportDownloadInfoRequestData(data));
//            }
//        });
        }

        holder.tv_name.setText(Html.fromHtml(info.getAppName()));
        String downloadCountStr = String.format(downloadCountFormatStr, info.getDownloadCountStr());
        holder.tv_version.setText(downloadCountStr + "  " + SystemUtil.bytes2kb(info.getAppSize()));
        int level = info.getAppLevel();
        float star = level * 1.0f / 2;
        holder.rb_score.setRating(star);

        progressBtnUtil.updateProgressBtn(holder.progressBtn, data);
    }

    public void updateView(ListView listView, boolean refreshImage) {
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
            AppListInfo appInfo = list.get(position);

            View view = listView.getChildAt(i);
            Holder holder = (Holder) view.getTag();

            if (holder == null) {
                continue;
            }

            changeViewData(holder, appInfo, data, refreshImage);
        }
    }

    public void setLoadImage(boolean loadImage) {
        this.loadImage = loadImage;
    }

    public void setLoadImage(int newState) {
        //根据newState滚状态做处理
        if (imageLoader != null) {
            switch (newState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    imageLoader.resume();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    imageLoader.pause();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    imageLoader.pause();
                    break;
            }
        }
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
            Holder holder = (Holder) view.getTag();

            if (holder == null) {
                continue;
            }

            holder.progressBtn.setTag("0");
        }
    }

}
