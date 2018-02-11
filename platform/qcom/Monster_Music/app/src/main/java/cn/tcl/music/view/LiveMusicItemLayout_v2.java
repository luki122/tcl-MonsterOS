package cn.tcl.music.view;

import android.app.Activity;
import android.content.Context;
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

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.Util;
import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.List;

/**
 * Created by jiangyuanxi on 3/8/16.
 */
public class LiveMusicItemLayout_v2 extends  LinearLayout{
    private static String TAG="LiveMusicItemLayout_v2";
    private LiveMusicClickListener mImgClickListener;
    private int mViewWidth;
    private int mViewHeight;
    private Context mContext;

    public LiveMusicItemLayout_v2(Context context, AttributeSet attrs) {
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
        //mViewWidth = getResources().getDisplayMetrics().widthPixels - Util.dip2px(getContext(), 8);
        mViewWidth = Util.dip2px(getContext(), 102);
        mViewHeight = Util.dip2px(getContext(),75);
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

        //图片
        Context context = activity.getApplicationContext();
        if(context!=null){
            Glide.with(context)
                    .load(ImageUtil.transferImgUrl(data.imgUrl, 300)).asBitmap().centerCrop()
                    .placeholder(R.drawable.default_cover_home)
                    .into((ImageView) item.getChildAt(0));
        }

        //描述
        ((TextView)item.getChildAt(3)).setText(data.name);

        //艺人名称
        if(!TextUtils.isEmpty(data.artist)){
            TextView artistTv = (TextView) item.getChildAt(4);
            artistTv.setVisibility(View.VISIBLE);
            artistTv.setText(data.artist);
            /*[BUGFIX]-Add by yanjia.li, 2016-05-30,BUG-2212026 begin*/
            artistTv.setSingleLine(true);
            artistTv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            artistTv.setMarqueeRepeatLimit(-1);
            artistTv.setFocusable(true);
            artistTv.setSelected(true);
            /*[BUGFIX]-Add by yanjia.li, 2016-05-30,BUG-2212026 end*/
        }
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
            params.topMargin = Util.dip2px(getContext(), 4);
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
        ImageView image_mask =new ImageView(getContext());
        ImageView shadeImge = new ImageView(getContext());
        TextView descripTv = new TextView(getContext());
        TextView artist_name = new TextView(getContext());

        int pad =  Util.dip2px(getContext(), 22);
        final int gap = (getResources().getDisplayMetrics().widthPixels - pad - mViewWidth*3)/2;
        final int itemHeight = Util.dip2px(getContext(), 107);
        LayoutParams layoutParams = new LayoutParams(mViewWidth, itemHeight);
        layoutParams.rightMargin = gap;
        itemLay.setLayoutParams(layoutParams);

        //显示图片
        final int padding = Util.dip2px(getContext(), 16);
        //final int margin_left = padding * 2;
        final int height = Util.dip2px(getContext(), 30);
        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(mViewWidth, mViewHeight);

        itemParams.gravity = Gravity.TOP;
        itemImg.setLayoutParams(itemParams);
        itemImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        itemImg.setImageResource(R.drawable.default_cover_home);

        //阴影
        itemParams = new FrameLayout.LayoutParams(mViewWidth, 8);
        itemParams.gravity = Gravity.TOP;
        itemParams.topMargin =  Util.dip2px(getContext(), 75);
        shadeImge.setLayoutParams(itemParams);
        //shadeImge.setBackgroundColor(getContext().getResources().getColor(R.color.red));
        shadeImge.setBackgroundResource(R.drawable.shadow_new);


        //遮罩
        itemParams = new FrameLayout.LayoutParams(mViewWidth, height);
        itemParams.gravity = Gravity.BOTTOM;
        image_mask.setLayoutParams(itemParams);
        image_mask.setBackgroundColor(getContext().getResources().getColor(R.color.music_theme_color));

        //文字描述
        itemParams = new FrameLayout.LayoutParams(mViewWidth, height);
        itemParams.gravity = Gravity.BOTTOM;
        descripTv.setLayoutParams(itemParams);
        descripTv.setSingleLine(true);
        descripTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        descripTv.setTextColor(getContext().getResources().getColor(R.color.base_live_title_color));
        descripTv.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
        descripTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        descripTv.setPadding(0, Util.dip2px(getContext(), 4), 0, 0);

        itemLay.addView(itemImg);//0
        itemLay.addView(shadeImge);//1
        itemLay.addView(image_mask);//2
        itemLay.addView(descripTv);//3

        //艺人名字
        itemParams = new FrameLayout.LayoutParams(mViewWidth,height);
        itemParams.gravity = Gravity.BOTTOM;
        artist_name.setLayoutParams(itemParams);
        artist_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        artist_name.setTextColor(getContext().getResources().getColor(R.color.base_live_title_color));
        artist_name.setPadding(0, padding, 0, 0);
        itemLay.addView(artist_name);//4
        artist_name.setVisibility(View.GONE);


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
        public String artist="";
    }
}
