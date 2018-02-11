package com.monster.appmanager.viewhelp;

import com.monster.appmanager.R;
import com.monster.appmanager.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import mst.widget.recycleview.RecyclerView.LayoutParams;

public class ButtonPreference extends Preference {
	private View mRootView;
	private Button button;
	public Button getButton() {
		return button;
	}

	public void setButton(Button button) {
		this.button = button;
	}

	public ButtonPreference(Context context) {
		super(context);
	}
	
	public ButtonPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Preference, 0, 0);
        int layoutResource = a.getResourceId(com.android.internal.R.styleable.Preference_layout,
                0);
        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        View container = LayoutInflater.from(getContext())
                .inflate(R.layout.preference_container, null, false);
        // Need to create view now so that findViewById can be called immediately.
        final View view = LayoutInflater.from(getContext())
                .inflate(layoutResource, (ViewGroup)container, false);

        final ViewGroup allDetails = (ViewGroup) view.findViewById(R.id.all_details);
        if (allDetails != null) {
            Utils.forceCustomPadding(allDetails, true /* additive padding */);
        }
        mRootView = view;
        button = (Button)view.findViewById(R.id.clear_data_button);
        setShouldDisableView(false);
	}

	public ButtonPreference(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public ButtonPreference(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		return mRootView;
	}
	
	public View findViewById(int id) {
		return mRootView.findViewById(id);
	}
}
