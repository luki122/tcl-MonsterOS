package com.mst.tms;
import com.mst.tms.MarkResult;
import com.mst.tms.UsefulNumberResult;

interface ITmsService {
    String getArea(String number);
    void updateDatabaseIfNeed();
    MarkResult getMark(int type, String number);
    List<UsefulNumberResult> getUsefulNumber(String number);
}
