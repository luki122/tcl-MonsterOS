package cn.tcl.music.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.Util;

public class LiveMusicItemRadioLayout extends LinearLayout {
    private static String TAG = LiveMusicItemRadioLayout.class.getSimpleName();
    private LiveMusicClickListener mImgClickListener;
    private int mViewWidth;
    private Context mContext;
    public int mMargin = 20; //
    public int mPadding = 11;
    public int mChildcountPerline = 2;
    public int mViewHeight = -1;
    public int mTopMargin = 12;

    public LiveMusicItemRadioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewHeight = 0;
        init();
        mContext = context;
    }

    public void setSize(int childPerline, int margin, int padding, int viewHeight) {
        mChildcountPerline = childPerline;
        mMargin = margin;
        mPadding = padding;
        mViewHeight = viewHeight;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setChildCount(4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();

        if (width > 0) {
            mViewWidth = getMeasuredWidth();
        }
    }

    /**
     * init
     */
    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        final int margin = Util.dip2px(getContext(), mMargin * 2);
        final int padding = Util.dip2px(getContext(), mPadding * 2);
        //mViewWidth = (getResources().getDisplayMetrics().widthPixels - margin - padding)/mChildcountPerline;
        mViewWidth = Util.dip2px(getContext(), 154);
        if (mViewHeight == -1) {
            mViewHeight = mViewWidth;
        } else if (mViewHeight == 0) {
            mViewHeight = Util.dip2px(getContext(), 66);
        }
    }

    public void setChildCount(int count) {
        removeAllViews();
        final int childCount = count;
        int lineCount = childCount % mChildcountPerline == 0 ? (childCount / mChildcountPerline) : (childCount / mChildcountPerline + 1);

        for (int i = 0; i < lineCount; i++) {
            addItemLay(i, childCount);
        }

        requestLayout();
    }

    /**
     * show data
     */
    public void showData(List<LiveItem> datas, Activity activity) {
        if (datas == null) {
            return;
        }

        int size = datas.size();

        for (int i = 0; i < size; i++) {
            bindChild(datas.get(i), i, activity);
        }
    }

    public void bindChild(LiveItem data, int position, Activity activity) {
        int line = position / mChildcountPerline;
        int row = position - line * mChildcountPerline;
        if (null == getChildAt(line)) {
            return;
        }
        FrameLayout item = (FrameLayout) ((LinearLayout) getChildAt(line)).getChildAt(row);
        if (item == null) {
            return;
        }

        Context context = activity.getApplicationContext();
        if (context != null) {
            /*Glide.with(context)
                    .load(ImageUtil.transferImgUrl(data.imgUrl, 720)).asBitmap().centerCrop()
                    .placeholder(R.drawable.default_cover_home)
                    .into((ImageView) item.getChildAt(0));*/
        }

        ((TextView) item.getChildAt(1)).setText(data.name);

    }

    /**
     * add img click listener
     */
    public void addImgClickListener(LiveMusicClickListener listener) {
        mImgClickListener = listener;
    }

    /**
     * add item lay
     */
    private void addItemLay(int line, int childCount) {
        int size = childCount;
        int startPos = line * mChildcountPerline;
        LinearLayout layout = new LinearLayout(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (line > 0) {
            params.topMargin = Util.dip2px(getContext(), mTopMargin);
        }

        layout.setOrientation(LinearLayout.HORIZONTAL);
        addView(layout, params);

        for (int i = startPos; i < startPos + mChildcountPerline && i < size; i++) {
            addItem(layout, i);
        }

    }

    /**
     * add item
     */
    private void addItem(LinearLayout parentLayout, int pos) {
        FrameLayout itemLay = new FrameLayout(getContext());
        ImageView itemImg = new ImageView(getContext());
        ImageView playIcon = new ImageView(getContext());
        TextView descripTv = new TextView(getContext());

        final int pad = Util.dip2px(getContext(), mPadding);
        LayoutParams layoutParams = new LayoutParams(mViewWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (pos % mChildcountPerline != 0) {
            layoutParams.leftMargin = pad;
        }
        itemLay.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(mViewWidth, mViewHeight);
        itemParams.gravity = Gravity.TOP;
        itemImg.setLayoutParams(itemParams);
        itemImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (pos == 0){
            itemImg.setImageResource(R.drawable.original);
        } else if(pos == 1){
            itemImg.setImageResource(R.drawable.scene);
        }else if (pos == 2){
            itemImg.setImageResource(R.drawable.style);
        }else if (pos == 3){
            itemImg.setImageResource(R.drawable.mood);
        }

        //PlayButton
        final int iconWidth = Util.dip2px(getContext(), 20);
        itemParams = new FrameLayout.LayoutParams(iconWidth, iconWidth);
        itemParams.gravity = Gravity.RIGHT;
        itemParams.rightMargin = Util.dip2px(getContext(), 8);
        itemParams.topMargin = mViewHeight - iconWidth - Util.dip2px(getContext(), 8);
        playIcon.setLayoutParams(itemParams);
        playIcon.setBackgroundResource(R.drawable.img_play_icon);

        itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        itemParams.bottomMargin = Util.dip2px(getContext(), 6);
        itemParams.leftMargin = Util.dip2px(getContext(), 8);
        descripTv.setLayoutParams(itemParams);
        descripTv.setSingleLine(true);
        descripTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        descripTv.setTextColor(getContext().getResources().getColor(R.color.white));
        descripTv.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
        descripTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));

        itemLay.addView(itemImg);//0
        itemLay.addView(descripTv);//1
        itemLay.addView(playIcon);//2


        final int position = pos;
        itemLay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImgClickListener != null) {
                    mImgClickListener.onImgClick(mContext, position);
                }
            }
        });

        parentLayout.addView(itemLay);
    }

    /**
     * img click listener
     */
    public interface LiveMusicClickListener {
        void onImgClick(Context context, int position);
    }

    public static class LiveItem {
        public String imgUrl = "";
        public String name = "";
    }
}
