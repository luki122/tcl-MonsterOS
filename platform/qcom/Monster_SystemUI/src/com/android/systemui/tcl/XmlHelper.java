package com.android.systemui.tcl;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuzhicang on 16-10-12.
 * pull读写xml，重启后读取清理的通知列表
 */

public class XmlHelper {
    private static XmlHelper mInstance;
    private static String PATH;
    private static final String FILE = "wdj_notify_clean.xml";

    public synchronized static XmlHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new XmlHelper(context);
        }
        return mInstance;
    }

    private XmlHelper(Context context) {
        PATH = context.getFilesDir().getAbsolutePath() + "/" + FILE;
    }

    /**
     * 写xml
     */
    public boolean writeXML(List<WdjNotifyClearGroup> wdjNotifyClearGroups) {
        File file = new File(PATH);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output",
                    true);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, "wdj");
            for (WdjNotifyClearGroup group : wdjNotifyClearGroups) {
                serializer.startTag(null, "wdj_notification");

                serializer.startTag(null, "notify_type");
                serializer.text(Utils.serialize(group.getNotifyType()));
                serializer.endTag(null, "notify_type");

                serializer.startTag(null, "notifications");
                serializer.text(Utils.serialize(group.getWdjNotifyClearItems()));
                serializer.endTag(null, "notifications");

                serializer.endTag(null, "wdj_notification");

            }
            serializer.endTag(null, "wdj");
            serializer.endDocument();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }

        return true;
    }

    /**
     * 解析xml
     */
    public List<WdjNotifyClearGroup> pullPhraseXml(Context context) {
        List<WdjNotifyClearGroup> wdjNotifyClearGroups = new ArrayList<WdjNotifyClearGroup>();
        WdjNotifyClearGroup group = null;
        InputStream is;
        try {
            is = new FileInputStream(PATH);
            // 由android.util.Xml创建一个XmlPullParser实例
            XmlPullParser xpp = Xml.newPullParser();
            // 设置输入流 并指明编码方式
            xpp.setInput(is, "UTF-8");
            // 产生第一个事件
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xpp.getName().equals("wdj_notification")) { // 判断开始标签元素是否是wdj_notification
                            group = new WdjNotifyClearGroup();
                        } else if (xpp.getName().equals("notify_type")) {
                            eventType = xpp.next();
                            group.setNotifyType(Utils.deSerialize(xpp.getText()));
                        } else if (xpp.getName().equals("notifications")) {
                            eventType = xpp.next();
                            group.setWdjNotifyClearItems(Utils.deSerialize(xpp.getText()));
                        }
                        break;

                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        if (xpp.getName().equals("wdj_notification")) {
                            wdjNotifyClearGroups.add(group);
                            group = null;
                        }
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wdjNotifyClearGroups;
    }

}
