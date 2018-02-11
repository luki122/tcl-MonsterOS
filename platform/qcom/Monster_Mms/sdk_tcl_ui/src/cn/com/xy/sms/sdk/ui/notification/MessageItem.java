package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 通知栏点击事件参数序列化对象
 * */
public class MessageItem implements Parcelable {

    public int mMsgId;
    public String mPhoneNum;
    public String mMsg;
    public Map<String, String> mExtend = new HashMap<String, String>();
    public Map<String, Object> mMap = new HashMap<String, Object>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Parcelable.Creator<MessageItem> CREATOR = new Creator() {

        @Override
        public MessageItem createFromParcel(Parcel source) {
            /*必须按成员变量声明的顺序读取数据，不然会出现获取数据出错*/
            MessageItem item = new MessageItem();
            item.mMsgId = source.readInt();
            item.mPhoneNum = source.readString();
            item.mMsg = source.readString();
            item.mExtend = source.readHashMap(HashMap.class.getClassLoader());
            item.mMap = source.readHashMap(HashMap.class.getClassLoader());
            return item;
        }

        @Override
        public MessageItem[] newArray(int size) {
            return new MessageItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMsgId);
        dest.writeString(mPhoneNum);
        dest.writeString(mMsg);
        dest.writeMap(mExtend);
        dest.writeMap(mMap);
    }

}
