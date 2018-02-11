package com.monster.interception.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.monster.interception.R;
import com.monster.interception.util.FormatUtils;
import com.monster.interception.util.InterceptionUtils;
import com.monster.interception.util.YuloreUtil;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.Toast;
import mst.widget.SliderView;

public class PhoneAdapter extends InterceptionAdapterBase{
	private static final String TAG = "PhoneAdapter";
	private String name;
	private int count;
	private int[] cardIcons = { R.drawable.svg_dial_card1, R.drawable.svg_dial_card2, R.drawable.sim_not_found };

	public PhoneAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		CallHolder mHolder = (CallHolder) arg0.getTag();

		final ImageView simView = mHolder.simView;

		final String number = arg2.getString(arg2.getColumnIndex("number"));
		final String area = arg2.getString(arg2.getColumnIndex("area"));
		final long dateTime = Long.parseLong(arg2.getString(arg2
				.getColumnIndex("date")));
		name = arg2.getString(arg2.getColumnIndex("name"));
		String date = FormatUtils.formatDateTime(dateTime);
		mHolder.date.setText(date);
		//bindSectionHeaderAndDivider(arg0, date, arg2);
		simView.setTag(name);
		final int simId = arg2.getInt(arg2.getColumnIndex("simId"));
		System.out.println("[bindView]slotId=" + simId);
		if (simId == 0 || simId == 1) {
			mHolder.simView.setImageResource(cardIcons[simId]);
			mHolder.simView.setVisibility(View.VISIBLE);
		} else {
			mHolder.simView.setVisibility(View.GONE);
		}		

		StringBuilder titleStr = new StringBuilder();
		if (name == null || "".equals(name)) {
		    titleStr.append(number);
		} else {
		    titleStr.append(name);
		}
		count = arg2.getInt(arg2.getColumnIndex("count"));
        if (count > 1) {
            /*mHolder.countView.setText("(" + count + ")");
            mHolder.countView.setVisibility(View.VISIBLE);*/
            mHolder.detail.setVisibility(View.VISIBLE);
            mHolder.detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    viewCallLog(number);
                }
            });
            titleStr.append(" (" + count +")");
        } else {
            mHolder.detail.setVisibility(View.GONE);
        }
        mHolder.title.setText(titleStr.toString());

		if (area == null || "".equals(area)) {
			mHolder.content.setText(arg1.getResources().getString(
					R.string.mars));
		} else {
			mHolder.content.setText(area);
		}

		String lable = arg2.getString(arg2.getColumnIndex("lable"));
		if (lable == null || "".equals(lable)) {
			mHolder.markView.setVisibility(View.GONE);
		} else {
			mHolder.markView.setText(" | " + lable);
			mHolder.markView.setVisibility(View.VISIBLE);
		}

		
//		final int pos = arg2.getPosition();
//		mHolder.cb.setChecked(mCheckedItem.contains(pos) ? true : false);
//		mHolder.cb.setVisibility(mCheckBoxEnable ? View.VISIBLE : View.GONE);
//
//		mHolder.slideDelete.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				mListener.slideDelete(pos);
//			}
//		});
		super.bindView(arg0, arg1, arg2);

	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub

	    SliderView view = (SliderView)mInflater.inflate(R.layout.call_log_list_item, null);
	    view.addTextButton(InterceptionUtils.SLIDER_BTN_POSITION_DELETE, arg0.getString(R.string.del));
		CallHolder mHolder = new CallHolder(view);
		view.setTag(mHolder);
		return view;
	}

	protected void bindSectionHeaderAndDivider(View view, String date,
			Cursor mCursor) {
		/*CallHolder mHolder = (CallHolder) view.getTag();
		LinearLayout headerUi = mHolder.head;
		if (mCursor.isFirst()) {
			addDateText(headerUi, date);
		} else {
			mCursor.moveToPrevious();
			long preDate = Long.parseLong(mCursor.getString(mCursor
					.getColumnIndex("date")));
			mCursor.moveToNext();
			String preDateString =FormatUtils. formatDateTime(preDate);
			if (!preDateString.equals(date)) {
				addDateText(headerUi, date);
			} else {
				if (headerUi != null) {
					headerUi.setVisibility(View.GONE);
				}
			}
		}*/

	}
	
	private void addDateText(LinearLayout  headerUi, String date) {
		/*if (headerUi != null) {
			ViewGroup.LayoutParams params = headerUi.getLayoutParams();
			params.height = mContext.getResources()
					.getDimensionPixelSize(R.dimen.item_header_height);
	
			TextView tv = new TextView(mContext);
			tv.setText(date);
			int paddingLeft = mContext.getResources()
					.getDimensionPixelSize(
							R.dimen.activity_horizontal_margin);
			tv.setTextAppearance(mContext,
					R.style.calllog_list_header_style);
			tv.setHeight(params.height);
			tv.setPadding(paddingLeft, 0, 0, 0);
			tv.setGravity(Gravity.CENTER_VERTICAL);
	
			headerUi.setBackgroundColor(mContext
					.getResources()
					.getColor(
							R.color.calllog_list_header_background_color));
			headerUi.removeAllViews();
			headerUi.setEnabled(false);
			headerUi.setClickable(false);
			headerUi.addView(tv);
			headerUi.setLayoutParams(params);
			headerUi.setVisibility(View.VISIBLE);
		}*/
	}
	

	private class CallHolder {
		TextView title;
		TextView markView;
		TextView content;
		ImageView simView;
		//TextView countView;
		CheckBox cb;
		TextView date;
		ImageButton detail;
		//LinearLayout head;
		//ImageView slideDelete;

		private CallHolder(View view) {
			title = (TextView) view.findViewById(R.id.title);
			markView = (TextView) view.findViewById(R.id.mark);

			content = (TextView) view.findViewById(R.id.content);
			simView = (ImageView) view.findViewById(R.id.sim);
			//countView = (TextView) view.findViewById(R.id.count);

			cb = (CheckBox) view
					.findViewById(R.id.list_item_check_box);
			date = (TextView) view.findViewById(R.id.date);
			detail = (ImageButton) view.findViewById(R.id.detail);
			/*head = (LinearLayout) view
					.findViewById(R.id.head);*/
			//slideDelete = (ImageView) view.findViewById(R.id.slidedelete);
		}
	}
	
	   private void viewCallLog(String number) {
	        // TODO Auto-generated method stub
	        final Intent intent = new Intent();
	        // String name = blackName;
	        Log.e(TAG, "viewCallLog  number = "
	                + number);
	        intent.setClassName("com.android.dialer",
	                "com.android.dialer.CallDetailActivity");
	        intent.putExtra("EXTRA_NUMBER", number);
	        intent.putExtra("reject_detail", true);
	        // intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

	        try {
	            mContext.startActivity(intent);
	        } catch (ActivityNotFoundException e) {
	            e.printStackTrace();
	        }
	    }

}
