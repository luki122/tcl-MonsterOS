package cn.tcl.transfer.data.Calllog;

import java.io.Serializable;

public class CallLogModel implements Serializable {
    private static final String TAG = "CallLogModel";

    public int mCallType;
    public long mDate;
    public String mNumber;
    public String mDuration;
    public int mNew;
    public String mAccountid;

}
