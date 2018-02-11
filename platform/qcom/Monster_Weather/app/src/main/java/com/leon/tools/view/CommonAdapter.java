/* Copyright (C) 2016 Tcl Corporation Limited */
package com.leon.tools.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @param <ItemData>
 * @author zhanghong
 */
public abstract class CommonAdapter<ItemData> extends BaseAdapter implements AdapterView.OnItemClickListener {
    protected List<ItemData> mDatas = new ArrayList<ItemData>();

    public CommonAdapter() {
    }

    /**
     * @param datas
     */
    public void setItemDatas(Collection<ItemData> datas) {
        mDatas.clear();
        if (null != datas)
            mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void exchangedPosition(int p1, int p2) {
        int firstIndex = p1;
        int secondIndex = p2;

        if (firstIndex > secondIndex) {
            int index = firstIndex;
            firstIndex = secondIndex;
            secondIndex = index;
        }

        ItemData item2 = mDatas.remove(secondIndex);
        ItemData item1 = mDatas.remove(firstIndex);

        mDatas.add(firstIndex, item2);
        if (mDatas.size() > secondIndex) {
            mDatas.add(secondIndex, item1);
        } else {
            mDatas.add(item1);
        }
        notifyDataSetChanged();
    }


    public void changePosition(int oldPosition, int newPosition){
        if(oldPosition != newPosition) {
            ItemData data = mDatas.remove(oldPosition);
//            if(oldPosition < newPosition){
//                newPosition --;
//            }
            mDatas.add(newPosition,data);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public ItemData getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int viewType = getItemViewType(position);
        CommonHolder<ItemData> holder;
        if (null == convertView) {
            holder = createNewHolder(viewType, position);
            convertView = holder.initHolder(parent.getContext());
            onUicontrollerSetted(holder.getUiController());
        } else {
            holder = (CommonHolder<ItemData>) convertView.getTag();
        }
        holder.setCurrentViewData(viewType, position);
        holder.convertData(getItem(position));
        return convertView;
    }

    protected void onLvHeaderClick(AdapterView<?> parent, View view, int position, long id) {
    }

    protected void onLvFooterClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int headerSize = 0;
        if (parent instanceof ListView) {
            ListView lv = (ListView) parent;
            headerSize = lv.getHeaderViewsCount();
            if (position < headerSize) {
                onLvHeaderClick(parent, view, position, id);
                return;
            } else if (position >= getCount() + headerSize) {
                onLvFooterClick(parent, view, position, id);
                return;
            }
        }
        position = position - headerSize;
        @SuppressWarnings("unchecked")
        CommonHolder<ItemData> holder = (CommonHolder<ItemData>) view.getTag();
        holder.onClick(holder.getUiController(), getItem(position), position);
    }

    protected void onUicontrollerSetted(DataCoverUiController<ItemData> ctr) {

    }

    /**
     * @param viewType
     * @return
     */
    protected abstract CommonHolder<ItemData> createNewHolder(int viewType, int position);

    /**
     * @author zhanghong
     */
    public static abstract class CommonHolder<ItemData> {
        private final int mLayoutId;
        private int mViewType, mViewPosition;
        private CommonUiController mUiController;

        public CommonHolder(int layoutId) {
            mLayoutId = layoutId;
        }

        public CommonHolder(View view) {
            mLayoutId = 0;
            mUiController = new CommonUiController(view);
        }

        final View initHolder(Context context) {
            View view = null;
            if (null == mUiController) {
                view = View.inflate(context, mLayoutId, null);
                mUiController = new CommonUiController(view);
            } else {
                view = mUiController.getView();
            }
            onInitHolder(mUiController);
            view.setTag(this);
            return view;
        }

        final void setCurrentViewData(int viewType, int position) {
            mViewType = viewType;
            mViewPosition = position;
        }

        protected final View getVeiw() {
            return mUiController.getView();
        }

        protected final DataCoverUiController<ItemData> getUiController() {
            return mUiController;
        }

        /**
         * @return
         */
        public final int getViewType() {
            return mViewType;
        }

        public final int getViewPosition() {
            return mViewPosition;
        }

        /**
         * @param ctr
         */
        protected void onInitHolder(DataCoverUiController<ItemData> ctr) {
        }

        final void convertData(ItemData data) {
            mUiController.convertData(data);
        }

        /**
         * @param viewId
         * @return
         */
        public <T extends View> T findView(int viewId) {
            return mUiController.findViewById(viewId);
        }

        /**
         * @param ctr
         * @param data
         * @param data
         */
        protected abstract void convertHolder(DataCoverUiController<ItemData> ctr, ItemData data);

        protected void onClick(DataCoverUiController<ItemData> ctr, ItemData data, int position) {
        }

        /**
         * @author zhanghong
         */
        private class CommonUiController extends DataCoverUiController<ItemData> {

            public CommonUiController(View view) {
                super(view);
            }

            @Override
            protected void onConvertData(ItemData data) {
                convertHolder(this, data);
            }

        }

    }

}
