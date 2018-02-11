package com.monster.market.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.monster.market.R;
import com.monster.market.activity.AppListActivity;
import com.monster.market.adapter.CategoryAdapter;
import com.monster.market.bean.AppTypeInfo;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AppTypeListResultData;
import com.monster.market.utils.LoadingPageUtil;
import com.monster.market.views.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import mst.widget.recycleview.GridLayoutManager;
import mst.widget.recycleview.RecyclerView;

/**
 * Created by xiaobin on 16-8-11.
 */
public class CategoryFragment extends BaseFragment {

    public static final String TAG = "CategoryFragment";
    public static final int TYPE_APP = 1;
    public static final int TYPE_GAME = 2;
    private static final String AGRS_TYPE = "type";

    private Context mContext;

    private RecyclerView recyclerView;

    private LoadingPageUtil loadingPageUtil;

    private int type;

    private List<AppTypeInfo> appTypeInfoList;
    private CategoryAdapter adapter;

    public static CategoryFragment newInstance(int type) {
        CategoryFragment f = new CategoryFragment();
        Bundle b = new Bundle();
        b.putInt(AGRS_TYPE, type);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContext = null;

        if (loadingPageUtil != null) {
            loadingPageUtil.exit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            type = args.getInt(AGRS_TYPE, TYPE_APP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container,
                false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        appTypeInfoList = new ArrayList<AppTypeInfo>();
        adapter = new CategoryAdapter(mContext, appTypeInfoList);

        initViews();
        initData();
    }

    @Override
    public void initViews() {
        View view = getView();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        int spacing = getResources()
                .getDimensionPixelOffset(R.dimen.category_item_left_right);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing, true));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                AppTypeInfo info = appTypeInfoList.get(position);
                Intent i = new Intent(mContext, AppListActivity.class);
                i.putExtra(AppListActivity.OPEN_TYPE, AppListActivity.TYPE_CATEGORY);
                i.putExtra(AppListActivity.TYPE_NAME, info.getTypeName());
                i.putExtra(AppListActivity.TYPE_SUB_ID, info.getTypeId());
                startActivity(i);
            }
        });

        initLoadingPage();
    }

    @Override
    public void initData() {
        RequestHelper.getAppTypeList(mContext, 0, 50, type, new DataResponse<AppTypeListResultData>() {
            @Override
            public void onResponse(AppTypeListResultData value) {
                if (value != null) {
                    appTypeInfoList.addAll(value.getAppTypeList());
                }

                adapter.notifyDataSetChanged();
                loadingPageUtil.hideLoadPage();
            }

            @Override
            public void onErrorResponse(RequestError error) {
                if (error.getErrorType() == RequestError.ERROR_NO_NETWORK) {
                    loadingPageUtil.showNoNetWork();
                } else {
                    loadingPageUtil.showNetworkError();
                }
            }
        });
    }

    private void initLoadingPage() {
        View view = getView();
        loadingPageUtil = new LoadingPageUtil();
        loadingPageUtil.init(mContext, view.findViewById(R.id.frameLayout));
        loadingPageUtil.setOnRetryListener(new LoadingPageUtil.OnRetryListener() {
            @Override
            public void retry() {
                initData();
            }
        });
        loadingPageUtil.setOnShowListener(new LoadingPageUtil.OnShowListener() {
            @Override
            public void onShow() {
                recyclerView.setVisibility(View.GONE);
            }
        });
        loadingPageUtil.setOnHideListener(new LoadingPageUtil.OnHideListener() {
            @Override
            public void onHide() {
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        loadingPageUtil.showLoadPage();
        loadingPageUtil.showLoading();
    }

}
