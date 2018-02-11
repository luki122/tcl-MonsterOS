/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.monster.appmanager.applications;

import java.util.ArrayList;
import java.util.List;

import android.annotation.ArrayRes;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference;
import android.preference.Preference.BaseSavedState;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.monster.appmanager.R;
import com.monster.appmanager.Utils;
import com.monster.appmanager.widget.DefaultAppListItem;
import com.monster.appmanager.AppListPreference.AppArrayAdapter;

public class LayoutGridViewPreference extends Preference implements OnItemClickListener {

    private DefaultAppListItem mRootView;
    private GridView gridView;
    private TextView titleTextView;
    private String lastPackageName;

    public LayoutGridViewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSelectable(false);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Preference, 0, 0);
        int layoutResource = R.layout.grid_view_panel;
        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        // Need to create view now so that findViewById can be called immediately.
        final View view = LayoutInflater.from(getContext())
                .inflate(layoutResource, null, false);

        final ViewGroup allDetails = (ViewGroup) view.findViewById(R.id.all_details);
        if (allDetails != null) {
            Utils.forceCustomPadding(allDetails, true /* additive padding */);
        }
        mRootView = (DefaultAppListItem)view;
        gridView = (GridView)view.findViewById(R.id.app_gridview);
        gridView.setOnItemClickListener(this);
        titleTextView = (TextView) findViewById(R.id.app_title);
        setShouldDisableView(false);
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if(mEntries.length>1){
			if(mShowItemNone && position == 0 
					&& (this instanceof DefaultSmsPreference 
							|| this instanceof DefaultPhonePreference
							|| this instanceof DefaultImePreference)) {
				return;
			}
			invalidSelectedView(view);
			String value = mEntryValues[position].toString();
            if (callChangeListener(value)) {
            }
            setValue(value);
		}
	}
	
    @Override
    protected View onCreateView(ViewGroup parent) {
        return mRootView;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        initAdapter();
//        PackageManager pm = getContext().getPackageManager();
//		try {
//			ApplicationInfo appInfo = pm.getApplicationInfo(getValue(), 0);
//			titleTextView.setText(getTitle()+"("+appInfo.loadLabel(pm)+")");            
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}		
		
		int selectedIndex = findIndexOfValue(getValue());
		if(selectedIndex<0){
			selectedIndex =0;
		}
		titleTextView.setText(getTitle()+"("+getEntries()[selectedIndex]+")");            
		
		initSelectedView();
    }
    
    private Handler setAdapterHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		if(mEntries!=null && mEntryDrawables != null){
            	gridView.setAdapter(createListAdapter());
            	LayoutParams lp = gridView.getLayoutParams();
            	int numColumns = gridView.getNumColumns();
//                lp.height = ((mEntries.length+numColumns-1)/numColumns)*getContext().getResources().getDimensionPixelSize(R.dimen.default_app_height);
            	boolean leftCount = mEntries.length % numColumns != 0;
            	int lineCount = mEntries.length / numColumns + (leftCount ? 1 : 0);
                lp.height = getContext().getResources().getDimensionPixelSize(R.dimen.default_app_height) * lineCount  + gridView.getVerticalSpacing() * (lineCount - 1);
                
                setAdapterHandler.post(new Runnable() {
					@Override
					public void run() {
						initSelectedView();
					}
				});
    		}
    	};
    };

    public View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    public static final String ITEM_NONE_VALUE = "";

    private Drawable[] mEntryDrawables;
    private boolean mShowItemNone = true;

    public class AppArrayAdapter extends ArrayAdapter<CharSequence> {
        private Drawable[] mImageDrawables = null;
        private int mSelectedIndex = 0;

        public AppArrayAdapter(Context context, int textViewResourceId,
                CharSequence[] objects, Drawable[] imageDrawables, int selectedIndex) {
            super(context, textViewResourceId, objects);
            mSelectedIndex = selectedIndex;
            mImageDrawables = imageDrawables;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            View view = inflater.inflate(R.layout.app_preference_item, parent, false);
            /*TextView textView = (TextView) view.findViewById(R.id.app_label);
            textView.setText(getItem(position));*/
            /*if (position == mSelectedIndex) {
                view.findViewById(R.id.default_label).setVisibility(View.VISIBLE);
            }*/
            ImageView imageView = (ImageView)view.findViewById(R.id.app_image);
            imageView.setImageDrawable(mImageDrawables[position]);
            return view;
        }
    }

    public void setShowItemNone(boolean showItemNone) {
        mShowItemNone = showItemNone;
    }
    
    public boolean isShowItemNone() {
    	return mShowItemNone;
    }

    public void setPackageNames(CharSequence[] packageNames, CharSequence defaultPackageName) {
        // Look up all package names in PackageManager. Skip ones we can't find.
        PackageManager pm = getContext().getPackageManager();
        final int entryCount = packageNames.length + (mShowItemNone ? 1 : 0);
        List<CharSequence> applicationNames = new ArrayList<>(entryCount);
        List<CharSequence> validatedPackageNames = new ArrayList<>(entryCount);
        List<Drawable> entryDrawables = new ArrayList<>(entryCount);
        int selectedIndex = -1;
        
        if (mShowItemNone) {
            applicationNames.add(
                    getContext().getResources().getText(R.string.app_list_preference_none));
            validatedPackageNames.add(ITEM_NONE_VALUE);
            entryDrawables.add(getContext().getDrawable(R.drawable.default_app_none));
        }
        
        for (int i = 0; i < packageNames.length; i++) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageNames[i].toString(), 0);
                applicationNames.add(appInfo.loadLabel(pm));
                validatedPackageNames.add(appInfo.packageName);
                entryDrawables.add(appInfo.loadIcon(pm));
                if (defaultPackageName != null &&
                        appInfo.packageName.contentEquals(defaultPackageName)) {
                    selectedIndex = mShowItemNone ? (i + 1) : i;
                }
            } catch (NameNotFoundException e) {
                // Skip unknown packages.
            }
        }


        setEntries(applicationNames.toArray(new CharSequence[applicationNames.size()]));
        setEntryValues(validatedPackageNames.toArray(new CharSequence[validatedPackageNames.size()]));
        mEntryDrawables = entryDrawables.toArray(new Drawable[entryDrawables.size()]);

        if (selectedIndex != -1) {
            setValueIndex(selectedIndex);
        } else {
            setValue(null);
        }
    }
    
    public void initAdapter(){
    	setAdapterHandler.sendEmptyMessage(0);
    }
       
    public void setValue(String value) {
    	setValueSuper(value);    	
    }
   
    protected ListAdapter createListAdapter() {
        final String selectedValue = getValue();
        final boolean selectedNone = selectedValue == null ||
                (mShowItemNone && selectedValue.contentEquals(ITEM_NONE_VALUE));
        int selectedIndex = selectedNone ? -1 : findIndexOfValue(selectedValue);
        return new AppArrayAdapter(getContext(),
            R.layout.app_preference_item, getEntries(), mEntryDrawables, selectedIndex);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(getEntryValues(), getValue(), mShowItemNone, superState);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mShowItemNone = savedState.showItemNone;
            setPackageNames(savedState.entryValues, savedState.value);
            super.onRestoreInstanceState(savedState.superState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private static class SavedState implements Parcelable {

        public final CharSequence[] entryValues;
        public final CharSequence value;
        public final boolean showItemNone;
        public final Parcelable superState;

        public SavedState(CharSequence[] entryValues, CharSequence value, boolean showItemNone,
                Parcelable superState) {
            this.entryValues = entryValues;
            this.value = value;
            this.showItemNone = showItemNone;
            this.superState = superState;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeCharSequenceArray(entryValues);
            dest.writeCharSequence(value);
            dest.writeInt(showItemNone ? 1 : 0);
            dest.writeParcelable(superState, flags);
        }

        public static Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                CharSequence[] entryValues = source.readCharSequenceArray();
                CharSequence value = source.readCharSequence();
                boolean showItemNone = source.readInt() != 0;
                Parcelable superState = source.readParcelable(getClass().getClassLoader());
                return new SavedState(entryValues, value, showItemNone, superState);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
    //add by luolaigang 
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mValue;
    /**
     * Sets the human-readable entries to be shown in the list. This will be
     * shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in
     * {@link #setEntryValues(CharSequence[])}.
     * 
     * @param entries The entries.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }
    
    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     * 
     * @return The list as an array.
     */
    public CharSequence[] getEntries() {
        return mEntries;
    }
    
    /**
     * The array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     * 
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * @see #setEntryValues(CharSequence[])
     * @param entryValuesResId The entry values array as a resource.
     */
    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }
    
    /**
     * Returns the array of values to be saved for the preference.
     * 
     * @return The array of values.
     */
    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    /**
     * Sets the value of the key. This should be one of the entries in
     * {@link #getEntryValues()}.
     * 
     * @param value The value to set for the key.
     */
    public void setValueSuper(String value) {
        // Always persist/notify the first time.
        final boolean changed = !TextUtils.equals(mValue, value);
        if (changed) {
        	lastPackageName = mValue;
            mValue = value;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    /**
     * Sets the value to the given index from the entry values.
     * 
     * @param index The index of the value to set.
     */
    public void setValueIndex(int index) {
        if (mEntryValues != null && index < mEntries.length) {
            setValue(mEntryValues[index].toString());
        }
    }
    
    /**
     * Returns the value of the key. This should be one of the entries in
     * {@link #getEntryValues()}.
     * 
     * @return The value of the key.
     */
    public String getValue() {
        return mValue; 
    }
    
    /**
     * Returns the entry corresponding to the current value.
     * 
     * @return The entry corresponding to the current value, or null.
     */
    public CharSequence getEntry() {
        int index = getValueIndex();
        return index >= 0 && mEntries != null ? mEntries[index] : null;
    }
    
    /**
     * Returns the index of the given value (in the entry values array).
     * 
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedString(mValue) : (String) defaultValue);
    }
    
    private void initSelectedView(){
    	int selectedIndex = findIndexOfValue(getValue());
    	if(selectedIndex<0){
    		selectedIndex =0;
    	}
    	View child= gridView.getChildAt(selectedIndex);
    	if(child!=null){
    		invalidSelectedView(child);
    	}
    }
    
	/**
	 * 刷新选中提示
	 * @param view
	 */
	private void invalidSelectedView(View view) {
		int left = gridView.getLeft() + view.getRight();
		int top = gridView.getTop() + view.getTop();
		mRootView.invalidSelectedView(left, top);
	}

	public String getLastPackageName() {
		return lastPackageName;
	}

	public void setLastPackageName(String lastPackageName) {
		this.lastPackageName = lastPackageName;
	}
}
