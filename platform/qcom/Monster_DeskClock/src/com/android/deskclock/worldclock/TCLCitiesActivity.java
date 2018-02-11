package com.android.deskclock.worldclock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import com.android.deskclock.DeskClock;
import com.android.deskclock.R;
import com.android.deskclock.Utils;
import com.android.deskclock.Util.EmployeePinyinComparator;
import com.android.deskclock.view.SideBar;
import com.android.deskclock.view.SideBar.OnTouchingLetterChangedListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import mst.widget.MstIndexBar;
import mst.widget.MstIndexBar.Letter;
import mst.widget.MstIndexBar.TouchState;

public class TCLCitiesActivity extends MstActivity implements MstIndexBar.OnSelectListener, MstIndexBar.OnTouchStateChangedListener {

    private TCLCitiesListAdapter mAdapter;
    private TCLCitiesListAdapter mSearchAdapter;

    private ListView my_listview;
    private RelativeLayout all_data_layout;
    private TextView mid_sidebar_text;
    private ListView search_listview;
    private TextView text_no_seach_data;
    private RelativeLayout search_data_layout;

    private MstSearchView mSearchView;

    private ArrayList<CityObj> dataList;

    private MstIndexBar index_bar;
    private LinearLayout titleLayout;
    private TextView title;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        getToolbar().setTitle("");
        setMstContentView(R.layout.tcl_cities_activity);
        index_bar = (MstIndexBar) findViewById(R.id.index_bar);
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        titleLayout.setVisibility(View.GONE);
        initData();
        initView();
        getCityData(false);

        index_bar.deleteLetter(0);//删除* #
        index_bar.deleteLetter(26);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getCityData(true);
            }
        },300);

    }

    public void getCityData(boolean trans_pinying) {
//        new AsyncTask<Object, Object, ArrayList>() {
//            @Override
//            protected ArrayList doInBackground(Object... params) {
//                return Utils.loadCitiesListFromXml(TCLCitiesActivity.this);
//            }
//
//            @Override
//            protected void onPostExecute(ArrayList arrayList) {
//                dataList = arrayList;
//                titleLayout.setVisibility(View.VISIBLE);
//                mAdapter.updateList(dataList);
//                initIndexBar(arrayList);
//                setListener();
//            }
//        }.executeOnExecutor(Executors.newCachedThreadPool());//zouxu 20161102

        dataList = Utils.loadCitiesListFromXml(TCLCitiesActivity.this,trans_pinying);
        titleLayout.setVisibility(View.VISIBLE);
        mAdapter.updateList(dataList);
        initIndexBar(dataList);
        setListener();


    }


    private void initData() {
        mAdapter = new TCLCitiesListAdapter(this);
        mSearchAdapter = new TCLCitiesListAdapter(this);
        mSearchAdapter.setSearchMode(true);
//        dataList = Utils.loadCitiesListFromXml(this);
    }

    private void initIndexBar(List array) {
        ArrayList<Letter> sub = null;
        String last = "";
        int lastindex = -1;
        int otherindex = -1;
        boolean changed = false;
        for (int p = 0; p < array.size(); p++) {
            CityObj c = (CityObj) array.get(p);
            int namesize = c.firstLetter.size();
            String firletter = "", secletter = "";
            for (int i = 0; i < namesize; i++) {
                if (i == 0) {
                    firletter = c.firstLetter.get(0);
                    changed = !firletter.equals(last);
                    last = firletter;
                } else if (i == 1) {
                    secletter = c.firstLetter.get(1);
                }
            }
            if (changed) {
                if (sub != null && lastindex != -1) {
                    //index_bar.setSubList(lastindex,sub);//不显示二次搜索
                }
                if (!"".equals(firletter)) {
                    int index = index_bar.getIndex(firletter);
                    if (index != -1) {
                        lastindex = index;
                        sub = new ArrayList<>();
                    } else {
                        sub = null;
                    }
                    if (index == -1) {//其他（#）的索引
                        index = index_bar.size() - 1;
                        if (otherindex == -1) {
                            otherindex = p;
                        }
                    }
                    //设置第一个字母对应的列表索引
                    Letter letter = index_bar.getLetter(index);
                    if (letter != null) {
                        letter.list_index = index == index_bar.size() - 1 ? otherindex : p;
                    }
                    index_bar.setEnables(true, index);
                }
            }
            //设置第二个字母的列表索引
            if (sub != null && secletter != "") {
                if (!sub.contains(Letter.valueOf(secletter))) {
                    Letter letter = Letter.valueOf(secletter);
                    letter.enable = true;
                    letter.list_index = p;
                    sub.add(letter);
                }
            }
        }
        if (sub != null && lastindex != -1) {
            //index_bar.setSubList(lastindex,sub); //不显示第二搜索
        }

    }


    private int lastFirstVisibleItem = -1;

    private void initView() {

        index_bar.setOnSelectListener(this);
        index_bar.setOnTouchStateChangedListener(this);
        index_bar.setBalloonFocusColor(getColor(R.color.clock_red));

//        index_bar.setLetterColor(getColor(R.color.text_normal_gray), getColor(R.color.clock_red));
//        index_bar.setColor(getColor(R.color.clock_red));

        title = (TextView) findViewById(R.id.title);

        my_listview = (ListView) findViewById(R.id.my_listview);
        all_data_layout = (RelativeLayout) findViewById(R.id.all_data_layout);
        mid_sidebar_text = (TextView) findViewById(R.id.mid_sidebar_text);
        search_listview = (ListView) findViewById(R.id.search_listview);
        text_no_seach_data = (TextView) findViewById(R.id.text_no_seach_data);
        search_data_layout = (RelativeLayout) findViewById(R.id.search_data_layout);
        my_listview.setAdapter(mAdapter);
        search_listview.setAdapter(mSearchAdapter);
    }

    private void setListener() {
//        sidebar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
//
//            @Override
//            public void onTouchingLetterChanged(String s) {
//                int position = mAdapter.getPositionForSection(s.charAt(0));
//                position = position + my_listview.getHeaderViewsCount();
//                if (position >= 0) {
//                    my_listview.setSelection(position);
//                }
//            }
//        });

        my_listview.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int arg1) {
                switch (arg1) {
                    case OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:// 滚动状态
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动
                        HideKeyboard(my_listview);
                        break;
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int first = firstVisibleItem - my_listview.getHeaderViewsCount();
                int section = mAdapter.getSectionForPosition(first);

                CityObj city = (CityObj) mAdapter.getItem(firstVisibleItem);
                if (city != null) {
                    int index = -1;
                    if (city.firstLetter != null && city.firstLetter.size() > 0) {
                        String fir = city.firstLetter.get(0);
                        index = index_bar.getIndex(fir);
                    } else {
//                        String fir = contact.name;//这里貌似有点问题
                        String fir = city.mCityIndex;
                        index = index_bar.getIndex(fir);
                    }

                    if (index == -1) {
                        index = index_bar.size() - 1;
                    }

//                    index_bar.setFocus(index);
                    index_bar.setFocus(index, getColor(R.color.clock_red));
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

        search_listview.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {


                switch (arg1) {
                    case OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:// 滚动状态
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动
                        HideKeyboard(search_listview);
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {


            }
        });


        search_listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CityObj> list = mSearchAdapter.getList();
                CityObj city = list.get(position - search_listview.getHeaderViewsCount());
                Cities.saveCityToSharedPrefs(PreferenceManager.getDefaultSharedPreferences(TCLCitiesActivity.this),
                        city);
                Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
                sendBroadcast(i);
                String select_city_id = city.mCityId;
                i.putExtra("select_city_id", select_city_id);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        my_listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CityObj> list = mAdapter.getList();
                CityObj city = list.get(position - my_listview.getHeaderViewsCount());
                Cities.saveCityToSharedPrefs(PreferenceManager.getDefaultSharedPreferences(TCLCitiesActivity.this),
                        city);
                Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
                sendBroadcast(i);
                String select_city_id = city.mCityId;
                i.putExtra("select_city_id", select_city_id);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        index_bar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HideKeyboard(index_bar);
                return false;
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
        mSearchView.needHintIcon(false);
        mSearchView.setQueryHint(getString(R.string.str_search_hint));

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

        if (dataList == null) {//add zouxu 20161008
            return list;
        }

        for (int i = 0; i < dataList.size(); i++) {
            CityObj city = dataList.get(i);
            String cityName = city.mCityName.trim().toUpperCase();

            String short_name = "";
            for (int j = 0; j < city.firstLetter.size(); j++) {
                short_name = short_name + city.firstLetter.get(j);
            }
            short_name = short_name.toUpperCase();

            if (cityName.contains(modifiedQuery) || short_name.contains(modifiedQuery) || city.pinyin.toUpperCase().contains(modifiedQuery)) {
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

    @Override
    public void onStateChanged(TouchState arg0, TouchState arg1) {

    }

    @Override
    public void onSelect(int index, int layer, MstIndexBar.Letter letter) {
//        int listindex = letter.list_index;
//        if(layer == 0){
//            listindex--;
//        }
//        my_listview.setSelection(listindex);
        letter.setFocusColor(getColor(R.color.clock_red));
        int position = mAdapter.getPositionForSection(letter.text.charAt(0));
        position = position + my_listview.getHeaderViewsCount();
        if (position >= 0) {
            my_listview.setSelection(position);
        }

    }

    public void HideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            if(mSearchView !=null){
                mSearchView.clearFocus();
            }
        }
    }

}
