package com.monster.market.bean;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sandysheny on 16-11-8.
 */

public class AppDetailAnimInfo implements Parcelable {
   public static final int TYPE_DEFAULT = 0;
   public static final int TYPE_BOTTOM_SLIDE_IN = 1;

   private int layoutInitHeight;
   private int layoutMarginTop ;
   private int iconMarginLeft ;
   private int iconMarginTop ;
   private int initIconSize ;
   private int finalIconSize ;
   private Point coordinate;
   private boolean isDebug;
   private int type;


   public int getLayoutInitHeight() {
      return layoutInitHeight;
   }

   public AppDetailAnimInfo setLayoutInitHeight(int layoutInitHeight) {
      this.layoutInitHeight = layoutInitHeight;
      return this;
   }

   public int getLayoutMarginTop() {
      return layoutMarginTop;
   }

   public AppDetailAnimInfo setLayoutMarginTop(int layoutMarginTop) {
      this.layoutMarginTop = layoutMarginTop;
      return this;
   }

   public int getIconMarginLeft() {
      return iconMarginLeft;
   }

   public AppDetailAnimInfo setIconMarginLeft(int iconMarginLeft) {
      this.iconMarginLeft = iconMarginLeft;
      return this;
   }

   public int getIconMarginTop() {
      return iconMarginTop;
   }

   public AppDetailAnimInfo setIconMarginTop(int iconMarginTop) {
      this.iconMarginTop = iconMarginTop;
      return this;
   }

   public int getInitIconSize() {
      return initIconSize;
   }

   public AppDetailAnimInfo setInitIconSize(int initIconSize) {
      this.initIconSize = initIconSize;
      return this;
   }

   public int getFinalIconSize() {
      return finalIconSize;
   }

   public AppDetailAnimInfo setFinalIconSize(int finalIconSize) {
      this.finalIconSize = finalIconSize;
      return this;
   }

   public Point getCoordinate() {
      return coordinate;
   }

   public AppDetailAnimInfo setCoordinate(Point coordinate) {
      this.coordinate = coordinate;
      return this;
   }

   public boolean isDebug() {
      return isDebug;
   }

   public void setDebug(boolean debug) {
      isDebug = debug;
   }

   public int getType() {
      return type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public AppDetailAnimInfo() {
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(this.layoutInitHeight);
      dest.writeInt(this.layoutMarginTop);
      dest.writeInt(this.iconMarginLeft);
      dest.writeInt(this.iconMarginTop);
      dest.writeInt(this.initIconSize);
      dest.writeInt(this.finalIconSize);
      dest.writeParcelable(this.coordinate, flags);
      dest.writeByte(this.isDebug ? (byte) 1 : (byte) 0);
      dest.writeInt(this.type);
   }

   protected AppDetailAnimInfo(Parcel in) {
      this.layoutInitHeight = in.readInt();
      this.layoutMarginTop = in.readInt();
      this.iconMarginLeft = in.readInt();
      this.iconMarginTop = in.readInt();
      this.initIconSize = in.readInt();
      this.finalIconSize = in.readInt();
      this.coordinate = in.readParcelable(Point.class.getClassLoader());
      this.isDebug = in.readByte() != 0;
      this.type = in.readInt();
   }

   public static final Creator<AppDetailAnimInfo> CREATOR = new Creator<AppDetailAnimInfo>() {
      @Override
      public AppDetailAnimInfo createFromParcel(Parcel source) {
         return new AppDetailAnimInfo(source);
      }

      @Override
      public AppDetailAnimInfo[] newArray(int size) {
         return new AppDetailAnimInfo[size];
      }
   };
}
