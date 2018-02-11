package com.android.systemui.tcl;

import com.wandoujia.nisdk.core.NIFilter;
import com.wandoujia.nisdk.core.model.NotificationPriority;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by liuzhicang on 16-10-12.
 * 序列化保存数据，重启机器数据不丢失
 */

public class WdjFilterResult implements Serializable {
    private NIFilter.FilterResult filterResult;

    public WdjFilterResult(NIFilter.FilterResult result) {
        this.filterResult = result;
    }

    public NIFilter.FilterResult getFilterResult() {
        return filterResult;
    }

    public void setFilterResult(NIFilter.FilterResult filterResult) {
        this.filterResult = filterResult;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(filterResult.categoryKey);
        out.writeObject(filterResult.categoryPriority);
        out.writeObject(filterResult.tagKey);
        out.writeObject(filterResult.notificationPriority);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        filterResult = new NIFilter.FilterResult();
        filterResult.categoryKey = (String) in.readObject();
        filterResult.categoryPriority = (NotificationPriority) in.readObject();
        filterResult.tagKey = (String) in.readObject();
        filterResult.notificationPriority = (NotificationPriority) in.readObject();
    }
}
