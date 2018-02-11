/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.ImageLoader;
import cn.tcl.meetingassistant.utils.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created on 9/1/16.
 */
public class ImportPointPreviewItemView extends LinearLayout{

    private Context mContext = null;
    private File[] mImageFiles;
    private List<String> mContentList;
    private final String TAG = ImportPointPreviewItemView.class.getSimpleName();

    public ImportPointPreviewItemView(ImportPoint data, Context context) {
        this(data, context, null);
    }

    public ImportPointPreviewItemView(ImportPoint data, Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        getImportPointImagesFiles(data);
        getImportPointContents(data);

        setOrientation(VERTICAL);
        initView(data);
    }

    private void getImportPointImagesFiles(ImportPoint data) {
        String filePath = FileUtils.IMAGE_FILE_PATH + data.getCreatTime();
        mImageFiles = FileUtils.getImageFilesByTime(filePath);
    }

    private void getImportPointContents(ImportPoint data) {
        String content = data.getInfoContent();
        mContentList = Utility.parseStringByNewLine(content);
    }

    private void initView(ImportPoint data) {
        List<View> viewList = createImportPointTextViews();
        addImportPointTextViews(viewList);

        List<View> imageViewList = createImportPointImageViews();
        addImportPointImageViews(imageViewList);
    }

    private List<View> createImportPointTextViews() {
        if(mContentList == null || mContentList.size() <= 0) {
            return null;
        }

        List<View> viewList = new ArrayList<>();
        for(int i = 0; i < mContentList.size(); i++) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_meeting_preview_import_point_text, this, false);
            TextView textView = (TextView)view.findViewById(R.id.meeting_info_import_point_text);
            textView.setText(mContentList.get(i));
            viewList.add(view);
        }
        return viewList;

    }

    private void addImportPointTextViews(List<View> viewList) {
        if(viewList == null || viewList.size() <= 0) return;

        int length = viewList.size();
        int marginLeftDimenId = R.dimen.layout_common_17dp;
        int marginTopDimenId = 0;
        int marginBottomDimenId = 0;

        for(int i = 0; i < length; i++) {
            View view = viewList.get(i);
            if(i == 0) {
                marginTopDimenId = R.dimen.layout_common_24dp;
                //next is text
               // marginBottomDimenId = R.dimen.layout_common_7dp;
                if (length>1){
                    marginBottomDimenId = R.dimen.layout_common_14dp;
                }

                if(length == 1) {
                    //next is none or image
                    marginBottomDimenId = R.dimen.layout_common_14dp;
                    if (mImageFiles == null || mImageFiles.length <= 0){
                        marginBottomDimenId = R.dimen.layout_common_25dp;
                    }
                }
            } else if(i == length -1 && length > 1) {
                marginTopDimenId = R.dimen.layout_common_0dp;
                marginBottomDimenId = R.dimen.layout_common_14dp;
                //next is none or image
             if (mImageFiles == null || mImageFiles.length <= 0){
                 marginBottomDimenId = R.dimen.layout_common_25dp;
             }


            } else {
                //next is text
                marginTopDimenId = 0;
                marginBottomDimenId = R.dimen.layout_common_14dp;
            }
            LayoutParams layoutParams = Utility.creatLayoutParams(mContext,
                    marginLeftDimenId, marginTopDimenId, 0, marginBottomDimenId);
            view.setLayoutParams(layoutParams);
            addView(view);
        }
    }

    private List<View> createImportPointImageViews() {
        if(mImageFiles == null || mImageFiles.length <= 0) {
            return null;
        }

        List<View> viewList = new ArrayList<>();
        for(int i = 0; i < mImageFiles.length; i++) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_meeting_preview_import_point_image, this, false);
            ImageView imageView = (ImageView)view.findViewById(R.id.meeting_info_import_point_image);
            ImageLoader.getInstance(ImageLoader.IMG_2,getContext()).loadBitmap(imageView,mImageFiles[i].getPath(),true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewList.add(view);
        }
        return viewList;
    }



    private void addImportPointImageViews(List<View> viewList) {
        if(viewList == null || viewList.size() <= 0) return;
        int length = viewList.size();
        int marginLeftDimenId = R.dimen.layout_common_11dp;
        int marginRightDimenId = R.dimen.layout_common_11dp;
        int marginTopDimenId = 0;
        int marginBottomDimenId = R.dimen.layout_common_15dp;
        for(int i = 0; i < viewList.size(); i++) {
            View view = viewList.get(i);

            marginTopDimenId = 0;
            //No text on top
            if(i == 0 && mContentList != null && mContentList.size() <= 0) {
                marginTopDimenId = R.dimen.layout_common_20dp;
            }
            if (i==length-1){
                marginBottomDimenId = R.dimen.layout_common_15dp;
            }else {
                marginBottomDimenId = R.dimen.layout_common_10dp;
            }
            LayoutParams layoutParams = Utility.creatLayoutParams(mContext,
                    marginLeftDimenId, marginTopDimenId, marginRightDimenId, marginBottomDimenId);
            view.setLayoutParams(layoutParams);
            addView(view);
        }
    }

}
