package com.monster.appmanager.utils.dialog;

import java.util.List;

import com.monster.appmanager.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class AbstractChoicePopWindow implements OnClickListener {

	protected Context mContext;
	protected View mParentView;

	protected ScrollView mScrollView;
	protected TextView mTVTitle;
	// protected Button mButtonOK;
	protected Button mButtonCancel;
	protected ListView mListView;

	protected PopupWindow mPopupWindow;
	protected List<String> mList;

	private OnClickListener mOkListener;

	public AbstractChoicePopWindow(Context context, View parentView, List<String> list) {
		mContext = context;
		mParentView = parentView;
		mList = list;

		initView(mContext);
	}

	protected void initView(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
		mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
		mTVTitle = (TextView) view.findViewById(R.id.tvTitle);
		// mButtonOK = (Button) view.findViewById(R.id.btnOK);
		// mButtonOK.setOnClickListener(this);
		mButtonCancel = (Button) view.findViewById(R.id.btnCancel);
		mButtonCancel.setOnClickListener(this);

		mPopupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mPopupWindow.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0x00);
		mPopupWindow.setBackgroundDrawable(dw);

		mListView = (ListView) view.findViewById(R.id.listView);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// case R.id.btnOK:
		// onButtonOK(v);
		// break;
		case R.id.btnCancel:
			onButtonCancel(v);
			break;
		}
	}

	public void setOnOKButtonListener(OnClickListener onClickListener) {
		mOkListener = onClickListener;
	}

	public void setTitle(String title) {
		mTVTitle.setText(title);
	}

	public void show(boolean bShow) {

		if (bShow) {
			backgroundAlpha((Activity)mContext,0.62f);
			mScrollView.scrollTo(0, 0);
			mPopupWindow.showAtLocation(mParentView, Gravity.TOP, 0, 0);
			mPopupWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					backgroundAlpha((Activity)mContext,1.0f);
				}
			});
		} else {
			mPopupWindow.dismiss();
		}
	}

	protected void onButtonOK(View v) {
		show(false);

		if (mOkListener != null) {
			mOkListener.onClick(v);
		}
	}

	protected void onButtonCancel(View v) {
		show(false);
	}

    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }
}
