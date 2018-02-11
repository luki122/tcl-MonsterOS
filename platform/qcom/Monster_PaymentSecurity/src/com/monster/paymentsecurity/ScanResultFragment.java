package com.monster.paymentsecurity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monster.paymentsecurity.adapter.ScanResultsAdapter;
import com.monster.paymentsecurity.util.IconCache;
import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.diagnostic.DiagnosticReport;

import mst.widget.MstRecyclerView;
import mst.widget.recycleview.LinearLayoutManager;;

/**
 * 扫描结果页
 * Created by logic on 16-12-21.
 */
public class ScanResultFragment extends Fragment {

    public static final String REPORT_DATA = "report_datas";
    public static final String TAG = ScanResultFragment.class.getSimpleName();
    private MstRecyclerView mRecyclerView;
    private DiagnosticReport report;
    IconCache mIconCache;
    BroadcastReceiver mReceiver;

    public static ScanResultFragment create(DiagnosticReport report){
        ScanResultFragment f = new ScanResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(REPORT_DATA, report);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        report = args.getParcelable(REPORT_DATA);
        IntentFilter filter = new IntentFilter(Constant.ACTION_PAYENV_CHANGE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mRecyclerView != null){
                    MstRecyclerView.Adapter adapter = mRecyclerView.getAdapter();
                    if (null != adapter)
                        adapter.notifyDataSetChanged();
                }
            }
        };
        mIconCache = new IconCache(getActivity());
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRecyclerView == null){
            mRecyclerView = (MstRecyclerView) inflater.inflate(R.layout.fragment_scan_result, container, false);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        }else {
            ViewGroup parent = (ViewGroup) mRecyclerView.getParent();
            parent.removeView(mRecyclerView);
        }
        return mRecyclerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setAdapter(new ScanResultsAdapter(getActivity(), report, mIconCache));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIconCache.clear();
        mIconCache = null;
        getActivity().unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public void onDetach() {
        report = null;
        mRecyclerView.setAdapter(null);
        super.onDetach();
    }
}
