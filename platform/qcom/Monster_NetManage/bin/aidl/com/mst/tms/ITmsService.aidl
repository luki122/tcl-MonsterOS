package com.mst.tms;
import com.mst.tms.MarkResult;
import com.mst.tms.UsefulNumberResult;
import com.mst.tms.CodeNameInfo;
import java.util.List;
import com.mst.tms.ITrafficCorrectListener;

interface ITmsService {
    String getArea(String number);
    void updateDatabaseIfNeed();
    MarkResult getMark(int type, String number);
    List<UsefulNumberResult> getUsefulNumber(String number);
    List<CodeNameInfo> getAllProvinces();
    List<CodeNameInfo> getCities(String provinceCode);
    List<CodeNameInfo> getCarries();
    List<CodeNameInfo> getBrands(String carryId);
    int setConfig(int simIndex, String provinceId, String cityId, String carryId, String brandId, int closingDay);
    int startCorrection(int simIndex);
    int analysisSMS(int simIndex, String queryCode, String queryPort, String smsBody);
    int[] getTrafficInfo(int simIndex);
    void trafficCorrectListener(ITrafficCorrectListener listener);
    void updateSimInfo(inout String[] simImsiArray);
}
