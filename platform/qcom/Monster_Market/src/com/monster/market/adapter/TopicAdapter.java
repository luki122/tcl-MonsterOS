package com.monster.market.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.bean.TopicInfo;
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
 * Created by xiaobin on 16-8-20.
 */
public class TopicAdapter extends BaseAdapter {

    private Context mContext;
    private List<TopicInfo> topicInfos;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    // 图片加载工具
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions optionsImage;

    public TopicAdapter(Context context, List<TopicInfo> topicInfos) {
        mContext = context;
        this.topicInfos = topicInfos;
        optionsImage = SystemUtil.buildTopicDisplayImageOptions(context);
    }

    @Override
    public int getCount() {
        return topicInfos == null ? 0 : topicInfos.size();
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = null;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_topic, null);

            holder = new Holder();
            holder.iv_img = (ImageView) view.findViewById(R.id.iv_img);
            holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            holder.tv_text = (TextView) view.findViewById(R.id.tv_text);
            holder.tv_count = (TextView) view.findViewById(R.id.tv_count);
            holder.bottom_detail = (RelativeLayout) view.findViewById(R.id.bottom_detail);

            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.bottom_detail.setVisibility(View.GONE);

        holder.tv_name.setText(topicInfos.get(i).getName());
        holder.tv_text.setText(topicInfos.get(i).getIntro());
        holder.tv_count.setText(String.format(mContext.getString(R.string.topic_app_count),
                topicInfos.get(i).getAppNum()));

        imageLoader.displayImage(topicInfos.get(i).getIcon(),
                holder.iv_img, optionsImage, animateFirstListener);

        return view;
    }

    private static class Holder {
        ImageView iv_img;
        TextView tv_name;
        TextView tv_text;
        TextView tv_count;
        RelativeLayout bottom_detail;
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
