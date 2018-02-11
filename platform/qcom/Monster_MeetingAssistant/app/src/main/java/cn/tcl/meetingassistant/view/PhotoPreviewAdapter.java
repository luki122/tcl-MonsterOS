/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import cn.tcl.meetingassistant.utils.ImageLoader;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-19
 * the PhotoPreviewAdaptor
 */
public class PhotoPreviewAdapter extends PagerAdapter {

    List<File> files;
    private Context mContext;

    public PhotoPreviewAdapter(Context context) {
        this.mContext = context;
    }


    public void setFiles(List<File> files) {
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        String path = files.get(position).getAbsolutePath();
        Bitmap bitmap    = compressImageFromFile(path);
        ImageView imageView = new ImageView(mContext);

        imageView.setImageBitmap(bitmap);
        imageView.setTag(bitmap);
        container.addView(imageView);
        return imageView;


    }
    private Bitmap compressImageFromFile(String srcPath){

        BitmapFactory.Options newOptions = new BitmapFactory.Options();

        newOptions.inJustDecodeBounds = true;
        Bitmap bitmap ;

        newOptions.inJustDecodeBounds =false;
        int w = newOptions.outWidth;
        int h =newOptions.outWidth;
        float hh = 800f;
        float ww =480f;
        int be = 1;
        if (w >h && w >ww){
            be =(int)(newOptions.outWidth/ww);

        }else  if (w <h && h >hh){
            be = (int)(newOptions.outHeight/hh);
        }
        if(be<=0)
            be = 1;
        newOptions.inSampleSize = be;
        newOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        newOptions.inPurgeable =true;
        newOptions.inInputShareable =true;
        bitmap = BitmapFactory.decodeFile(srcPath,newOptions);

        return  bitmap;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageView imageView = (ImageView) object;
        container.removeView((View) object);
        if(imageView.getTag() instanceof Bitmap){
            Bitmap bitmap = (Bitmap) imageView.getTag();
            bitmap.recycle();

        }


    }
    private int mChildCount = 0;

    @Override

    public void notifyDataSetChanged() {

        mChildCount = getCount();

        super.notifyDataSetChanged();

    }


    @Override

    public int getItemPosition(Object object)   {

        if ( mChildCount > 0) {

            mChildCount --;

            return POSITION_NONE;

        }

        return super.getItemPosition(object);

    }

}
