package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class AirUISelectDialog {
    private JSONArray train_array;
    private Context mContext;
    private int mCurrentTrainIndex;
    private List<View> mAllSelects;
    private OnBottomClick mOnBottomClick = null;
    private TextView mLeft;
    private TextView mRight;
    private Dialog dialog;
    public static final int CONFIRM = 0;
    public static final int CANNEL = 1;
    public DialogParams params;

    private static final String MAINTEXT = "main_text";
    private static final String SECTEXT = "sec_text";
    private static final String TITLETEXT = "title_text";
   
    public AirUISelectDialog(JSONArray train_array, Context context, int currentTrainIndex) {
        super();
        this.train_array = train_array;
        this.mContext = context;
        this.mCurrentTrainIndex = currentTrainIndex;
        mAllSelects = new ArrayList<View>();

        params = new DialogParams();
        params.mDefaultTextColor = R.color.duoqu_ui_3010;
        params.mSelectTextColor = R.color.duoqu_ui_3010;
        params.mDefaultTitleName = "";
    }

    private void setItemViewBg(View view, int index, int count) {
        if (count == 1) {
            view.setBackgroundResource(R.drawable.duoqu_ui_group_navi_item_single_s);
            View sepLine = view.findViewById(R.id.sep_line);
            sepLine.setVisibility(View.GONE);
        } else {
            if (index == count - 1) {
                view.setBackgroundResource(R.drawable.duoqu_ui_group_navi_item_bot_s);
                View sepLine = view.findViewById(R.id.sep_line);
                sepLine.setVisibility(View.GONE);
            } else {
                view.setBackgroundResource(R.drawable.duoqu_ui_group_navi_item_mid_s);
            }
        }
    }

    public void ShowDialog(OnBottomClick click) {
        // TODO Auto-generated method stub
        try {

            this.mOnBottomClick = click;
            if (train_array != null && train_array.length() > 0) {
                dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                Window window = dialog.getWindow();
                window.setGravity(Gravity.BOTTOM);
                window.getDecorView().setPadding(ViewUtil.dp2px(mContext, 0), 0, ViewUtil.dp2px(mContext, 0),
                        ViewUtil.dp2px(mContext, 0));
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);
                View customView = LayoutInflater.from(mContext).inflate(R.layout.duoqu_ui_select_list_dialog, null);
                LinearLayout rootLayout = (LinearLayout) customView.findViewById(R.id.item_roots);
                TextView titile = (TextView) customView.findViewById(R.id.title);
                // 设置弹出框标题
                TextView showText = (TextView) customView.findViewById(R.id.bottom_sept_show_text);
                showText.setText(train_array.getJSONObject(0).optString(TITLETEXT));

                ContentUtil.setText(titile, params.mDefaultTitleName, "");
                dialog.setContentView(customView);

                for (int position = 0; position < train_array.length(); position++) {
                    try {
                        JSONObject itemJson = train_array.optJSONObject(position);

                        View itemChildView = View.inflate(mContext, R.layout.duoqu_ui_list_items_content_part, null);
                        setItemViewBg(itemChildView, position, train_array.length());

                        TextView itemCity = (TextView) itemChildView.findViewById(R.id.item_city);
                        RelativeLayout itemLayout = (RelativeLayout) itemChildView.findViewById(R.id.item_layout);
                        String secText = itemJson.optString(SECTEXT);
                        DisplayMetrics metrics = ContentUtil.getDisplayMetrics();
                        ViewGroup.LayoutParams layoutParams = itemLayout.getLayoutParams();
                        if (StringUtils.isNull(secText)) {
                            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54,
                                    metrics);
                            itemCity.setVisibility(View.GONE);
                        } else {
                            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64,
                                    metrics);
                            itemCity.setText(secText);
                            itemCity.setVisibility(View.VISIBLE);
                        }
                        itemLayout.setLayoutParams(layoutParams);

                        TextView itemText = (TextView) itemChildView.findViewById(R.id.item_text);
                        itemText.setText(itemJson.optString(MAINTEXT));

                        View itemCheck = itemChildView.findViewById(R.id.item_check);
                        if (position == mCurrentTrainIndex) {
                            itemCheck.setBackgroundResource(R.drawable.btn_radio_off_disabled_focused_holo_light);
                        } else {
                            itemCheck.setBackgroundResource(R.drawable.btn_radio_off_disabled_holo_light);
                        }
                        itemChildView.setOnClickListener(new OnItemSelectDialog());
                        mAllSelects.add(itemChildView);
                        rootLayout.addView(itemChildView);
                    } catch (Throwable e) {
                    }
                }
                mLeft = (TextView) customView.findViewById(R.id.duoqu_select_dialog_left);
                mLeft.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        if (mOnBottomClick != null) {
                            mOnBottomClick.Onclick(CANNEL, mCurrentTrainIndex);
                        }
                    }
                });
                mRight = (TextView) customView.findViewById(R.id.duoqu_select_dialog_right);
                mRight.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        if (mOnBottomClick != null) {
                            mOnBottomClick.Onclick(CONFIRM, mCurrentTrainIndex);
                        }
                    }
                });
                dialog.show();
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("AirSelectDialog", e);
        }
    }

    private class OnItemSelectDialog implements OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            for (int i = 0; i < mAllSelects.size(); i++) {
                View item = mAllSelects.get(i);
                if (item == v) {
                    View itemCheck = item.findViewById(R.id.item_check);
                    itemCheck.setBackgroundResource(R.drawable.btn_radio_off_disabled_focused_holo_light);
                    mCurrentTrainIndex = i;
                } else {
                    View itemCheck = item.findViewById(R.id.item_check);
                    itemCheck.setBackgroundResource(R.drawable.btn_radio_off_disabled_holo_light);
                }
            }
        }
    }

    public interface OnBottomClick {
        public void Onclick(int type, int select);
    }

    public class DialogParams {
        public int mDefaultTextColor;
        public int mSelectTextColor;
        public String mDefaultTitleName;
        public String mSelectItemKey;
    }

}
