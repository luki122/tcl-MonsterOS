package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONArray;

public class DuoquTrainNumsDataSource implements DuoquSource {

    private JSONArray mDataSource = null;

    public DuoquTrainNumsDataSource(JSONArray dataSource) {
        mDataSource = dataSource;
    }

    @Override
    public int getLength() {
        return mDataSource == null ? 0 : mDataSource.length();
    }

    @Override
    public Object getValue(int index) {
        if (mDataSource == null || mDataSource.length() <= index) {
            return null;
        }

        return mDataSource.opt(index);
    }
}
