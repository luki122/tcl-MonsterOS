/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.filemanager.R;

public class PopDialogFragment{
    public static String SORT_ITEM = "sort_item";
    public static String VIEW_ITEM = "view_item";
    public static String TYPE = "type";
    public List<String> textAdapter = new ArrayList<String>();
    public static AlertDialog createSingleChoice(Context context, String title, int choiceItem,List<String> itemAdapter,
            DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        SimPickerAdapter simAdapter = new SimPickerAdapter(context,itemAdapter);
        simAdapter.setSingleChoice(true);
        simAdapter.setSingleChoiceIndex(choiceItem);
        builder.setSingleChoiceItems(simAdapter, -1, listener).setTitle(title);
        return builder.create();
    }

    private static class SimPickerAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        boolean mSingleChoice;
        int mSingleChoiceIndex;
        List<String> mItemAdapter;
        public SimPickerAdapter(Context context,List<String> itemAdapter){
            mInflater = LayoutInflater.from(context);
            mItemAdapter = itemAdapter;
        }

        public int getCount() {
            return mItemAdapter.size();
        }

        public void setSingleChoice(boolean singleChoice) {
            mSingleChoice = singleChoice;
        }

        @SuppressWarnings("unused")
        public boolean getSingleChoice() {
            return mSingleChoice;
        }

        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }

        public void setSingleChoiceIndex(int singleChoiceIndex) {
            mSingleChoiceIndex = singleChoiceIndex;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            SingleHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.select_dialog_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new SingleHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.mRadioButton = (RadioButton)convertView.findViewById(R.id.select);
                convertView.setTag(holder);
            } else {
                 //Get the ViewHolder back to get fast access to the TextView
                 //and the ImageView.
                holder = (SingleHolder) convertView.getTag();
            }
            holder.mRadioButton.setVisibility(View.VISIBLE);
            if (position == mSingleChoiceIndex) {
                holder.mRadioButton.setChecked(true);
            } else {
                holder.mRadioButton.setChecked(false);
            }
            // Bind the data efficiently with the holder.
            holder.text.setText(mItemAdapter.get(position));
            return convertView;
        }

        }
    static class SingleHolder {
        TextView text;
        RadioButton mRadioButton;
    }
}
