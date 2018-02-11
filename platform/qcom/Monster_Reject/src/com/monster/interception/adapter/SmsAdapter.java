package com.monster.interception.adapter;

import java.util.HashSet;
import java.util.Set;

import com.monster.interception.R;
import com.monster.interception.util.FormatUtils;
import com.monster.interception.util.InterceptionUtils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView.RecyclerListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import mst.widget.SliderView;

public class SmsAdapter extends InterceptionAdapterBase {

	private int count;

	public SmsAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		MessageHolder mHolder = (MessageHolder) arg0.getTag();
		mHolder.content.setText(arg2.getString(arg2.getColumnIndex("body")));
		String address = arg2.getString(arg2.getColumnIndex("address"));
		String name = arg2.getString(arg2.getColumnIndex("name"));
		if (arg2.getInt(arg2.getColumnIndex("ismms")) == 1) {
			mHolder.attachment.setVisibility(View.VISIBLE);
		} else {
			mHolder.attachment.setVisibility(View.GONE);
		}

		count = arg2.getInt(arg2.getColumnIndex("count"));

		String title = TextUtils.isEmpty(name) ? address : name;
		if (count > 1) {
			mHolder.title.setText(title +"(" + count + ")");
		} else {
			mHolder.title.setText(title);
		}

		mHolder.date.setText(FormatUtils.formatTimeStampString(mContext,
				Long.parseLong(arg2.getString(arg2.getColumnIndex("date"))),
				true));

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
	    SliderView view = (SliderView)mInflater.inflate(R.layout.sms_list_item, null);
        view.addTextButton(InterceptionUtils.SLIDER_BTN_POSITION_DELETE, arg0.getString(R.string.del));
		MessageHolder mHolder = new MessageHolder(view);
		view.setTag(mHolder);
		return view;
	}

	private class MessageHolder {
		private TextView title;
		private ImageView attachment;
		private TextView content;
		private TextView date;
		//private CheckBox cb;
		//private ImageView slideDelete;

		private MessageHolder(View view) {
			title = (TextView) view.findViewById(R.id.sms_title);
			attachment = (ImageView) view.findViewById(R.id.mms);

			content = (TextView) view.findViewById(R.id.sms_content);
			date = (TextView) view.findViewById(R.id.sms_date);

			//cb = (CheckBox) view.findViewById(R.id.sms_checkbox);
			//slideDelete = (ImageView) view.findViewById(R.id.slidedelete);
		}
	}	


}
