package cn.tcl.music.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tcl.framework.log.NLog;

import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.util.LogUtil;


/**
 * Main Adapter for our RecyclerViewFragments.
 * It has different itemViewTypes and adapt its size, depending on them.
 *
 * @author nordhal
 *
 * @param <VH>
 */
public abstract class RecyclerViewAdapter<VH extends ClickableViewHolder> extends RecyclerView.Adapter<VH> implements
        ClickableViewHolder.OnViewHolderClickListener {

    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();
    private int[] mItemViewTypes;
    protected Context mContext;

    public RecyclerViewAdapter(Context context) {
        mContext = context;
    }

    public RecyclerViewAdapter(Context context, int[] itemViewTypes) {
        mContext = context;
        mItemViewTypes = itemViewTypes;
    }

    @Override
    public int getItemCount() {
        final int itemViewTypesSize = mItemViewTypes.length;
        int itemCount = 0;
        for (int i = 0; i < itemViewTypesSize; i++)
        {
            itemCount += getItemCountFor(mItemViewTypes[i]);
        }
        return itemCount;
    }

    public interface OnItemClickListener
    {
        void onItemClick(RecyclerView.ViewHolder viewHolder, int position, View v);
    }

    protected OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    public OnItemClickListener getOnItemClickListener()
    {
        return mOnItemClickListener;
    }

    @Override
    public void onViewHolderClick(RecyclerView.ViewHolder vh, int position, View v) {
        if (mOnItemClickListener != null)
            LogUtil.d(TAG,  "onViewHolderClick onItemClick");
        mOnItemClickListener.onItemClick(vh, position, v);
    }

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView recyclerView = (RecyclerView) parent;

        RecyclerView.LayoutManager recyclerViewLayoutManager = recyclerView.getLayoutManager();

        return onCreateViewHolder(parent, viewType, recyclerViewLayoutManager);
    }

    /**
     * <p> Retrieve the real position we should set to the content items,
     *     depending on the position on within the recyclerView
     *     For example, if there is a header, the real content position should substract
     *     the header count.
     *     </p>
     *
     * @param position the position within the recyclerView
     * @return the position that should be set on the content Cursor.
     */
    public int getPositionForContent(int position)
    {
        return position;
    }

    public GridLayoutManager.SpanSizeLookup createSpanSizeLookUp()
    {
        return new GridLayoutManager.DefaultSpanSizeLookup();
    }

    /**
     * On BindViewTypeToViewHolder should be called when the itemViewType is not
     * of <b>media_content</b> and need specific binding. For example, it could be
     * a header.
     * For implemented method, see the RecyclerViewCursorFragment for example.
     *
     * @param viewHolder The view holder which has the views at this position.
     * @param position The position where the views need to be populated
     * @param itemViewType The view type associated.
     */
    public abstract void onBindViewTypeToViewHolder(VH viewHolder, int position, int itemViewType);

    /**
     * Retrieve the id for a specific position and a specific view type.
     * @param position The position where the itemId is requested
     * @param itemViewType The view type associated
     * @return The id of this item.
     */
    public abstract long getItemIdForViewType(int position, int itemViewType);

    /**
     * OnCreateViewHolder with the currentLayoutManager used for this recyclerView.
     * It enables us to change the layout inflated for views, depending of this layoutManager
     *
     * @param parent The parent of the view which should be inflated
     * @param viewType The view type associated
     * @param currentRecyclerLayoutManager The current LayoutManager
     * @return the ViewHolder
     */
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType, RecyclerView.LayoutManager currentRecyclerLayoutManager);

    /**
     * Return the count for a specific itemViewType.
     * Default implementation just return the count of the content cursor.
     * @param itemViewType The view type we are counting
     * @return the total count of this type of items
     */
    public abstract int getItemCountFor(int itemViewType);
}
