package com.mst.wallpaper.object;

import java.io.Serializable;

import android.graphics.Bitmap;

public class WallpaperImageInfo implements Cloneable, Serializable {
    private long id;
    private String title;
    private Bitmap samllIcon;
    private Object bigIcon;
    private String identify;
    private int belongGroup;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getSamllIcon() {
        return samllIcon;
    }

    public void setSamllIcon(Bitmap samllIcon) {
        this.samllIcon = samllIcon;
    }

    public Object getBigIcon() {
        return bigIcon;
    }

    public void setBigIcon(Object bigIcon) {
        this.bigIcon = bigIcon;
    }

    public String getIdentify() {
        return identify;
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }

    public int getBelongGroup() {
        return belongGroup;
    }

    public void setBelongGroup(int belongGroup) {
        this.belongGroup = belongGroup;
    }

    public OnMountListener mountListener;

    public void setOnMountListener(OnMountListener listener) {
        mountListener = listener;
    }

    @Override
    public String toString() {
        return "PictureInfo [id=" + id + ", title=" + title + ", samllIcon=" + samllIcon + ", bigIcon="
                + bigIcon + ", identify=" + identify + ", belongGroup=" + belongGroup + ", mountListener="
                + mountListener + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public WallpaperImageInfo clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
    	WallpaperImageInfo pictureInfo = null;
        pictureInfo = (WallpaperImageInfo) super.clone();
        if (pictureInfo == null) {
            pictureInfo = this.clone();
        }
        return pictureInfo;
    }

    public interface OnMountListener {
        public void onMount();

        public void onUnMount();
    }

}
