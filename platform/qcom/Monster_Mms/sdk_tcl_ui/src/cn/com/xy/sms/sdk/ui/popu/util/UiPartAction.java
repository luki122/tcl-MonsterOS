package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.menu.PopMenus;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;

public class UiPartAction implements UiPartInterface{
    public Object doUiAction(int type, Object data){
        Boolean result = Boolean.FALSE ;
        switch(type){
            case ViewManger.UIPART_ACTION_SET_BG:
                doSetBG(data);
                break;
            case ViewManger.UIPART_ACTION_SET_POP_MENU:
                result = dosetUpPopMenu(data) ;
                break ;
            case ViewManger.UIPART_ACTION_SHOW_POP_MENU:
                result = showPopMenu(data) ;
                break ;
            case ViewManger.UIPART_ACTION_SHOW_DIALOG:
                result = doUiActionDialog() ;
                break ;
            default:
                result = Boolean.FALSE ;
                    
        }
        
        return result ;
    }
    private void doSetBG(Object data) {
        if(data == null){
            return ;
        }
        if(data instanceof View){
            ((View) data).setBackgroundResource(R.drawable.duoqu_ui_part_uipartaction_background); 
        }
        return ;
    }
    @Override
    public Object doUiActionMulti(int type, Object... data) {
        Object result = null;
        try {
            switch (type) {
            case ViewManger.UIPART_ACTION_SET_BUTTON_TEXT_COLOR:
                result = setBubbleText((Activity) data[0], (TextView) data[1], (JSONObject) data[2]);
                break;
            default:
                result = null;
            }
        } catch (Throwable e) {
        }

        return result;
    }

    private Boolean setBubbleText(Activity mContext, TextView buttonText, JSONObject actionMap) {
        String btnName = ContentUtil.getBtnName(actionMap);
        if (!StringUtils.isNull(btnName)) {
            buttonText.setText(btnName);
        }
        return Boolean.TRUE;
    }
    private Boolean doUiActionDialog() {
        return Boolean.TRUE;
    }

    private Boolean showPopMenu(Object data) {
        if (data == null || !(data instanceof PopMenus)) {
            return Boolean.FALSE;
        }

        try {
            final PopMenus pm = (PopMenus) data;
            final View parentView = pm.getmParentView();
            if (parentView == null) {
                return Boolean.FALSE;
            }
            View containerView = pm.getmContainerView();
            PopupWindow pw = pm.getmPopupWindow();
            pw.setBackgroundDrawable(new ColorDrawable());
            containerView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

            /*linwejie start */
            int myPopupWith = containerView.getMeasuredWidth();
            int parentWith = parentView.getWidth();
            int x = (parentWith - myPopupWith) / 2;
            /*linwejie end */
            
            int popupWith = -pw.getWidth() / 2 + parentView.getMeasuredWidth() / 2;

            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) containerView.getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            int mScreenWidth = dm.widthPixels;
            int[] location = new int[2];
            parentView.getLocationOnScreen(location);
            int vRight = mScreenWidth - location[0];
            if (vRight < pw.getWidth()) {
                popupWith = popupWith * 2;
            }

////             pw.showAsDropDown(parentView,
////             popupWith,-pw.getContentView().getMeasuredHeight() -
////             parentView.getMeasuredHeight() - 10);
            
//            pw.showAsDropDown(parentView, popupWith,
//                    -pw.getContentView().getMeasuredHeight() - parentView.getMeasuredHeight());
            pw.showAsDropDown(parentView, x,
                  -containerView.getMeasuredHeight() - parentView.getMeasuredHeight()-15);
            pw.setOutsideTouchable(true);
            pw.setFocusable(false);
            pw.update();
            pw.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    pm.setmIsShow(false);
                    if (parentView != null) {
                        parentView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                parentView.setTag(null);
                            }
                        }, 200);
                    }
                    pm.destory();
                }
            });
        } catch (Throwable e) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @SuppressLint("ResourceAsColor")
    private Boolean dosetUpPopMenu(Object data) {
        if (data == null || !(data instanceof PopMenus)) {
            return Boolean.FALSE;
        }
        try {
            PopMenus pm = (PopMenus) data;
            Context context = pm.getmContext();
            // containerView init                                   
            View containerView = LayoutInflater.from(context).inflate(R.layout.duoqu_ui_part_popmenus, null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT, 1.0f);
            containerView.setLayoutParams(lp);
            pm.setmContainerView(containerView);
            // listView init
            LinearLayout listView = (LinearLayout) pm.getmContainerView().findViewById(R.id.layout_subcustommenu);
            pm.setmListView(listView);
            if (listView != null) {
                listView.setBackgroundColor(R.color.duoqu_menu_color_withe);
                listView.setFocusableInTouchMode(true);
                listView.setFocusable(false);
            }
            setSubMenu(pm);

            // PopupWindow init
            int width = pm.getmWidth();
            int hight = pm.getmHeight();
            PopupWindow pw = new PopupWindow(containerView, width == 0 ? LayoutParams.WRAP_CONTENT : width,
                    hight == 0 ? LayoutParams.WRAP_CONTENT : hight);

            pw.setAnimationStyle(R.style.duoqu_ui_part_popwin_anim_style);

            pm.setmPopupWindow(pw);
        } catch (Throwable e) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private void setSubMenu(final PopMenus pm) throws Throwable {
        if (pm == null) {
            return;
        }
        LinearLayout listView = pm.getmListView();
        JSONArray jsonArray = pm.getmJsonArray();
        final Activity c = pm.getmContext();
        View containerView = pm.getmContainerView();
        listView.removeAllViews();

        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject ob = jsonArray.getJSONObject(i);
            LinearLayout layoutItem = (LinearLayout) ((LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.duoqu_ui_part_pomenu_menuitem,
                            null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT, 1.0f);
            containerView.setLayoutParams(lp);
            layoutItem.setFocusable(true);
            TextView tv_funbtntitle = (TextView) layoutItem.findViewById(R.id.pop_ui_part_item_textView);
//            View pop_item_line = layoutItem.findViewById(R.id.pop_ui_part_item_line);
//            pop_item_line.setVisibility(View.GONE);
//            if ((i + 1) == jsonArray.length()) {
//                pop_item_line.setVisibility(View.GONE);
//            }
            tv_funbtntitle.setText(ob.getString("name"));
            layoutItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    try {
                        Map<String, String> extend = new HashMap<String, String>();
                        extend.put("simIndex", "0");

                        ParseManager.doAction(c, ob.get("action_data").toString(), extend);
                        pm.dismiss();
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("setSubMenu", e);
                    }
                }
            });
            listView.addView(layoutItem);
        }
        listView.setVisibility(View.VISIBLE);
    }


}
