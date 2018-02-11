/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.drm.DrmManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.view.CustomPopupWindowBasedAnchor;

public class FileInfoAdapter extends BaseAdapter {

    public static final int MODE_INVALID = -1;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_EDIT = 1;
    public static final int MODE_SEARCH = 2;
    public static final int MODE_GLOBALSEARCH = 3;
    public static final int MODE_COPY = 4;
    public static final int MODE_ADD_ENCRYPT_FILE = 5;
    public static final int MODE_ADD_DECRYPT_FILE = 6;

    public static final float CUT_ICON_ALPHA = 0.6f;
    public static final float HIDE_ICON_ALPHA = 0.3f;
    public static final float DEFAULT_ICON_ALPHA = 1f;
    private static final String TAG = FileInfoAdapter.class.getSimpleName(); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721

    protected final List<FileInfo> mFileInfoList = new ArrayList<FileInfo>();
    protected final FileInfoManager mFileInfoManager;
    protected final Context mContext;
    protected IconManager mIconManager;

    protected int mMode = MODE_NORMAL;
    private SimpleDateFormat mDateFormat;
    private FileManagerApplication mApplication;
    protected int selectedPosition = -1;
    public CustomPopupWindowBasedAnchor mItemMorePop;
    protected int mItemMorePopWidth;

    private DisplayMetrics mDisplayMetrics;

    private static final int MAX_NAME_LENGTH = 400;

//    protected int mItemMorePopNoShareHeight;
//    protected int mItemMorePopShareHeight;
//    protected int mItemMorePopDrmNoShareHeight;
    /**
     * The constructor to construct a FileInfoAdapter.
     *
     * @param context the context of FileManagerActivity
     * @param fileInfoManager a instance of FileInfoManager, which manages all
     *            files.
     */
    public FileInfoAdapter(Context context, FileInfoManager fileInfoManager) {
        mFileInfoManager = fileInfoManager;
        //mFileInfoList = fileInfoManager.getShowFileList();
        if (fileInfoManager!=null) {
            mFileInfoList.addAll(fileInfoManager.getShowFileList());
        }
        mContext = context;
        mIconManager = IconManager.getInstance();
        mApplication = (FileManagerApplication)context.getApplicationContext();
        mItemMorePopWidth = context.getResources().getDimensionPixelSize(R.dimen.sort_menu_width);
//        mItemMorePopNoShareHeight = context.getResources().getDimensionPixelSize(R.dimen.more_menu_land_pop_multishare_height);
//        mItemMorePopShareHeight = context.getResources().getDimensionPixelSize(R.dimen.more_menu_land_pop_folder_height);
    }

    // ADD START FOR PR433886 BY HONGBIN.CHEN 20150901
    public void refresh() {
        mFileInfoList.clear();
        mFileInfoList.addAll(mFileInfoManager.getShowFileList());
       // notifyDataSetChanged();
    }
    // ADD END FOR PR433886 BY HONGBIN.CHEN 20150901
    /**
     * This method gets index of certain fileInfo in fileInfoList
     *
     * @param fileInfo the fileInfo which wants to be located.
     * @return the index of the item in the listView.
     */
    public int getPosition(FileInfo fileInfo) {
        return mFileInfoList.indexOf(fileInfo);
    }

    /**
     * This method sets the item's check boxes
     *
     * @param id the id of the item
     * @param checked the checked state
     */
    public void setChecked(int position, boolean checked) {
        FileInfo checkInfo = mFileInfoList.get(position);
        if (checked) {
            if (!mCheckedFileList.contains(checkInfo)) {
                mCheckedFileList.add(checkInfo);
            }
        } else {
            mCheckedFileList.remove(checkInfo);
        }
        notifyDataSetChanged();
    }
    /**
     * This method sets the item's check boxes
     *
     * @param id the id of the item
     * @param checked the checked state
     */
    public void setSelect(int position) {
        FileInfo checkInfo = mFileInfoList.get(position);
            if (!mCheckedFileList.contains(checkInfo)) {
                mCheckedFileList.add(checkInfo);
                selectedPosition=position;
            }
         else {
            mCheckedFileList.remove(checkInfo);
        }
         notifyDataSetChanged();
    }
    /**
     * This method add fileinfo to check list
     *
     * @param fileInfo
     */
    public void addDeleteFileInfo(FileInfo fileInfo) {
        if (!mCheckedFileList.contains(fileInfo)) {
            mCheckedFileList.add(fileInfo);
        }
    }

    /**
     * This method is remove fileinfo in checklist
     * @param fileInfo
     */
    public void removeCheck(FileInfo fileInfo){
        mCheckedFileList.remove(fileInfo);
    }

    public void clearEncryptedFileList() {
        if (null != mEncryptedFileList) {
            mEncryptedFileList.clear();
        }
    }

    public void addEncryptedFileList(FileInfo fileInfo) {
        if (null != mEncryptedFileList) {
            mEncryptedFileList.add(fileInfo);
        }
    }

    public List<FileInfo> getEncryptedFileList() {
        List<FileInfo> list = new CopyOnWriteArrayList<FileInfo>();
        list.addAll(mEncryptedFileList);
        return list;
    }

    public void removeNeedDecryptFileList(FileInfo fileInfo){
        mNeedDecryptedFileList.remove(fileInfo);
    }

    public void clearNeedDecryptFileList() {
        if (null != mNeedDecryptedFileList) {
            mNeedDecryptedFileList.clear();
        }
    }

    public void addAllListToNeedDecryptFileList(List<FileInfo> fileInfos) {
        clearNeedDecryptFileList();
        mNeedDecryptedFileList.addAll(fileInfos);
    }

    public List<FileInfo> getNeedDecryptFileList() {
        List<FileInfo> list = new CopyOnWriteArrayList<FileInfo>();
        if (mNeedDecryptedFileList.size() > 0) {
            list.addAll(mNeedDecryptedFileList);
        }
        return list;
    }

    public void setItemEditSelect(int position){
        try {
            if(mFileInfoList.size() <= position || position < 0){
                LogUtils.d("HOL", "position is invalid");
                return;
            }
            FileInfo itemEditInfo = mFileInfoList.get(position);

            if (mItemEditFileList.size() != 0) {
                mItemEditFileList.clear();
            }
            mItemEditFileList.add(itemEditInfo);
            LogUtils.d("HOL", "this is enter mItemEditFileList size " + mItemEditFileList.size());
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public boolean isHasDrm(int position){
        try {
            if(mFileInfoList.size() <= position || position < 0){
                LogUtils.d("HOL", "position is invalid");
                return false;
            }
            FileInfo itemEditInfo = mFileInfoList.get(position);
            return itemEditInfo.isDrmFile();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCanShare(int position){
        try {
            if(mFileInfoList.size() <= position || position < 0){
                LogUtils.d("HOL", "position is invalid");
                return false;
            }
            FileInfo itemEditInfo = mFileInfoList.get(position);
            if (!DrmManager.getInstance(mContext.getApplicationContext()).isDrmSDFile(itemEditInfo.getFileAbsolutePath())) {
                return false;
            } else {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<FileInfo> getItemEditSelect(){
        return mItemEditFileList;
    }
    /**
     * This method sets all items' check boxes
     *
     * @param checked the checked state
     */
    public void setAllItemChecked(boolean checked) {
        mCheckedFileList.clear();
        if (checked) {
            mCheckedFileList.addAll(mFileInfoList);
        }
        notifyDataSetChanged();
    }

    public boolean isAllItemChecked() {
    	return mFileInfoList.size() > 0 && mFileInfoList.size() == mCheckedFileList.size();
    }

    /**
     * This method gets the number of the checked items
     *
     * @return the number of the checked items
     */
    public int getCheckedItemsCount() {
        return mCheckedFileList.size();
    }

    /**
     * This method gets the list of the checked items
     *
     * @return the list of the checked items
     */
    public List<FileInfo> getCheckedFileInfoItemsList() {
//        return mCheckedFileList;
    	// We return the copy of mCheckedFileList
    	// Because the mCheckedFileList will be cleared async by other software engineer before
        List<FileInfo> list = new CopyOnWriteArrayList<FileInfo>();
        list.addAll(mCheckedFileList);
    	return list;
    }

    public List<FileInfo> getItemEditFileInfoList() {
//      return mCheckedFileList;
      // We return the copy of mCheckedFileList
      // Because the mCheckedFileList will be cleared async by other software engineer before
      List<FileInfo> list = new CopyOnWriteArrayList<FileInfo>();
      if(mCheckedFileList.size() >0){
          list.addAll(mCheckedFileList);
      }else{
          list.addAll(mItemEditFileList);
      }
      return list;
  }

    /**
     * This method gets the first item in the list of the checked items
     *
     * @return the first item in the list of the checked items
     */
    public FileInfo getFirstCheckedFileInfoItem() {
        if (mCheckedFileList.size() > 0) {
            return mCheckedFileList.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method change all checked items to be unchecked state
     */
    public void clearChecked() {
        mCheckedFileList.clear();
        notifyDataSetChanged();
    }

    /**
     * This method gets the count of the items in the name list
     *
     * @return the number of the items
     */
    @Override
    public int getCount() {
        return mFileInfoList.size();
    }

    /**
     * This method gets the name of the item at the specified position
     *
     * @param pos the position of item
     * @return the name of the item
     */
    @Override
    public FileInfo getItem(int pos) {
    	// Because mFileInfoList is used to be async by other software engineers
    	// I have to make a judge due to the IndexOutOfBoundsException
    	if (pos < mFileInfoList.size()) {
    		return mFileInfoList.get(pos);
    	} else {
    		return null;
    	}
    }

    /**
     * This method gets the item id at the specified position
     *
     * @param pos the position of item
     * @return the id of the item
     */
    @Override
    public long getItemId(int pos) {
        return pos;
    }

    /**
     * This method gets the view to be displayed
     *
     * @param pos the position of the item
     * @param convertView the view to be shown
     * @param parent the parent view
     * @return the view to be shown
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        return convertView;

    }

    /**
     * This method changes the mode of adapter between MODE_NORMAL, MODE_EDIT,
     * and MODE_SEARCH
     *
     * @param mode the mode which will be changed to be.
     */
    public boolean changeMode(int mode) {
    	if (mMode != mode) {
			switch (mode) {
			case MODE_NORMAL:
				clearChecked();
				break;
			default:
				break;
			}
	        mMode = mode;
	        notifyDataSetChanged();
	        return true;
    	}
    	return false;
    }

    /**
     * This method clear the list.
     */
    public void clearList() {
        mFileInfoList.clear();
        mCheckedFileList.clear();
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
        if (mFileInfoManager != null) {
            mFileInfoManager.getShowFileList().clear();
        }
        notifyDataSetChanged();
    }

    /**
     * This method changes the mode of MODE_SEARCH to MODE_NORMAL
     */
    public void changeModeFromSearchToNormal() {
        if (isMode(MODE_SEARCH)||isMode(MODE_GLOBALSEARCH)) {
//            mFileInfoList.clear();
//            mCheckedFileList.clear();
            if (!changeMode(MODE_NORMAL)) {
            	notifyDataSetChanged();
            }
        }
    }

    /**
     * This method gets current mode of the adapter.
     *
     * @return current display mode of adapter
     */
    public int getMode() {
        return mMode;
    }

    /**
     * This method checks that current mode equals to certain mode, or not.
     *
     * @param mode the display mode of adapter
     * @return true for equal, and false for not equal
     */
    public boolean isMode(int mode) {
        return mMode == mode;
    }

    public void setSearchHighLight(TextView textView, String searchText) {
        if (!TextUtils.isEmpty(searchText)) {
            Spanned finalText = setHighLight(textView.getText().toString(), searchText);
            textView.setText(finalText);
        }
    }

    public Spanned setHighLight(String allText, String searchText) {
        if (!TextUtils.isEmpty(searchText)) {
            StringBuilder search = new StringBuilder();
            int len = searchText.length();
            for (int i = 0; i < len; i++) {
                char c = searchText.charAt(i);
                if (c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}'
                        || c == '+' || c == '.' || c == '$' || c == '^') {
                    search.append('\\');
                    search.append(c);
                } else {
                    search.append(c);
                }
            }
            String searchReg = "(?i)" + search;
            Matcher matcher = Pattern.compile(searchReg).matcher(allText);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String matchText = matcher.group(0);
                StringBuilder text = new StringBuilder();
                int len2 = matchText.length();
                for (int i = 0; i < len2; i++) {
                    char c = matchText.charAt(i);
                    if (c == '$') {
                        text.append('\\');
                        text.append(c);
                    } else {
                        text.append(c);
                    }
                }
                matcher.appendReplacement(sb, "<font color='#27b8af'>" + text + "</font>"); //MODIFIED by haifeng.tang, 2016-04-07,BUG-1911859
            }
            matcher.appendTail(sb);
            Spanned finalText = Html.fromHtml(sb.toString());
            return finalText;
        }
        return null;
    }

    public void showNameAndSize(FileInfo fileInfo, TextView name, TextView size) {
        if (fileInfo.isDirectory()) {
            name.setVisibility(View.VISIBLE);
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                size.setVisibility(View.VISIBLE);
            } else {
                size.setVisibility(View.GONE);
            }
        } else {
            name.setVisibility(View.GONE);
            size.setVisibility(View.GONE);
        }
    }

    public void showSize(FileInfo fileInfo, TextView size) {
        if (fileInfo.isDirectory()) {
            size.setVisibility(View.GONE);
        } else {
            size.setVisibility(View.VISIBLE);
        }
    }
    public void setTimeSizeTextForPictures(TextView timeView, TextView sizeView, final FileInfo fileInfo) {
        long time = fileInfo.getFileLastModifiedTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        if (mDateFormat == null) {
            String strDateFormat = "yyyy-MM-dd HH:mm";
            mDateFormat = new SimpleDateFormat(strDateFormat);
        }

        String mModifiedTime = mDateFormat.format(new Date(time)).toString();
        if (!fileInfo.isDirectory()) {
            sizeView.setText(mModifiedTime + "" + fileInfo.getFileSizeStr());
        } else {
            List<FileInfo> fileInfos = fileInfo.getSubFileInfo();
            if (fileInfos != null) {
                sizeView.setText("" + fileInfos.size());
            }
        }
        if (null != timeView) {
            timeView.setText(mModifiedTime);
            timeView.setVisibility(View.VISIBLE);
        }
    }

    public void setTimeSizeText(TextView timeView, TextView sizeView, final FileInfo fileInfo) {
        long time = fileInfo.getFileLastModifiedTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        if (mDateFormat == null) {
            String strDateFormat = "yyyy-MM-dd HH:mm";
            mDateFormat = new SimpleDateFormat(strDateFormat);
        }

        String mModifiedTime = mDateFormat.format(new Date(time)).toString();
        if (!fileInfo.isDirectory()) {
            sizeView.setVisibility(View.VISIBLE);
            sizeView.setText(mModifiedTime + "" + fileInfo.getFileSizeStr());
        } else {
            List<FileInfo> fileInfos = fileInfo.getSubFileInfo();
            if (fileInfos != null) {
                sizeView.setText("" + fileInfos.size());
            }
        }
        if (null != timeView) {
            timeView.setText(mModifiedTime);
            timeView.setVisibility(View.VISIBLE);
        }
    }

    public void setTimeSizeTextForDownload(TextView timeView, TextView sizeView, final FileInfo fileInfo) {
        long time = fileInfo.getFileLastModifiedTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        if (mDateFormat == null) {
            String strDateFormat = "yyyy-MM-dd HH:mm";
            mDateFormat = new SimpleDateFormat(strDateFormat);
        }

        String mModifiedTime = mDateFormat.format(new Date(time)).toString();
        if (!fileInfo.isDirectory()) {
            sizeView.setVisibility(View.VISIBLE);
            sizeView.setText(fileInfo.getFileSizeStr());
        } else {
            List<FileInfo> fileInfos = fileInfo.getSubFileInfo();
            if (fileInfos != null) {
                sizeView.setText("" + fileInfos.size());
            }
        }
        if (null != timeView) {
            timeView.setText(mModifiedTime);
            timeView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * measure to make the name and timesize in the TextView
     */
    public void setItemTimeSizeTextL(TextView sizeView, final FileInfo fileInfo) {
        long time = fileInfo.getFileLastModifiedTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        if(mDateFormat == null){
            String strDateFormat = "yyyy-MM-dd HH:mm";
            mDateFormat = new SimpleDateFormat(strDateFormat);
        }
        String mModifiedTime = mDateFormat.format(new Date(time)).toString();
        if (!fileInfo.isDirectory()) {
            sizeView.setVisibility(View.VISIBLE);
            sizeView.setText(mModifiedTime + "  " + fileInfo.getFileSizeStr());
            sizeView.setAlpha((float) 0.5);
        } else {
            sizeView.setText(mModifiedTime);
            sizeView.setAlpha((float) 0.5);
            sizeView.setVisibility(View.VISIBLE);
        }
    }

    public void setPathText(TextView timeView, TextView sizeView, TextView pathView, final FileInfo fileInfo) {
        if (null != sizeView) {
            sizeView.setVisibility(View.GONE);
        }
        if (null != timeView) {
            timeView.setVisibility(View.GONE);
        }
        if (null != pathView) {
            pathView.setVisibility(View.VISIBLE);
            String path = fileInfo.getShowPath();
            if (!TextUtils.isEmpty(path)) {
                int index = path.lastIndexOf("/");
                pathView.setText(path.substring(0, index));
            }
        }
    }

    public void setPathText(TextView sizeView, String path) {
        if (null != sizeView) {
            sizeView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(path)) {
                int index = path.lastIndexOf("/");
                sizeView.setText(path.substring(0, index));
            }
        }
    }

    protected void setNameText(TextView textView, FileInfo fileInfo) {
        if (fileInfo.isDirectory()) {
            if (!MountManager.getInstance().isMountPoint(fileInfo.getFileAbsolutePath())
                    && fileInfo.getSubFileInfo() == null) {
                int sum = fileInfo.getSubFileNum();
                if (sum > 0) {
                    textView.setText(fileInfo.getShowName() + "(" + sum + ")");
                    return;
                }
            }
        }
        if (fileInfo.getFileName().equals("0") && fileInfo.getFileAbsolutePath().equals("/storage/emulated/0")) {
            textView.setText(mContext.getString(R.string.phone_storage_cn));
        } else {
            textView.setText(fileInfo.getShowName());
        }
    }

    /**
     * get sum width
     * @param text
     * @param size
     * @return
     */
    public float getCharacterWidth(String text, float size){
        if(null == text || "".equals(text))
            return 0;
        float width = 0;
        Paint paint = new Paint();
        paint.setTextSize(size);
        float text_width = paint.measureText(text);//get sum length
        return text_width;
    }

    protected void setFolderNameText(TextView textView, FileInfo fileInfo) {
        textView.setText(fileInfo.getFolderName());
    }

    protected void setPicturesCount(TextView textView, FileInfo fileInfo) {
        textView.setText("" + getSubFilePicCount(fileInfo));
    }

    protected int getSubFilePicCount(FileInfo fileInfo) {
        int length = fileInfo.getFile().list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String fileName) {
                if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.endsWith(".png") ||
                        fileName.endsWith(".gif") || fileName.endsWith(".bmp")) {
                    return true;
                }
                return false;
            }
        }).length;
        return length;
    }

    private void initDM(){
        if (null == mDisplayMetrics){
            mDisplayMetrics = new DisplayMetrics();
            ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        }
    }

    /*
     * file name too length to hide chars
     */
    protected String hideFileName(FileInfo fileInfo) {
        initDM();
        int width = (int) getCharacterWidth(fileInfo.getShowName(),
                mContext.getResources().getDimension(R.dimen.list_title_text_size));
        LogUtils.i(TAG, "width:" + width + ",name :" + fileInfo.getShowName() + ":"+mContext.getResources().getDimension(R.dimen.list_title_text_size));
        String fileName = fileInfo.getShowName();
        if (!TextUtils.isEmpty(fileName)) {
            int length = fileName.length();
            if (!fileInfo.isDirectory()) {
                int fileTypeIndex = fileName.lastIndexOf(".");
                if (fileTypeIndex > 0) {
                    length = fileTypeIndex;
                }
            }
            int charWidth = width / fileName.length();
            LogUtils.i(TAG, "charWidth:" + charWidth);
            int size = MAX_NAME_LENGTH / charWidth;
            LogUtils.i(TAG, "size:" + size);
            if (fileName.length() > size) {
                int index = (size - 2) / 2;
                fileName = fileName.substring(0, index) + "..." + fileName.substring(length - index + 1, fileName.length());
            }
        }
        return fileName;
    }

    protected void loadImage(FileInfo fileInfo){
        //do nothing
    }

    protected List<FileInfo> mCheckedFileList = new ArrayList<FileInfo>();
    protected List<FileInfo> mItemEditFileList = new ArrayList<FileInfo>();
    protected List<FileInfo> mEncryptedFileList = new ArrayList<FileInfo>();
    protected List<FileInfo> mNeedDecryptedFileList = new ArrayList<FileInfo>();
}
