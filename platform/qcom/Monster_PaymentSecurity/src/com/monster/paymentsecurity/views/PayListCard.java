package com.monster.paymentsecurity.views;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.paymentsecurity.PayListActivity;
import com.monster.paymentsecurity.R;
import com.monster.paymentsecurity.adapter.PayListCardAdapter;
import com.monster.paymentsecurity.bean.PayAppInfo;
import com.monster.paymentsecurity.db.PayListDao;
import com.monster.paymentsecurity.util.Utils;

import java.util.List;

import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;

/**
 *
 *  正在防护支付应用卡片
 * Created by sandysheny on 16-11-28.
 */

public class PayListCard {
    private TextView mTextView;
    private ImageView mImageView;

    private PayListCardAdapter mListAdapter;
    private PayListDao mPayListDao;
    List<PayAppInfo> list;
    private Context mContext;

    public PayListCard(Context context, View view) {
        this.mContext = context;
        initView(context, view);
    }

    private void initView(Context context, View view) {

        mTextView = (TextView) view.findViewById(R.id.title);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mImageView = (ImageView) view.findViewById(R.id.icon_more);
        Button mButton = (Button) view.findViewById(R.id.action);

        mListAdapter = new PayListCardAdapter(mContext, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        SpaceDecoration divider = new SpaceDecoration(mContext, LinearLayoutManager.HORIZONTAL, Utils.dip2px
                (mContext, 32));
        mRecyclerView.addItemDecoration(divider);
        mRecyclerView.setAdapter(mListAdapter);

        mButton.setOnClickListener(v -> {
            Intent payListIntent = new Intent(mContext,PayListActivity.class);
            mContext.startActivity(payListIntent);
        });

        mPayListDao = new PayListDao(mContext);
        initData(true);
    }

    public void initData(boolean query) {
        boolean refresh = query || list == null;
        if (refresh) {
             list = mPayListDao.getPayList(true);
            mTextView.setText(mContext.getString(R.string.applist_protecting, list.size()));

            if (list.size() > 4) {
                list = list.subList(0,4);
                mImageView.setVisibility(View.VISIBLE);
            } else {
                mImageView.setVisibility(View.GONE);
            }
            mListAdapter.setData(list);
        }
    }

}
