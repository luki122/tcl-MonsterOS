/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Handler;

public class PercentageBarManager {

    private List<PercentageBar.Entry> mEntries = new ArrayList<PercentageBar.Entry>();
    private PercentageBar mChart;

    public PercentageBarManager(PercentageBar Chart) {
        mChart = Chart;
        mChart.setEntries(mEntries);
    }

    public void clear() {
        mEntries.clear();
    }

    public void Commit() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                if (mChart != null) {
                    mChart.invalidate();
                }
            }
        });
    }

    public List<PercentageBar.Entry> getEntries() {
        return mEntries;
    }

    public void addEntry(int order, float percentage, Drawable drawable) {
        mEntries.add(PercentageBar.createEntry(order, percentage, drawable));
        Collections.sort(mEntries);
    }

    public int getEntrySize() {
        return mEntries.size();
    }
}
