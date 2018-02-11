package cn.tcl.music.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
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

/**
 * Created by jiangyuanxi on 3/8/16.
 */
public class LiveMusicItemLayout_v4 extends  LinearLayout{
    private static String TAG = LiveMusicItemLayout_v4.class.getSimpleName();
    private LiveMusicClickListener mImgClickListener;
    private int mViewWidth;
    private int mViewHeight;
    private Context mContext;

    public LiveMusicItemLayout_v4(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setChildCount(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();

        if(width > 0){
            mViewWidth = getMeasuredWidth();
        }
    }

    /**
     * init
     */
    private void init(){
        setOrientation(LinearLayout.VERTICAL);
        final int margin = Util.dip2px(getContext(), 22);
        final int padding = Util.dip2px(getContext(), 12);
        mViewWidth = (getResources().getDisplayMetrics().widthPixels - margin - padding)/3;
        mViewHeight = mViewWidth;
    }

    public void setChildCount(int count){
        removeAllViews();
        final int childCount = count;
        int lineCount = childCount % 3 == 0 ? (childCount / 3) : (childCount / 3 + 1);

        for(int i = 0; i < lineCount; i++){
            addItemLay(i, childCount);
        }

        requestLayout();
    }

    /**
     * show data
     */
    public void showData(List<LiveItem> datas, Activity activity){
        if(datas == null){
            return;
        }

        int size = datas.size();

        for(int i = 0; i < size; i++){
            bindChild(datas.get(i), i,activity);
        }
    }

    public void bindChild(LiveItem data, int position, Activity activity){
        int line = position / 3;
        int row = position - line * 3;
        if (null == getChildAt(line)) {
            return;
        }
        FrameLayout item = (FrameLayout) ((LinearLayout) getChildAt(line)).getChildAt(row);
        if(item == null){
            return;
        }

        Context context = activity.getApplicationContext();
        if(context!=null){
            Glide.with(context)
                    .load(ImageUtil.transferImgUrl(data.imgUrl, 300)).asBitmap().centerCrop()
                    .placeholder(R.drawable.default_cover_home)
                    .into((ImageView) item.getChildAt(0));
        }

        ((TextView)item.getChildAt(1)).setText(data.name);

        ((TextView)item.getChildAt(3)).setText(data.count);
    }

    /**
     * add img click listener
     */
    public void addImgClickListener(LiveMusicClickListener listener){
        mImgClickListener = listener;
    }

    /**
     * add item lay
     */
    private void addItemLay(int line, int childCount){
        int size = childCount;
        int startPos = line * 3;
        LinearLayout layout = new LinearLayout(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if(line > 0){
            params.topMargin = Util.dip2px(getContext(), 16);
        }

        layout.setOrientation(LinearLayout.HORIZONTAL);
        addView(layout, params);

        for(int i = startPos; i < startPos + 3 && i < size; i++){
            addItem(layout, i);
        }

    }

    /**
     * add item
     */
    private void addItem(LinearLayout parentLayout, int pos){
        FrameLayout itemLay = new FrameLayout(getContext());
        ImageView itemImg = new ImageView(getContext());
        ImageView image_mask_top =new ImageView(getContext());
        TextView numberTv = new TextView(getContext());
        TextView descripTv = new TextView(getContext());

        final int pad =  Util.dip2px(getContext(), 6);
        LayoutParams layoutParams = new LayoutParams(mViewWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (pos % 3 != 0) {
            layoutParams.leftMargin = pad;
        }
        itemLay.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(mViewWidth, mViewHeight);

        itemParams.gravity = Gravity.TOP;
        itemImg.setLayoutParams(itemParams);
        itemImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        itemImg.setImageResource(R.drawable.default_cover_home);

        final int topHeight =  Util.dip2px(getContext(), 20);
        itemParams = new FrameLayout.LayoutParams(mViewWidth, topHeight);
        itemParams.gravity = Gravity.TOP;
        image_mask_top.setLayoutParams(itemParams);
        image_mask_top.setBackgroundColor(Color.argb(50, 0, 0, 0));

        itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.gravity = Gravity.TOP |Gravity.RIGHT;
        itemParams.rightMargin = Util.dip2px(getContext(), 3);
        itemParams.topMargin = Util.dip2px(getContext(), 2);
        itemParams.bottomMargin = Util.dip2px(getContext(), 2);
        final Drawable drawable = getContext().getDrawable(R.drawable.hot_songs_header);
        final int imgwidth =  Util.dip2px(getContext(), 7);
        drawable.setBounds(0, 0,imgwidth , imgwidth);
        numberTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        numberTv.setTextColor(getContext().getResources().getColor(R.color.base_play_count_color));
        numberTv.setLayoutParams(itemParams);
        numberTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        numberTv.setCompoundDrawables(drawable,null,null,null);
        numberTv.setCompoundDrawablePadding(Util.dip2px(getContext(), 7));

        itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        itemParams.topMargin = mViewHeight + Util.dip2px(getContext(), 8);
        descripTv.setLayoutParams(itemParams);
        descripTv.setSingleLine(true);
        descripTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        descripTv.setTextColor(getContext().getResources().getColor(R.color.base_live_title_color));
        descripTv.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
        descripTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));

        itemLay.addView(itemImg);//0
        itemLay.addView(descripTv);//1
        itemLay.addView(image_mask_top);//2
        itemLay.addView(numberTv);//3


        //点击进入列表
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
    public interface LiveMusicClickListener{
        void onImgClick(Context context, int position);
    }

    public static class LiveItem{
        public String imgUrl = "";
        public String name = "";
        public String id = "";
        public String count="";
    }
}
