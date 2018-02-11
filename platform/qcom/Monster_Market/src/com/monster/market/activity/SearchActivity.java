package com.monster.market.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MstSearchView;

import com.monster.market.R;
import com.monster.market.adapter.SearchHistoryAdapter;
import com.monster.market.adapter.SearchKeyAdapter;
import com.monster.market.bean.SearchKeyInfo;
import com.monster.market.constants.Constant;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.DownloadInitListener;
import com.monster.market.fragment.AppListFragment;
import com.monster.market.http.DataResponse;
import com.monster.market.http.Request;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.SearchKeyListResultData;
import com.monster.market.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xiaobin on 16-8-22.
 */
public class SearchActivity extends BaseActivity {

    private static final int MODE_HISTORY = 1;
    private static final int MODE_HINT = 2;
    private static final int MODE_SEARCH = 3;

    private MstSearchView mstSearchView;
    private ListView lv_searchKey;
    private View clearFooterView;
    private View container;

    private String query;

    private final int KEY_TYPE_HISTORY = 1;
    private final int KEY_TYPE_POP = 2;
    private int keyType = KEY_TYPE_HISTORY;
    private SearchKeyAdapter searchKeyAdapter;
    private List<SearchKeyInfo> searchKeyInfoList;
    private SearchHistoryAdapter searchHistoryAdapter;
    private List<String> searchHistoryList;

    private SharedPreferences mSharedPreference;

    private AppListFragment appListFragment;

    private boolean searchByIntent = false;     // 是否已经由外部开始请求搜索

    private int mode = MODE_HISTORY;

    private String searchKey;
    private Request searchKeyRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_search);
        LogUtil.i(TAG, "SearchActivity onCreate()");

        initViews();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String scheme = intent.getScheme();

        if (!TextUtils.isEmpty(scheme) && scheme.equals("market")) {
            Uri uri = intent.getData();
            String host = uri.getHost();
            if (host.equals("search")) {
                query = uri.getQueryParameter("q");
                searchByIntent = true;

                if (!TextUtils.isEmpty(query)) {
                    mstSearchView.setQuery(query, true);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mstSearchView.clearFocus();
                        }
                    }, 50);
                }
            }
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    @Override
    public void initViews() {
        mToolbar = getToolbar();
        mToolbar.inflateMenu(R.menu.toolbar_action_search);

        lv_searchKey = (ListView) findViewById(R.id.lv_searchKey);
        lv_searchKey.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int size = 0;
                if (keyType == KEY_TYPE_POP) {
                    size = searchKeyInfoList.size();
                } else if (keyType == KEY_TYPE_HISTORY) {
                    size = searchHistoryList.size();
                }

                if (i >= 0 && i < size) {
                    String key;
                    if (keyType == KEY_TYPE_POP) {
                        key = searchKeyInfoList.get(i).getKey();
                    } else {
                        key = searchHistoryList.get(i);
                    }
                    mstSearchView.setQuery(key, true);

                    mode = MODE_SEARCH;
                    changeMode();
                }
            }
        });

        clearFooterView = LayoutInflater.from(this).inflate(R.layout.item_search_footer, null);
        clearFooterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSearchRecords();
            }
        });

        container = findViewById(R.id.container);

        initSearchView();
    }

    @Override
    public void initData() {
        searchKeyInfoList = new ArrayList<SearchKeyInfo>();
        searchKeyAdapter = new SearchKeyAdapter(this, searchKeyInfoList);

        searchHistoryList = new ArrayList<String>();
        searchHistoryAdapter = new SearchHistoryAdapter(this, searchHistoryList);

        loadSearchRecords();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        appListFragment = AppListFragment.newInstance(AppListFragment.TYPE_SEARCH, query);

        ft.add(R.id.container, appListFragment);
        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        AppDownloadService.checkInit(this, new DownloadInitListener() {
            @Override
            public void onFinishInit() {
                startIntentSearch();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        container.clearFocus();
    }

    private void startIntentSearch() {
        if (!searchByIntent) {
            Intent intent = getIntent();
            String scheme = intent.getScheme();

            if (!TextUtils.isEmpty(scheme) && scheme.equals("market")) {
                Uri uri = intent.getData();
                String host = uri.getHost();
                if (host.equals("search")) {
                    query = uri.getQueryParameter("q");
                    searchByIntent = true;

                    if (!TextUtils.isEmpty(query)) {
                        mstSearchView.setQuery(query, true);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mstSearchView.clearFocus();
                            }
                        }, 50);
                    }
                }
            }
        }
    }

    private void initSearchView() {
        mstSearchView = (MstSearchView) mToolbar.getMenu().findItem(R.id.searchView).getActionView();

        mstSearchView.setQueryHint(getString(R.string.search_app));
        mstSearchView.setIconifiedByDefault(false);
        mstSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mstSearchView.needHintIcon(false);

        mstSearchView.onActionViewCollapsed();
        mstSearchView.onActionViewExpanded();

        mstSearchView.setOnQueryTextListener(new MstSearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String s) {

                query = s;
                saveSearchRecords();
                search();

                mode = MODE_SEARCH;
                changeMode();

                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String s) {

                if (searchKeyRequest != null) {
                    searchKeyRequest.cancel();
                }
                searchKey = s;
                if (!TextUtils.isEmpty(s)) {
                    getSearchKey(s);
                    mode = MODE_HINT;
                } else {
                    loadSearchRecords();
                    mode = MODE_HISTORY;
                }
                changeMode();

                return false;
            }
        });
    }

    private void getSearchKey(final String key) {
        searchKeyRequest = RequestHelper.getSearchKey(this, key, new DataResponse<SearchKeyListResultData>() {
            @Override
            public void onResponse(SearchKeyListResultData value) {
                LogUtil.i("SearchActivity", value.toString());

                if (key.equals(searchKey)) {
                    searchKeyInfoList.clear();
                    searchKeyInfoList.addAll(value.getKeyList());
                    keyType = KEY_TYPE_POP;
                    lv_searchKey.removeFooterView(clearFooterView);
                    lv_searchKey.setAdapter(searchKeyAdapter);
                }
            }

            @Override
            public void onErrorResponse(RequestError error) {
                LogUtil.i("SearchActivity", error.toString());
            }
        });
    }

    private void loadSearchRecords() {
        searchHistoryList.clear();

        if (mSharedPreference == null) {
            mSharedPreference = this
                    .getSharedPreferences(Constant.HISTORY_RECORDS_FILENAME,
                            Context.MODE_PRIVATE);
        }

        for (int i = 0; i < Constant.HISTORY_MAX_LIMIT; i++) {

            String lHistory = mSharedPreference.getString(
                    Constant.HISTORY_RECORDS + i, null);

            if (lHistory != null) {
                searchHistoryList.add(lHistory);
            }
        }

        Collections.reverse(searchHistoryList);

        lv_searchKey.removeFooterView(clearFooterView);
        if (searchHistoryList.size() > 0) {
            lv_searchKey.addFooterView(clearFooterView);
        }

        keyType = KEY_TYPE_HISTORY;
        lv_searchKey.setAdapter(searchHistoryAdapter);
    }

    private void clearSearchRecords() {
        if (mSharedPreference == null) {
            mSharedPreference = this.getSharedPreferences(
                    Constant.HISTORY_RECORDS_FILENAME,
                    Context.MODE_PRIVATE);
        }

        mSharedPreference.edit().clear().commit();
        searchHistoryList.clear();
        keyType = KEY_TYPE_HISTORY;
        lv_searchKey.removeFooterView(clearFooterView);
        lv_searchKey.setAdapter(searchHistoryAdapter);

    }

    private void saveSearchRecords() {

        if (!query.trim().equals("") && query != null) {
            if (mSharedPreference == null) {
                mSharedPreference = this
                        .getSharedPreferences(Constant.HISTORY_RECORDS_FILENAME,
                                Context.MODE_PRIVATE);
            }

            // 是否已经存在
            boolean exists = false;

            // 读取配置的数据
            List<String> tempList = new ArrayList<String>();
            for (int i = 0; i < Constant.HISTORY_MAX_LIMIT; i++) {

                String lHistory = mSharedPreference.getString(
                        Constant.HISTORY_RECORDS + i, null);

                if (lHistory != null) {
                    tempList.add(lHistory);
                }

                if (lHistory != null && lHistory.equals(query)) {
                    exists = true;
                }
            }

            if (exists) {
                tempList.remove(query);
            }
            tempList.add(query);

            int size = tempList.size();
            if (size > Constant.HISTORY_MAX_LIMIT) {
                tempList.remove(0);
            }
            size = tempList.size();

            mSharedPreference.edit().clear().commit();
            SharedPreferences.Editor editor = mSharedPreference.edit();
            for (int i = 0; i < Constant.HISTORY_MAX_LIMIT; i++) {
                if (i < size) {
                    editor.putString(
                            Constant.HISTORY_RECORDS + i, tempList.get(i));
                }
            }
            editor.commit();

        }
    }

    private void search() {
        appListFragment.search(query);

        container.requestFocusFromTouch();
    }

    private void changeMode() {
        switch (mode) {
            case MODE_HISTORY:
                lv_searchKey.setVisibility(View.VISIBLE);
                break;
            case MODE_HINT:
                lv_searchKey.setVisibility(View.VISIBLE);
                break;
            case MODE_SEARCH:
                lv_searchKey.setVisibility(View.GONE);
                break;
        }
    }

}
