package cn.tcl.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.SystemUtility;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/24 16:26
 * @copyright TCL-MIE
 */
public class EmptyLayoutV2  extends LinearLayout implements
        View.OnClickListener {// , ISkinUIObserver {
    private static final String TAG = EmptyLayout.class.getSimpleName();
    public static final int HIDE_LAYOUT = 4;
    public static final int NETWORK_ERROR = 1;
    public static final int NETWORK_LOADING = 2;
    public static final int NODATA = 3;
    public static final int NODATA_ENABLE_CLICK = 5;
    public static final int NO_LOGIN = 6;
    public static final int NO_VALID_SONG = 7;


    private ProgressBar animProgress;
    private boolean clickEnable = true;
    private final Context context;
    public ImageView img;
    private OnClickListener listener;
    private int mErrorState;
    private LinearLayout mLayout;
    private String strNoDataContent = "";
    private TextView tv;

    public EmptyLayoutV2(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public EmptyLayoutV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        View view = View.inflate(context, R.layout.view_empty_layout, null);
        img = (ImageView) view.findViewById(R.id.img_error_layout);
        tv = (TextView) view.findViewById(R.id.tv_error_layout);
        mLayout = (LinearLayout) view.findViewById(R.id.pageerrLayout);
        animProgress = (ProgressBar) view.findViewById(R.id.animProgress);
        setOnClickListener(this);
        addView(view);
        changeErrorLayoutBgMode(context);
    }

    public void changeErrorLayoutBgMode(Context context1) {
    }

    public void dismiss() {
        mErrorState = HIDE_LAYOUT;
        setVisibility(View.GONE);
    }

    public int getErrorState() {
        return mErrorState;
    }

    public boolean isLoadError() {
        return mErrorState == NETWORK_ERROR;
    }

    public boolean isLoading() {
        return mErrorState == NETWORK_LOADING;
    }

    @Override
    public void onClick(View v) {
        if (clickEnable) {
            if (listener != null)
                listener.onClick(v);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onSkinChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onSkinChanged() {
    }

    public void setDayNight(boolean flag) {}

    public void setErrorMessage(String msg) {
        tv.setText(msg);
    }

    /**
     * 新添设置背景
     *
     *
     */
    public void setErrorImag(int imgResource) {
        try {
            img.setImageResource(imgResource);
        } catch (Exception e) {
        }
    }

    public void setErrorType(int i) {
        setVisibility(View.VISIBLE);
        LogUtil.d(TAG, "setErrorType = " + i);
        switch (i) {
            case NETWORK_ERROR:
                mErrorState = NETWORK_ERROR;
                if (SystemUtility.getNetworkType() != SystemUtility.NetWorkType.none) {
                    tv.setText(R.string.network_error_prompt2);
                    img.setBackgroundResource(R.drawable.ic_no_data_grey); // MODIFIED by beibei.yang, 2016-07-01,BUG-2390311
                } else {
                    tv.setText(R.string.network_error_prompt2);
                    img.setBackgroundResource(R.drawable.ic_network_error_black); // MODIFIED by binbin.chang, 2016-06-17,BUG-2354550
                }
                img.setVisibility(View.VISIBLE);
                animProgress.setVisibility(View.GONE);
                clickEnable = true;
                break;
            case NETWORK_LOADING:
                mErrorState = NETWORK_LOADING;
                animProgress.setVisibility(View.VISIBLE);
                img.setVisibility(View.GONE);
                tv.setText(R.string.loading2);
                clickEnable = false;
                break;
            case NODATA:
                mErrorState = NODATA;
                img.setBackgroundResource(R.drawable.ic_no_data);
                img.setVisibility(View.VISIBLE);
                animProgress.setVisibility(View.GONE);
                setTvNoDataContent();
                clickEnable = true;
                break;
            case HIDE_LAYOUT:
                setVisibility(View.GONE);
                break;
            case NODATA_ENABLE_CLICK:
                mErrorState = NODATA_ENABLE_CLICK;
                img.setBackgroundResource(R.drawable.ic_no_data);
                img.setVisibility(View.VISIBLE);
                animProgress.setVisibility(View.GONE);
                setTvNoDataContent();
                clickEnable = true;
                break;
            case NO_VALID_SONG:
                mErrorState = NO_VALID_SONG;
                img.setVisibility(View.GONE);
                animProgress.setVisibility(View.GONE);
                setTvNoDataContent(getContext().getString(R.string.error_no_valid_song));
                break;
            default:
                break;
        }
    }

    public void setNoDataContent(String noDataContent) {
        strNoDataContent = noDataContent;
    }

    public void setOnLayoutClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void setTvNoDataContent() {
        if (!strNoDataContent.equals("")){
            tv.setText(strNoDataContent);
        }
        else{
            tv.setText(R.string.error_view_no_data);
        }
    }

    public void setTvNoDataContent(String tips) {
        if (!tips.equals("")){
            tv.setText(tips);
        }
        else{
            tv.setText(R.string.error_view_no_data);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.GONE){
            mErrorState = HIDE_LAYOUT;
        }
        super.setVisibility(visibility);
    }

}
