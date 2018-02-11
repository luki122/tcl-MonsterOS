package cn.tcl.music.model;

public class QualityBean {
    private String mItemTitle;
    private String mItemSubTitle;
    private boolean mSelectedItem;
    private boolean mSVIPItem;

    public String getItemTitle() {
        return mItemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.mItemTitle = itemTitle;
    }

    public String getItemSubTitle() {
        return mItemSubTitle;
    }

    public void setItemSubTitle(String itemSubTitle) {
        this.mItemSubTitle = itemSubTitle;
    }

    public boolean isSelectedItem() {
        return mSelectedItem;
    }

    public void setSelectedItem(boolean selectedItem) {
        this.mSelectedItem = selectedItem;
    }

    public boolean ismSVIPItem() {
        return mSVIPItem;
    }

    public void setSVIPItem(boolean mSVIPItem) {
        this.mSVIPItem = mSVIPItem;
    }
}
