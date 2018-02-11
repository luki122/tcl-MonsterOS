package com.monster.appmanager.utils.dialog;

import java.util.ArrayList;
import java.util.List;

import com.monster.appmanager.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class SingleChoicAdapter<T> extends BaseAdapter implements OnItemClickListener {

	private List<T> mObjects = new ArrayList<T>();
	private int mSelectItem = 0;

	private LayoutInflater mInflater;
	private OnItemClickListener mListner;

	public SingleChoicAdapter(Context context) {
		init(context);
	}

	public SingleChoicAdapter(Context context, List<T> objects) {
		init(context);
		if (objects != null) {
			mObjects = objects;
		}
	}

	private void init(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void refreshData(List<T> objects) {
		if (objects != null) {
			mObjects = objects;
			setSelectItem(0);
		}
	}

	public void setSelectItem(int selectItem) {
		if (selectItem >= 0 && selectItem < mObjects.size()) {
			mSelectItem = selectItem;
			notifyDataSetChanged();
		}

	}

	public int getSelectItem() {
		return mSelectItem;
	}

	public void clear() {
		mObjects.clear();
		notifyDataSetChanged();
	}

	public int getCount() {
		return mObjects.size();
	}

	public T getItem(int position) {
		return mObjects.get(position);
	}

	public int getPosition(T item) {
		return mObjects.indexOf(item);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.dialog_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.mTextView = (TextView) convertView.findViewById(R.id.textView);
			viewHolder.mCheckBox = convertView.findViewById(R.id.checkBox);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (mSelectItem == position) {
			viewHolder.mCheckBox.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mCheckBox.setVisibility(View.INVISIBLE);
		}

		T item = getItem(position);
		if (item instanceof CharSequence) {
			viewHolder.mTextView.setText((CharSequence) item);
		} else {
			viewHolder.mTextView.setText(item.toString());
		}

		return convertView;
	}

	public static class ViewHolder {
		public TextView mTextView;
		public View mCheckBox;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mSelectItem = position;
		notifyDataSetChanged();

		if(getListner() != null){
			getListner().onItemClick(parent, view, position, id);
		}
	}

	public OnItemClickListener getListner() {
		return mListner;
	}

	public void setListner(OnItemClickListener mListner) {
		this.mListner = mListner;
	}
}
