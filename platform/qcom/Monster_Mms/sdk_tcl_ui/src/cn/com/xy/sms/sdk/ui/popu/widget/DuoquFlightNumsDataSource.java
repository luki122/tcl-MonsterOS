package cn.com.xy.sms.sdk.ui.popu.widget;

public class DuoquFlightNumsDataSource implements DuoquSource {

    private String[] mDataSource = null;

    public DuoquFlightNumsDataSource(String[] dataSource) {
        if (dataSource != null) {
            mDataSource = dataSource.clone();
        }
    }

    @Override
    public int getLength() {
        return mDataSource == null ? 0 : mDataSource.length;
    }

    @Override
    public Object getValue(int index) {
        if (mDataSource == null || mDataSource.length <= index) {
            return null;
        }
        return mDataSource[index];
    }
}
