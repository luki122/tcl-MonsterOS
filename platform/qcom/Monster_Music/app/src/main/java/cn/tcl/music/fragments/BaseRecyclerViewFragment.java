package cn.tcl.music.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import cn.tcl.music.R;
import cn.tcl.music.adapter.RecyclerViewAdapter;
import cn.tcl.music.adapter.RecyclerViewAdapter.OnItemClickListener;
import cn.tcl.music.view.image.ImageFetcher;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;

public abstract class BaseRecyclerViewFragment extends Fragment {
    private static final String TAG = BaseRecyclerViewFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ViewGroup mProgressContainer;
    private ViewGroup mRecyclerViewContainer;
    protected View mEmptyView;
    protected View mImageView;
    private boolean mRecyclerViewShown;
    private RecyclerViewAdapter<?> mAdapter;
    protected Set<ItemDecoration> mItemDecorations = new HashSet<RecyclerView.ItemDecoration>();

    protected ImageFetcher mImageFetcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() == null || getContext() == null)
            return;

        int artworkSize = getResources().getDimensionPixelSize(R.dimen.image_artwork_row_size);
        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getContext(), artworkSize, R.drawable.default_cover_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        try {
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public ImageFetcher getImageFetcher() {
        return mImageFetcher;
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //When QueueFragment create will create QueueAdapter(Context context, Cursor c, ImageFetcher imageFetcher)
        //so the adapter has the mImageFetcher A,A controll load image by the value mExitTasksEarly
        //And QueueFragment secondly create,the adapter doesn't change,then load imge also use the mImageFetcher A,not the new ImageFetcher
        mImageFetcher = null;
        mAdapter = null;
    }

    public interface ItemClickFromFragmentListener {
        boolean onRecyclerItemClick(BaseRecyclerViewFragment fragment, final ViewHolder viewHolder, final int position, View v);
    }

    protected ItemClickFromFragmentListener mItemClickFromFragmentListener;

    /**
     * Main OnItemClickListener, calling onRecyclerItemClick but handling first the item_menu_image_button,
     * in order to display the popup menu for this row.
     */
    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        public void onItemClick(final ViewHolder viewHolder, final int position, View v) {
            if (v.getId() == R.id.item_menu_image_button) {
                LogUtil.d(TAG, "mOnItemClickListener onItemClick item_menu_image_button");
                PopupMenu popup = new PopupMenu(getActivity(), v, Gravity.CENTER);
                onPopulatePopupMenu(popup.getMenuInflater(), popup.getMenu(), viewHolder, position);
            } else if (v.getId() == cn.tcl.music.R.id.recover_scan_button) {
                Log.d(TAG,"mOnItemClickListener and click folder item recover button");
                recoverScan(position);
            } else {
                LogUtil.d(TAG, "mOnItemClickListener alreadyHandled mItemClickFromFragmentListener = " + mItemClickFromFragmentListener);
                boolean alreadyHandled = false;
                if (mItemClickFromFragmentListener != null) {
                    alreadyHandled = mItemClickFromFragmentListener.onRecyclerItemClick(BaseRecyclerViewFragment.this, viewHolder, position, v);
                }
                if (!alreadyHandled) {
                    onRecyclerItemClick(viewHolder, position, v);
                }
            }
        }
    };

    public void setItemClickFromFragmentListener(ItemClickFromFragmentListener itemClickListener) {
        mItemClickFromFragmentListener = itemClickListener;
    }

    private RecyclerView.OnScrollListener mScrollListener;

    public RecyclerView getRecyclerView() {
        ensureRecyclerView();
        return mRecyclerView;
    }

    @Override
    public void onDestroyView() {
        mRecyclerView = null;
        mRecyclerViewShown = false;
        mEmptyView = mProgressContainer = mRecyclerViewContainer = null;
        mImageView = null;
        super.onDestroyView();
    }

    /**
     * Provide the recycler adapter for the view.
     */
    public void setRecyclerAdapter(RecyclerViewAdapter<?> recyclerAdapter) {
        final boolean hadAdapter = mAdapter != null;
        mAdapter = recyclerAdapter;
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(recyclerAdapter);
            if (mAdapter != null) {
                mAdapter.setOnItemClickListener(mOnItemClickListener);
            }
            if (!mRecyclerViewShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter.  It is now time to show it.
                setRecyclerViewShown(true, getView().getWindowToken() != null);
            }
        }
    }

    public Adapter<?> getRecyclerViewAdapter() {
        return mAdapter;
    }

    /**
     * Provide the LayoutManager associated with the recycler View.
     * The Recycler View should be created
     *
     * @return the new layoutManager has been correctly set
     */
    public boolean changeRecyclerLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.getRecycledViewPool().clear();
            for (ItemDecoration decor : mItemDecorations)
                mRecyclerView.removeItemDecoration(decor);
            mItemDecorations.clear();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add items decoration to the recyclerView.
     *
     * @param itemDecoration
     */
    public void addRecyclerItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        boolean hasBeenChanged = mItemDecorations.add(itemDecoration);
        if (mRecyclerView != null && hasBeenChanged) {
            mRecyclerView.addItemDecoration(itemDecoration);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureRecyclerView();
    }

    /**
     * Control whether the recycler view is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * <p/>
     * It won't work it it is called before the onCreateView has finished.
     * For example, if a LoaderManager is started/restarted during the onCreateView,
     * you won't be able to display the spinner wheel.
     * The best way is to handle it on the OnViewCreated method.
     *
     * @param shown If true, the recycler view is shown; if false, the progress
     *              indicator.  The initial value is true.
     */
    public void setRecyclerViewShown(boolean shown) {
        setRecyclerViewShown(shown, true);
    }

    /**
     * Like {@link #setRecyclerViewShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     */
    public void setRecyclerViewShownNoAnimation(boolean shown) {
        setRecyclerViewShown(shown, false);
    }

    /**
     * Handy method to find a viewHolder at a specific position.
     *
     * @param position
     * @return
     */
    public ViewHolder findViewHolderForPosition(int position) {
        return mRecyclerView.findViewHolderForPosition(position);
    }

    /**
     * Control whether the recycler view is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown   If true, the recycler view is shown; if false, the progress
     *                indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     *                new state.
     */
    private void setRecyclerViewShown(boolean shown, boolean animate) {
        int numTrack = MusicUtil.getSongCount(getContext());
        ensureRecyclerView();
        if (getActivity() == null) {// We are detached
            return;
        }
        if (mProgressContainer == null) {
            LogUtil.e(TAG, "Can't be used without progressContainer");
            return;
        }
        if (mRecyclerViewShown == shown) {
            return;
        }
        mRecyclerViewShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mRecyclerViewContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mRecyclerViewContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mRecyclerViewContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mRecyclerViewContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mRecyclerViewContainer.clearAnimation();
            }
            if (numTrack == 0) {
                mProgressContainer.setVisibility(View.GONE);
                mRecyclerViewContainer.setVisibility(View.GONE);
            } else {
                mProgressContainer.setVisibility(View.VISIBLE);
                mRecyclerViewContainer.setVisibility(View.GONE);
            }
        }
    }

    public void setRecyclerScrollListener(RecyclerView.OnScrollListener listener) {
        mScrollListener = listener;
        if (mRecyclerView != null) {
            mRecyclerView.setOnScrollListener(mScrollListener);
        }
    }

    public void manageEmptyView(boolean isRecyclerViewEmpty) {
        if (mEmptyView == null) {
            return;
        }
        mEmptyView.setVisibility(isRecyclerViewEmpty ? View.VISIBLE : View.GONE);
        mImageView.setVisibility(isRecyclerViewEmpty ? View.VISIBLE : View.GONE); //[BUGFIX]-Add by TCTNJ,liuqiang.song, 2015-4-10,PR960470
//        if (getActivity() != null && getActivity() instanceof ActivityAddSong)
//            ((ActivityAddSong) getActivity()).refreshSelect(isRecyclerViewEmpty); // MODIFIED by beibei.yang, 2016-07-05,BUG-2459696
    }

    public void manageEmptyPermissionView(boolean isRecyclerViewEmpty) {
        if (mEmptyView == null) {
            return;
        }
        TextView m = (TextView) mEmptyView.findViewById(android.R.id.empty);
        m.setText(cn.tcl.music.R.string.permission_tips3);

        mEmptyView.setVisibility(isRecyclerViewEmpty ? View.VISIBLE : View.GONE);
        mImageView.setVisibility(isRecyclerViewEmpty ? View.VISIBLE : View.GONE); //[BUGFIX]-Add by TCTNJ,liuqiang.song, 2015-4-10,PR960470
    }

    public void manageEmptyNoDataView(boolean isRecyclerViewEmpty) {
        if (mEmptyView == null) {
            return;
        }
        TextView m = (TextView) mEmptyView.findViewById(android.R.id.empty);
        m.setText(cn.tcl.music.R.string.no_song_found);
        mEmptyView.setVisibility(isRecyclerViewEmpty ? View.VISIBLE : View.GONE);
        mImageView.setVisibility(isRecyclerViewEmpty ? View.VISIBLE : View.GONE); //[BUGFIX]-Add by TCTNJ,liuqiang.song, 2015-4-10,PR960470
    }


    /**
     * Ensure the Recycler View is correctly set.
     */
    private boolean ensureRecyclerView() {
        if (mRecyclerView != null) {
            return false;
        }

        View root = getView();
        if (root == null) {
            LogUtil.e(TAG, "Content View not yet created.");
            return false;
        }

        if (root instanceof RecyclerView) {
            mRecyclerView = (RecyclerView) root;
        } else {
            mRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
            if (mRecyclerView == null) {
                throw new RuntimeException(
                        "Your content must have a RecyclerView whose id attribute is " +
                                "'R.id.recycler_view'");
            }

            mEmptyView = root.findViewById(android.R.id.empty);
            mImageView = root.findViewById(android.R.id.icon);
            mProgressContainer = (ViewGroup) root.findViewById(R.id.progress_container);
            mRecyclerViewContainer = (ViewGroup) root.findViewById(R.id.recycler_container);
            mRecyclerViewShown = true;
            if (mAdapter != null) {
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(mOnItemClickListener);
                if (!mItemDecorations.isEmpty()) {
                    for (ItemDecoration decor : mItemDecorations) {
                        mRecyclerView.addItemDecoration(decor);
                    }
                }
            } else {
                // We are starting without an adapter and a layoutManager, so assume we won't
                // have our data right away and start with the progress indicator.
                if (mProgressContainer != null) {
//                    if (DeezerUtil.isWifiOrMobileData(getActivity())) {
//                        setRecyclerViewShown(false, false);
//                    }
                }
            }

            mRecyclerView.setOnScrollListener(mScrollListener);
            registerForContextMenu(mRecyclerView);
        }
        onRecyclerViewCreated(mRecyclerView);

        return true;
    }

    /**
     * Method called everytime a OnClickListener is set on a view. By default, the main container of a row call this.
     *
     * @param viewHolder The viewHolder where the click has occured
     * @param position   The position where the click has occured.
     * @param v          The view that triggered the click
     */
    protected void onRecyclerItemClick(final ViewHolder viewHolder, final int position, View v) {
    }

    /**
     * When there is an item with id <b>item_menu_image_button</b> in your row,
     * the recyclerViewFragment triggers this method on the click.
     * You should inflate your menu in it, depending of the item selected.
     *
     * @param menuInflater   The inflater used for inflating the menu
     * @param menu           The menu which will be displayed
     * @param itemViewHolder The ViewHolder where the menu would be populated
     * @param position       The position of the item requesting a menu.
     */
    protected void onPopulatePopupMenu(MenuInflater menuInflater, Menu menu, ViewHolder itemViewHolder, int position) {
    }

    /**
     * Method triggered when an item is selected within the popupMenu
     *
     * @param item           The item selected
     * @param itemViewHolder The viewHolder where the popupMenu has been displayed
     * @param position       The position of the item requesting a menu.
     * @return <b>true</b> if the popupMenuItem was correctly handled. <b>false</b> if this is not the case.
     */
    protected boolean onPopupMenuItemSelected(MenuItem item, ViewHolder itemViewHolder, int position) {
        return false;
    }

    /**
     * Method called when the recyclerView is really created.
     * You should do your specific configs here instead of calling
     * getRecyclerView() for example.
     * You should also specify your LayoutManager here
     *
     * @param rv The recyclerView newly created.
     */
    protected void onRecyclerViewCreated(RecyclerView rv) {
        if (getActivity() == null) {
            return;
        }

        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    /**
     * 恢复扫描
     */
    public void recoverScan(int position){}

    public abstract void leaveMultiChoose();
}
