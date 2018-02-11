package cn.tcl.music.model;

public class ClosedTimeBean {
    private String mTime;
    private boolean mIsSelect;

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        this.mTime = time;
    }

    public boolean isSelect() {
        return mIsSelect;
    }

    public void setSelect(boolean select) {
        mIsSelect = select;
    }
}
