package com.monster.cloud.adpater;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.activity.sms.CallBack;
import com.monster.cloud.bean.SmsByContact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mst.app.dialog.ProgressDialog;

/**
 * Created by zouxu on 16-11-28.
 */
public class SmsByContactAdapter extends BaseAdapter {

    private Context mContext;
    private List<SmsByContact> mDataList = new ArrayList<SmsByContact>();
    private LayoutInflater inflater;
    private CallBack callback;

    public SmsByContactAdapter(Context context,CallBack m_callback){
        mContext = context;
        inflater = LayoutInflater.from(context);
        callback = m_callback;
    }

    public void updateData(List<SmsByContact> list){
        if(list!=null){
            mDataList.clear();
            mDataList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void selectAll(boolean select){
        for(int i=0;i<mDataList.size();i++){
            mDataList.get(i).is_select = select;
        }
        notifyDataSetChanged();
    }

    public void click(int i){
        if(i>=mDataList.size()){
            return;
        }

        mDataList.get(i).is_select = !mDataList.get(i).is_select;
        notifyDataSetChanged();
    }

    public  List<SmsByContact> getDataList(){
        return mDataList;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        final SmsByContact data = mDataList.get(i);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.sms_by_contact_item, parent, false);
            viewHolder.main_item = (RelativeLayout)convertView.findViewById(R.id.main_item);
            viewHolder.select_box = (CheckBox)convertView.findViewById(R.id.select_box);
            viewHolder.contact_and_count = (TextView)convertView.findViewById(R.id.contact_and_count);
            viewHolder.sms_info = (TextView)convertView.findViewById(R.id.sms_info);
            viewHolder.date = (TextView)convertView.findViewById(R.id.date);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.select_box.setChecked(data.is_select);
        viewHolder.main_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.is_select = !data.is_select;
                notifyDataSetChanged();
                callback.callBack();
            }
        });
        if(TextUtils.isEmpty(data.sms.name)){

            viewHolder.contact_and_count.setText(data.sms.number+"("+data.sms.num+")");
        } else {
            viewHolder.contact_and_count.setText(data.sms.name+"("+data.sms.num+")");
        }

        Date m_date = new Date(data.sms.sendTime);


//        Time mTime = new Time();
//        mTime.setJulianDay(data.sms.sendTime*1000);
//        Date mDate = new Date(mTime.toMillis(true));
        Date mDate = new Date(data.sms.sendTime*1000);
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
        String sDateTime = sdf.format(mDate);

        viewHolder.date.setText(sDateTime);

        viewHolder.sms_info.setText(data.sms.summary);

        return convertView;
    }

    @Override
    public Object getItem(int i) {
        return mDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    class ViewHolder{
        RelativeLayout main_item;
        CheckBox select_box;
        TextView contact_and_count;
        TextView date;
        TextView sms_info;
    }

}
