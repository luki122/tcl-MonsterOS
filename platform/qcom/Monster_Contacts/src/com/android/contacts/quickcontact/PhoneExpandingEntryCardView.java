package com.android.contacts.quickcontact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.android.contacts.quickcontact.ExpandingEntryCardView.Entry;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryView;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.R;

import android.net.Uri;
import android.database.Cursor;
import android.graphics.drawable.Drawable;


public class PhoneExpandingEntryCardView extends ExpandingEntryCardView {

	private Context mContext;
	public PhoneExpandingEntryCardView(Context context) {
		this(context, null);
	}

	public PhoneExpandingEntryCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	protected View createEntryView(LayoutInflater layoutInflater,
			final Entry entry, int iconVisibility) {
		View view = super
				.createEntryView(layoutInflater, entry, iconVisibility);
		final TextView header = (TextView) view.findViewById(R.id.header);	
		String number = entry.getHeader();	
		boolean isPhoneNumber = entry.getIntent().getAction() == Intent.ACTION_CALL;
		if (isPhoneNumber && isBlackNumber(number)) {
			Drawable right = mContext.getResources().getDrawable(
					R.drawable.black_icon);
	        int mSimIconMargin = getContext().getResources().getDimensionPixelSize(
	                R.dimen.sim_icon_margin);
			header.setCompoundDrawablePadding(mSimIconMargin);
			header.setCompoundDrawablesWithIntrinsicBounds(null, null, right, null);
		} else {
			header.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}

		return view;
	}

	private static Uri black_uri = Uri.parse("content://com.android.contacts/black");

	private static final String[] BLACK_PROJECTION = new String[] {
		"_id",   
		"isblack",  
		"lable",   
		"black_name", 
		"number", 
		"reject" 
	};

	public String StringFilter(String str)   throws   PatternSyntaxException   {      
		// 只允许字母和数字        
		// String   regEx  =  "[^a-zA-Z0-9]";
		// 清除掉所有特殊字符
		String regEx = "[^0-9]";
//		String regEx="[`~!@#$%^&*()+-=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]P";
		Pattern   p   =   Pattern.compile(regEx);     
		Matcher   m   =   p.matcher(str);     
		return   m.replaceAll("").trim();     
	} 


	public boolean isBlackNumber(String number) {
		Log.v("PhoneExpandingEntryCardView", " isBlackNumber number = " + number);

		number=StringFilter(number);
		number=number.replaceAll(" ", "");
		Log.d("liyang","\\number:"+number);

		if(TextUtils.isEmpty(number)) {
			return false;
		}

		Cursor cursor = mContext.getContentResolver().query(black_uri, BLACK_PROJECTION,
				"(reject = '1' OR reject = '3') AND PHONE_NUMBERS_EQUAL(number,  \"" + number + " \", 0)", null, null);
		Log.v("PhoneExpandingEntryCardView", " cursor = " + cursor);
		try {
			if (cursor != null && cursor.getCount() > 0) {
			    Log.v("PhoneExpandingEntryCardView", " isBlackNumber number true ");
				return true;
			}
			return false;
		} finally {
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

}