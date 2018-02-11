package com.monster.cloud.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.activity.app.AppRecoveryActivity;
import com.monster.cloud.bean.RecoveryAppItem;
import com.monster.cloud.utils.SystemUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mst.widget.recycleview.RecyclerView;

/**
 * Created by xiaobin on 16-10-24.
 */
public class AppRecoveryAdapter extends RecyclerView.Adapter<AppRecoveryAdapter.MyViewHolder> {

    private Context mContext;
    private List<RecoveryAppItem> itemList;
    private Set<Integer> selectSet;

    private OnItemClickListener onItemClickListener;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    // 图片加载工具
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions optionsImage;

    public AppRecoveryAdapter(Context context, List<RecoveryAppItem> itemList) {
        mContext = context;
        this.itemList = itemList;
        selectSet = new ArraySet<Integer>();

        optionsImage = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_app_default)
                .showImageForEmptyUri(R.drawable.icon_app_default)
                .showImageOnFail(R.drawable.icon_app_default)
                .cacheInMemory(true).cacheOnDisk(true).build();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        MyViewHolder holder = null;
        if (viewType == RecoveryAppItem.ITEM_VIEW_TYPE_HEADER) {
            holder = new MyViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.item_app_recovery_header, viewGroup,
                            false), RecoveryAppItem.ITEM_VIEW_TYPE_HEADER);
        } else {
            holder = new MyViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.item_app_recovery, viewGroup,
                            false), RecoveryAppItem.ITEM_VIEW_TYPE_ITEM);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        myViewHolder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    public boolean isHeader(int position) {
        if (itemList != null && itemList.get(position) != null) {
            return itemList.get(position).getType() == RecoveryAppItem.ITEM_VIEW_TYPE_HEADER;
        }
        return false;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_recovery_left;
        private TextView tv_recovery_right;

        private LinearLayout item;
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_size;
        private CheckBox checkbox;

        public MyViewHolder(View itemView, int type) {
            super(itemView);

            if (type == RecoveryAppItem.ITEM_VIEW_TYPE_ITEM) {
                iv_icon = (ImageView) itemView.findViewById(R.id.iv_icon);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_size = (TextView) itemView.findViewById(R.id.tv_size);
                checkbox = (CheckBox) itemView.findViewById(R.id.checkbox);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(getAdapterPosition());
                        }
                    }
                });
            } else if (type == RecoveryAppItem.ITEM_VIEW_TYPE_HEADER) {
                tv_recovery_left = (TextView) itemView.findViewById(R.id.tv_recovery_left);
                tv_recovery_right = (TextView) itemView.findViewById(R.id.tv_recovery_right);

                tv_recovery_right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (itemList.get(getAdapterPosition()).getHeaderType()
                                == RecoveryAppItem.ITEM_VIEW_HEADER_TYPE_RECOVERY) {
                            if (((AppRecoveryActivity) mContext).isRecoveryAppSelectedAll()) {
                                ((AppRecoveryActivity) mContext).selectAllRecoveryApp(false);
                            } else {
                                ((AppRecoveryActivity) mContext).selectAllRecoveryApp(true);
                            }
                        } else {
                            if (((AppRecoveryActivity) mContext).isRecommendAppSelectedAll()) {
                                ((AppRecoveryActivity) mContext).selectAllRecommendApp(false);
                            } else {
                                ((AppRecoveryActivity) mContext).selectAllRecommendApp(true);
                            }
                        }
                    }
                });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }

        public void bindData(int position) {
            RecoveryAppItem appItem = itemList.get(position);

            if (isHeader(position)) {
                if (appItem.getHeaderType() == RecoveryAppItem.ITEM_VIEW_HEADER_TYPE_RECOVERY) {
                    int count = ((AppRecoveryActivity) mContext).getRecoveryAppCount();
                    String text = mContext.getString(R.string.app_recovery_count, count);
                    tv_recovery_left.setText(text);

                    if (((AppRecoveryActivity) mContext).isRecoveryAppSelectedAll()) {
                        tv_recovery_right.setText(mContext.getString(R.string.reverse_select_all));
                    } else {
                        tv_recovery_right.setText(mContext.getString(R.string.select_all));
                    }
                } else if (appItem.getHeaderType() == RecoveryAppItem.ITEM_VIEW_HEADER_TYPE_RECOMMEND) {
                    tv_recovery_left.setText(mContext.getString(R.string.app_recommend_app));

                    if (((AppRecoveryActivity) mContext).isRecommendAppSelectedAll()) {
                        tv_recovery_right.setText(mContext.getString(R.string.reverse_select_all));
                    } else {
                        tv_recovery_right.setText(mContext.getString(R.string.select_all));
                    }
                }
            } else {

                imageLoader.displayImage(appItem.getAppInfo().getBigAppIcon(), iv_icon, animateFirstListener);
                tv_name.setText(appItem.getAppInfo().getAppName());
                tv_size.setText(SystemUtil.bytes2kb(appItem.getAppInfo().getAppSize()));

                if (selectSet.contains(position)) {
                    checkbox.setChecked(true);
                } else {
                    checkbox.setChecked(false);
                }
            }
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

    public Set<Integer> getSelectSet() {
        return selectSet;
    }
}
