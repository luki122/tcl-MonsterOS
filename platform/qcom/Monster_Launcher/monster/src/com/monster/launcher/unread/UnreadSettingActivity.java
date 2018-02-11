package com.monster.launcher.unread;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.monster.launcher.R;
import com.monster.launcher.Utilities;
import com.monster.launcher.compat.UserHandleCompat;
import com.monster.launcher.theme.utils.PhotoUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lj on 16-9-11.
 */
public class UnreadSettingActivity extends Activity implements MonsterUnreadLoader.UnreadUpdateListener{

    private ListView mList = null;
    UnreadListAdapter mAdapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unread_setting);
        mList = (ListView) findViewById(R.id.unread_list);
        mAdapter = new UnreadListAdapter(this);
        mList.setAdapter(mAdapter);
//        MonsterUnreadLoader.addListener(this);
    }

    @Override
    public void onUnreadPackageChange() {
        mAdapter.updateData();
    }

    class UnreadListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private Context mContext;
        ArrayList<UnreadSupportShortcut> unReadAllListDatas = new ArrayList<>();
        ArrayList<UnreadSupportShortcut> unReadEnableListDatas = new ArrayList<>();
        public UnreadListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            updateData();
        }

        @Override
        public int getCount() {
            return unReadAllListDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return unReadAllListDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.unread_setting_list_item, null);
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.unread_item_icon);
                viewHolder.title = (TextView) convertView.findViewById(R.id.unread_item_text);
                viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.unread_item_checkbox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.icon.setImageBitmap(unReadAllListDatas.get(position).icon);
            viewHolder.title.setText(unReadAllListDatas.get(position).component.getPackageName());
            viewHolder.checkbox.setChecked(isEnableUnreadShortcut(unReadAllListDatas.get(position)));
            return convertView;
        }

        private void updateData(){
            unReadAllListDatas.clear();
            SharedPreferences sp = mContext.getSharedPreferences(MonsterUnreadLoader.UNREAD_SETTINGS_AllLIST_PREFERENCE, Context.MODE_PRIVATE);
            synchronized (this) {
                Set<String> strings = sp.getStringSet(MonsterUnreadLoader.UNREAD_SETTINGS_APPS, null);
                if (strings != null) {
                    Iterator<String> iterator = strings.iterator();
                    while (iterator.hasNext()) {
                        String component = iterator.next();
                        UnreadSupportShortcut uss = getUnreadShortcut(component);
                        if(uss != null) unReadAllListDatas.add(uss);
                    }
                }
            }

            unReadEnableListDatas.clear();
            SharedPreferences sp2 = mContext.getSharedPreferences(MonsterUnreadLoader.UNREAD_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
            synchronized (this) {
                Set<String> strings = sp2.getStringSet(MonsterUnreadLoader.UNREAD_SETTINGS_APPS, null);
                if (strings != null) {
                    Iterator<String> iterator = strings.iterator();
                    while (iterator.hasNext()) {
                        String component = iterator.next();
                        UnreadSupportShortcut uss = getUnreadShortcut(component);
                        if(uss != null) unReadEnableListDatas.add(uss);
                    }
                }
            }
            notifyDataSetChanged();
        }

        boolean isEnableUnreadShortcut(UnreadSupportShortcut uss){
            if(unReadEnableListDatas == null || unReadEnableListDatas.size()<=0)return false;
            for(UnreadSupportShortcut us : unReadEnableListDatas){
                if(uss.component.getPackageName().equals(us.component.getPackageName())
                        && uss.component.getClassName().equals(us.component.getClassName())){
                    return true;
                }
            }
            return false;
        }

        private UnreadSupportShortcut getUnreadShortcut(String component){
            String[] ss = component.split("__");
            if(ss.length < 2)return null;
            String pkg = ss[0];
            String cls = ss[1];
//            String user = ss[2];//lijun here need
//            UserHandle us = null;//
            Bitmap icon = Utilities.getAppIcon(mContext,pkg, UserHandleCompat.myUserHandle());
            if(icon == null){
                icon = PhotoUtils.drawable2bitmap(mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon));
            }
            return new UnreadSupportShortcut(pkg,cls,
                    icon,
                    MonsterUnreadLoader.UNREAD_TYPE_NORMAL,UserHandleCompat.myUserHandle().getUser());
        }

        class ViewHolder {
            ImageView icon;
            TextView title;
            CheckBox checkbox;
        }
    }
}
