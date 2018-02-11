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

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.Util;

/**
 * Created by jiangyuanxi on 3/8/16.
 */
public class LiveMusicItemLayout_v6 extends  LinearLayout{
    private static String TAG=LiveMusicItemLayout_v6.class.getSimpleName();
    private LiveMusicClickListener mImgClickListener;
    private int mViewWidth;
    private int mViewHeight;
    private Context mContext;
    private int mRankRes[] = new int[]{R.drawable.music_orginal_icon, R.drawable.music_collect_icon, R.drawable.sina_rank_icon};

    public LiveMusicItemLayout_v6(Context context, AttributeSet attrs) {
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
    }

    /**
     * init
     */
    private void init(){
        setOrientation(LinearLayout.VERTICAL);
        //mViewWidth = Util.dip2px(getContext(), 106);
        //mViewHeight = Util.dip2px(getContext(),108);
        final int margin = Util.dip2px(getContext(), 22);
        final int padding = Util.dip2px(getContext(), 12);
        mViewWidth = getResources().getDisplayMetrics().widthPixels - margin;
        mViewHeight = Util.dip2px(getContext(),108);
    }

    public void setChildCount(int count){
        removeAllViews();
        final int childCount = count;
        int lineCount = childCount % 1 == 0 ? (childCount / 1) : (childCount / 1 + 1);

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
        int line = position / 1;
        int row = position - line * 1;
        if (null == getChildAt(line)) {
            return;
        }
        FrameLayout item = (FrameLayout) ((LinearLayout) getChildAt(line)).getChildAt(row);
        if(item == null){
            return;
        }

        Context context = activity.getApplicationContext();
        if (context != null) {
            Glide.with(context)
                    .load(ImageUtil.transferImgUrl(data.imgUrl, 300)).asBitmap().centerCrop()
                    .placeholder(R.drawable.default_cover_home)
                    .into((ImageView) item.getChildAt(0));
        }

        if (CommonConstants.LIVE_RANK_CATE_MUSIC_ORIGINAL.equals(data.type)) {
            ((ImageView) item.getChildAt(3)).setImageDrawable(getContext().getDrawable(mRankRes[0]));
        } else if (CommonConstants.LIVE_RANK_CATE_MUSIC_COLLECT.equals(data.type)) {
            ((ImageView) item.getChildAt(3)).setImageDrawable(getContext().getDrawable(mRankRes[1]));
        } else {
            ((ImageView) item.getChildAt(3)).setImageDrawable(getContext().getDrawable(mRankRes[2]));
        }

        ((TextView)item.getChildAt(1)).setText(data.title);

        MusicDetailView musicDetailView = ((MusicDetailView)item.getChildAt(2));
        for (int i = 0;i<musicDetailView.getChildCount();i++) {
            LinearLayout layout = (LinearLayout)((LinearLayout)musicDetailView.getChildAt(i)).getChildAt(0);
            ((TextView)(layout.getChildAt(0))).setText(getResources().getString(R.string.live_rank_songs_detail_name,(i+1)+" "+data.songsDetail.get(i).name));
            ((TextView)(layout.getChildAt(1))).setText(getResources().getString(R.string.live_rank_songs_detail_artist,data.songsDetail.get(i).artistName));
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
        int startPos = line * 1;
        LinearLayout layout = new LinearLayout(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        if(line > 0){
            params.topMargin = Util.dip2px(getContext(), 13);
        }

        layout.setOrientation(LinearLayout.VERTICAL);
        addView(layout, params);

        for(int i = startPos; i < startPos + 1 && i < size; i++){
            addItem(layout, i);
        }

    }

    /**
     * add item
     */
    private void addItem(LinearLayout parentLayout, int pos){
        FrameLayout itemLay = new FrameLayout(getContext());
        ImageView itemBoreImg = new ImageView(getContext());
        ImageView itemImg = new ImageView(getContext());
        TextView titleTv = new TextView(getContext());
        MusicDetailView musicDetailView = new MusicDetailView(getContext());

        LayoutParams layoutParams = new LayoutParams(mViewWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemLay.setLayoutParams(layoutParams);

        //Pic
        final int imgWidth = mViewHeight;
        FrameLayout.LayoutParams itemParams = new FrameLayout.LayoutParams(imgWidth, imgWidth);
        itemParams.gravity = Gravity.LEFT;
        itemBoreImg.setLayoutParams(itemParams);
        itemBoreImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        itemBoreImg.setImageResource(R.drawable.default_cover_home);

        //PicSongs
        itemParams = new FrameLayout.LayoutParams(imgWidth, imgWidth);
        itemParams.gravity = Gravity.LEFT;
        itemImg.setLayoutParams(itemParams);
        itemImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        itemImg.setImageResource(R.drawable.default_cover_home);

        //Title
        final int height = mViewHeight / 4;
        itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.leftMargin = imgWidth + Util.dip2px(getContext(), 24);
        itemParams.topMargin = Util.dip2px(getContext(), 10);
        itemParams.gravity = Gravity.TOP;
        titleTv.setLayoutParams(itemParams);
        titleTv.setSingleLine(true);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        titleTv.setTextColor(getContext().getResources().getColor(R.color.base_live_title_color));
        titleTv.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
        titleTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));

        itemParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.leftMargin = imgWidth + Util.dip2px(getContext(), 24);
        itemParams.topMargin = Util.dip2px(getContext(), 6);
        itemParams.bottomMargin = Util.dip2px(getContext(), 6);
        itemParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        musicDetailView.setLayoutParams(itemParams);
        musicDetailView.setChildCount(3);

        itemLay.addView(itemImg);//0
        itemLay.addView(titleTv);//1
        itemLay.addView(musicDetailView);//2
        itemLay.addView(itemBoreImg);//3

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
        public String title = "";
        public String type = "";
        public String imgUrl = "";
        public ArrayList<SongsDetail> songsDetail = new ArrayList<>();
    }

    public static class SongsDetail {
        public String name="";
        public String artistName = "";
        public String imgUrl = "";
    }
    private class MusicDetailView extends  LinearLayout {
        public MusicDetailView(Context context) {
            super(context);
            init();
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            setChildCount(3);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        private void init(){
            setOrientation(LinearLayout.VERTICAL);
        }

        public void setChildCount(int count){
            removeAllViews();
            final int childCount = count;
            int lineCount = childCount % 1 == 0 ? (childCount / 1) : (childCount / 1 + 1);

            for(int i = 0; i < lineCount; i++){
                addItemLay(i, childCount);
            }

            requestLayout();
        }

        private void addItemLay(int line, int childCount){
            int size = childCount;
            int startPos = line * 1;
            LinearLayout layout = new LinearLayout(getContext());
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            if(line > 0){
                params.topMargin = Util.dip2px(getContext(), 8);
            }

            layout.setOrientation(LinearLayout.HORIZONTAL);
            addView(layout, params);

            for(int i = startPos; i < startPos + 1 && i < size; i++){
                addItem(layout, i);
            }

        }

        private void addItem(LinearLayout parentLayout, int pos){
            LinearLayout itemLay = new LinearLayout(getContext());
            TextView songTv = new TextView(getContext());
            TextView singerTv = new TextView(getContext());

            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemLay.setLayoutParams(layoutParams);

            //songs
            LinearLayout.LayoutParams itemRightParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            songTv.setLayoutParams(itemRightParams);
            songTv.setSingleLine(true);
            songTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            songTv.setTextColor(getContext().getResources().getColor(R.color.base_live_title_color));
            singerTv.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
            songTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));

            //singerTv
            itemRightParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemRightParams.leftMargin = Util.dip2px(getContext(), 2);
            singerTv.setLayoutParams(itemRightParams);
            singerTv.setSingleLine(true);
            singerTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            singerTv.setTextColor(getContext().getResources().getColor(R.color.base_live_title_color));
            singerTv.setAlpha(CommonConstants.VIEW_LOCAL_NO_SELECTER_TITLE_ALPHA);
            singerTv.setEllipsize(TextUtils.TruncateAt.valueOf("END"));

            itemLay.addView(songTv);//0
            itemLay.addView(singerTv);//1

            parentLayout.addView(itemLay);
        }
    }
}
