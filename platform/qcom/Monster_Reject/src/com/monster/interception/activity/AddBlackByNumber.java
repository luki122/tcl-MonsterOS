package com.monster.interception.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;
import mst.widget.toolbar.Toolbar;
import mst.widget.MstListView;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import android.provider.CallLog.Calls;
import android.provider.ContactsContract;

import com.monster.interception.R;
import com.monster.interception.adapter.AddBlackByCallLogAdapter;
import com.monster.interception.util.YuloreUtil;

public class AddBlackByNumber extends MstActivity implements OnItemClickListener {
	
	private static final String TAG = "AddBlackByNumber";
    private static Context mContext;
    
    private static final int QUERY_TOKEN = 1;
    
    
    private QueryHandler mQueryHandler;
    private static AddBlackByCallLogAdapter mAdapter;
    private MstListView mList;
    private static TextView mEmptyView;
    private String mBlackNumbers = null;
    
    public static HashMap<String, Integer> mCheckedItem = new HashMap<String, Integer>();
    private static boolean mIsAdding = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setMstContentView(R.layout.add_black_from_call_log_frag);
		mEmptyView = (TextView)findViewById(R.id.calllog_empty);
		mContext = this;
		
		mAdapter = new AddBlackByCallLogAdapter(mContext);
	    mList = (MstListView)findViewById(android.R.id.list);
	    mList.setItemsCanFocus(false);
	    mList.setOnItemClickListener(this);
	    mList.setAdapter(mAdapter);
	    mList.setFastScrollEnabled(false);
	    mList.setFastScrollAlwaysVisible(false);
	    
	    Bundle extras = getIntent().getExtras();
	    if (null != extras) {
	    	mBlackNumbers = extras.getString("blacknumbers");
	    }
		
	    mQueryHandler = new QueryHandler(this);
	    
		initBottomMenuAndActionbar();
	}

	@Override
    protected void onResume() {
	    super.onResume();
	    
	    startQuery();
	}
	
	@Override
    protected void onDestroy() {
		mCheckedItem.clear();
		
		super.onDestroy();
		if(mAdapter != null) {
			mAdapter.changeCursor(null);
		}
	}

	private void startQuery() {
		String selection = null;
		if (mBlackNumbers != null) {
			selection = "number not in(" + mBlackNumbers + ")";
		}
		mQueryHandler.startQuery(QUERY_TOKEN, null, 
				Uri.parse("content://call_log/mstcallsjoindataview"), 
				AddBlackByCallLogAdapter.CALL_LOG_PROJECTION,
				selection, null, "_id DESC");
	}
	
	private final class QueryHandler extends AsyncQueryHandler {
		private final WeakReference<AddBlackByNumber> mActivity;

		/**
		 * Simple handler that wraps background calls to catch
		 * {@link SQLiteException}, such as when the disk is full.
		 */
		protected class CatchingWorkerHandler extends
				AsyncQueryHandler.WorkerHandler {
			public CatchingWorkerHandler(Looper looper) {
				super(looper);
			}

			@Override
			public void handleMessage(Message msg) {
				try {
					// Perform same query while catching any exceptions
					super.handleMessage(msg);
				} catch (SQLiteDiskIOException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteFullException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteDatabaseCorruptException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				}
			}
		}

		@Override
		protected Handler createHandler(Looper looper) {
			// Provide our special handler that catches exceptions
			return new CatchingWorkerHandler(looper);
		}

		public QueryHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<AddBlackByNumber>(
					(AddBlackByNumber) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			final AddBlackByNumber activity = mActivity.get();
			if (activity != null && !activity.isFinishing()) {
				final AddBlackByCallLogAdapter callsAdapter = activity.mAdapter;
				callsAdapter.clearCheckedItem();
				callsAdapter.changeCursor(cursor);
				
				if (cursor == null) {
					Log.e(TAG, "onQueryCompleted - cursor is null");
					mEmptyView.setVisibility(View.VISIBLE);
					return;
				}
                
				mCheckedItem.clear();
				setBottomMenuEnable(false);
				if (cursor.getCount() == 0) {
					mEmptyView.setVisibility(View.VISIBLE);
				} else {
				    if(!isInDeleteMode()) {
				        initActionBar(true);
				    }
					mEmptyView.setVisibility(View.GONE);			
				}
				
                Log.i(TAG, "onQueryCompleted - Count:" + cursor.getCount());
			} else {
				if(cursor != null) {
					cursor.close();
				}
			}
		}
	}
	
	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter == null) {
			mEmptyView.setVisibility(View.VISIBLE);
			return;
		}
		
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.list_item_check_box);
		if (null != checkBox) {
            boolean checked = checkBox.isChecked();
            checkBox.setChecked(!checked);
            String name = mAdapter.getName(position);
            String number = mAdapter.getNumber(position);
            if (number == null) {
            	return;
            }
            Log.i(TAG, "checked="+checked);
            if (!checked) {
            	mCheckedItem.put(number, position);
            	mAdapter.setCheckedItem(number);
            } else {
            	mCheckedItem.remove(number);
            	mAdapter.removeCheckedItem(number);
            }
            
            updateActionMode();
        }
	}
	
	private static class MyProgressDialog extends ProgressDialog {
        public MyProgressDialog(Context context) {
            super(context);
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (mIsAdding) {
                    return true;
                }
                break;
            }
            }
            
            return super.onKeyDown(keyCode, event);
        }
    };
    
    private MyProgressDialog mSaveProgressDialog = null;
    private static final int START = 0;
    private static final int END = 1;
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            case START: {
            	mIsAdding = true;
            	
                if (!isFinishing()) {
                    if (null == mSaveProgressDialog) {
                        mSaveProgressDialog = new MyProgressDialog(mContext);
                    }
                    mSaveProgressDialog.setTitle(R.string.save_title);
                    mSaveProgressDialog.setIndeterminate(false);
                    mSaveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    try {
                        mSaveProgressDialog.show();
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            
            case END: {
            	mIsAdding = false;
                if (!isFinishing() 
                        && null != mSaveProgressDialog && mSaveProgressDialog.isShowing()) {
                    try {
                        mSaveProgressDialog.dismiss();
                        mSaveProgressDialog = null;
                        finish();
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            }
            
            super.handleMessage(msg);
        }
    
    };
    
    private void addContacts() {
    	int selectedCount = mCheckedItem.size();
        if (0 >= selectedCount) {
            return;
        }
        
        if (selectedCount > 100) {
        	Toast.makeText(mContext, R.string.select_more_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        Set<String> numbers = mCheckedItem.keySet();
        ArrayList<String> numbersForBlack = new ArrayList<String>();
        ArrayList<String> namesForBlack = new ArrayList<String>();
        ArrayList<String> markForBlack = new ArrayList<String>();
        for (String number : numbers) {
        	numbersForBlack.add(number);
        	int position = mCheckedItem.get(number);
        	Cursor cursor = (Cursor) mAdapter.getItem(position);
	        if (cursor != null) {
	        	namesForBlack.add(cursor.getString(2));
	        	markForBlack.add(cursor.getString(4));
	        	Log.d(TAG, "number = " + number + "   name = " + cursor.getString(2) + "  mark = " + cursor.getString(4));
	        }
        }
        
        if (mIsAdding) {
        	return;
        }
        
        new AddBlackNameThread(namesForBlack, numbersForBlack, markForBlack).start();
    }
    
    private class AddBlackNameThread extends Thread {
        ArrayList<String> nameForBlackName = new ArrayList<String>();
        ArrayList<String> numberForBlackName = new ArrayList<String>();
        ArrayList<String> markForBlackName = new ArrayList<String>();
        ArrayList<String> numberAdded = new ArrayList<String>();
        
        public AddBlackNameThread(ArrayList<String> nameList, ArrayList<String> numberList, ArrayList<String> markList) {
            this.nameForBlackName = nameList;
            this.numberForBlackName = numberList;
            this.markForBlackName = markList;
        }
        
        @Override
        public void run() {
            if (numberForBlackName == null || numberForBlackName.size() < 1) {
                return;
            }
            
            mHandler.sendEmptyMessage(START);
            ContentValues values = new ContentValues();
            
            for (int i = 0; i < numberForBlackName.size(); i++) {
            	String number = numberForBlackName.get(i);
            	if (numberAdded.contains(number)) {
            		continue;
            	}
            	
            	values.put("isblack", 1);
            	values.put("black_name", nameForBlackName.get(i));
            	values.put("number", number);
            	values.put("reject", 3);
            	String mark = YuloreUtil.getUserMark(mContext, number);
            	int userMark = -1;
            	Log.d(TAG, "number = " + number + "  mark =  " + mark);
            	if (mark == null) {
//            		mark = YuloreUtil.getMarkContent(number);
            		userMark = YuloreUtil.getMarkNumber(mContext, number);
            	}
            	if (null != mark) {
            		Log.i(TAG, "mark="+mark);
            		values.put("lable", mark);
            		values.put("user_mark", userMark);
            	}
            	
            	mContext.getContentResolver().insert(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), values);
                values.clear();
                numberAdded.add(number);
                try {
                	sleep(200);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
            
            mHandler.sendEmptyMessage(END);
            mCheckedItem.clear();
        }
    }
    
    private String mSelectAllStr;
	private String mUnSelectAllStr;
	private BottomNavigationView mBottomNavigationView;
	public ActionMode mActionMode;
	private ActionModeListener mActionModeListener = new ActionModeListener() {

		@Override
		public void onActionItemClicked(Item item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case ActionMode.POSITIVE_BUTTON:
				int checkedCount = mCheckedItem.size();
				int all = mAdapter.getCount();
				selectAll(checkedCount < all);
				break;
			case ActionMode.NAGATIVE_BUTTON:
//				safeQuitDeleteMode();
			    finish();
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

	private void selectAll(boolean checked) {
		for (int position = 0; position < mAdapter.getCount(); ++position) {
			  String number = mAdapter.getNumber(position);
			if (checked) {				
			   	mAdapter.setCheckedItem(number);
				mCheckedItem.put(number, position);
			} else {
			   	mAdapter.removeCheckedItem(number);
				mCheckedItem.remove(number);
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

	private boolean isInDeleteMode() {
		return getActionMode().isShowing();
	}


	private void updateActionMode() {
		if (mAdapter == null) {
			finish();
			return;
		}
		int checkedCount = mCheckedItem.size();
		int all = mAdapter.getCount();

		if (checkedCount >= all) {
			mActionMode.setPositiveText(mUnSelectAllStr);
		} else {
			mActionMode.setPositiveText(mSelectAllStr);
		}
		
		//mActionMode.setNagativeText("");

		if (checkedCount > 0) {
			setBottomMenuEnable(true);
		} else {
			setBottomMenuEnable(false);
		}

		updateActionModeTitle(mContext.getString(R.string.selected_total_num,
				checkedCount));
	}

	private void setBottomMenuEnable(boolean flag) {
		mBottomNavigationView.setEnabled(flag);
	}


	private void initActionBar(boolean flag) {
		showActionMode(flag);
		mBottomNavigationView.setVisibility(flag ? View.VISIBLE : View.GONE);
	}

	private void initBottomMenuAndActionbar() {
		mSelectAllStr = mContext.getResources().getString(R.string.select_all);
        mUnSelectAllStr = mContext.getResources().getString(R.string.deselect_all);
		mActionMode = getActionMode();
		mActionMode.setNagativeText(mContext.getResources().getString(R.string.cancel));
		mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
		mBottomNavigationView
				.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.menu_add:
							addContacts();
							return true;
						default:
							return false;
						}
					}
				});
		setActionModeListener(mActionModeListener);
		updateActionMode();
	}
	
}
