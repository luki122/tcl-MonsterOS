package cn.com.xy.sms.sdk.ui.menu;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.UiPartInterface;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.util.ParseManager;

public class PopMenus {
    private static final String TAG = "PopMenus";
    private JSONArray mJsonArray;
    public JSONArray getmJsonArray() {
        return mJsonArray;
    }

    public void setmJsonArray(JSONArray mJsonArray) {
        this.mJsonArray = mJsonArray;
    }

    public Activity getmContext() {
        return mContext;
    }

    public void setmContext(Activity mContext) {
        this.mContext = mContext;
    }

    public PopupWindow getmPopupWindow() {
        return mPopupWindow;
    }

    public void setmPopupWindow(PopupWindow mPopupWindow) {
        this.mPopupWindow = mPopupWindow;
    }

    public LinearLayout getmListView() {
        return mListView;
    }

    public void setmListView(LinearLayout mListView) {
        this.mListView = mListView;
    }

    public int getmWidth() {
        return mWidth;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public View getmContainerView() {
        return mContainerView;
    }

    public void setmContainerView(View mContainerView) {
        this.mContainerView = mContainerView;
    }

    public View getmParentView() {
        return mParentView;
    }

    public void setmParentView(View mParentView) {
        this.mParentView = mParentView;
    }

    public boolean ismIsShow() {
        return mIsShow;
    }

    public void setmIsShow(boolean mIsShow) {
        this.mIsShow = mIsShow;
    }

    private Activity mContext;
    private PopupWindow mPopupWindow;
    private LinearLayout mListView;
    private int mWidth, mHeight;
    private View mContainerView;
    private View mParentView = null;
    private boolean mIsShow = false;
    private UiPartInterface mUiInterface;

    @SuppressLint("ResourceAsColor")
    public PopMenus(Activity context, JSONArray _jsonArray, int _width, int _height) {
        this.mContext = context;
        this.mJsonArray = _jsonArray;
        this.mWidth = _width;
        this.mHeight = _height;
        mUiInterface = ViewManger.getUiPartInterface();
        if(mUiInterface != null){
            try{
                Boolean result = (Boolean) mUiInterface.doUiAction(ViewManger.UIPART_ACTION_SET_POP_MENU, this);
                if(result.equals(Boolean.TRUE)){
                    return;
                }
            }catch(Throwable e){
            }
        }
        
        mContainerView = LayoutInflater.from(context).inflate(R.layout.duoqu_popmenus, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT, 1.0f);
        mContainerView.setLayoutParams(lp);

        mListView = (LinearLayout) mContainerView.findViewById(R.id.layout_subcustommenu);
        try {
            setSubMenu();
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        mListView.setBackgroundColor(R.color.duoqu_white);
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(false);
        mPopupWindow = new PopupWindow(mContainerView, mWidth == 0 ? LayoutParams.WRAP_CONTENT : mWidth,
                mHeight == 0 ? LayoutParams.WRAP_CONTENT : mHeight);
        mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
    }

    public void showAtLocation(View parent) {
        mParentView = parent;
        showPopupAccordingParentView();
    }

    public void showPopupAccordingParentView() {
        if (mParentView == null) {
            return;
        }
        //ui_part handle this action first
        if(mUiInterface != null){
            try{
                Boolean result = (Boolean) mUiInterface.doUiAction(ViewManger.UIPART_ACTION_SHOW_POP_MENU, this);
                if(result.equals(Boolean.TRUE)){
                    return;
                }
            }catch(Throwable e){
            }
        }
        
      //ui_part do nothing go on
        
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mContainerView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        // int x = (int) ViewUtil.getDimension(R.dimen.duoqu_popu_menu_x);
        int popupWith = mPopupWindow.getContentView().getMeasuredWidth();
        int parentWith = mParentView.getWidth();
        int x = (parentWith - popupWith) / 2;
        mPopupWindow.showAsDropDown(mParentView, x,
                -mPopupWindow.getContentView().getMeasuredHeight() - mParentView.getMeasuredHeight()-21);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(false);
        mPopupWindow.update();
        mPopupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                mIsShow = false;
                if (mParentView != null) {
                    mParentView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mParentView.setTag(null);
                        }
                    }, 200);
                }
                destory();
            }
        });
        mIsShow = true;
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    private void setSubMenu() throws JSONException {
        mListView.removeAllViews();
        for (int i = 0; i < mJsonArray.length(); i++) {
            final JSONObject ob = mJsonArray.getJSONObject(i);
            LinearLayout layoutItem = (LinearLayout) ((LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.duoqu_pomenu_menuitem, null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT, 1.0f);
            mContainerView.setLayoutParams(lp);
            layoutItem.setFocusable(true);
            TextView tv_funbtntitle = (TextView) layoutItem.findViewById(R.id.pop_item_textView);
            View pop_item_line = layoutItem.findViewById(R.id.pop_item_line);
            if ((i + 1) == mJsonArray.length()) {
                pop_item_line.setVisibility(View.GONE);
            }
            tv_funbtntitle.setText(ob.getString("name"));
            layoutItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    try {
                        Map<String, String> extend = new HashMap<String, String>();
                        extend.put("simIndex", "0");
                        ParseManager.doAction(mContext, ob.get("action_data").toString(), extend);
                        dismiss();
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                    }

                }
            });
            mListView.addView(layoutItem);
        }
        mListView.setVisibility(View.VISIBLE);
    }

    public void destory() {
        mContext = null;
        mJsonArray = null;
    }

    public boolean isShow() {
        return mIsShow;
    }

}
