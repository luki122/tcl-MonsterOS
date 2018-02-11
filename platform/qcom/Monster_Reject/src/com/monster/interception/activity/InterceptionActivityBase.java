package com.monster.interception.activity;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.monster.interception.adapter.BlackAdapter;
import com.monster.interception.adapter.InterceptionAdapterBase;
import com.monster.interception.util.BlackUtils;

import mst.app.MstActivity;
import mst.app.dialog.ProgressDialog;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.SliderView;
import mst.widget.ActionMode.Item;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import mst.widget.MstListView;
import mst.widget.SliderView;
import com.monster.interception.R;

public class InterceptionActivityBase extends MstActivity implements
		OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener,
		SliderView.OnSliderButtonLickListener {
	private static final String TAG = "InterceptionActivityBase";

	protected final static int DELAY_TIME = 500;
	protected final static int MESSAGE_START_DELETE = 0;
	protected final static int MESSAGE_END_DELETE = 1;
	protected final static int MESSAGE_START_INSERT = 2;
	protected final static int MESSAGE_END_INSERT = 3;

	protected Context mContext;
	protected ContentResolver mContentResolver;
	protected AsyncQueryHandler mQueryHandler;

	protected /*android.app.*/ProgressDialog mProgressDialog;
	protected ProgressDialog mDeleteProgressDialog;
	protected Handler mBaseHandler = new Handler();
	protected MstListView mList;
	protected TextView mEmpty;
	protected InterceptionAdapterBase mAdapter = null;
	protected Toolbar myToolbar;

	protected String mSelectAllStr;
	protected String mUnSelectAllStr;
	protected BottomNavigationView mBottomNavigationView;
	protected ActionMode mActionMode;
	protected boolean isQuery = false;
	protected boolean isOperating = false;//include delete and insert
	
	protected ContentObserver changeObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfUpdate) {
			Log.i(TAG, "onChange.................................");
			if (!isOperating) {
			    startQuery();
			}
		}
	};
	
	
	protected Uri mListUri;
	protected void startQuery() {
	}

	
	protected class QueryHandler extends AsyncQueryHandler {
		private final Context context;

		public QueryHandler(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		// todo lgy
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			isQuery = false;
			processQueryComplete(context, cursor);
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);

		}
	}
	
	protected void processQueryComplete(Context context, Cursor cursor) {
	
	}

	private ActionModeListener mActionModeListener = new ActionModeListener() {

		@Override
		public void onActionItemClicked(Item item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case ActionMode.POSITIVE_BUTTON:
				int checkedCount = mAdapter.getCheckedItem().size();
				int all = mAdapter.getCount();
				selectAll(checkedCount < all);
				break;
			case ActionMode.NAGATIVE_BUTTON:
				safeQuitDeleteMode();
				break;
			default:
			}

		}

		@Override
		public void onActionModeDismiss(ActionMode arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onActionModeShow(ActionMode arg0) {
			// TODO Auto-generated method stub
			updateActionMode();
		}
	};
	
	protected void init() {
		mContext = this;
		mContentResolver = getContentResolver();
		
		mList.setOnItemClickListener(this);
		mList.setOnItemLongClickListener(this);
		
		mQueryHandler = new QueryHandler(mContentResolver, mContext);
		startQuery();
		mContentResolver.registerContentObserver(mListUri, true, changeObserver);
		
		initToolBar();
		initBottomMenuAndActionbar();
	}

	protected void initBottomMenuAndActionbar() {
		mSelectAllStr = getResources().getString(R.string.select_all);
		mUnSelectAllStr = getResources().getString(R.string.deselect_all);
		mActionMode = getActionMode();
		mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
		setActionModeListener(mActionModeListener);
	}

	protected void initToolBar() {
		//myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
	    myToolbar = getToolbar();
		myToolbar.setOnMenuItemClickListener(this);
	}
	
	@Override
    public void onNavigationClicked(View view) {
        finish();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.changeCursor(null);
		}
		mContentResolver.unregisterContentObserver(changeObserver);
		hideDeleteDialog();
	}
	

	private void selectAll(boolean checked) {
		for (int position = 0; position < mAdapter.getCount(); ++position) {
			if (checked) {
				mAdapter.setCheckedItem(position);
			} else {
				mAdapter.clearCheckedItem();
			}

			int realPos = position - mList.getFirstVisiblePosition();
			if (realPos >= 0) {
				View view = mList.getChildAt(realPos);
				if (view != null) {
					final CheckBox checkBox = (CheckBox) view
							.findViewById(R.id.list_item_check_box);
					if (null != checkBox) {
						checkBox.setChecked(checked);
					}
				}
			}
		}

		updateActionMode();
	}

	private void updateActionMode() {
		if (mAdapter == null) {
			finish();
			return;
		}

		int checkedCount = mAdapter.getCheckedItem().size();
		int all = mAdapter.getCount();

		if (checkedCount >= all) {
			mActionMode.setPositiveText(mUnSelectAllStr);
		} else {
			mActionMode.setPositiveText(mSelectAllStr);
		}
		updateBottomMenuItems(checkedCount);

		updateActionModeTitle(this.getString(R.string.selected_total_num,
				checkedCount));
	}

	protected void updateBottomMenuItems(int checkedCount) {
	    mBottomNavigationView.setEnabled(checkedCount > 0);
	}

	private void safeQuitDeleteMode() {
		try {
			Thread.sleep(300);
			changeToNormalMode(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void changeToNormalMode(boolean flag) {
		initActionBar(false);

		try {
			mAdapter.clearCheckedItem();
			mAdapter.setCheckBoxEnable(false);
			mAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initActionBar(boolean flag) {
		showActionMode(flag);
		mBottomNavigationView.setVisibility(flag ? View.VISIBLE : View.GONE);
	}


	
	protected void showDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMessage(getResources().getString(R.string.removing));
		}
		mProgressDialog.show();
	}

	protected void hideDialog() {
	    if(mProgressDialog != null) {
	        mProgressDialog.dismiss();
	    }
	}

	private Runnable mDeletingRunnable = new Runnable() {
        @Override
        public void run() {
            mBaseHandler.postDelayed(mShowDeletingProgressDialogRunnable, DELAY_TIME);
        }
    };

    private Runnable mShowDeletingProgressDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDeleteProgressDialog == null) {
                mDeleteProgressDialog = createDeleteProgressDialog();
            }
            mDeleteProgressDialog.show();
        }
    };

    private ProgressDialog createDeleteProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setInformationVisibility(false);
        //dialog.setProgress(mDeleteThreadCount);
        dialog.setMessage(getText(R.string.dels));
        return dialog;
    }

    protected void showDeleteDialog() {
        isOperating = true;
        if (mDeletingRunnable != null) {
            mDeletingRunnable.run();
        }
    }

    protected void hideDeleteDialog() {
        isOperating = false;
        mBaseHandler.removeCallbacks(mShowDeletingProgressDialogRunnable);
        if (mDeleteProgressDialog != null && mDeleteProgressDialog.isShowing()) {
            mDeleteProgressDialog.dismiss();
        }
        //startQuery();
    }
	
	protected static final int SHOW_BUSY = 0;
	protected static final int SHOW_LIST = 1;
	protected static final int SHOW_EMPTY = 2;
	protected static final int DIALOG_REFRESH = 1;
	private int mState;
	protected void updateState(int state) {
		if (mState == state) {
			return;
		}

		mState = state;
		switch (state) {
		case SHOW_LIST:
			mEmpty.setVisibility(View.GONE);
			mList.setVisibility(View.VISIBLE);
			break;
		case SHOW_EMPTY:
			mList.setVisibility(View.GONE);
			mEmpty.setVisibility(View.VISIBLE);
			break;
		case SHOW_BUSY:
			mList.setVisibility(View.GONE);
			mEmpty.setVisibility(View.GONE);
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (isInDeleteMode()) {
					this.changeToNormalMode(true);
					return true;
				}
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					return true;
				}
				break;
			case KeyEvent.KEYCODE_MENU: {
	
				return true;
	
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private boolean isInDeleteMode() {
		return getActionMode().isShowing();
	}
	
	protected void showDialogMenu(final int pos) {
		
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}

	@Override
    public void onSliderButtonClick(int id, View view, ViewGroup parent) {
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (isInDeleteMode()) {
			selectItem(view, position);
		} else {
			showDialogMenu(position);
		}

	}
	
	protected void selectItem(View view, int position) {
		CheckBox mCheckBox = (CheckBox) view
				.findViewById(R.id.list_item_check_box);
		if (mCheckBox == null) {
			return;
		}
		boolean isChecked = mCheckBox.isChecked();
		mCheckBox.setChecked(!isChecked);

		if (!isChecked) {
			mAdapter.setCheckedItem(position);
		} else {
			mAdapter.removeCheckedItem(position);
		}

		updateActionMode();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		if (!isInDeleteMode()) {
			mAdapter.setCheckedItem(position);
			mAdapter.setCheckBoxEnable(true);
			mAdapter.notifyDataSetChanged();
			initActionBar(true);
			updateActionMode();
			return true;
		}
		return false;

	}

}