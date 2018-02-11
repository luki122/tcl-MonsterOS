package cn.tcl.music.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import cn.tcl.music.util.Util;
import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.List;

/**
 * 在线音乐-音乐展示栏item
 */
public class LiveMusicItemLayout extends LinearLayout{
    private static String TAG="LiveMusicItemLayout";
    private LiveMusicClickListener mImgClickListener;
    private int mViewWidth;
    private int ITEM_WIDTH = Util.dip2px(getContext(), 150);//dp
    private int ITEM_HEIGHT = Util.dip2px(getContext(), 108);//dp
    private int ITEM_LAYER_HEIGHT = Util.dip2px(getContext(), 27);//dp

    private Context mContext;

    public LiveMusicItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setChildCount(2);
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
        mViewWidth = getResources().getDisplayMetrics().widthPixels - Util.dip2px(getContext(), 32);
    }

    public void setChildCount(int count){
        removeAllViews();
        final int childCount = count;
        int lineCount = childCount % 2 == 0 ? (childCount / 2) : (childCount / 2 + 1);

        for(int i = 0; i < lineCount; i++){
            addItemLay(i, childCount);
        }

        requestLayout();
    }

    /**
     * show data
     */
    public void showData(List<LiveItem> datas,Activity activity){
        if(datas == null){
            return;
        }

        int size = datas.size();

        for(int i = 0; i < size; i++){
            bindChild(datas.get(i), i,activity);
        }
    }

    public void bindChild(LiveItem data, int position,Activity activity){
        int line = position / 2;
        int row = position - line * 2;
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
        int startPos = line * 2;
        LinearLayout layout = new LinearLayout(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if(line > 0){
            params.topMargin = Util.dip2px(getContext(), 4);
        }

        layout.setOrientation(LinearLayout.HORIZONTAL);
        addView(layout, params);

        for(int i = startPos; i < startPos + 2 && i < size; i++){
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

        final int gap = Util.dip2px(getContext(), 4);
        LayoutParams layoutParams = new LayoutParams(ITEM_WIDTH, ITEM_HEIGHT);
        int layout_padding = (mViewWidth - ITEM_WIDTH * 2)/2;
        if(pos%2 == 0){
            layoutParams.rightMargin = layout_padding;
        }else {
            layoutParams.leftMargin = layout_padding;
        }
        itemLay.setLayoutParams(layoutParams);

        //显示图片
        final int padding = Util.dip2px(getContext(), 4);
        final int margin_left = padding * 2;
        final int height = padding * 6;
        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(ITEM_WIDTH, Util.dip2px(getContext(), 100));
        itemParams.gravity = Gravity.TOP;
        itemImg.setLayoutParams(itemParams);
        itemImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        itemImg.setImageResource(R.drawable.default_cover_home);

        //阴影
        itemParams = new FrameLayout.LayoutParams(ITEM_WIDTH, 8);
        itemParams.gravity = Gravity.TOP;
        itemParams.topMargin =  Util.dip2px(getContext(), 100);
        shadeImge.setLayoutParams(itemParams);
        //shadeImge.setBackgroundColor(getContext().getResources().getColor(R.color.red));
        shadeImge.setBackgroundResource(R.drawable.shadow_new);

        //遮罩
        itemParams = new FrameLayout.LayoutParams(ITEM_WIDTH, ITEM_LAYER_HEIGHT);
        itemParams.gravity = Gravity.BOTTOM;
        itemParams.bottomMargin = Util.dip2px(getContext(), 8);
        image_mask.setLayoutParams(itemParams);
        image_mask.setBackgroundColor(Color.argb(160, 0, 0, 0));

        //文字描述
        itemParams = new FrameLayout.LayoutParams(ITEM_WIDTH, LayoutParams.WRAP_CONTENT);
        itemParams.gravity = Gravity.BOTTOM;
        itemParams.bottomMargin = Util.dip2px(getContext(), 8);
        descripTv.setLayoutParams(itemParams);
        descripTv.setSingleLine(true);
        descripTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        descripTv.setTextColor(Color.argb(255, 255, 255, 255));
        descripTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        descripTv.setPadding(margin_left, padding, 0, 46);

        itemLay.addView(itemImg);//0
        itemLay.addView(shadeImge);//1
        itemLay.addView(image_mask);//2
        itemLay.addView(descripTv);//3

        //艺人名字
        itemParams = new FrameLayout.LayoutParams(ITEM_WIDTH,LayoutParams.WRAP_CONTENT);
        itemParams.gravity = Gravity.BOTTOM;
        itemParams.bottomMargin = Util.dip2px(getContext(), 8);
        artist_name.setLayoutParams(itemParams);
        artist_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        artist_name.setTextColor(Color.argb(204, 255, 255, 255));
        artist_name.setPadding(margin_left, padding * 5, 0, -1);
        itemLay.addView(artist_name);
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
