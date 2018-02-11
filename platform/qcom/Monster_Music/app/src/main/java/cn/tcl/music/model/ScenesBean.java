package cn.tcl.music.model;

import java.io.Serializable;

public class ScenesBean implements Serializable {

    private long mScenesId;
    private String mScenesText;
    private int mScenesIcon;
    private int mScenesImage;

    public long getScenesId() {
        return mScenesId;
    }

    public void setScenesId(long scenesId) {
        mScenesId = scenesId;
    }

    public String getScenesText() {
        return mScenesText;
    }

    public void setScenesText(String scenesText) {
        mScenesText = scenesText;
    }

    public int getScenesIcon() {
        return mScenesIcon;
    }

    public void setScenesIcon(int scenesIcon) {
        mScenesIcon = scenesIcon;
    }

    public int getScenesImage() {
        return mScenesImage;
    }

    public void setScenesImage(int scenesImage) {
        mScenesImage = scenesImage;
    }

    @Override
    public String toString() {
        return "ScenesBean{" +
                "mScenesId=" + mScenesId +
                ", mScenesText='" + mScenesText + '\'' +
                ", mScenesIcon=" + mScenesIcon +
                ", mScenesImage=" + mScenesImage +
                '}';
    }
}
