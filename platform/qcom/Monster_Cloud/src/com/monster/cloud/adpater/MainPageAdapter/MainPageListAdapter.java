package com.monster.cloud.adpater.MainPageAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.constants.Constant;
import com.monster.cloud.preferences.FilePreferences;
import com.monster.cloud.preferences.Preferences;
import com.monster.cloud.sync.BaseSyncTask;
import com.monster.cloud.utils.SyncTimeUtil;

import java.util.ArrayList;

/**
 * Created by yubai on 16-12-2.
 */
public class MainPageListAdapter extends BaseAdapter {

    private FilePreferences preferences;
    private SharedPreferences sharedPreferences;

    private ArrayList<ListEntity> list;
    private ArrayList<Integer> hiddenPositions = new ArrayList<>();

    private Context context;
    private ImageView imageView;
    private TextView name, time;
    private TextView label, percent;
    private ProgressBar progressBar;

    private boolean isUpdating;

    private String neverSync;
    private String lastSync;
    private String neverBackup;
    private String lastBackup;

    public MainPageListAdapter(Context context, ArrayList<ListEntity> list) {
        this.context = context;
        this.list = list;

        neverSync = context.getResources().getString(R.string.never_sync);
        lastSync = context.getResources().getString(R.string.last_sync);
        neverBackup = context.getResources().getString(R.string.never_backup);
        lastBackup = context.getResources().getString(R.string.last_backup);

        //listen to the sharedPreference
        preferences = (FilePreferences) Preferences.Factory.getInstance(context, Constant.FILE_TYPE);
        sharedPreferences = preferences.getSharedPreferences();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.main_page_item, null);
        }

        //magic trick
        for (int i = 0; i < hiddenPositions.size(); ++i) {
            if (hiddenPositions.get(i) <= position) {
                position += 1;
            }
        }

        imageView = (ImageView) convertView.findViewById(R.id.icon);
        name = (TextView) convertView.findViewById(R.id.name);
        percent = (TextView) convertView.findViewById(R.id.percent);
        percent.setText(list.get(position).progress == 0 ? "等待备份" : "正在备份" + list.get(position).progress + "%");
        progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
        progressBar.setProgress(list.get(position).progress);
        label = (TextView) convertView.findViewById(R.id.switch_label);

        time = (TextView) convertView.findViewById(R.id.time);
        long lastSyncTime = 0;
        boolean isAutoOn = false;
        switch (list.get(position).tag) {
            case BaseSyncTask.TASK_TYPE_SYNC_CONTACT:
                imageView.setImageResource(R.drawable.contact);
                name.setText(R.string.contact);
                lastSyncTime = SyncTimeUtil.getContactSyncTime(context);
                isAutoOn = SyncTimeUtil.getContactSyncLabel(context);
                break;
            case BaseSyncTask.TASK_TYPE_SYNC_SMS:
                imageView.setImageResource(R.drawable.message);
                name.setText(R.string.message);
                lastSyncTime = SyncTimeUtil.getSmsSyncTime(context);
                isAutoOn = SyncTimeUtil.getSmsSyncLabel(context);
                break;
            case BaseSyncTask.TASK_TYPE_SYNC_CALLLOG:
                imageView.setImageResource(R.drawable.record);
                name.setText(R.string.call_log);
                lastSyncTime = SyncTimeUtil.getRecordSyncTime(context);
                isAutoOn = SyncTimeUtil.getRecordSyncLabel(context);
                break;
            case BaseSyncTask.TASK_TYPE_SYNC_SOFT:
                imageView.setImageResource(R.drawable.app_list);
                name.setText(R.string.app_list);
                lastSyncTime = SyncTimeUtil.getAppListSyncTime(context);
                isAutoOn = SyncTimeUtil.getAppListSyncLabel(context);
                break;
        }
        label.setText(isAutoOn ? R.string.auto_sync_on : R.string.auto_sync_off);

        switch (list.get(position).tag) {
            case BaseSyncTask.TASK_TYPE_SYNC_CONTACT:
                if (lastSyncTime == 0) {
                    time.setText(neverSync);
                } else {
                    time.setText(lastSync + SyncTimeUtil.setTime(lastSyncTime, context));
                }
                break;
            default:
                if (lastSyncTime == 0) {
                    time.setText(neverBackup);
                } else {
                    time.setText(lastBackup + SyncTimeUtil.setTime(lastSyncTime, context));
                }
                break;
        }

        if (isUpdating) {
            label.setVisibility(View.INVISIBLE);
            time.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            percent.setVisibility(View.VISIBLE);
        } else {
            label.setVisibility(View.VISIBLE);
            time.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            percent.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size() - hiddenPositions.size();
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    public void updateLabels() {
        deleteHiddenPositions();
        if (!SyncTimeUtil.getContactSyncLabel(context)) {
            hiddenPositions.add(0);
        }

        if (!SyncTimeUtil.getSmsSyncLabel(context)) {
            hiddenPositions.add(1);
        }

        if (!SyncTimeUtil.getRecordSyncLabel(context)) {
            hiddenPositions.add(2);
        }

        if (!SyncTimeUtil.getAppListSyncLabel(context)) {
            hiddenPositions.add(3);
        }
    }

    public void deleteHiddenPositions() {
        hiddenPositions.clear();
    }

}
