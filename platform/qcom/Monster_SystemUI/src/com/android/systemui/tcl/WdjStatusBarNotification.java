package com.android.systemui.tcl;

import android.app.Notification;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by liuzhicang on 16-10-13.
 * 序列化保存数据，重启机器数据不丢失
 */

public class WdjStatusBarNotification implements Serializable {
    public StatusBarNotification sbn;

    public WdjStatusBarNotification(StatusBarNotification sbn) {
        this.sbn = sbn;
    }

    public StatusBarNotification getSbn() {
        return sbn;
    }

    public void setSbn(StatusBarNotification sbn) {
        this.sbn = sbn;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(sbn.getPackageName());
        out.writeObject(sbn.getOpPkg());
        out.writeObject(sbn.getId());
        out.writeObject(sbn.getTag());
        out.writeObject(sbn.getUid());
        out.writeObject(sbn.getInitialPid());
        out.writeObject(sbn.getUserId());
        out.writeObject(sbn.getOverrideGroupKey());
        out.writeObject(sbn.getPostTime());
        //notification title
        out.writeObject(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE));
        //notification text
        out.writeObject(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT));
        out.writeObject(sbn.getNotification().tickerText);
        out.writeObject(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
    }

    //对应的，反序列化过程中JVM也会检查类似的一个私有方法。
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String pkg = (String) in.readObject();
        String opPkg = (String) in.readObject();
        int id = (int) in.readObject();
        String tag = (String) in.readObject();
        int uid = (int) in.readObject();
        int initialPid = (int) in.readObject();
        int userId = (int) in.readObject();
        String overrideGroupKey = (String) in.readObject();
        long postTime = (long) in.readObject();
        CharSequence title = (CharSequence) in.readObject();
        CharSequence text = (CharSequence) in.readObject();
        CharSequence tickerText = (CharSequence) in.readObject();
        CharSequence subText = (CharSequence) in.readObject();
        Notification notification = Utils.getNotification(pkg, title, text, tickerText, subText);
        this.sbn = new StatusBarNotification(pkg, opPkg, id, tag, uid, initialPid, 0, notification, UserHandle.getUserHandleForUid(userId), postTime);
    }


}
