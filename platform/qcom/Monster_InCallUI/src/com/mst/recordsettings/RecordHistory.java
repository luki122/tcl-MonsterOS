package com.mst.recordsettings;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;

import com.android.incallui.InCallApp;
import com.android.incallui.R;
import com.mediatek.telecom.recording.RecorderUtils;
import com.mst.recordsettings.RecordHistoryAdapter.updateListener;

import mst.app.MstActivity;
import mst.widget.ActionMode;
import mst.widget.ActionMode.Item;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.MstListView;

public class RecordHistory extends MstActivity implements OnItemClickListener,
		updateListener {

	private static final String TAG = "RecordHistory";
	private static String mSelectAllStr;
	private static String mUnSelectAllStr;
	private Context mContext;

	private static RecordHistoryAdapter mAdapter;
	private ArrayList<CallRecord> mRecords = new ArrayList<CallRecord>();
	private MstListView mListView;
	private static View mEmptyView;
	private static ProgressBar mProgress;
	private RecordProgressDialog mSaveProgressDialog = null;
	private static final int START = 0;
	private static final int END = 1;
	private static final int REFRESH = 2;
	private static final int WAIT_CURSOR_START = 3;
	private static final int WAIT_CURSOR_END = 4;
	private static final long WAIT_CURSOR_DELAY_TIME = 500;
	private static boolean isFinished = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.call_record_history);
		mEmptyView = findViewById(R.id.calllog_empty);
		mProgress = (ProgressBar) findViewById(R.id.progress_loading);
		mContext = RecordHistory.this;
		mSelectAllStr = mContext.getResources().getString(R.string.select_all);
		mUnSelectAllStr = mContext.getResources().getString(
				R.string.unselect_all);

		initListView();

		initBottomMenuAndActionbar();
		initToolBar();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		buildData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.setListener(null);
	}

	private void initListView() {
		mListView = (MstListView) findViewById(android.R.id.list);
		mListView.setItemsCanFocus(true);
		mListView.setOnItemClickListener(this);
		mListView.setFastScrollEnabled(false);
		mListView.setFastScrollAlwaysVisible(false);
		mListView.setOnCreateContextMenuListener(this);
	}

	private void initActionBar(boolean flag) {
		showActionMode(flag);
		mBottomNavigationView.setVisibility(flag ? View.VISIBLE : View.GONE);
	}

	private void buildData() {
		isFinished = false;
		mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
				WAIT_CURSOR_DELAY_TIME);

		String historyPath = RecorderUtils.getRecordPath();
		mRecords = RecordParseUtils.parseRecording(historyPath);

		mAdapter = new RecordHistoryAdapter(mContext, mRecords);
		mAdapter.setListener(this);
		mListView.setAdapter(mAdapter);
		isFinished = true;
		mHandler.sendEmptyMessage(WAIT_CURSOR_END);

	}

	private void refresh() {
		mAdapter.setRecords(mRecords);
	}

	private void selectAll(boolean checked) {
		for (int position = 0; position < mAdapter.getCount(); ++position) {
			if (checked) {
				mAdapter.setCheckedItem(String.valueOf(position),
						mRecords.get(position));
			} else {
				mAdapter.clearCheckedItem();
			}

			int realPos = position - mListView.getFirstVisiblePosition();
			if (realPos >= 0) {
				View view = mListView.getChildAt(realPos);
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);

		View targetView = ((AdapterContextMenuInfo) menuInfo).targetView;

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
		
		if(isInDeleteMode()) {
		    return;
		}

		Log.d(TAG, "info.id = " + info.id + "   info.po = " + info.position);
		int pos = info.position;
		mAdapter.setCheckedItem(String.valueOf(pos), mRecords.get(pos));

		mAdapter.setCheckBoxEnable(true);
		mAdapter.setNeedAnim(true);
		mAdapter.notifyDataSetChanged();
		initActionBar(true);
		updateActionMode();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mAdapter == null) {
			mEmptyView.setVisibility(View.VISIBLE);
			return;
		}

		if (!isInDeleteMode()) {
			String historyPath = mRecords.get(position).getPath();
			playRecord(historyPath);
			return;
		}

		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.list_item_check_box);
		if (null != checkBox) {
			boolean checked = checkBox.isChecked();
			checkBox.setChecked(!checked);

			if (!checked) {
				mAdapter.setCheckedItem(String.valueOf(position),
						mRecords.get(position));
			} else {
				mAdapter.removeCheckedItem(String.valueOf(position));
			}

			updateActionMode();
		}
	}

	private void changeToNormalMode(boolean flag) {
		initActionBar(false);

		try {
			mAdapter.clearCheckedItem();
			mAdapter.setNeedAnim(true);
			mAdapter.setCheckBoxEnable(false);
			if (!flag) {
				refresh();
			}
			mAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void playRecord(String path) {
		Log.i(TAG, "playRecord path = " + path);
		Uri data = Uri.fromFile(new File(path));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, "audio/amr");
//		intent.setClassName("com.android.music",
//				"com.android.music.AudioPreview");
       intent.setClassName("cn.tcl.music", "cn.tcl.music.activities.AudioPreview");
		try {
			mContext.startActivity(intent);
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.no_music_activity), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO move to the fragment
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {
			if (isInDeleteMode()) {
				safeQuitDeleteMode();
				return true;
			}
			break;
		}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void safeQuitDeleteMode() {
		try {
			Thread.sleep(300);
			changeToNormalMode(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class RecordProgressDialog extends ProgressDialog {
		public RecordProgressDialog(Context context) {
			super(context);
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK: {
				if (this.isShowing()) {
					return true;
				}
				break;
			}
			}

			return super.onKeyDown(keyCode, event);
		}
	};

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case WAIT_CURSOR_START: {
				if (!isFinished) {
					mProgress.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				}
				break;
			}

			case WAIT_CURSOR_END: {
				isFinished = true;
				mProgress.setVisibility(View.GONE);

				if (mRecords.size() <= 0) {
					mEmptyView.setVisibility(View.VISIBLE);
				} else {
					mEmptyView.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
				}

				break;
			}

			case REFRESH: {
				isFinished = true;

				if (mRecords.size() <= 0) {
					mEmptyView.setVisibility(View.VISIBLE);
				} else {
					mEmptyView.setVisibility(View.GONE);
				}

				break;
			}

			case START: {

				if (!isFinishing()) {
					if (null == mSaveProgressDialog) {
						mSaveProgressDialog = new RecordProgressDialog(mContext);
					}
					// mSaveProgressDialog.setTitle(R.string.save_title);
					mSaveProgressDialog.setIndeterminate(false);
					mSaveProgressDialog
							.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					try {
						mSaveProgressDialog.show();
					} catch (Exception e) {

					}
				}
				break;
			}

			case END: {
				if (!isFinishing() && null != mSaveProgressDialog
						&& mSaveProgressDialog.isShowing()) {
					try {
						mSaveProgressDialog.dismiss();
						mSaveProgressDialog = null;
						mAdapter.setCheckBoxEnable(false);
						changeToNormalMode(false);
					} catch (Exception e) {

					}
				}
				break;
			}
			}

			super.handleMessage(msg);
		}

	};

	private void deleteCallRecord() {
		int selectedCount = mAdapter.getCheckedItem().size();
		if (0 >= selectedCount) {
			return;
		}

		AlertDialog dialog = null;
		if (null == dialog) {
			dialog = new AlertDialog.Builder(mContext)
					.setTitle(R.string.call_record_delete_title)
					// .setMessage(R.string.call_record_delete_message)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mHandler.sendEmptyMessage(START);

									new deleteThread(mAdapter.getCheckedItem())
											.start();
								}
							}).create();
		}

		dialog.show();
	}

	private void deleteAllCallRecord() {

		AlertDialog dialog = null;
		if (null == dialog) {
			dialog = new AlertDialog.Builder(mContext)
					.setTitle(R.string.call_record_delete_all_title)
					// .setMessage(R.string.call_record_delete_all_message)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(R.string.clear_all,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mHandler.sendEmptyMessage(START);
									HashMap<String, CallRecord> filelist = new HashMap<String, CallRecord>();
									for (int position = 0; position < mAdapter
											.getCount(); ++position) {
										filelist.put(String.valueOf(position),
												mRecords.get(position));
									}

									new deleteThread(filelist).start();
								}
							}).create();
		}

		dialog.show();
	}

	private class deleteThread extends Thread {

		private HashMap<String, CallRecord> deleteItems = new HashMap<String, CallRecord>();

		public deleteThread(HashMap<String, CallRecord> items) {
			deleteItems = items;
		}

		@Override
		public void run() {
			if (deleteItems == null || deleteItems.size() <= 0) {
				return;
			}

			CallRecord acr = null;
			String path = null;
			Set<String> pisitions = deleteItems.keySet();
			Object[] pos = pisitions.toArray();
			try {
				for (int i = 0; i < deleteItems.size(); i++) {
					acr = deleteItems.get(pos[i]);
					path = acr.getPath();
					File file = new File(path);
					if (file.exists()) {
						boolean move = file.delete();
						mRecords.remove(acr);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			mHandler.sendEmptyMessage(END);
			mHandler.sendEmptyMessage(REFRESH);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.record_history_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_all_record:
			deleteAllCallRecord();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void update() {
		mHandler.sendEmptyMessage(REFRESH);
	}

	private BottomNavigationView mBottomNavigationView;
	public ActionMode mActionMode;
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

	private void initBottomMenuAndActionbar() {
		mActionMode = getActionMode();
		mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
		mBottomNavigationView
				.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.delete_record:
							deleteCallRecord();
							return true;
						default:
							return false;
						}
					}
				});
		setActionModeListener(mActionModeListener);
	}

	private boolean isInDeleteMode() {
//		return getActionMode().isShowing();
		return mBottomNavigationView.getVisibility() == View.VISIBLE;
	}

	private void setBottomMenuEnable(boolean flag) {
		mBottomNavigationView.setEnabled(flag);
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

		if (checkedCount > 0) {
			setBottomMenuEnable(true);
		} else {
			setBottomMenuEnable(false);
		}

		updateActionModeTitle(mContext.getString(R.string.selected_total_num,
				checkedCount));
	}

	private void initToolBar() {
		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		myToolbar.setTitle(R.string.record_history);
		myToolbar.setNavigationIcon(com.mst.R.drawable.ic_recede);
        
		myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
        });
	}

}
