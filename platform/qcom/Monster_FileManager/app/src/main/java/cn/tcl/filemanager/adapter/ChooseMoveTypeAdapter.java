/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.utils.CommonUtils;

/**
 * Created by user on 16-3-11.
 */
public class ChooseMoveTypeAdapter extends BaseAdapter{

    private Context mContext;
    private ArrayList<CategoryItem> mList;
    private LayoutInflater mInflater;
    //private SafeCategoryCountManager mCategoryCountManager;
    private Typeface tf;
    private Resources mResources;

    private class CagegoryGrider {
        ImageView iconView;
        TextView nameView;
        TextView countView;
    }

    public ChooseMoveTypeAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        //mCategoryCountManager = CategoryCountManager.getInstance();
        tf = CommonUtils.getRobotoMedium();
        mResources = mContext.getResources();
        initData();
    }

    private void initData() {
        mList = new ArrayList<>();
        mList.ensureCapacity(4);
        //mList.add(new CategoryItem(R.drawable.category_recent, R.string.main_recents));
        //mList.add(new CategoryItem(R.drawable.category_apk, R.string.main_installers));
        //mList.add(new CategoryItem(R.drawable.category_bluetooth, R.string.category_bluetooth));
        mList.add(new CategoryItem(R.drawable.category_files, R.string.category_files)); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
        //mList.add(new CategoryItem(R.drawable.category_download, R.string.category_download));
        mList.add(new CategoryItem(R.drawable.category_music, R.string.category_music));
        mList.add(new CategoryItem(R.drawable.category_image, R.string.category_pictures));
        mList.add(new CategoryItem(R.drawable.category_video, R.string.category_vedios));
        //mList.add(new CategoryItem(R.drawable.category_safe, R.string.category_safe));

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        CagegoryGrider mGridHolder;
        if (view == null) {
            mGridHolder = new CagegoryGrider();
            view = mInflater.inflate(R.layout.category_item, null);
            mGridHolder.iconView = (ImageView) view
                    .findViewById(R.id.category_img);
            mGridHolder.nameView = (TextView) view
                    .findViewById(R.id.category_name);
            mGridHolder.countView = (TextView) view
                    .findViewById(R.id.category_count);
//            mGridHolder.mainLayout = (LinearLayout) view
//                    .findViewById(R.id.main_item_bac);
//            mGridHolder.itemLayout = (LinearLayout) view
//                    .findViewById(R.id.main_item_mes_bac);
            view.setTag(mGridHolder);
        } else {
            mGridHolder = (CagegoryGrider) view.getTag();
        }

        CategoryItem item = mList.get(position);
        mGridHolder.iconView.setImageResource(item.getIcon());
        mGridHolder.countView.setVisibility(View.GONE);
        // mGridHolder.mainLayout.setBackgroundColor(getResources().getColor(
        // item.getMainColorId()));
//        mGridHolder.mainLayout.setBackground(item.getMainDrawable());
        // mGridHolder.itemLayout.setBackgroundColor(getResources().getColor(
        // item.getItemColorId()));
        // mGridHolder.iconView.setBackground(item.getBgIcon());
        mGridHolder.nameView.setText(item.getName());
        mGridHolder.nameView.setTypeface(tf);
        //mGridHolder.countView.setTypeface(tf);
        //mGridHolder.countView.setTag(String.valueOf(position));
        // mGridHolder.countView.setText("0");
        //loadCountText(mGridHolder.countView, position);
        return view;
    }

    private class CategoryItem {
        private int icon;
        private int nameId;

        public CategoryItem(int icon, int name) {
            this.icon = icon;
            this.nameId = name;
        }


        public int getIcon() {
            return icon;
        }

        public int getName() {
            return nameId;
        }


    }
}
