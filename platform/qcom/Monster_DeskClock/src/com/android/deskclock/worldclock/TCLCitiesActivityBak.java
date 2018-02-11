package com.android.deskclock.worldclock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.android.deskclock.R;
import com.android.deskclock.Utils;
import com.android.deskclock.Util.EmployeePinyinComparator;
import com.android.deskclock.view.SideBar;
import com.android.deskclock.view.SideBar.OnTouchingLetterChangedListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MstSearchView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MstSearchView.OnQueryTextListener;
import mst.app.MstActivity;

public class TCLCitiesActivityBak extends MstActivity {

    private TCLCitiesListAdapter mAdapter;
    private TCLCitiesListAdapter mSearchAdapter;

    private ListView my_listview;
    private FrameLayout all_data_layout;
    private TextView mid_sidebar_text;
    private SideBar sidebar;
    private ListView search_listview;
    private TextView text_no_seach_data;
    private RelativeLayout search_data_layout;

    private MstSearchView mSearchView;

    private List<CityObj> dataList;

    private LinearLayout titleLayout;
    private TextView title;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setVolumeControlStream(AudioManager.STREAM_ALARM);

        setMstContentView(R.layout.tcl_cities_activity_bak);
        initData();
        initView();

        setListener();
    }

    private void initData() {
        mAdapter = new TCLCitiesListAdapter(this);
        mSearchAdapter = new TCLCitiesListAdapter(this);
        mSearchAdapter.setSearchMode(true);
        dataList = Utils.loadCitiesListFromXml(this,true);
        mAdapter.updateList(dataList);
    }

    private int lastFirstVisibleItem = -1;

    private void initView() {

        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.title);

        my_listview = (ListView) findViewById(R.id.my_listview);
        all_data_layout = (FrameLayout) findViewById(R.id.all_data_layout);
        mid_sidebar_text = (TextView) findViewById(R.id.mid_sidebar_text);
        sidebar = (SideBar) findViewById(R.id.sidebar);
        search_listview = (ListView) findViewById(R.id.search_listview);
        text_no_seach_data = (TextView) findViewById(R.id.text_no_seach_data);
        search_data_layout = (RelativeLayout) findViewById(R.id.search_data_layout);

        my_listview.setAdapter(mAdapter);
        search_listview.setAdapter(mSearchAdapter);
        sidebar.setDialogTextView(mid_sidebar_text);
        sidebar.updateCataLogList(Utils.getSortLetterMap());
    }

    private void setListener() {
        sidebar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getPositionForSection(s.charAt(0));
                position = position + my_listview.getHeaderViewsCount();
                if (position >= 0) {
                    my_listview.setSelection(position);
                }
            }
        });

        my_listview.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int first = firstVisibleItem - my_listview.getHeaderViewsCount();
                int section = mAdapter.getSectionForPosition(first);
                if (sidebar.getDialogTextView().isShown()) {
                } else {
                    if (section > 0) {
                        sidebar.setSelction(section);
                    }
                }

                int nextSection = mAdapter.getSectionForPosition(first + 1);
                int nextSecPosition = mAdapter.getPositionForSection(nextSection);

                if (first != lastFirstVisibleItem) {
                    MarginLayoutParams params = (MarginLayoutParams) titleLayout.getLayoutParams();
                    params.topMargin = 0;
                    titleLayout.setLayoutParams(params);
                    if (String.valueOf((char) section) != null) {
                        title.setText(String.valueOf((char) section));
                    }
                }
                if (nextSecPosition == first + 1) {
                    View childView = view.getChildAt(0);
                    if (childView != null) {
                        int titleHeight = titleLayout.getHeight();
                        int bottom = childView.getBottom();
                        MarginLayoutParams params = (MarginLayoutParams) titleLayout.getLayoutParams();
                        if (bottom < titleHeight) {
                            float pushedDistance = bottom - titleHeight;
                            params.topMargin = (int) pushedDistance;
                            titleLayout.setLayoutParams(params);

                        } else {
                            if (params.topMargin != 0) {
                                params.topMargin = 0;
                                titleLayout.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = first;

            }
        });

        search_listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CityObj> list = mSearchAdapter.getList();
                CityObj city = list.get(position - search_listview.getHeaderViewsCount());
                Cities.saveCityToSharedPrefs(PreferenceManager.getDefaultSharedPreferences(TCLCitiesActivityBak.this),
                        city);
                Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
                sendBroadcast(i);
                finish();
            }
        });

        my_listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CityObj> list = mAdapter.getList();
                CityObj city = list.get(position - my_listview.getHeaderViewsCount());
                Cities.saveCityToSharedPrefs(PreferenceManager.getDefaultSharedPreferences(TCLCitiesActivityBak.this),
                        city);
                Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
                sendBroadcast(i);
                finish();
            }
        });

    }

    @Override
    protected void initialUI(Bundle savedInstanceState) {
        super.initialUI(savedInstanceState);
        inflateToolbarMenu(R.menu.toolbar_menu);
        MenuItem searchMenu = getToolbar().getMenu().findItem(R.id.menu_search);
        mSearchView = (MstSearchView) MenuItemCompat.getActionView(searchMenu);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getString(R.string.str_search_hint));
        // mSearchView.needHintIcon(false);

        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {

                String key = arg0.toString().trim();
                if (key.length() > 0) {
                    all_data_layout.setVisibility(View.GONE);
                    search_data_layout.setVisibility(View.VISIBLE);
                    List<CityObj> list = SearchByKey(key);
                    mSearchAdapter.updateList(list);
                    if (list.size() == 0) {
                        text_no_seach_data.setVisibility(View.VISIBLE);
                    } else {
                        text_no_seach_data.setVisibility(View.GONE);
                    }
                } else {
                    all_data_layout.setVisibility(View.VISIBLE);
                    search_data_layout.setVisibility(View.GONE);
                }

                return false;
            }
        });

    }

    private List<CityObj> SearchByKey(String key) {

        String modifiedQuery = key.toString().trim().toUpperCase();

        List<CityObj> list = new ArrayList<CityObj>();

        for (int i = 0; i < dataList.size(); i++) {
            CityObj city = dataList.get(i);
            String cityName = city.mCityName.trim().toUpperCase();
            if (cityName.contains(modifiedQuery)) {
                list.add(city);
            }
        }

        Collections.sort(list, new EmployeePinyinComparator());

        return list;
    }

    @Override
    public void onNavigationClicked(View view) {
        // 在这里处理Toolbar上的返回按钮的点击事件
        finish();
    }

}
