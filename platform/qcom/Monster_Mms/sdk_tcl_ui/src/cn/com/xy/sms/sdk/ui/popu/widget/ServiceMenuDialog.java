package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.ui.R;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ServiceMenuDialog {  
    public static void showMenuDialog(final Context context, JSONObject jobj, Map map) {
        SelectDialog dlg = new SelectDialog(jobj, context);
        dlg.ShowDialog();
    }
    

    public static class SelectDialog {
        private JSONArray       mActionData;
        private Context         mContext;
        private int             mCurrentTrainIndex = 0;
        private List<View>      mAllSelects = null;
        private TextView        mLeft;
        private TextView        mRight;
        private Dialog          mDialog;
        public DialogParams     params;
        
        
        public SelectDialog(JSONObject jObject, Context context) {
            super();
            mActionData = (JSONArray) jObject.opt("action_data");
            mContext = context;
            mAllSelects = new ArrayList<View>();
 
            params = new DialogParams();
            params.mDefaultTextColor = R.color.duoqu_theme_color_3010;
            params.mSelectTextColor = R.color.duoqu_theme_color_3010;
            params.mDefaultTitleName = jObject.optString("extract_text");
        }
        
        private void setItemViewBg(View view, int index, int count) {
            if(view == null)
                return;
            
            view.setBackgroundResource(R.drawable.duoqu_group_navi_item_mid_s);

            if (index == count - 1) {
                View sepLine = view.findViewById(R.id.sep_line);
                sepLine.setVisibility(View.GONE);
            }
        }
        
        public void ShowDialog() {
            // TODO Auto-generated method stub
            if (mActionData != null && mActionData.length() > 0) {
                mDialog = new Dialog(mContext);
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                Window window = mDialog.getWindow();
                window.setGravity(Gravity.BOTTOM);
                window.getDecorView().setPadding(ViewUtil.dp2px(mContext, 8), 0, ViewUtil.dp2px(mContext, 8), ViewUtil.dp2px(mContext, 8));
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);
                View customView = LayoutInflater.from(mContext).inflate(R.layout.duoqu_select_list_dialog, null);
                LinearLayout rootLayout = (LinearLayout) customView.findViewById(R.id.item_roots);
                TextView titile = (TextView) customView.findViewById(R.id.title);
                
                ContentUtil.setText(titile, params.mDefaultTitleName, "");
                mDialog.setContentView(customView);
                for (int position = 0; position < mActionData.length(); position++) {
                    JSONObject itemJson = mActionData.optJSONObject(position);
                    
                    View itemChildView = View.inflate(mContext, R.layout.duoqu_list_items_content_part, null);
                    setItemViewBg(itemChildView, position, mActionData.length());
                    
                    TextView itemText = (TextView) itemChildView.findViewById(R.id.item_text);
                    itemText.setText(itemJson.optString("name"));
                    
                    View itemCheck = itemChildView.findViewById(R.id.item_check);
                    if (position == 0) {
                        itemText.setTextColor(mContext.getResources().getColor(params.mSelectTextColor));
                        itemCheck.setVisibility(View.VISIBLE);
                    }else{
                        itemText.setTextColor(mContext.getResources().getColor(params.mDefaultTextColor));
                        itemCheck.setVisibility(View.GONE);
                    }
                    
                    itemChildView.setOnClickListener(new OnItemSelectDialog());
                    mAllSelects.add(itemChildView);
                    rootLayout.addView(itemChildView);
                }
                mLeft = (TextView) customView.findViewById(R.id.duoqu_select_dialog_left);
                mLeft.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
                mRight = (TextView) customView.findViewById(R.id.duoqu_select_dialog_right);
                mRight.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        if(mActionData != null && mCurrentTrainIndex < mActionData.length()){
                            JSONObject itemJson = (JSONObject) mActionData.opt(mCurrentTrainIndex);
                            final String actionData = itemJson.opt("action_data").toString();
                            DuoquUtils.doActionContext(mContext, actionData, null);
                        }
                    }
                });
                mDialog.show();
            }
        }
        
        private class OnItemSelectDialog implements OnClickListener {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                for (int i = 0; i < mAllSelects.size(); i++) {
                    View item = mAllSelects.get(i);
                    if (item == v) {
                        TextView itemText = (TextView) item.findViewById(R.id.item_text);
                        View itemCheck = item.findViewById(R.id.item_check);
                        itemCheck.setVisibility(View.VISIBLE);
                        itemText.setTextColor(mContext.getResources().getColor(params.mSelectTextColor));
                        mCurrentTrainIndex = i;
                    } else {
                        TextView itemText = (TextView) item.findViewById(R.id.item_text);
                        View itemCheck = item.findViewById(R.id.item_check);
                        itemCheck.setVisibility(View.GONE);
                        itemText.setTextColor(mContext.getResources().getColor(params.mDefaultTextColor));
                    }
                }
            }
        }
        
        
        public class DialogParams {
            public int    mDefaultTextColor;
            public int    mSelectTextColor;
            public String mDefaultTitleName;
            public String mSelectItemKey;
        }
    }
    
}
