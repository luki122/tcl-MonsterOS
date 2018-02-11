/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data.sms;

import java.io.Serializable;

/**
 * Created by user on 16-9-23.
 */
public class SmsModel implements Serializable {

    private static final String TAG = "SmsModel";

    //public int mThreadId;           // thread_id
    public String mAddress;         // address
    public int mPerson;             // person
    public long mDate;              // date
    public long mDateSent;          // date_sent
    public int mProtocol;           // protocol
    public int mRead;               // read
    public int mStatus;             // status
    public int mType;               // type
    public int mReply;              // reply_path_present
    public String mSubject;         // subject
    public String mBody;            // body
    public String mServiceCenter;   // service_center
    public int mLocked;             // locked
    public int mErrorCode;          // error_code
    public int mSeen;               // seen
}
