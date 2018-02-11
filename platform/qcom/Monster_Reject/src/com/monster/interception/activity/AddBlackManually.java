package com.monster.interception.activity;

import java.util.ArrayList;
import java.util.List;

import mst.app.MstActivity;
import mst.app.dialog.ProgressDialog;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;

import com.monster.interception.util.BlackUtils;
import com.monster.interception.util.ContactUtils;
import com.monster.interception.util.FormatUtils;
import com.monster.interception.InterceptionApplication;
import com.monster.interception.util.YuloreUtil;
import com.monster.interception.R;

public class AddBlackManually extends MstActivity implements
		OnMenuItemClickListener {
	private static final String TAG = "AddBlackManually";
	private ContentResolver mContentResolver;
	private AsyncQueryHandler mQueryHandler;

	private CheckBox mPhoneCheckBox, mSmsCheckBox;
	private EditText mNumberEdit, mNameEdit;
	private String mNumber;
	private String mName;
	private Bundle mBundle;
	private String mTargetId;
	private String mType;
	private boolean mIsAddFromOther = false;
	private ProgressDialog mPregressDialog;
	private String mLable = null;
	private int userMark = -1;
	private String mNumberOrig = "";
	private String mNameOrig = "";
	private boolean mShowAddToast = true;
	private boolean touch = false;
	private Context mContext;

	private Handler mDatabaseHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (mPregressDialog != null) {
				mPregressDialog.dismiss();
			}
			ContentResolver cr = getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put("isblack", 1);
			cv.put("lable", mLable);
			cv.put("user_mark", userMark);
			cv.put("number", mNumber);
			cv.put("black_name", mName);
			if (mPhoneCheckBox.isChecked() && mSmsCheckBox.isChecked()) {
				cv.put("reject", 3);
			} else if (!mPhoneCheckBox.isChecked() && mSmsCheckBox.isChecked()) {
				cv.put("reject", 2);
			} else if (mPhoneCheckBox.isChecked() && !mSmsCheckBox.isChecked()) {
				cv.put("reject", 1);
			}
			if (mBundle == null || mIsAddFromOther) {
			    if(mIsExsit) {
	                  cr.update(BlackUtils.BLACK_URI, cv, getPhoneNumberEqualString(mNumber), null);
			    } else {
	                 cr.insert(BlackUtils.BLACK_URI, cv);
			    }
			} else {
				cr.update(BlackUtils.BLACK_URI, cv, "_ID=?",
						new String[] { mTargetId });
			}
			touch = false;
			if (mShowAddToast) {
			    Toast.makeText(
                    mContext,
                    mContext.getResources().getString(
                            R.string.add_to_black_over),
                    Toast.LENGTH_LONG).show();
			}
			AddBlackManually.this.finish();

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manually_add);
		//setMstContentView(R.layout.manually_add);

		mContext = this;
		mContentResolver = getContentResolver();

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		mNumberEdit = (EditText) findViewById(R.id.number);
		mNameEdit = (EditText) findViewById(R.id.name);
		//begin tangyisen
		mNumberEdit.setHintTextColor(R.color.hint_text_color);
		mNameEdit.setHintTextColor(R.color.hint_text_color);
		//end tangyisen
		mPhoneCheckBox = (CheckBox) findViewById(R.id.phone);
		//mPhoneCheckBox.setChecked(true);
		mSmsCheckBox = (CheckBox) findViewById(R.id.sms);
		//mSmsCheckBox.setChecked(true);

		mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mNumberOrig = mBundle.getString("add_number");
			mNumberEdit.setText(mNumberOrig);
			mNumberEdit.setSelection(mNumberOrig.length());
		    mNameOrig = mBundle.getString("add_name");
			if (mNameOrig != null && !"".equals(mNameOrig)) {
				mNameEdit.setText(mNameOrig);
			}
			mTargetId = mBundle.getString("targetId");
			mIsAddFromOther = mBundle.getBoolean("add");
			mType = mBundle.getString("type");
			if(TextUtils.isEmpty(mType)) {
			    mPhoneCheckBox.setChecked(true);
	            mSmsCheckBox.setChecked(true);
			} else {
			    if (mType.equals("1")) {
	                mPhoneCheckBox.setChecked(true);
	                mSmsCheckBox.setChecked(false);
	            } else if(mType.equals("2")){
	                mPhoneCheckBox.setChecked(false);
	                mSmsCheckBox.setChecked(true);
	            } else if(mType.equals("3")){
	                mPhoneCheckBox.setChecked(true);
	                mSmsCheckBox.setChecked(true);
	            } else{
	                mPhoneCheckBox.setChecked(false);
	                mSmsCheckBox.setChecked(false);
	            }
			}
		} else {
		    mPhoneCheckBox.setChecked(true);
		    mSmsCheckBox.setChecked(true);
		}

		/*mNumberEdit
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(15) });
		mNameEdit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				15) });*/

		mPhoneCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mSmsCheckBox.isChecked() && !mPhoneCheckBox.isChecked()) {
					mPhoneCheckBox.setChecked(true);
					showAtLeastToast();
				}
			}
		});

		mSmsCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mPhoneCheckBox.isChecked() && !mSmsCheckBox.isChecked()) {
					mSmsCheckBox.setChecked(true);
					showAtLeastToast();
				}
			}
		});

		mNumberEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				mNumber = mNumberEdit.getText().toString().replace("-", "")
						.replace(" ", "");
				if (mNumber != null || !"".equals(mNumber)) {
					String contactName = ContactUtils
							.getContactNameByPhoneNumber(mContext, mNumber);
					if (contactName != null) {
						mNameEdit.setText(contactName);
					} else {
						mNameEdit.setText("");

					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		initToolBar();

		mQueryHandler = new QueryHandler(mContentResolver, this);
		startQuery();

	}

	private void showAtLeastToast() {
		Toast.makeText(mContext,
				mContext.getResources().getString(R.string.at_least_one),
				Toast.LENGTH_LONG).show();
	}

	private void showDialog() {
		if (mPregressDialog != null) {
			mPregressDialog.dismiss();
		}
		mPregressDialog = new ProgressDialog(this);
		mPregressDialog.setIndeterminate(true);
		mPregressDialog.setCancelable(false);
		mPregressDialog.setMessage(getResources()
				.getString(R.string.save_title));
		mPregressDialog.show();
	}

	@Override
	public void onBackPressed() {
		doCanelAction();
	}

	protected void doCanelAction() {

		// TODO Auto-generated method stub
		if ((mNumberEdit.getText().toString() == null || "".equals(mNumberEdit
				.getText().toString()))
				&& (mNameEdit.getText().toString() == null || ""
						.equals(mNameEdit.getText().toString()))
				&& mPhoneCheckBox.isChecked()
				&& mSmsCheckBox.isChecked()
				&& !mIsAddFromOther) {
			finish();
		} else {
			AlertDialog dialogs = new AlertDialog.Builder(mContext)
					/*.setTitle(
							mContext.getResources().getString(
									R.string.black_add_manually_discard_title))*/
					.setMessage(R.string.black_add_manually_discard_content)
					.setPositiveButton(
							mContext.getResources().getString(
									R.string.confirm),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									AddBlackManually.this.finish();
								}

							})
							.setNegativeButton(mContext.getResources().getString(
                                R.string.cancel), null).show();
			dialogs.setCanceledOnTouchOutside(false);

		}
	}

	protected void doSaveAction() {

		// TODO Auto-generated method stub
		if (touch) {
			return;
		}
		touch = true;
		mNumber = mNumberEdit.getText().toString().replace("-", "")
				.replace(" ", "");
		mName = mNameEdit.getText().toString().replace("-", "")
				.replace(" ", "");
		if(mNumber.equals(mNumberOrig) && mName.equals(mNameOrig)) {
		    mShowAddToast = false;
		}
		if (mNumber != null && !"".equals(mNumber)) {
			if (isNoneDigit()) {
				return;
			}
			List<String> blacklist = InterceptionApplication.getInstance()
					.getBlackList();
			if (mBundle == null || mIsAddFromOther) {
			    if (mNumber.startsWith("+86")) {
			        mNumber = mNumber.substring(3);
                }
			    if(blacklist != null && blacklist.contains(mNumber)) {
			        mIsExsit = true;
			    }
			    if (mIsExsit) {
			        showDuplicatedDialog();
			    } else {
		            saveInternal();
			    }
			} else {
				String str = null;
				blacklist.remove(mNumberOrig);
				if (mNumberOrig.startsWith("+86")) {
					str = mNumberOrig.substring(3);
					blacklist.remove(str);
				} else {
					str = "+86" + mNumberOrig;
					blacklist.remove(str);
				}

				if (mNumberOrig != null) {
	                if(blacklist != null && blacklist.contains(mNumber)) {
	                    mIsExsit = true;
	                }
	                if (mIsExsit) {
	                    showDuplicatedDialog();
	                } else {
	                    saveInternal();
	                }
				}
			}
		} else {
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.no_number),
					Toast.LENGTH_LONG).show();
			touch = false;
			return;
		}

	}

	private void showDuplicatedDialog() {
	    AlertDialog dialogs = new AlertDialog.Builder(mContext)
        .setMessage(R.string.duplicated_save_tip)
        .setPositiveButton(
                mContext.getResources().getString(
                        R.string.confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int which) {
                        dialog.dismiss();
                        saveInternal();
                    }
                })
                .setNegativeButton(mContext.getResources().getString(
                    R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int which) {
                        dialog.dismiss();
                        touch = false;
                    }
                }).show();
        dialogs.setCanceledOnTouchOutside(false);
	}

	private boolean isNoneDigit() {
		boolean isDigit = false;
		for (int i = 0; i < mNumber.length(); i++) {
			if (Character.isDigit(mNumber.charAt(i))) {
				isDigit = true;
			}
		}
		if (mNumber.indexOf('+', 1) > 0) {
			isDigit = false;
		}
		if (!isDigit) {
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.format),
					Toast.LENGTH_LONG).show();
			touch = false;
			return true;
		}
		return false;
	}

	private boolean mIsExsit = false;
	private void saveInternal() {
		mLable = YuloreUtil.getUserMark(mContext, mNumber);
		if (mLable == null) {
			showDialog();
			new Thread() {
				public void run() {
					try {
						mLable = YuloreUtil.getMarkContent(mNumber, mContext);
						userMark = YuloreUtil.getMarkNumber(mContext, mNumber);
						mDatabaseHandler.obtainMessage().sendToTarget();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

				};
			}.start();

		} else {
			userMark = -1;
			mDatabaseHandler.obtainMessage().sendToTarget();
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void startQuery() {
		mQueryHandler.startQuery(0, null, BlackUtils.BLACK_URI, null,
				"isblack=1 and reject in (1,2,3)", null, "_id desc");
	}

	private class QueryHandler extends AsyncQueryHandler {
		private final Context context;

		public QueryHandler(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);

			List<String> blackList = new ArrayList<String>();
			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					blackList.clear();
				} else {
					blackList.clear();
					do {
						String number = cursor.getString(cursor
								.getColumnIndex("number"));
						if (number == null) {
							continue;
						}
						String numberE164 = PhoneNumberUtils
								.formatNumberToE164(number,
										FormatUtils.getCurrentCountryIso(context));

						blackList.add(number);

						if (numberE164 != null && !number.equals(numberE164)) {
							blackList.add(numberE164);
							continue;
						}

						try { // modify in the future
							if (numberE164 != null
									&& numberE164.equals(number)
									&& FormatUtils.getCurrentCountryIso(context)
											.equals("CN")
									&& number.startsWith("+86")) {
								numberE164 = number.substring(3,
										number.length());
								blackList.add(numberE164);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} while (cursor.moveToNext());
				}
				InterceptionApplication.getInstance().setBlackList(blackList);
			}
			if (cursor != null) {
				cursor.close();
			}

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

	private TextView mCancel;
	private TextView mAdd;
	private void initToolBar() {
		/*Toolbar myToolbar = getToolbar();//(Toolbar) findViewById(R.id.my_toolbar);
		myToolbar.setTitle(R.string.manually);
		myToolbar.inflateMenu(R.menu.menu_addmanually);
		myToolbar.setTitle(R.string.cancel);
        myToolbar.inflateMenu(R.menu.menu_addmanually);
		myToolbar.setOnMenuItemClickListener(this);*/
	    //use actionBar to instead toolbar
	    /*mActionMode = getActionMode();
	    //showActionMode(true);
	    setActionModeListener(mActionModeListener);
	    mActionMode.setPositiveText(getString(R.string.confirm));
	    mActionMode.setNagativeText(getString(R.string.cancel));*/
	    mCancel = (TextView) findViewById(R.id.cancel_finish);
	    mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doCanelAction();
            }
        });
	    mAdd = (TextView) findViewById(R.id.confirm_add);
	    mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doSaveAction();
            }
        });
	}

	
	/*private ActionModeListener mActionModeListener = new ActionModeListener() {

        @Override
        public void onActionItemClicked(Item item) {
            // TODO Auto-generated method stub
            switch (item.getItemId()) {
            case ActionMode.POSITIVE_BUTTON:
                doSaveAction();
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
    };*/

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMenuItemClick--->" + item.getTitle());
		if (item.getItemId() == R.id.menu_add) {
			doSaveAction();
			return true;
		}
		return false;
	}
	
    private static String getPhoneNumberEqualString(String number) {
        return " PHONE_NUMBERS_EQUAL(number, \"" + number + "\", 0) ";
    }

}
