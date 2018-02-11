package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class AirTrainSelectDialog {
	private JSONArray mTrainArray;
	private Context mContext;
	private int mCurrentTrainIndex;
	private List<View> mAllSelects;
	private OnBottomClick mOnBottomClick = null;
	private TextView mLeft;
	private TextView mRight;
	private Dialog mDialog;
	public static final int CONFIRM = 0;
	public static final int CANNEL = 1;
	public DialogParams mParams;
	private static final String TRAIN_LIST_KEY = "view_m_trainnumber";
	private static final String AIR_LISE_KEY = "view_flight_number";
	private static final String DEPART_CITY = "view_depart_city";
	private static final String ARRIVE_CITY = "view_arrive_city";
	private String mDepartCity = null;
	private String mArriveCity = null;
	private String mDialogInfo = null;
	private String mDepartAirPort = null;
	private String mArriveAirPort = null;
	private String mDepartTerminal = null;
	private String mArriveTerminal = null;

	public AirTrainSelectDialog(JSONArray train_array, Context mContext,
			int mCurrentTrainIndex) {
		super();
		this.mTrainArray = train_array;
		this.mContext = mContext;
		this.mCurrentTrainIndex = mCurrentTrainIndex;
		mAllSelects = new ArrayList<View>();

		mParams = new DialogParams();
		mParams.mDefaultTitleName = "";
	}


	public void ShowDialog(OnBottomClick click) {
		this.mOnBottomClick = click;
		if (mTrainArray != null && mTrainArray.length() > 0) {
			mDialog = new Dialog(mContext);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			Window window = mDialog.getWindow();
			window.setGravity(Gravity.BOTTOM);
			window.getDecorView().setPadding(ViewUtil.dp2px(mContext, 0), 0,
					ViewUtil.dp2px(mContext, 0), ViewUtil.dp2px(mContext, 0));
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			window.setAttributes(lp);
			View customView = LayoutInflater.from(mContext).inflate(
					R.layout.duoqu_select_list_dialog, null);
			LinearLayout rootLayout = (LinearLayout) customView
					.findViewById(R.id.item_roots);
			TextView titile = (TextView) customView.findViewById(R.id.title);

			ChannelContentUtil.setText(titile, mParams.mDefaultTitleName, "");
			mDialog.setContentView(customView);
			for (int position = 0; position < mTrainArray.length(); position++) {
				JSONObject itemJson = mTrainArray.optJSONObject(position);
				View itemChildView = View.inflate(mContext,
						R.layout.duoqu_list_items_content_part, null);
				TextView itemText = (TextView) itemChildView
						.findViewById(R.id.item_text);
				TextView itemInfo = (TextView) itemChildView
						.findViewById(R.id.item_info);
				String item = null;
				String itemName = itemJson.optString(mParams.mSelectItemKey);
				itemName = itemName.replace(" ", "").replace(
						ChannelContentUtil.TRAIN_DEF_END, "");
				if (mParams.mSelectItemKey.endsWith(TRAIN_LIST_KEY)) {
					item = itemName + ChannelContentUtil.TRAIN_DEF_END;
				} else if (mParams.mSelectItemKey.endsWith(AIR_LISE_KEY)) {
					item = itemName + ChannelContentUtil.FLIGHT_DEF_END;
				}
				itemText.setText(item);

				mDepartCity = itemJson.optString(DEPART_CITY);
				mArriveCity = itemJson.optString(ARRIVE_CITY);
				mDepartAirPort = itemJson.optString("view_depart_airport");
				mArriveAirPort = itemJson.optString("view_arrive_airport");
				mDepartTerminal = itemJson.optString("view_depart_terminal");
				mArriveTerminal = itemJson.optString("view_arrive_terminal");
				if (StringUtils.isNull(mDepartCity)
						&& StringUtils.isNull(mArriveCity)) {
					mDialogInfo = "";
					itemInfo.setText(mDialogInfo);
				}
				if (StringUtils.isNull(mArriveCity)) {
					mDialogInfo = mDepartCity + ChannelContentUtil.TRAIN_DEPART;
					itemInfo.setText(mDialogInfo);
				}
				if (StringUtils.isNull(mDepartCity)) {
					if (mParams.mSelectItemKey.endsWith(AIR_LISE_KEY)) {
						mDialogInfo = ChannelContentUtil.FLIGHT_ARRIVE
								+ mArriveCity;
						itemInfo.setText(mDialogInfo);
					} else if (mParams.mSelectItemKey.endsWith(TRAIN_LIST_KEY)) {
						mDialogInfo = ChannelContentUtil.TRAIN_ARRIVE
								+ mArriveCity;
						itemInfo.setText(mDialogInfo);
					}
				}
				if (!StringUtils.isNull(mDepartCity)
						&& !StringUtils.isNull(mArriveCity)) {
					itemInfo.setText(mDepartCity + " " + mDepartAirPort
							+ mDepartTerminal+" "+ChannelContentUtil.SPLIT_KEY+" "+mArriveCity + " " + mArriveAirPort
							+ mArriveTerminal);
				}

				View itemCheck = itemChildView.findViewById(R.id.item_check);
				if (position == mCurrentTrainIndex) {
					itemCheck
							.setBackgroundResource(R.drawable.btn_radio_off_pressed_holo_light);
				} else {
					itemCheck
							.setBackgroundResource(R.drawable.btn_radio_off_holo_light);
				}
				itemChildView.setOnClickListener(new OnItemSelectDialog());
				mAllSelects.add(itemChildView);
				rootLayout.addView(itemChildView);
			}
			mLeft = (TextView) customView
					.findViewById(R.id.duoqu_select_dialog_left);
			mLeft.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					if (mOnBottomClick != null) {
						mOnBottomClick.Onclick(CANNEL, mCurrentTrainIndex);
					}
				}
			});
			mRight = (TextView) customView
					.findViewById(R.id.duoqu_select_dialog_right);
			mRight.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					if (mOnBottomClick != null) {
						mOnBottomClick.Onclick(CONFIRM, mCurrentTrainIndex);
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
					View itemCheck = item.findViewById(R.id.item_check);
					itemCheck
							.setBackgroundResource(R.drawable.btn_radio_off_pressed_holo_light);
					mCurrentTrainIndex = i;
				} else {
					View itemCheck = item.findViewById(R.id.item_check);
					itemCheck
							.setBackgroundResource(R.drawable.btn_radio_off_holo_light);
				}
			}
		}
	}

	public interface OnBottomClick {
		public void Onclick(int type, int select);
	}

	public class DialogParams {
		public String mDefaultTitleName;
		public String mSelectItemKey;
	}
}
