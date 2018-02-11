package com.monster.interception.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.view.menu.BottomWidePopupMenu;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.monster.interception.InterceptionApplication;
import com.monster.interception.util.BlackUtils;
import com.monster.interception.util.FormatUtils;
import com.monster.interception.util.SimUtils;
import com.monster.interception.util.YuloreUtil;
import com.monster.interception.R;

import android.location.CountryDetector;

public class InterceptionItemClickHelper {

	private InterceptionActivity mActivity;
	private ContentResolver mContentResolver;
	private String targetNumber;
	private String blackName;
	private int targetSimId;
	private String targetId;
	private int type;

	public InterceptionItemClickHelper(InterceptionActivity context) {
		mActivity = context;
		mContentResolver = context.getContentResolver();
	}

	// onItemClick start
	void initData(Cursor c) {
		targetNumber = c.getString(c.getColumnIndex("number"));
		targetSimId = c.getInt(c.getColumnIndex("simId"));
		targetId = c.getString(c.getColumnIndex("_id"));
		type = c.getInt(c.getColumnIndex("reject"));
		blackName = c.getString(c.getColumnIndex("name"));
	}

	void showDialogMenu() {

		BottomWidePopupMenu menu = new BottomWidePopupMenu(mActivity);
		menu.inflateMenu(R.menu.calllog_menu);
		// 设置取消按钮,如果需要显示取消按钮就调用这个方法
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
				case R.id.place_call:
					placeCall();
					break;
				case R.id.send_sms:
					sendSms();
					break;
				case R.id.remove_black:
					removeBlack();
					break;
				/*case R.id.view_call_detail:
					viewCallLog();
					break;
				case R.id.delete_calllog:
					deleteCallLog();
					break;*/

				}
				return true;
			}
		});
		menu.show();

	}

	private void placeCall() {
		Intent intents;
        TelephonyManager tm = (TelephonyManager) mActivity
                .getSystemService(Context.TELEPHONY_SERVICE);		
		if (tm.isMultiSimEnabled()) {
			intents = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ targetNumber));
			if (SimUtils.isShowDoubleButton(mActivity)) {
				intents.putExtra("slot", targetSimId);
			}
		} else {
			intents = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ targetNumber));
		}
		mActivity.startActivity(intents);
	}

	private void sendSms() {
		Uri uri = Uri.parse("smsto:" + targetNumber);
		Intent it = new Intent(Intent.ACTION_SENDTO, uri);
		mActivity.startActivity(it);
	}

	private void removeBlack() {
		removeBlack(targetNumber);	
	}
	
    void removeBlack(final String number) {
	        View view = LayoutInflater.from(mActivity).inflate(
	                R.layout.black_phone_remove, null);
	        final CheckBox phone = (CheckBox) view.findViewById(R.id.phone);
	        phone.setChecked(true);
	        AlertDialog dia = new AlertDialog.Builder(mActivity)
	                .setTitle(
	                        mActivity.getResources().getString(
	                                R.string.confirm_no_reject))
	                .setView(view)
	                .setPositiveButton(android.R.string.ok,
	                        new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialog,
	                                    int whichButton) {
	                                ContentResolver cr = mActivity
	                                        .getContentResolver();
	                                ContentValues cv = new ContentValues();
	                                if (phone.isChecked()) {
	                                    cv.put("isblack", 0);

	                                } else {
	                                    cv.put("isblack", -1);
	                                }
	                                cv.put("number", number);
	                                cv.put("reject", type);
	                                int uri2 = cr.update(
	                                        BlackUtils.BLACK_URI,
	                                        cv, "number=?",
	                                        new String[] { number });
	                                System.out.println("updated" + ":" + uri2);
	                                dialog.dismiss();
	                                Toast.makeText(mActivity, R.string.no_reject_success_toast, Toast.LENGTH_SHORT).show();
	                            }
	                        })
	                .setNegativeButton(android.R.string.cancel, null).show();
	        dia.setCanceledOnTouchOutside(false);
	    }

	private void viewCallLog() {
		// TODO Auto-generated method stub
		final Intent intent = new Intent();
		// String name = blackName;
		Log.e("System.out", "name = " + blackName + "  number = "
				+ targetNumber);
		intent.setClassName("com.android.contacts",
				"com.android.contacts.FullCallDetailActivity");
		intent.putExtra("number", targetNumber);
		intent.putExtra("black_name", blackName);
		intent.putExtra("reject_detail", true);
		// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		String userMark = YuloreUtil.getUserMark(mActivity, targetNumber);
		String markContent = YuloreUtil.getMarkContent(targetNumber, mActivity);
		int markCount = YuloreUtil.getMarkNumber(mActivity, targetNumber);
		intent.putExtra("user-mark", userMark);
		intent.putExtra("mark-content", markContent);
		intent.putExtra("mark-count", markCount);

		try {
			mActivity.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void deleteCallLog() {
		AlertDialog dialog = new AlertDialog.Builder(mActivity)
				.setTitle(
						mActivity.getResources().getString(
								R.string.remove_one_dial_bn))
				.setPositiveButton(R.string.remove_dial_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								deleteOne();
							}
						})
				.setNegativeButton(android.R.string.cancel, null).show();
	}

	private void deleteOne() {
		new Thread() {
			public void run() {
				String p = targetNumber;
				String numberE164 = PhoneNumberUtils.formatNumberToE164(p,
						FormatUtils.getCurrentCountryIso(mActivity));
				if (numberE164 != null) {
					mContentResolver.delete(Calls.CONTENT_URI,
							"number=? and type in (1,3) and reject=1",
							new String[] { numberE164 });
				}
				mContentResolver.delete(Calls.CONTENT_URI,
						"number=? and type in (1,3) and reject=1",
						new String[] { p });

			};
		}.start();
	}

	void viewSms(Cursor c) {
		final Intent SmsIntent = new Intent();
		SmsIntent.setClassName("com.android.mms",
				"com.android.mms.ui.ComposeMessageActivity");
		SmsIntent.putExtra("thread_id",
				c.getLong(c.getColumnIndex("thread_id")));
		SmsIntent.putExtra("isFromReject", true);
		try {
			mActivity.startActivity(SmsIntent);
		} catch (ActivityNotFoundException a) {
			a.printStackTrace();
		}
	}

	// onNavigationItemSelected start
	private List<String> smsIds;
	private List<String> smsNumbers;
	private List<String> smsNumbersE164;

	boolean handleBottomMenuItemClick(MenuItem item) {

		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.del:
			deleteSelectedCallLog();
			return true;
		case R.id.delSms:
			deleteSelectedSms();
			return true;
		case R.id.showSms:
			recoverSelectedSms();
			return true;
		}
		return false;
	}

	private void deleteSelectedCallLog() {
		/*String title3 = null;
		int selectCount3 = mActivity.mCallListAdapter.getCheckedItem().size();
		if (selectCount3 <= 1) {
			title3 = mActivity.getResources().getString(
					R.string.remove_one_dial_bn);
		} else {
			int totalCount3 = mActivity.mCallListAdapter.getCount();
			if (selectCount3 < totalCount3) {
				title3 = mActivity.getResources().getString(
						R.string.remove_multi_dial_bn, selectCount3);
			} else {
				title3 = mActivity.getResources().getString(
						R.string.remove_all_dial_bn);
			}
		}*/
		AlertDialog dialogs = new AlertDialog.Builder(mActivity)
				.setTitle(R.string.delete_reject)
				.setMessage(R.string.multi_delete_calllog_reject_dialg_msg)
				.setPositiveButton(R.string.remove_dial_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								deleteSelectedCallLogInternal();
							}
						})
				.setNegativeButton(android.R.string.cancel, null).show();
		dialogs.setCanceledOnTouchOutside(false);
	}

	private void deleteSelectedSms() {
		/*String title1 = null;
		int selectCount1 = mActivity.mSmsListAdapter.getCheckedItem().size();
		if (selectCount1 <= 1) {
			title1 = mActivity.getResources().getString(
					R.string.remove_one_conv_bn);
		} else {
			int totalCount1 = mActivity.mSmsListAdapter.getCount();
			if (selectCount1 < totalCount1) {
				title1 = mActivity.getResources().getString(
						R.string.remove_multi_conv_bn, selectCount1);
			} else {
				title1 = mActivity.getResources().getString(
						R.string.remove_all_conv_bn);
			}
		}*/
		AlertDialog dias = new AlertDialog.Builder(mActivity)
				.setTitle(R.string.delete_reject)
				 .setMessage(R.string.multi_delete_sms_reject_dialg_msg)
				.setPositiveButton(R.string.remove_conv_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								delSelectedSmsInternal();

							}
						})
				.setNegativeButton(android.R.string.cancel, null).show();
		dias.setCanceledOnTouchOutside(false);
	}

	private void recoverSelectedSms() {
		/*String title2 = null;
		int selectCount2 = mActivity.mSmsListAdapter.getCheckedItem().size();
		if (selectCount2 <= 1) {
			title2 = mActivity.getResources().getString(
					R.string.resume_one_conv_bn);
		} else {
			int totalCount2 = mActivity.mSmsListAdapter.getCount();
			if (selectCount2 < totalCount2) {
				title2 = mActivity.getResources().getString(
						R.string.resume_multi_conv_bn, selectCount2);
			} else {
				title2 = mActivity.getResources().getString(
						R.string.resume_all_conv_bn);
			}
		}*/
		AlertDialog dialog = new AlertDialog.Builder(mActivity)
				// .setTitle(mActivity.getResources().getString(R.string.recover_sms))
				.setTitle(R.string.recover_sms_dlg_title)
				.setMessage(R.string.multi_recover_sms_reject_dialg_msg)
				.setPositiveButton(R.string.resume_conv_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								recoverSmsInternal();
							}
						})
				.setNegativeButton(android.R.string.cancel, null).show();
		dialog.setCanceledOnTouchOutside(false);
	}

	boolean smsBath = false;
	boolean callBath = false;
	private Handler mDialogHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.arg1 == 0) {
				showDialog();
			} else {
				mActivity.changeToNormalMode(true);
				if (msg.arg1 == 2) {
					callBath = false;
					hideDialog();
					mActivity.mCalllogQueryManager.startQuery();
				} else {
					smsBath = false;
					hideDialog();
					mActivity.mSmsQueryManager.startQueryMms();
				}

			}
		};
	};

	private ProgressDialog mProgressDialog;
	private int operationType = 0;

	private void showDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(mActivity);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
		}
		if (operationType == 0) {
			mProgressDialog.setMessage(mActivity.getResources().getString(
					R.string.dels));
		} else {
			mProgressDialog.setMessage(mActivity.getResources().getString(
					R.string.recovery));
			operationType = 0;
		}
		mProgressDialog.show();
	}

	private void hideDialog() {
	    if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
	}

	boolean isDialogShowing() {
		return mProgressDialog != null && mProgressDialog.isShowing();
	}

	private void deleteSelectedCallLogInternal() {
		new Thread() {
			@Override
			public void run() {
				Uri callsUri = Calls.CONTENT_URI;
				callBath = true;
				Message message = mDialogHandler.obtainMessage();
				message.arg1 = 0;
				message.sendToTarget();
				// TODO Auto-generated method stub
				Set<Integer> list = mActivity.mCallListAdapter.getCheckedItem();
				List<String> calls = new ArrayList<String>();
				smsNumbersE164 = new ArrayList<String>();
				Cursor pcursor;
				String num;
				String numberE164;
				for (int pos : list) {
					pcursor = mActivity.mCallListAdapter.getCursor();
					pcursor.moveToPosition(pos);
					num = pcursor.getString(pcursor.getColumnIndex("number"));
					calls.add(num);
					numberE164 = PhoneNumberUtils.formatNumberToE164(num,
							FormatUtils.getCurrentCountryIso(mActivity));
					if (numberE164 != null) {
						smsNumbersE164.add(numberE164);
					}
				}
				for (int i = 0; i < calls.size(); i++) {
					mContentResolver.delete(callsUri, "number='" + calls.get(i)
							+ "' and type in (1,3) and reject=1", null);
				}
				for (int i = 0; i < smsNumbersE164.size(); i++) {
					mContentResolver.delete(callsUri, "number='"
							+ smsNumbersE164.get(i)
							+ "' and type in (1,3) and reject=1", null);
				}
				message = mDialogHandler.obtainMessage();
				message.arg1 = 2;
				message.sendToTarget();
			}
		}.start();

	}

	private void delSelectedSmsInternal() {
		// 点击了垃圾桶的响应事件

		new Thread() {
			@Override
			public void run() {
				Uri uriSms = Uri.parse("content://sms");
				Uri uriMms = Uri.parse("content://mms");
				smsBath = true;
				// TODO Auto-generated method stub
				Message message = mDialogHandler.obtainMessage();
				message.arg1 = 0;
				message.sendToTarget();
				Set<Integer> list = mActivity.mSmsListAdapter.getCheckedItem();
				Cursor pcursor;
				smsIds = new ArrayList<String>();
				smsNumbers = new ArrayList<String>();
				smsNumbersE164 = new ArrayList<String>();
				String num;
				String numberE164;
				for (int pos : list) {
					pcursor = mActivity.mSmsListAdapter.getCursor();
					pcursor.moveToPosition(pos);
					num = pcursor.getString(pcursor.getColumnIndex("address"));
					smsNumbers.add(num);
					numberE164 = PhoneNumberUtils.formatNumberToE164(num,
							FormatUtils.getCurrentCountryIso(mActivity));
					if (numberE164 != null) {
						smsNumbersE164.add(numberE164);
					}
					smsIds.add(pcursor.getString(pcursor
							.getColumnIndex("thread_id")));
				}
				System.out.println("smsNumbers.get(i)=" + smsNumbers.get(0));
				for (int i = 0; i < list.size(); i++) {
					mContentResolver.delete(uriMms,
							"reject=1 and msg_box=1 and thread_id=?",
							new String[] { smsIds.get(i) });
					/*mContentResolver.delete(uriSms,
							"address=? and type=1 and reject=1",
							new String[] { smsNumbers.get(i) });*/
					mContentResolver.delete(uriSms,
                        "type=1 and reject=1 and thread_id=?",
                        new String[] { smsIds.get(i) });
				}
				for (int i = 0; i < smsNumbersE164.size(); i++) {
					mContentResolver.delete(uriSms,
							"address=? and type=1 and reject=1",
							new String[] { smsNumbersE164.get(i) });
				}
				message = mDialogHandler.obtainMessage();
				message.arg1 = 1;
				message.sendToTarget();
			}
		}.start();

	}

	private void recoverSmsInternal() {
		// 点击了垃圾桶的响应事件
		operationType = 1;
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
			    Uri uriSms = Uri.parse("content://sms");
                Uri uriMms = Uri.parse("content://mms");
				smsBath = true;
				Message message = mDialogHandler.obtainMessage();
				message.arg1 = 0;
				message.sendToTarget();
				Set<Integer> list = mActivity.mSmsListAdapter.getCheckedItem();
				Cursor pcursor;
				smsIds = new ArrayList<String>();
				smsNumbers = new ArrayList<String>();
				smsNumbersE164 = new ArrayList<String>();
				String num;
				String numberE164;
				for (int pos : list) {
					pcursor = mActivity.mSmsListAdapter.getCursor();
					pcursor.moveToPosition(pos);
					smsIds.add(pcursor.getString(pcursor
							.getColumnIndex("thread_id")));
				}

				ContentValues threadValuesInsert = new ContentValues();
				threadValuesInsert.put("reject", 0);
				for (int i = 0; i < list.size(); i++) {
					/*Uri threadUriInsert = Uri
							.parse("content://mms-sms/conversations_resume_all/"
									+ smsIds.get(i));
					mContentResolver.update(threadUriInsert,
							threadValuesInsert, null, null);*/
				    String threadidid = smsIds.get(i);
				    mContentResolver.update(uriMms,
                        threadValuesInsert, 
                        "reject=1 and msg_box=1 and thread_id=?",
                        new String[] { smsIds.get(i) });
				    mContentResolver.update(uriSms,
                        threadValuesInsert, 
                        "type=1 and reject=1 and thread_id=?",
                        new String[] { smsIds.get(i) });
                /*mContentResolver.delete(uriSms,
                        "address=? and type=1 and reject=1",
                        new String[] { smsNumbers.get(i) });*/
				}
				message = mDialogHandler.obtainMessage();
                message.arg1 = 1;
                message.sendToTarget();
			}
		}.start();

	}

}