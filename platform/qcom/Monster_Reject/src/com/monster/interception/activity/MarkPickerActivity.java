package com.monster.interception.activity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.widget.ActionMode;
import mst.widget.ActionMode.Item;
import mst.widget.ActionModeListener;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.OnItemClickListener;

import com.monster.interception.R;

public class MarkPickerActivity extends MstActivity implements
		OnItemClickListener {
	
	private static final String TAG = "MarkPickerActivity";
	private Context mContext = null;
	private static Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
	
	protected ActionMode mActionMode;
	
	private ListView mListView;
	private LinearLayout mNewMarkLayout;
	private LinearLayout mNoMarkLayout;
	private MarkAdapter mAdapter;
	private QueryHandler mQueryHandler;
	private boolean mIsEdit = false;
	private ArrayList<String> mMarkList = new ArrayList<String>();
	private int mCheckedPosition = -1;
	private String mSelectedMark = null;
	private static String mUserMarkOrgi = null;
	private static String mNoMarkStr = null;
	private CheckedTextView mNoMarkCTV = null;
	private Handler mHanlder = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setMstContentView(R.layout.mark_picker_activity);
		mContext = this;
        
	    initActionbar();
		
		mAdapter = new MarkAdapter(mContext);
	    mListView = (ListView)findViewById(R.id.list);
	    mListView.setItemsCanFocus(false);
	    mListView.setOnItemClickListener(this);
	    mListView.setFastScrollEnabled(false);
	    mListView.setFastScrollAlwaysVisible(false);
	    
	    Bundle extras = getIntent().getExtras();
	    if (null != extras) {
	    	mSelectedMark = mUserMarkOrgi = extras.getString("user_mark");
	    	if (mUserMarkOrgi != null) {
	    		mIsEdit = true;
	    	}
	    }
	    
	    mNoMarkLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.no_mark, null);
	    mNoMarkCTV = (CheckedTextView) mNoMarkLayout.findViewById(R.id.no_mark);
	    if (!mIsEdit && null != mNoMarkCTV) {
	    	mNoMarkCTV.setChecked(true);
		}
		mNoMarkLayout.setOnClickListener(new View.OnClickListener() {
		    	@Override
	            public void onClick(View v) {
		    		if (null != mNoMarkCTV) {
		    			mNoMarkCTV.setChecked(true);
		    		}
		    		mSelectedMark = null;
		    		mCheckedPosition = -1;
		    		startQuery();
		    		updateOkUi();
		    	}
		    });
		mListView.addHeaderView(mNoMarkLayout);
		
	    mNewMarkLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.new_mark, null);
	    mNewMarkLayout.setOnClickListener(new View.OnClickListener() {
	    	@Override
            public void onClick(View v) {
	    		createNewMark();
	    	}
	    });
	    mListView.addFooterView(mNewMarkLayout);
	    mListView.setAdapter(mAdapter);
	    
	    mNoMarkStr = mContext.getResources().getString(R.string.no_mark);
	    
	    updateOkUi();
		
	    mQueryHandler = new QueryHandler(this);
	    startQuery();
	}
	
	
	private boolean isFirstShow = true;
	@Override
    public void onResume() {
        super.onResume();
    }

	
	   private void initActionbar() {
	        String confirm = getResources().getString(R.string.confirm);
	        String cancel = getResources().getString(R.string.cancel);
	        mActionMode = getActionMode();
	        mActionMode.setPositiveText(confirm);	        
	        mActionMode.setNagativeText(cancel);     
	        setActionModeListener(mActionModeListener);
	    }
	   
	    private ActionModeListener mActionModeListener = new ActionModeListener() {

	        @Override
	        public void onActionItemClicked(Item item) {
	            // TODO Auto-generated method stub
	            switch (item.getItemId()) {
	            case ActionMode.POSITIVE_BUTTON:
                    final Intent intent = new Intent();
                    intent.putExtra("user_mark", mSelectedMark);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
	                break;
	            case ActionMode.NAGATIVE_BUTTON:
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
	        }

	    };
	
	private void updateOkUi() {
		if (mUserMarkOrgi == null && mSelectedMark == null && !mIsEdit) {
//    		mActionBar.getOkButton().setEnabled(false);
    	} else if (mUserMarkOrgi != null && mUserMarkOrgi == mSelectedMark) {
//    		mActionBar.getOkButton().setEnabled(false);
    	} else {
//    		mActionBar.getOkButton().setEnabled(true);
    	}
	}
	
	private void startQuery() {
		mQueryHandler.startQuery(0, null, mMarkUri, null, "lable is not null and number is null", null, null);
	}
	
	private class QueryHandler extends AsyncQueryHandler {
		private final WeakReference<MarkPickerActivity> mActivity;

		public QueryHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<MarkPickerActivity>(
					(MarkPickerActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			final MarkPickerActivity activity = mActivity.get();
			if (activity != null && !activity.isFinishing()) {
				final MarkAdapter adapter = activity.mAdapter;
				adapter.changeCursor(cursor);
				
				if (cursor == null) {
					Log.e(TAG, "onQueryCompleted - cursor is null");
					return;
				}
				
                Log.i(TAG, "onQueryCompleted - Count:" + cursor.getCount());
                /*mHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if(isFirstShow) {
                            //better when query complete to show action mode to true
                            showActionMode(true);
                            isFirstShow = false;
                        }
                    }
                });*/
                if(isFirstShow) {
                    //better when query complete to show action mode to true
                    showActionMode(true);
                    isFirstShow = false;
                }
			} else {
				cursor.close();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
		if (mNoMarkCTV != null) {
			mNoMarkCTV.setChecked(false);
		}
		
		int position = getRealPosition(arg2);
		mSelectedMark = mAdapter.getMark(position);
		setCheckedPosition(position);
	}
	
	private void setCheckedPosition(int checkedPosition) {
    	if (mCheckedPosition != checkedPosition) {
    		mCheckedPosition = checkedPosition;
    		startQuery();
    	}
    }
	
	private void createNewMark() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.new_mark_dia, null);
		  final EditText mark_content=(EditText) view.findViewById(R.id.mark_content);
		  mark_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
		  final AlertDialog dialog = new AlertDialog.Builder(mContext)
          .setTitle(mContext.getResources().getString(R.string.new_mark))
          .setView(view)
          .setPositiveButton(android.R.string.ok,
              new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int whichButton) {
                  	 String content = mark_content.getText().toString();
                  	 mSelectedMark = content;
                  	 if (!content.equals("")) {
                  		 if (content != null && content.replaceAll(" ", "").equals("")) {
                  			Toast.makeText(mContext, mContext.getResources().getString(R.string.mark_error), Toast.LENGTH_SHORT).show();
                  			return;
                  		 }
                  		 
                  		 if (!mMarkList.contains(content)) {
                       		ContentResolver cr = getContentResolver();
                    			ContentValues cv = new ContentValues();
                    			cv.put("lable", content);
                    			Uri uri2 = cr.insert(mMarkUri, cv);
                    			try {
                    				Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing" );
	                  			    field.setAccessible( true );
                 			    	field.set(dialog, true );
                 			    } catch(Exception e) {
                 			    	e.printStackTrace();
                 			    }
                    			
                    			if (mNoMarkCTV != null) {
                        			mNoMarkCTV.setChecked(false);
                        		}
                          		
                          		setCheckedPosition(mAdapter.getCount());
                  		 } else {
                  		    Toast.makeText(mContext, mContext.getResources().getString(R.string.mark_content_exist), Toast.LENGTH_SHORT).show();
                  			try {
                  				Field field = dialog.getClass()
	                  			            .getSuperclass().getDeclaredField(
	                  			                     "mShowing" );
	                  			    field.setAccessible( true );
                 			    	field.set(dialog, false );
                 			} catch(Exception e) {
                 			    	
                 			}
                  		 }
                  	 }
                  	 dialog.dismiss();
                  }
              }
          )
          .setNegativeButton(android.R.string.cancel, null)
          .show();
		  dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		  
		  mark_content.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					if (s != null && s.length() > 0) {
						dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
					} else {
						dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					}
				}
			  });
		  
		  dialog.setCanceledOnTouchOutside(false);
		  dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) ;
	}
	
	private class MarkAdapter extends CursorAdapter {
		private LayoutInflater mInflater;
		private Context context;
		
		public MarkAdapter(Context context) {
			super(context, null, false);
			this.context=context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
            	convertView = (CheckedTextView) mInflater.inflate(R.layout.mark_pick_item, parent, false);
            }
			
			Cursor cursor = mCursor;
			if (position <= cursor.getCount()) {
				cursor.moveToPosition(position);
			}
			
			bindView(convertView, mContext, cursor);
			
			return convertView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if (null == view) {
				return;
			}
			
			CheckedTextView checkedView = (CheckedTextView)view;
			
			int position = cursor.getPosition();
			String mark = cursor.getString(1);
			
			if (mark != null && !mMarkList.contains(mark)) {
				mMarkList.add(mark);
			}
			
			checkedView.setText(mark);
			Log.d(TAG, "position = " + position + "  mark = " + mark + "  mCheckedPosition = " + mCheckedPosition + "  mUserMarkOrgi = " + mUserMarkOrgi + " mSelectedMark = " + mSelectedMark);
			checkedView.setChecked(mCheckedPosition != -1 && mCheckedPosition == position);
			if (mark != null && mUserMarkOrgi != null && mSelectedMark != null && 
					mark.equals(mUserMarkOrgi) && mUserMarkOrgi.equals(mSelectedMark)) {
				checkedView.setChecked(true);
			}
			
			if (mCheckedPosition != -1 && mCheckedPosition == position) {
				mSelectedMark = mark;
				updateOkUi();
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			CheckedTextView view;
			view = (CheckedTextView) mInflater.inflate(R.layout.mark_pick_item, parent, false);
			return view;
		}
		
		public String getMark(int position) {
			Cursor cursor = (Cursor) getItem(position);
	        if (cursor == null) {
	            return null;
	        }
	        
	        String result = null;
	        try {
	        	result = cursor.getString(1);
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	        
	        return result;
		}

	}
	
	private int getRealPosition(int position) {
	    return (position - 1);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mAdapter.changeCursor(null);
	}
	
}
