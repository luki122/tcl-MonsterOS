package com.monster.interception.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.view.menu.BottomWidePopupMenu;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import mst.widget.MstListView;

import com.monster.interception.adapter.BlackAdapter;
import com.monster.interception.adapter.InterceptionAdapterBase;
import com.monster.interception.database.BlackItem;
import com.monster.interception.util.BlackUtils;
import com.monster.interception.util.ContactUtils;
import com.monster.interception.util.FormatUtils;
import com.monster.interception.util.InterceptionUtils;
import com.monster.interception.util.YuloreUtil;
import com.monster.interception.InterceptionApplication;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.OperationApplicationException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import mst.widget.SliderView;
import com.monster.interception.R;
import android.provider.ContactsContract.Data;

public class BlackList extends InterceptionActivityBase {
	private static final String TAG = "BlackList";

	private boolean isInBatchDel = false;

	private List<String> mBlackStringlist = new ArrayList<String>();
	private StringBuilder mBlackNumbers = new StringBuilder();
	private StringBuilder mBlackNumbersForQuery = new StringBuilder();

	private Handler mProgressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.arg1 == MESSAGE_START_DELETE) {
				//showDialog();
			    showDeleteDialog();
				Log.i(TAG, "isShowing=true");
			} else if (msg.arg1 == MESSAGE_END_DELETE){
				Log.i(TAG, "isShowing=false");
				startQuery();
				//hideDeleteDialog();
				changeToNormalMode(true);
			} else if (msg.arg1 == MESSAGE_START_INSERT){
                Log.i(TAG, "isShowing=true");
                showInsertDialog();
            } else if (msg.arg1 == MESSAGE_END_INSERT){
                Log.i(TAG, "isShowing=false");
                startQuery();
                //hideInsertDialog();
            }
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.black_name_list);
		setMstContentView(R.layout.black_name_list);
		mListUri = BlackUtils.BLACK_URI;
		init();
	}

	protected void init() {
	       mList = (MstListView) findViewById(R.id.black_name_list);
	        mEmpty = (TextView) findViewById(R.id.black_name_empty);
		super.init();
	}

	private boolean mIsFirst = true;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		/*if(mIsBackShowInsertDialg) {
		    showInsertDialog();
		}*/
		/*if (mIsFirst) {
			mIsFirst = false;
		} else if (!isInBatchDel) {
			startQuery();
			Log.i(TAG, "startQuery();");
		}*/
	}

	protected void startQuery() {
	    if (!isQuery) {
	        isQuery = true;
	        mQueryHandler.startQuery(0, null, mListUri, null,
                "isblack=1 and reject in (1,2,3)", null, "_id desc");
	    }
	}

	private void delAllSelected() {
		View view = LayoutInflater.from(BlackList.this).inflate(
				R.layout.black_remove, null);
		final CheckBox black_remove = (CheckBox) view
				.findViewById(R.id.black_remove);
		black_remove.setChecked(true);
		String title = null;
		AlertDialog dialogs = new AlertDialog.Builder(BlackList.this)
				/*.setTitle(R.string.black_remove)*/
				.setMessage(R.string.black_remove_dialog_multi_message)
				.setView(view)
				.setPositiveButton(R.string.remove_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								new Thread() {
									public void run() {
										isInBatchDel = true;
										Message message = mProgressHandler
												.obtainMessage();
										message.arg1 = MESSAGE_START_DELETE;
										message.sendToTarget();
										Set<Integer> lists = mAdapter
												.getCheckedItem();
										List<String> nums = new ArrayList<String>();
										List<String> names = new ArrayList<String>();
										List<Integer> rejects = new ArrayList<Integer>();
										for (int pos : lists) {
											Cursor cursor = (Cursor) mList
													.getItemAtPosition(pos);
											nums.add(cursor.getString(cursor
													.getColumnIndex("number")));
											names.add(cursor.getString(cursor
													.getColumnIndex("black_name")));
											rejects.add(cursor.getInt(cursor
													.getColumnIndex("reject")));

										}

										for (int i = 0; i < lists.size(); i++) {
											ContentResolver cr = getContentResolver();
											ContentValues cv = new ContentValues();
											if (black_remove.isChecked()) {
												cv.put("isblack", 0);

											} else {
												cv.put("isblack", -1);
											}
											Log.i(TAG, "i=" + i);

											String num = nums.get(i);
											cv.put("number", num);
											cv.put("black_name", names.get(i));
											cv.put("reject", rejects.get(i));

											int uri2 = cr.update(
													BlackUtils.BLACK_URI, cv,
													"number=?",
													new String[] { num });
											Log.i(TAG, "updated" + ":" + uri2);

										}
										message = mProgressHandler
												.obtainMessage();
										message.arg1 = MESSAGE_END_DELETE;
										message.sendToTarget();
									};
								}.start();

							}
						}).setNegativeButton(android.R.string.cancel, null)
				.show();
		dialogs.setCanceledOnTouchOutside(false);
	}

	@Override
	protected void processQueryComplete(Context context, Cursor cursor) {
		if (cursor != null) {
			if (!cursor.moveToFirst()) {
				updateState(SHOW_EMPTY);
			} else if (mAdapter == null) {
				mAdapter = new BlackAdapter(context, cursor);
				mAdapter.setListener(BlackList.this);
				mList.setAdapter(mAdapter);
				updateState(SHOW_LIST);
			} else {
				mAdapter.changeCursor(cursor);
				updateState(SHOW_LIST);
				mAdapter.notifyDataSetChanged();
			}
		} else {
			if (mAdapter != null) {
				mAdapter.changeCursor(null);
				mAdapter.notifyDataSetChanged();
			}
			updateState(SHOW_EMPTY);
		}

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			isInBatchDel = false;
		}
		final Cursor finalCursor = cursor;
		final Context finalContext = context;
		hideAllDialog();
		//tangyisen if use thread,it will something exception,so now not use thread,because adpater also need use cursor,so it will exception
		mBaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (finalCursor != null) {
                    if (!finalCursor.moveToFirst()) {
                        mBlackStringlist.clear();
                        mBlackNumbers = null;
                        mBlackNumbersForQuery = null;
                        finalCursor.close();
                    } else {
                        mBlackStringlist.clear();
                        mBlackNumbers = new StringBuilder();
                        mBlackNumbersForQuery = new StringBuilder();
                        do {
                            String number = finalCursor.getString(finalCursor
                                    .getColumnIndex("number"));
                            if (number == null) {
                                continue;
                            }
                            String numberE164 = PhoneNumberUtils.formatNumberToE164(
                                    number, FormatUtils.getCurrentCountryIso(finalContext));
                            Log.i(TAG, "numberE164=" + numberE164);
                            mBlackStringlist.add(number);
                            mBlackNumbers.append("'");
                            mBlackNumbers.append(number);
                            mBlackNumbers.append("',");
                            mBlackNumbersForQuery.append(number);
                            mBlackNumbersForQuery.append(",");

                            if (numberE164 != null && !number.equals(numberE164)) {
                                mBlackStringlist.add(numberE164);
                                mBlackNumbers.append("'");
                                mBlackNumbers.append(numberE164);
                                mBlackNumbers.append("',");
                                mBlackNumbersForQuery.append(numberE164);
                                mBlackNumbersForQuery.append(",");
                                continue;
                            }

                            try { // modify in the future
                                if (numberE164 != null
                                        && numberE164.equals(number)
                                        && FormatUtils.getCurrentCountryIso(finalContext)
                                                .equals("CN")
                                        && number.startsWith("+86")) {
                                    numberE164 = number.substring(3, number.length());
                                    mBlackStringlist.add(numberE164);
                                    mBlackNumbers.append("'");
                                    mBlackNumbers.append(numberE164);
                                    mBlackNumbers.append("',");
                                    mBlackNumbersForQuery.append(numberE164);
                                    mBlackNumbersForQuery.append(",");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } while (finalCursor.moveToNext());
                        finalCursor.moveToFirst();
                    }
                    InterceptionApplication.getInstance()
                            .setBlackList(mBlackStringlist);
                    Log.i(TAG, mBlackStringlist.size() + " list.size() ");
                }
            }
        },100);
		/*new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (finalCursor != null) {
                    if (!finalCursor.moveToFirst()) {
                        mBlackStringlist.clear();
                        mBlackNumbers = null;
                        mBlackNumbersForQuery = null;
                        finalCursor.close();
                    } else {
                        mBlackStringlist.clear();
                        mBlackNumbers = new StringBuilder();
                        mBlackNumbersForQuery = new StringBuilder();
                        do {
                            String number = finalCursor.getString(finalCursor
                                    .getColumnIndex("number"));
                            if (number == null) {
                                continue;
                            }
                            String numberE164 = PhoneNumberUtils.formatNumberToE164(
                                    number, FormatUtils.getCurrentCountryIso(finalContext));
                            Log.i(TAG, "numberE164=" + numberE164);
                            mBlackStringlist.add(number);
                            mBlackNumbers.append("'");
                            mBlackNumbers.append(number);
                            mBlackNumbers.append("',");
                            mBlackNumbersForQuery.append(number);
                            mBlackNumbersForQuery.append(",");

                            if (numberE164 != null && !number.equals(numberE164)) {
                                mBlackStringlist.add(numberE164);
                                mBlackNumbers.append("'");
                                mBlackNumbers.append(numberE164);
                                mBlackNumbers.append("',");
                                mBlackNumbersForQuery.append(numberE164);
                                mBlackNumbersForQuery.append(",");
                                continue;
                            }

                            try { // modify in the future
                                if (numberE164 != null
                                        && numberE164.equals(number)
                                        && FormatUtils.getCurrentCountryIso(finalContext)
                                                .equals("CN")
                                        && number.startsWith("+86")) {
                                    numberE164 = number.substring(3, number.length());
                                    mBlackStringlist.add(numberE164);
                                    mBlackNumbers.append("'");
                                    mBlackNumbers.append(numberE164);
                                    mBlackNumbers.append("',");
                                    mBlackNumbersForQuery.append(numberE164);
                                    mBlackNumbersForQuery.append(",");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } while (finalCursor.moveToNext());
                        finalCursor.moveToFirst();
                    }
                    InterceptionApplication.getInstance()
                            .setBlackList(mBlackStringlist);
                    Log.i(TAG, mBlackStringlist.size() + " list.size() ");
                }
            }
        },"setAppBlackList").start();
		hideAllDialog();*/
	}

	protected void showDialogMenu(final int pos) {
		Cursor cursor = (Cursor) mList.getItemAtPosition(pos);
		final String targetNumber = cursor.getString(cursor
				.getColumnIndex("number"));
		final String targetName = cursor.getString(cursor
				.getColumnIndex("black_name"));
		final String targetId = cursor.getString(cursor.getColumnIndex("_id"));
		final String type = cursor.getString(cursor.getColumnIndex("reject"));
		
		BottomWidePopupMenu menu = new BottomWidePopupMenu(this);
        menu.inflateMenu(R.menu.black_name_menu);
        menu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onItemClicked(MenuItem item) {
                // TODO Auto-generated method stub
                int id = item.getItemId();
                switch (id) {

                    case R.id.black_call:
                        Intent intents = new Intent(Intent.ACTION_CALL, Uri
                                .parse("tel:" + targetNumber));
                        startActivity(intents);
                        break;
                    case R.id.black_sms:
                        Uri uri = Uri.parse("smsto:" + targetNumber);
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        startActivity(it);
                        break;
                    case R.id.black_edit:
                        Intent intentss = new Intent(BlackList.this,
                                AddBlackManually.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("targetId", targetId);
                        bundle.putString("type", type);
                        bundle.putString("add_number", targetNumber);
                        if (targetName != null) {
                            bundle.putString("add_name", targetName);
                        } else {
                            bundle.putString("add_name", "");
                        }
                        intentss.putExtras(bundle);
                        BlackList.this.startActivity(intentss);
                        break;
                    case R.id.black_remove:
                        doRemoveBlack(pos);
                        break;
                    default:
                        break;
                    }

                return true;
            }
        });
        menu.show();
        
		/*AlertDialog dialogs = new AlertDialog.Builder(BlackList.this).setItems(
				R.array.black_name_menu, new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int position) {
						switch (position) {
						case 0:
							Intent intents = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel:" + targetNumber));
							startActivity(intents);
							break;
						case 1:
							Uri uri = Uri.parse("smsto:" + targetNumber);
							Intent it = new Intent(Intent.ACTION_SENDTO, uri);
							startActivity(it);
							break;
						case 2:
							Intent intentss = new Intent(BlackList.this,
									AddBlackManually.class);
							Bundle bundle = new Bundle();
							bundle.putString("targetId", targetId);
							bundle.putString("type", type);
							bundle.putString("add_number", targetNumber);
							if (targetName != null) {
								bundle.putString("add_name", targetName);
							} else {
								bundle.putString("add_name", "");
							}
							intentss.putExtras(bundle);
							BlackList.this.startActivity(intentss);
							break;
						default:
							break;
						}
					}
				}).show();*/

	}

	protected void initBottomMenuAndActionbar() {
		super.initBottomMenuAndActionbar();
		mBottomNavigationView
				.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.delete:
							delAllSelected();
							return true;
						default:
							return false;
						}
					}
				});
	}

	protected void initToolBar() {
		super.initToolBar();
		myToolbar.setTitle(R.string.black);
		myToolbar.inflateMenu(R.menu.menu_black);
	}

	protected void updateBottomMenuItems(int checkedCount) {
	    mBottomNavigationView.setItemEnable(R.id.delete, checkedCount > 0);
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMenuItemClick--->" + item.getTitle());
		if (item.getItemId() == R.id.add) {

			BottomWidePopupMenu menu = new BottomWidePopupMenu(this);
			menu.inflateMenu(R.menu.black_name);
			/*menu.setNegativeButton(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
				}
			});*/
			menu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onItemClicked(MenuItem item) {
					// TODO Auto-generated method stub
					int id = item.getItemId();
					switch (id) {

					case R.id.contact:
						/*final Intent contactIntent = new Intent();
						contactIntent
								.setClassName("com.android.contacts",
										"com.android.contacts.activities.AuroraSimContactListActivity");
						contactIntent.putExtra("blackname_select", true);
						if (!TextUtils.isEmpty(mBlackNumbers)) {
							contactIntent.putExtra(
									"blacknumbers",
									mBlackNumbers.toString()
											.substring(
													0,
													mBlackNumbers.toString()
															.length() - 1));
						}

						// Intent intent2=new
						// Intent("com.android.contacts.action.MST_AUTO_RECORD_CONTACTS_LIST");
						// intent2.setType("vnd.android.cursor.dir/phone");
						try {
							startActivity(contactIntent);
						} catch (ActivityNotFoundException a) {
							a.printStackTrace();
						}*/
					    launchMultiplePhonePicker();

						break;
					case R.id.phone:
						final Intent callLogIntent = new Intent(
								"com.monster.add.black.by.calllog");
						if (!TextUtils.isEmpty(mBlackNumbers)) {
							callLogIntent.putExtra(
									"blacknumbers",
									mBlackNumbers.toString()
											.substring(
													0,
													mBlackNumbers.toString()
															.length() - 1));
						}
						startActivity(callLogIntent);
						break;
					case R.id.sms:
						/*final Intent SmsIntent = new Intent();
						SmsIntent.setClassName("com.android.mms",
								"com.aurora.mms.ui.AuroraRejConvOperActivity");
						SmsIntent.putExtra("isFromReject", true);
						try {
							startActivity(SmsIntent);
						} catch (ActivityNotFoundException a) {
							a.printStackTrace();
						}*/
					    launchMultipleSmsPhonePicker();
						break;
					case R.id.manually:
						Intent intent = new Intent(getApplicationContext(),
								AddBlackManually.class);
						startActivity(intent);
						break;

					}

					return true;
				}
			});
			menu.show();

			return true;
		}
		return false;
	}

	public static final int REQUEST_CODE_SMS_PHONE_PICK = 110;
	private void launchMultipleSmsPhonePicker() {
        Intent intent = new Intent("android.intent.action.conversation.list.PICKMULTIPHONES");
        intent.setType("vnd.android.cursor.dir/phone");
        // lichao modify end
        //begin tangyisen
        //long[] ids = mRecipientsEditor.constructContactsFromInput(false).getDataIds(false);
        //intent.putExtra("data_ids", ids);
        //end tangyisen
        try {
            startActivityForResult(intent, REQUEST_CODE_SMS_PHONE_PICK);
        } catch (ActivityNotFoundException ex) {
            //Toast.makeText(this, R.string.contact_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

	@Override
    public void onSliderButtonClick(int id, View view, ViewGroup parent) {

        int position = Integer.parseInt(((SliderView) parent).getTag(R.id.swipe_view).toString());
        switch (id) {
            case InterceptionUtils.SLIDER_BTN_POSITION_DELETE:
                doRemoveBlack(position);
                break;
            default:
                break;
        }
        if (((SliderView) parent).isOpened()) {
            ((SliderView) parent).close(false);
        }
    }

	private void doRemoveBlack(int position) {
	    final int pos = position;
        View viewRemove = LayoutInflater.from(this).inflate(R.layout.black_remove,
            null);
       final CheckBox black_remove = (CheckBox) viewRemove
            .findViewById(R.id.black_remove);
       black_remove.setChecked(true);
        AlertDialog dialogs = new AlertDialog.Builder(this)
                /*.setTitle(R.string.black_remove)*/
                .setMessage(R.string.black_remove_dialog_message)
                .setView(viewRemove)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                ContentResolver cr = getContentResolver();
                                ContentValues cv = new ContentValues();
                                if (black_remove.isChecked()) {
                                    cv.put("isblack", 0);

                                } else {
                                    cv.put("isblack", -1);
                                }
                                Cursor cursor = (Cursor) mList
                                        .getItemAtPosition(pos);
                                String targetNumber = cursor.getString(cursor
                                        .getColumnIndex("number"));
                                String targetName = cursor.getString(cursor
                                        .getColumnIndex("black_name"));
                                String type = cursor.getString(cursor
                                        .getColumnIndex("reject"));

                                cv.put("number", targetNumber);
                                cv.put("reject", Integer.parseInt(type));
                                cv.put("black_name", targetName);
                                int uri2 = cr.update(BlackUtils.BLACK_URI, cv,
                                        "number=?",
                                        new String[] { targetNumber });
                                Log.i(TAG, "updated" + ":" + uri2);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .show();
        dialogs.setCanceledOnTouchOutside(false);
	}

	public static final int REQUEST_CODE_CONTACT_PHONE_PICK = 109;
	private static final String RESULT_INTENT_EXTRA_DATA_NAME = "com.mediatek.contacts.list.pickdataresult";
    private static final String RESULT_INTENT_EXTRA_CONTACTS_NAME = "com.mediatek.contacts.list.pickcontactsresult";
	private void launchMultiplePhonePicker() {
        //lichao modify begin
        /*
        Intent intent = new Intent(INTENT_MULTI_PICK_ACTION, Contacts.CONTENT_URI);
        String exsitNumbers = mRecipientsEditor.getExsitNumbers();
        if (!TextUtils.isEmpty(exsitNumbers)) {
            intent.putExtra(Intents.EXTRA_PHONE_URIS, exsitNumbers);
        }
        */
        Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTIPHONES");
        intent.setType("vnd.android.cursor.dir/phone");
        ContentResolver cr = getContentResolver();
        ArrayList<Long> idsList = new ArrayList<>();
        /*for(String number : mBlackStringlist) {
            Cursor cur = cr.query(Data.CONTENT_URI,new String[]{"_id"},"data1="+number,null,null);
            if(cur != null && cur.moveToFirst()) {
                do{
                    idsList.add(cur.getLong(0));
                }while(cur.moveToNext());
            }
        }
        long[] ids = new long[idsList.size()];
        for(int i = 0;i < idsList.size(); i ++) {
            ids[i] = idsList.get(i);
        }*/
        Cursor cur = null;
        if (mBlackNumbersForQuery != null) {
            cur = getContentResolver().query(Data.CONTENT_URI,new String[]{"_id"},"data1 IN (" + mBlackNumbersForQuery.toString().substring(0, mBlackNumbersForQuery.length() - 2) + ")",null,null);
        }
        if(cur != null && cur.getCount() > 0 && cur.moveToFirst()) {
            long[] ids = new long[cur.getCount()];
            int i = 0;
            do{
                //idsList.add(cur.getLong(0));
                ids[i] = cur.getLong(0);
                i ++;
            }while(cur.moveToNext());
            intent.putExtra("data_ids", ids);
        }
        intent.putExtra("isFromReject", true);
        //end tangyisen
        try {
            startActivityForResult(intent, REQUEST_CODE_CONTACT_PHONE_PICK);
        } catch (ActivityNotFoundException ex) {
            //Toast.makeText(this, R.string.contact_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }


	private List<BlackItem> mPickBlackItem;
	//private boolean mIsBackShowInsertDialg = false;
	@Override
    protected void onActivityResult(int maskResultCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK){
            return;
        }
        switch (maskResultCode) {
            case REQUEST_CODE_CONTACT_PHONE_PICK:
                //depend on data id to query name and phonenumber to insert black
                final long[] dataIds = data.getLongArrayExtra(RESULT_INTENT_EXTRA_DATA_NAME);
                if (dataIds == null || dataIds.length <= 0) {
                    return;
                }
                //mIsBackShowInsertDialg = true;
                clearPickData();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Message message = mProgressHandler
                            .obtainMessage();
                        message.arg1 = MESSAGE_START_INSERT;
                        message.sendToTarget();
                        mPickBlackItem = ContactUtils.getBlackItemListByDataId(mContext, dataIds);
                        doSaveBlackFromContact();
                        message = mProgressHandler
                            .obtainMessage();
                        message.arg1 = MESSAGE_END_INSERT;
                        message.sendToTarget();
                    }
                }).start();
                break;
            case REQUEST_CODE_SMS_PHONE_PICK:
                break;
            default:
                break;
        }
	}

	private void clearPickData() {
	    if(mPickBlackItem != null) {
	        mPickBlackItem.clear();
	        mPickBlackItem = null;
	    }
	    mCount = 0;
	}

	private void doSaveBlackFromContact() {
	    saveInternal();
	}

	private ProgressDialog mInsertPregressDialog;

	private Runnable mInsertingRunnable = new Runnable() {
        @Override
        public void run() {
            mBaseHandler.postDelayed(mShowInsetingProgressDialogRunnable, DELAY_TIME);
        }
    };

    private Runnable mShowInsetingProgressDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mInsertPregressDialog == null) {
                mInsertPregressDialog = createInsertProgressDialog();
            }
            mInsertPregressDialog.show();
        }
    };

    private ProgressDialog createInsertProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage(getResources()
                .getString(R.string.save_title));
        return dialog;
    }

	private void showInsertDialog() {
        /*if (mInsertPregressDialog != null) {
            mInsertPregressDialog.dismiss();
        }
        mInsertPregressDialog = new ProgressDialog(this);
        mInsertPregressDialog.setIndeterminate(true);
        mInsertPregressDialog.setCancelable(false);
        mInsertPregressDialog.setMessage(getResources()
                .getString(R.string.save_title));
        mInsertPregressDialog.show();*/
	    isOperating = true;
        if (mInsertingRunnable != null) {
            mInsertingRunnable.run();
        }
    }

	private void hideInsertDialog() {
	    /*if (mInsertPregressDialog != null) {
            mInsertPregressDialog.dismiss();
        }*/
	    isOperating = false;
        mBaseHandler.removeCallbacks(mShowInsetingProgressDialogRunnable);
        if (mInsertPregressDialog != null && mInsertPregressDialog.isShowing()) {
            mInsertPregressDialog.dismiss();
        }
        //startQuery();
	}

	private void hideAllDialog(){
	    hideDeleteDialog();
	    hideInsertDialog();
	}
	private int mCount;
    private void saveInternal() {
        /*if (mPickBlackItem == null || mPickBlackItem.size() == 0) {
            mIsBackShowInsertDialg = false;
            hideInsertDialog();
            return;
        }*/
        //showInsertDialog();
        for(BlackItem blackItem : mPickBlackItem) {
            final BlackItem fBlackItem = blackItem;
            final String number = blackItem.getmNumber();
            String lable = YuloreUtil.getUserMark(mContext, number);
            if (lable == null) {
                /*new Thread() {
                    public void run() {*/
                try {
                    fBlackItem.setmLable(YuloreUtil.getMarkContent(number, mContext));
                    fBlackItem.setmUserMark(YuloreUtil.getMarkNumber(mContext, number));
                    mCount++;
                    ensureInsertBlack();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                    //};
                //}.start();
            } else {
                fBlackItem.setmUserMark(-1);
                mCount++;
                ensureInsertBlack();
            }
        }
        //mDatabaseHandler.obtainMessage().sendToTarget();
    }

    private void ensureInsertBlack() {
        if(mCount == mPickBlackItem.size()) {
            //mDatabaseHandler.obtainMessage().sendToTarget();
            ArrayList<ContentProviderOperation> ops = 
                new ArrayList<ContentProviderOperation>();
            for(BlackItem item : mPickBlackItem) {
                ops.add(
                    ContentProviderOperation.newInsert(BlackUtils.BLACK_URI)
                        .withValue("isblack", 1)
                        .withValue("lable", item.getmLable())
                        .withValue("user_mark", item.getmUserMark())
                        .withValue("number", item.getmNumber())
                        .withValue("black_name", item.getmBlackName())
                        .withValue("reject", 3)//except add manually,other will default 3
                        .withYieldAllowed(true)
                        .build());
            }
            try {
                getContentResolver().applyBatch(BlackUtils.BLACK_AUTHORITY, ops);
             } catch (RemoteException e) {
                e.printStackTrace();
             } catch (OperationApplicationException e) {
                 e.printStackTrace();
             }
        }
    }

    private Handler mDatabaseHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            /*if (mPickBlackItem == null || mPickBlackItem.size() == 0) {
                mIsBackShowInsertDialg = false;
                hideDialog();
                return;
            }*/
            ArrayList<ContentProviderOperation> ops = 
                new ArrayList<ContentProviderOperation>();
            for(BlackItem item : mPickBlackItem) {
                ops.add(
                    ContentProviderOperation.newInsert(BlackUtils.BLACK_URI)
                        .withValue("isblack", 1)
                        .withValue("lable", item.getmLable())
                        .withValue("user_mark", item.getmUserMark())
                        .withValue("number", item.getmNumber())
                        .withValue("black_name", item.getmBlackName())
                        .withValue("reject", 3)//except add manually,other will default 3
                        .withYieldAllowed(true)
                        .build());
            }
            try {
                getContentResolver().applyBatch(BlackUtils.BLACK_AUTHORITY, ops);
             } catch (RemoteException e) {
                e.printStackTrace();
             } catch (OperationApplicationException e) {
                 e.printStackTrace();
             }
            //Uri uris = cr.insert(BlackUtils.BLACK_URI, cv);
            //hideInsertDialog();
        };
    };
}
