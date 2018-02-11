/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data.sms;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Rainmin on 2016/9/27.
 */

public class MmsModel implements Serializable {

    public long _id;
    public int thread_id;
    public int date;
    public int date_sent;
    public int msg_box;
    public int read;
    public String m_id;
    public String sub;
    public int sub_cs;
    public String ct_t;
    public String ct_l;
    public int exp;
    public String m_cls;
    public int m_type;
    public int v;
    public int m_size;
    public int pri;
    public int rr;
    public int rpt_a;
    public int resp_st;
    public int st;
    public int st_ext;
    public String tr_id;
    public int retr_st;
    public String retr_txt;
    public int retr_txt_cs;
    public int read_status;
    public int ct_cls;
    public String resp_txt;
    public int d_tm;
    public int d_rpt;
    public int locked;
    public int sub_id;
//    public String service_center;
    public int seen;
    public int text_only;

    public ArrayList<MmsPart> mmsPartsList;
    public ArrayList<MmsAddr> mmsAddrsList;

    public MmsModel() {
        mmsPartsList = new ArrayList<>();
        mmsAddrsList = new ArrayList<>();
    }

    public static class MmsPart implements Serializable {

        public int mid;
        public int seq;
        public String ct;
        public String name;
        public int chset;
        public String cd;
        public String fn;
        public String cid;
        public String cl;
        public int ctt_s;
        public String ctt_t;
        public String _data;
        public String text;

        public String toString() {
            return "MmsPart [mid=" + mid + ", seq=" + seq + ", ct=" + ct + ", name=" + name +
                    ", chset=" + chset + ", cd=" + cd + ", fn=" + fn + ", cid=" + cid + ", cl=" +
                    cl + ", ctt_s=" + ctt_s + ", ctt_t=" + ctt_t + ", _data=" + _data + ", text=" +
                    text + "]";
        }
    }

    public static class MmsAddr implements Serializable {

        public int msg_id;
        //public int contact_id;
        public String address;
        public int type;
        public int charset;

        public String toString() {
            return "MmsAddr [msg_id=" + msg_id + ", address=" + address + ", type=" + type +
                    ", charset=" + charset + "]";
        }
    }

}
