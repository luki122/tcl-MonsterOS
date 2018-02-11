package com.monster.interception.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.monster.interception.R;
import com.monster.interception.activity.slideDeleteListener;
import com.monster.interception.util.FormatUtils;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.Toast;
import mst.widget.SliderLayout;
import mst.widget.SliderView;
import mst.widget.SliderLayout.SwipeListener;

public class InterceptionAdapterBase extends CursorAdapter {
	private static final String TAG = "InterceptionAdapterBase";
	protected LayoutInflater mInflater;
	protected Context mContext;

	public InterceptionAdapterBase(Context context, Cursor c) {
		super(context, c);
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}


	@Override
	  public  void bindView(View view, Context context, Cursor cursor){
		
		final int pos = cursor.getPosition();
		CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.list_item_check_box);
		checkBox.setChecked(mCheckedItem.contains(pos) ? true : false);
		checkBox.setVisibility(mCheckBoxEnable ? View.VISIBLE : View.GONE);

		/*ImageView slideDelete = (ImageView) view.findViewById(R.id.slidedelete);
		slideDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mListener.slideDelete(pos);
			}
		});*/
		SliderView sliderView = (SliderView)view.findViewById(R.id.swipe_view);
		if(sliderView != null) {
		    sliderView.setOnSliderButtonClickListener(mListener);
		    sliderView.setTag(R.id.swipe_view, String.valueOf(cursor.getPosition()));
		    sliderView.setSwipeListener(new SwipeListener(){
                public void onClosed(SliderLayout view){
                    currentSliderView=null;
                }
                public void onOpened(SliderLayout view){
                    currentSliderView=sliderView;
                }
                public void onSlide(SliderLayout view, float slideOffset){
                }
            });
            sliderView.setLockDrag(mCheckBoxEnable);
            if(sliderView.isOpened()){
                sliderView.close(false);
            }
		}
	}

	/*protected slideDeleteListener mListener;

	public void setListener(slideDeleteListener listener) {
        mListener = listener;
	}*/
	protected SliderView.OnSliderButtonLickListener mListener;

    public void setListener(SliderView.OnSliderButtonLickListener listener) {
        mListener = listener;
    }

    private SliderView currentSliderView;
    public SliderView getCurrentSliderView() {
        return currentSliderView;
    }
    public void setCurrentSliderView(SliderView currentSliderView1) {
        currentSliderView = currentSliderView1;
    }

	protected boolean mCheckBoxEnable = false;

	public void setCheckBoxEnable(boolean flag) {
		mCheckBoxEnable = flag;
	}

	public boolean getCheckBoxEnable() {
		return mCheckBoxEnable;
	}

	protected Set<Integer> mCheckedItem = new HashSet<Integer>();

	public void setCheckedItem(int position) {
		if (mCheckedItem == null) {
			mCheckedItem = new HashSet<Integer>();
		}

		if (!mCheckedItem.contains(position)) {
			mCheckedItem.add(position);
		}
	}

	public Set<Integer> getCheckedItem() {
		return mCheckedItem;
	}

	public void removeCheckedItem(int position) {
		if (mCheckedItem.contains(position)) {
			mCheckedItem.remove(position);
		}
	}

	public void clearCheckedItem() {
		mCheckedItem.clear();
	}


	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		return null;
	}


}
