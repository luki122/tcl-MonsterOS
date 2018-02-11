/* Copyright (C) 2016 Tcl Corporation Limited */
package com.leon.tools.view;

/**
 * @param <ItemData>
 * @author zhanghong
 */
public abstract class ResLayoutAdapter<ItemData> extends CommonAdapter<ItemData> {

    private int[] mItemLayoutIds;

    public ResLayoutAdapter(int layoutId) {
        this(new int[]{layoutId});
    }

    /**
     * @param layoutIds R.layout.id
     */
    public ResLayoutAdapter(int[] layoutIds) {
        mItemLayoutIds = layoutIds;
    }

    @Override
    public final int getViewTypeCount() {
        return mItemLayoutIds.length;
    }


    protected void onItemClick(DataCoverUiController<ItemData> ctr, ItemData itemData, int position) {
    }

    /**
     * @param ctr
     * @param data
     * @param position
     * @param viewType
     */
    protected abstract void convertItemData(DataCoverUiController<ItemData> ctr, ItemData data, int position,
                                            int viewType);

    @Override
    protected final CommonHolder<ItemData> createNewHolder(int viewType, int position) {
        return new CommonHolder<ItemData>(mItemLayoutIds[viewType]) {
            @Override
            protected void convertHolder(DataCoverUiController<ItemData> ctr, ItemData data) {
                convertItemData(ctr, data, getViewPosition(), getViewType());
            }

            @Override
            protected void onClick(DataCoverUiController<ItemData> ctr, ItemData itemData, int position) {
                onItemClick(ctr, itemData, position);
            }
        };
    }

}
