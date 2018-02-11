/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.MstSearchView;

import cn.tcl.note.R;
import cn.tcl.note.db.DBData;
import cn.tcl.note.ui.DialogHelper;
import cn.tcl.note.ui.NotificationHelper;
import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.PermissionUtil;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.FloatingActionButton;

public class NoteHomeActivity extends RootActivity implements View.OnClickListener, MstSearchView.OnCloseListener, MstSearchView.OnQueryTextListener {

    private final String TAG = NoteHomeActivity.class.getSimpleName();
    private RecyclerView mHomeRecycler;
    private NoteHomeAdapter mNoteHomeAdapter;
    private ContentResolver mContentResolver;
    private MstSearchView mSearchView;
    private FloatingActionButton mAddButton;
    private Drawable mToolBarIcon;
    private boolean mEditMode;
    private BottomNavigationView mBottombar;
    private Boolean mSelectAll = false;
    private MenuItem mDelButtonMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NoteLog.getAppInfo(this);
        NoteLog.v(TAG, "onCreate start");
        setMstContentView(R.layout.activity_note_home);

        initToolBar(R.string.app_name);
        mToolBarIcon = mToolBar.getNavigationIcon();


        inflateToolbarMenu(R.menu.menu_home_search);
        mSearchView = (MstSearchView) findViewById(R.id.menu_search);
        mSearchView.setOnCloseListener(this);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnSearchClickListener(this);
        mSearchView.needHintIcon(false);
        mSearchView.setQueryHint(getString(R.string.home_search_hint));
        mAddButton = (FloatingActionButton) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteLog.v(TAG, "go into new editor activity");
                Intent intent = new Intent();
                intent.setAction(NoteEditorActivity.ACTION_NEW_ADD);
                startActivity(intent);
            }
        });
        mBottombar = (BottomNavigationView) findViewById(R.id.bottom_menu);
        mDelButtonMenu = mBottombar.getMenu().findItem(R.id.delete);
        mBottombar.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (!mNoteHomeAdapter.isCheck()) {
                    return false;
                }
                DialogHelper.showDialog(NoteHomeActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            mNoteHomeAdapter.delItem();
                            enterEditMode(false);
                        }
                    }
                }, R.string.dialog_del_title, R.string.dialog_del_note_msg);

                return false;
            }
        });
        enterEditMode(false);
        mHomeRecycler = (RecyclerView) findViewById(R.id.note_home_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mHomeRecycler.setLayoutManager(layoutManager);
        mHomeRecycler.setItemAnimator(null);
        mContentResolver = getContentResolver();
        setupActionModeWithDecor(mToolBar);
        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                switch (item.getItemId()) {
                    case ActionMode.NAGATIVE_BUTTON:
                        enterEditMode(false);
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        changeAllCheck();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {

            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {

            }
        });

        setActionModeTitle(0);
        NoteLog.v(TAG, "onCreate end");
    }

    public void changeAllCheck() {

        if (mSelectAll) {
            getActionMode().setPositiveText(getResources().getString(R.string.un_select_all));
            mNoteHomeAdapter.checkAll(true);
            setDelButtonStatus(true);
        } else {
            getActionMode().setPositiveText(getResources().getString(R.string.home_all_selection));
            mNoteHomeAdapter.checkAll(false);
            setDelButtonStatus(false);
        }
        mSelectAll = !mSelectAll;
    }

    public void setActionModeTitle(int num){
        String title = getString(R.string.action_mode_title);
        getActionMode().setTitle(String.format(title,num));
    }

    /**
     * true all check,fale->un check
     *
     * @param result
     */
    public void changeAllCheck(boolean result) {
        mSelectAll = result;
        if (mSelectAll) {
            getActionMode().setPositiveText(getResources().getString(R.string.home_all_selection));
        } else {
            getActionMode().setPositiveText(getResources().getString(R.string.un_select_all));
        }
    }

    public void setDelButtonStatus(boolean enable) {
        mBottombar.setItemEnable(R.id.delete, enable);
        /*Drawable drawable = mDelButtonMenu.getIcon();
        int alphaValue = enable ? 255 : 50;
        drawable.setAlpha(alphaValue);
        mDelButtonMenu.setIcon(drawable);*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtil.requestRecordPermission(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NoteLog.d(TAG, "onResume");
        Cursor cursor = getCursor();
        if (mEditMode) {
            //mutil selection mode,not reload data,or check status not save
        } else if (mSearchView.isIconified()) {
            mToolBar.setNavigationIcon(null);

            mNoteHomeAdapter = new NoteHomeAdapter(this, cursor);
            mHomeRecycler.setAdapter(mNoteHomeAdapter);
        } else {
            //when resume on search state,also need update data.Or home not update
            mNoteHomeAdapter.setHomeAdapter(cursor, false);
            mNoteHomeAdapter.setSearchAdapter(cursor, true);
            mNoteHomeAdapter.updateSearchView();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        NoteLog.d(TAG, "OnRestart");
    }

    /**
     * click one item,then view the note
     *
     * @param id note's id
     */
    public void goToEdit(long id) {
        NoteLog.d(TAG, "view the item:" + id);
        Intent intent = new Intent();
        intent.setAction(NoteEditorActivity.ACTION_VIEW);
        intent.putExtra(NoteEditorActivity.INTENT_KEY_ID, id);
        startActivity(intent);
    }

    private void closeSearchView() {
        if (!mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            if (!mSearchView.isIconified()) {
                mSearchView.setIconified(true);
            }
        }
    }

    @Override
    public boolean onClose() {
        NoteLog.d(TAG, "search close");
        mAddButton.setVisibility(View.VISIBLE);
        mToolBar.setNavigationIcon(null);
        mNoteHomeAdapter.closeSearch();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        NoteLog.d(TAG, "search submit= " + query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        NoteLog.d(TAG, "search change= " + newText);
        if (mNoteHomeAdapter != null) {
            mNoteHomeAdapter.searchText(newText);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        NoteLog.d(TAG, "search click");
        mAddButton.setVisibility(View.GONE);
        mToolBar.setNavigationIcon(mToolBarIcon);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearchView();
            }
        });
        Cursor cursor = getCursor();
        mNoteHomeAdapter.setSearchData(cursor);
        mHomeRecycler.scrollToPosition(0);
    }

    private Cursor getCursor() {
        return mContentResolver.query(DBData.TABLE_URI,
                new String[]{DBData.COLUMN_ID, DBData.COLUMN_FIRSTLINE, DBData.COLUMN_SECOND_LINE,
                        DBData.COLUMN_WILL, DBData.COLUMN_IMG, DBData.COLUMN_AUDIO, DBData.COLUMN_TIME, DBData.COLUMN_XML},
                null, null, DBData.COLUMN_TIME + " DESC");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mSearchView.isIconified()) {
                NoteLog.d(TAG, "back close search view");
                closeSearchView();
                return true;
            }
            if (mEditMode) {
                enterEditMode(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        NoteLog.d(TAG, "OnDestory");
        NotificationHelper.cancelNotification(this);
        super.onDestroy();
    }

    public boolean getEditMode() {
        return mEditMode;
    }

    public void enterEditMode(boolean editMode) {
        if (!mSearchView.isIconified()) {
            return;
        }
        if (editMode) {
            mToolBar.setVisibility(View.INVISIBLE);
            mAddButton.setVisibility(View.GONE);
            mBottombar.setVisibility(View.VISIBLE);
        } else {
            mToolBar.setVisibility(View.VISIBLE);
            mAddButton.setVisibility(View.VISIBLE);
            mBottombar.setVisibility(View.GONE);
        }
        mEditMode = editMode;
        showActionMode(editMode);
        if (mNoteHomeAdapter != null) {
            mNoteHomeAdapter.notifyDataSetChanged();
        }
    }
}
