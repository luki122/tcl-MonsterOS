package com.mst.tms;
import com.mst.tms.MarkResult;

interface ITmsService {
    String getArea(String number);
    void updateDatabaseIfNeed();
    MarkResult getMark(int type, String number);
}
