package com.android.contacts.editor;

import com.android.contacts.ContactsApplication;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactModifier;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.R;

import android.content.Context;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import mst.provider.ContactsContract.Contacts;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.contacts.common.model.ContactLoader;

public class MstRingtoneEditorView extends LinearLayout implements OnClickListener {
	
	private RawContactDelta mState;
	private String mRingtoneUri;
	private onPickRingtoneListener mPickRingtoneListener;
	
	private DataKind mKind;
    private TextView mRingtoneTv;
    private TextView kindTitle;
    private String mNoGroupString;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;
    private int mHintTextColor;
    
	public MstRingtoneEditorView(Context context) {
        super(context);
    }

    public MstRingtoneEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = getContext().getResources();
        mPrimaryTextColor = resources.getColor(R.color.primary_text_color);
        mSecondaryTextColor = resources.getColor(R.color.secondary_text_color);
      
        mRingtoneTv = (TextView)findViewById(R.id.kind_message);
        kindTitle = (TextView)findViewById(R.id.kind_title);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mRingtoneTv != null) {
            mRingtoneTv.setEnabled(enabled);
            ((View)(mRingtoneTv.getParent())).setOnClickListener(this);
        }
    }
    
    public void setKind(DataKind kind) {
        mKind = kind;
        kindTitle.setText(getResources().getString(kind.titleRes).toUpperCase());
        kindTitle.setVisibility(View.GONE);
    }
    
    public void setRingtoneData(String ringtoneUri) {
		mRingtoneUri = ringtoneUri;
		if (null != mRingtoneUri) {
        	new SimpleAsynTask() {
        		String title = getContext().getResources().getString(R.string.mst_ringtone_default);
				@Override
				protected Integer doInBackground(Integer... params) {
					title = ContactLoader.loadContactRingtoneData(getContext(), Uri.parse(mRingtoneUri));
					return null;
				}
				
				@Override
				protected void onPostExecute(Integer result) {
					Log.e("liumx---","title:"+title);
					mRingtoneTv.setText(title);
				}
			}.execute();
        } else {
        	mRingtoneTv.setText(getContext().getResources().getString(R.string.mst_ringtone_default));
        }
	}

    public void setState(RawContactDelta state) {
    	mState = state;
    	ValuesDelta values = mState.getValues();
        if (null != mRingtoneUri) {
        	new SimpleAsynTask() {
        		String title = getContext().getResources().getString(R.string.mst_ringtone_default);
				@Override
				protected Integer doInBackground(Integer... params) {
					title = ContactLoader.loadContactRingtoneData(getContext(), Uri.parse(mRingtoneUri));
					return null;
				}
				
				@Override
				protected void onPostExecute(Integer result) {
					Log.e("liumx---","title:"+title);
					mRingtoneTv.setText(title);
				}
			}.execute();
        } else {
        	mRingtoneTv.setText(getContext().getResources().getString(R.string.mst_ringtone_default));
        }
    }

    private void updateView() {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

	@Override
	public void onClick(View v) {
		if (null != mPickRingtoneListener) {
			mPickRingtoneListener.onPickClick(mRingtoneTv, mState, mRingtoneUri);
		}
	}
	
	public interface onPickRingtoneListener {
		public void onPickClick(TextView view, RawContactDelta state, String ringtoneUri);
	}
	
	public void setOnPickRingtoneListener(onPickRingtoneListener pickRingtoneListener) {
		mPickRingtoneListener = pickRingtoneListener;
	}
	
	public abstract class SimpleAsynTask extends AsyncTask<Integer, Integer, Integer>{
		@Override
		protected abstract void onPostExecute(Integer result);
	}

}
