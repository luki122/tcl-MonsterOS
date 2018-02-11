package com.monster.interception.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.monster.interception.R;
import com.monster.interception.adapter.InterceptionAdapterBase;
import com.monster.interception.adapter.MarkAdapter;
import com.monster.interception.util.BlackUtils;
import com.monster.interception.util.InterceptionUtils;
import com.monster.interception.util.YuloreUtil;


import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;

import mst.app.MstActivity;
import mst.widget.ActionMode;
import mst.widget.ActionMode.Item;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import mst.widget.MstListView;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.SliderView;
import mst.view.menu.BottomWidePopupMenu;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

public class MarkList extends InterceptionActivityBase {
	private static final String TAG = "MarkList";

	private List<String> mMarkStringList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.mark_list);
		setMstContentView(R.layout.mark_list);
		mListUri = BlackUtils.MARK_URI;
		init();
	}
	
	protected void init() {
	       mList = (MstListView) findViewById(R.id.mark_list);
	        mEmpty = (TextView) findViewById(R.id.mark_empty);      
		super.init();
	}
	
	protected void startQuery() {
		mQueryHandler.startQuery(0, null, mListUri, null,
				"lable is not null and number is null", null, null);		
	}
	
	@Override
	protected void processQueryComplete(Context context, Cursor cursor) {
		if (cursor != null) {
			mMarkStringList.clear();
			if (cursor.moveToFirst()) {
				do {
					mMarkStringList.add(cursor.getString(cursor
							.getColumnIndex("lable")));
				} while (cursor.moveToNext());
			}

		}
		if (cursor != null) {
			if (!cursor.moveToFirst()) {
				updateState(SHOW_EMPTY);
			} else if (mAdapter == null) {
				mAdapter = new MarkAdapter(context, cursor);
				mAdapter.setListener(MarkList.this);
				mList.setAdapter(mAdapter);
				updateState(SHOW_LIST);
			} else {
				mAdapter.changeCursor(cursor);
				updateState(SHOW_LIST);
			}
		} else {
			if (mAdapter != null) {
				mAdapter.changeCursor(null);
			}
			updateState(SHOW_EMPTY);
		}
	}
	
	@Override
	protected void showDialogMenu(final int pos) {
	    BottomWidePopupMenu menu = new BottomWidePopupMenu(this);
        menu.inflateMenu(R.menu.mark);
        menu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onItemClicked(MenuItem item) {
                // TODO Auto-generated method stub
                int id = item.getItemId();
                switch (id) {
                    case R.id.edit:
                        showEditDialogMenu(pos);
                        break;
                    case R.id.del:
                        showDeleteDialog(pos);
                        break;
                    default:
                        break;
                    }

                return true;
            }
        });
        menu.show();
	}

	private void showEditDialogMenu(final int pos) {
		Cursor cursor = (Cursor) mList
				.getItemAtPosition(pos);
		final String mTarget = cursor.getString(cursor.getColumnIndex("lable"));
		final String mTargetId = cursor.getString(cursor.getColumnIndex("_id"));
		
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_edittext, null);
		final EditText mark_content = (EditText) view
				.findViewById(R.id.mark_content);
		mark_content.setText(mTarget);
		mark_content.setSelection(mTarget.length());
		mark_content
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(15) });
		AlertDialog dialogs = new AlertDialog.Builder(mContext)
				.setTitle(
						mContext.getResources().getString(
								R.string.edit_mark))
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String s = mark_content.getText().toString()
										.replace(" ", "");
								if (!s.equals("")) {
									s = mark_content.getText().toString();
									ContentResolver cr = getContentResolver();
									ContentValues cv = new ContentValues();
									cv.put("lable", s);
									int uri2 = cr.update(BlackUtils.MARK_URI, cv, "lable=?",
											new String[] { mTarget });

									updateData(mTarget, s);
								} else {
									Toast.makeText(
											mContext,
											mContext
													.getResources().getString(
															R.string.no_marks),
											Toast.LENGTH_LONG).show();
									return;
								}
								dialog.dismiss();
							}
						})
				.setNegativeButton(android.R.string.cancel, null).show();
		dialogs.setCanceledOnTouchOutside(false);
		dialogs.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void updateData(String oldLable, String newLable) {
		final String oldMark = oldLable;
		final String newMark = newLable;
		Log.e(TAG, "oldMark = " + oldMark + " newMark = " + newMark);

		new Thread() {
			public void run() {
				Cursor callCursor = mContext.getContentResolver().query(
						Calls.CONTENT_URI,
						new String[] { "_id", "number" },
						"mark='" + oldMark
								+ "' and user_mark='-1' and reject in (0,1)",
						null, null, null);
				if (callCursor != null) {
					if (callCursor.moveToFirst()) {
						do {
							ContentValues cv = new ContentValues();
							int userMark = 0;
							String number = callCursor.getString(1);
							Log.e(TAG, "number ========== " + number);

							if (newMark != null) {
								YuloreUtil.insertUserMark(mContext, number,
										newMark);
								userMark = -1;
							} else {
								userMark = 0;
								YuloreUtil.deleteUserMark(mContext, number);
							}

							cv.put("mark", newMark);
							cv.put("user_mark", userMark);
							mContext.getContentResolver().update(
									Calls.CONTENT_URI, cv,
									"_id=" + callCursor.getString(0), null);
						} while (callCursor.moveToNext());
					}

					callCursor.close();
				}

				Uri blackUri = BlackUtils.BLACK_URI;
				System.out.println("oldMark=" + oldMark);
				Cursor blackCursor = mContext.getContentResolver().query(
						blackUri,
						new String[] { "_id", "number", "user_mark" },
						"lable='" + oldMark + "' and user_mark='-1'", null,
						null, null);
				if (blackCursor != null) {
					if (blackCursor.moveToFirst()) {
						try {
							do {
								ContentValues cv = new ContentValues();
								int userMark = 0;
								String number = blackCursor.getString(1);

								if (newMark != null) {
									YuloreUtil.insertUserMark(mContext, number,
											newMark);
									userMark = -1;
								} else {
									userMark = 0;
									YuloreUtil.deleteUserMark(mContext, number);
								}

								cv.put("lable", newMark);
								cv.put("user_mark", userMark);
								int i = mContext.getContentResolver()
										.update(blackUri,
												cv,
												"_id="
														+ blackCursor
																.getString(0),
												null);
								System.out.println("i=" + i);
							} while (blackCursor.moveToNext());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					blackCursor.close();
				}
			}
		}.start();
	}

	protected void initBottomMenuAndActionbar() {
		super.initBottomMenuAndActionbar();		
		mBottomNavigationView
				.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.delete:
							deleteSelectedMark();
							return true;
						default:
							return false;
						}
					}
				});
	}

	private void deleteSelectedMark() {
		// TODO Auto-generated method stub

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(
						this.getResources().getString(
								R.string.del_mark))
				.setMessage(
						this.getResources().getString(
								R.string.is_confirm_del))
				.setPositiveButton(R.string.del_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								ContentResolver cr = getContentResolver();
								Cursor pcursor = (Cursor) mAdapter.getCursor();
								for (int pos : mAdapter.getCheckedItem()) {
									pcursor.moveToPosition(pos);
									final String lable = pcursor.getString(1);
									cr.delete(BlackUtils.MARK_URI, "lable=?",
											new String[] { lable });

									updateData(lable, null);
								}
								changeToNormalMode(true);
							}
						})
				.setNegativeButton(android.R.string.cancel, null).show();
		dialog.setCanceledOnTouchOutside(false);
	}

	protected void initToolBar() {
		super.initToolBar();
		myToolbar.setTitle(R.string.marks_manage);
		myToolbar.inflateMenu(R.menu.menu_mark);
	}


	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMenuItemClick--->" + item.getTitle());
		if (item.getItemId() == R.id.add) {
			View view = LayoutInflater.from(mContext).inflate(
					R.layout.dialog_edittext, null);
			final EditText mMarkText = (EditText) view
					.findViewById(R.id.mark_content);
			mMarkText
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							15) });
			AlertDialog dialogs = new AlertDialog.Builder(
					mContext)
					.setTitle(
							mContext.getResources().getString(
									R.string.add_mark))
					.setView(view)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {

									String s = mMarkText.getText().toString();
									if (!s.equals("")) {
										if (s != null
												&& s.replaceAll(" ", "")
														.equals("")) {
											Toast.makeText(
													mContext,
													mContext.getResources()
															.getString(
																	R.string.mark_error),
													Toast.LENGTH_SHORT).show();
											return;
										}

										if (!mMarkStringList.contains(s)) {
											ContentResolver cr = getContentResolver();
											ContentValues cv = new ContentValues();
											cv.put("lable", s);
											try {
												Uri uri2 = cr.insert(BlackUtils.MARK_URI, cv);
											} catch (Exception e) {
												e.printStackTrace();
											}
										} else {
											Toast.makeText(
													mContext,
													mContext
															.getResources()
															.getString(
																	R.string.mark_content_exist),
													Toast.LENGTH_LONG).show();
										}

									} else {
										Toast.makeText(
												mContext,
												mContext
														.getResources()
														.getString(
																R.string.no_content),
												Toast.LENGTH_LONG).show();
									}
									dialog.dismiss();
								}
							}).show();
			dialogs.setCanceledOnTouchOutside(false);
			dialogs.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
		return false;
	}

	/*public void slideDelete(int position) {

		// TODO Auto-generated method stub
		Cursor pcursor = (Cursor) mList.getItemAtPosition(position);
		if (pcursor == null) {
			return;
		}
		final String lable = pcursor.getString(1);

		AlertDialog dialog = new AlertDialog.Builder(mContext)
				.setTitle(
						mContext.getResources().getString(
								R.string.del_mark))
				.setMessage(
						mContext.getResources().getString(
								R.string.is_confirm_del))
				.setPositiveButton(R.string.del_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								ContentResolver cr = getContentResolver();
								cr.delete(BlackUtils.MARK_URI, "lable=?",
										new String[] { lable });

								updateData(lable, null);
							}
						})
				.setNegativeButton(android.R.string.cancel, null).show();
		dialog.setCanceledOnTouchOutside(false);

	}*/
	@Override
    public void onSliderButtonClick(int id, View view, ViewGroup parent) {

        int position = Integer.parseInt(((SliderView) parent).getTag(R.id.swipe_view).toString());
        switch (id) {
            case InterceptionUtils.SLIDER_BTN_POSITION_DELETE:
                showDeleteDialog(position);
                break;
            default:
                break;
        }
        if (((SliderView) parent).isOpened()) {
            ((SliderView) parent).close(false);
        }
    }

	private void showDeleteDialog(int position) {
	    Cursor pcursor = (Cursor) mList.getItemAtPosition(position);
        if (pcursor == null) {
            return;
        }
        final String lable = pcursor.getString(1);

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(
                        mContext.getResources().getString(
                                R.string.del_mark))
                .setMessage(
                        mContext.getResources().getString(
                                R.string.is_confirm_del))
                .setPositiveButton(R.string.del_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                dialog.dismiss();
                                ContentResolver cr = getContentResolver();
                                cr.delete(BlackUtils.MARK_URI, "lable=?",
                                        new String[] { lable });

                                updateData(lable, null);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null).show();
        dialog.setCanceledOnTouchOutside(false);
	}
}
