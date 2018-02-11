/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mst.recordsettings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Audio.Media;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import com.android.incallui.R;
import com.android.incallui.AccelerometerListener.OrientationListener;

import mst.app.dialog.AlertDialog;
import mst.widget.SliderLayout;
import mst.widget.SliderView;


public class RecordHistoryAdapter extends BaseAdapter implements SliderView.OnSliderButtonLickListener {
	
	private final static String TAG = "RecordHistoryAdapter";
	
	private Context mContext;
	
	private HashMap<String, CallRecord> mCheckedItem = new HashMap<String, CallRecord>();
	private boolean mCheckBoxEnable = false;
	private boolean mNeedAnim = false;
	
	private ArrayList<CallRecord> mRecords = new ArrayList<CallRecord>();
	
	public RecordHistoryAdapter(Context context, ArrayList<CallRecord> records) {
		mContext = context;
		mRecords = records;
	}
	
	public void setRecords(ArrayList<CallRecord> records) {
		mRecords = records;
	}
	
	public void setCheckBoxEnable(boolean flag) {
        mCheckBoxEnable = flag;
    }
    
    public boolean getCheckBoxEnable() {
        return mCheckBoxEnable;
    }
    
    public void setNeedAnim(boolean flag) {
        mNeedAnim = flag;
    }
    
    public boolean getNeedAnim() {
        return mNeedAnim;
    }
    
    
    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            setNeedAnim(false);
            super.handleMessage(msg);
        }

    };

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mRecords.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mRecords.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub		
		 View item;
		if(convertView !=  null) {
			item = convertView;
		} else {
           item = LayoutInflater.from(mContext).inflate(
				R.layout.record_history_item_slider, null);
           SliderView s = (SliderView)item.findViewById(R.id.slider_view);
           s.addTextButton(1, mContext.getString(R.string.delete));
		}
        
        RelativeLayout layout = (RelativeLayout)item.findViewById(R.id.item);
		TextView nameTv = (TextView)item.findViewById(R.id.name);
		TextView dateTv = (TextView)item.findViewById(R.id.date);
		ImageView detail = (ImageView)item.findViewById(R.id.detail);
               
		
		final CallRecord record = mRecords.get(position);
		
		nameTv.setText(record.getName());
		String date = RecordFormatUtils.formatDate(record.getEndTime());
		dateTv.setText(date);
		
		detail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FileInfoUtils.showFileDetail(record,mContext);
			}
		});
		
        CheckBox checkBox = (CheckBox) item.findViewById(R.id.list_item_check_box);
       	checkBox.setChecked(mCheckedItem.containsKey(position) ? true : false);
        if (mCheckBoxEnable) {
        	detail.setVisibility(View.GONE);
            checkBox.setVisibility(View.VISIBLE);
        } else {
        	detail.setVisibility(View.VISIBLE);
            checkBox.setVisibility(View.GONE);
        }
		
		if (getNeedAnim()) {
            mHandler.sendMessage(mHandler.obtainMessage());
        }
						
		SliderView slider = (SliderView)item.findViewById(R.id.slider_view);
		
		if(mCheckBoxEnable){
	        slider.setLockDrag(true);
        }else{
            slider.setLockDrag(false);
        }
	        
        if(slider.isOpened()){
            slider.close(false);
        }
		
		slider.setTag(position);
		slider.setOnSliderButtonClickListener(this);		
		slider.setSwipeListener(new mst.widget.SliderLayout.SwipeListener(){
              /**
               * Called when the main view becomes completely closed.
               */
              public void onClosed(SliderLayout view){
                  //              swipeOpenPosition=-1;
              }

              /**
               * Called when the main view becomes completely opened.
               */
              public void onOpened(SliderLayout view){
                  //              swipeOpenPosition=position;
              }

              /**
               * Called when the main view's position changes.
               * @param slideOffset The new offset of the main view within its range, from 0-1
               */
              public void onSlide(SliderLayout view, float slideOffset){
                  //              Log.d(TAG,"onSlide:"+view+" slideOffset:"+slideOffset);
              }
          });
		
		return item;
	}
	
	
	public String getName(int position) {
		return mRecords.get(position).getName();
	}
	
	public String getPath(int position) {
		return mRecords.get(position).getPath();
	}
	
	public void setCheckedItem(String position, CallRecord acr) {
		if (mCheckedItem == null) {
            mCheckedItem = new HashMap<String, CallRecord>();
        }
		
		if (!mCheckedItem.containsKey(position)) {
			mCheckedItem.put(position, acr);
		}
	}
	
	public HashMap<String, CallRecord> getCheckedItem() {
		return mCheckedItem;
	}
	
    public void removeCheckedItem(String position) {
    	if (mCheckedItem.containsKey(position)) {
    		mCheckedItem.remove(position);
    	}
	}
    
    public void clearCheckedItem() {
    	mCheckedItem.clear();
	}
    
    public interface updateListener {
        public void update();
    }
    
    private updateListener mListener;
    
    public void setListener(updateListener listener) {
        mListener = listener;
    }
    
    @Override
    public void onSliderButtonClick(int id, View view, ViewGroup parent) {
        
        Log.d(TAG,"onSliderButtonClick,id:"+id+" view:"+view+" parent:"+parent);
        switch (id) {
        case 1:
            if(((SliderView)parent).isOpened()){
                ((SliderView)parent).close(false);
            }
            
            final int position = (int) parent.getTag();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//            builder.setMessage(mContext.getString(R.string.mst_remove_group_member_message));
            builder.setTitle(mContext.getString(R.string.slide_delete_record_title));
            builder.setNegativeButton(mContext.getString(android.R.string.cancel), null);
            builder.setPositiveButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        final CallRecord record = mRecords.get(position);
                        String historyPath = record.getPath();
                        File file = new File(historyPath);
                        if (file.exists()) {
                            file.delete();
                            mRecords.remove(position);
                            setCheckBoxEnable(false);
                            setNeedAnim(false);
                            notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }               
                    mListener.update();     
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            break;
        }

   
    
    }
 
}
