package com.monster.netmanage.view;

import com.monster.netmanage.R;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import mst.preference.EditTextPreference;

/**
 * 
 * @author zhaolaichao
 *
 */
public class EditDataPreference extends EditTextPreference {

	private View mView;
	private TextView mTvTitle;
	private EditText mEt;
	private String mTitle;
	private String mEtValue;
	/**
	 * 监听输入
	 */
	private IEtChangeListener mEtChangeListener;
	
	public EditDataPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public EditDataPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EditDataPreference(Context context) {
		this(context, null);
	}

	public EditDataPreference(Context context, AttributeSet attrs, int defStyleAttr, int arg3) {
		super(context, attrs, defStyleAttr, arg3);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		mView = LayoutInflater.from(getContext()).inflate(R.layout.lay_edit_preference, parent, false);
		initView();
		return mView;
	}
	
	private void initView() {
		mTvTitle = (TextView) mView.findViewById(R.id.tv_dataplan_free);
		mEt = (EditText) mView.findViewById(R.id.edt_dataplan_total_free);
		if (null != mTvTitle) {
			mTvTitle.setText(mTitle);
		}
		if (null != mEt) {
			mEt.setSingleLine();
			mEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			mEt.setText(mEtValue);
			if (mEt.isFocused()) {
				mEt.selectAll();
			}
			mEt.addTextChangedListener(textWatcher);
			mEt.setOnFocusChangeListener(focusChangeListener);
			mEt.setOnEditorActionListener(actionListener);
			mEt.performClick();
		}
		Log.v("mEt", "mEtValue>>" + mEtValue);
	}
	
	public void setItemTitle(String title) {
		mTitle = title;
		notifyChanged();
	}
	
	public void setEtValue(String etValue) {
		mEtValue = etValue;
		notifyChanged();
	}
	
	public String getEtValue() {
		return mEtValue;
	}
	
	public TextWatcher textWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mEtChangeListener.onTextChanged(s, start, before, count);
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			mEtChangeListener.beforeTextChanged(s, start, count, after);
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if (null != s) {
				mEtValue = s.toString();
			}
			mEtChangeListener.afterTextChanged(s);
		}
	};
	
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				((EditText) v).selectAll();
			}
		}
	};
	
	private OnEditorActionListener actionListener = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			 /*判断是否是“GO”键*/  
            if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED){  
                /*隐藏软键盘*/  
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);  
                if (imm.isActive()) {  
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);  
                }  
                return true;  
            }  
			return false;
		}
	};
	
	public void setEtChangeListener(IEtChangeListener changeListener) {
		mEtChangeListener = changeListener;
	}
	
	public interface IEtChangeListener {
		void onTextChanged(CharSequence s, int start, int before, int count);
		void beforeTextChanged(CharSequence s, int start, int count, int after);
		void afterTextChanged(Editable s);
	}
}
