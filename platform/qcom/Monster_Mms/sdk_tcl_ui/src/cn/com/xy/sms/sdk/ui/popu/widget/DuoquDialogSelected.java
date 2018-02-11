package cn.com.xy.sms.sdk.ui.popu.widget;

public class DuoquDialogSelected {
    private int mSelectIndex = -1;
    private String mSelectName = null;

    public int getSelectIndex() {
        return mSelectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        mSelectIndex = selectIndex;
        mSelectName = null;
    }

    public String getSelectName() {
        return mSelectName;
    }

    public void setSelectName(String selectName) {
        mSelectName = selectName;
        mSelectIndex = -1;
    }
}
