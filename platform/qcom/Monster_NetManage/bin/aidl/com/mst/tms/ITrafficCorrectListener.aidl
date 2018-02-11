package com.mst.tms;
interface ITrafficCorrectListener {
     void onNeedSmsCorrection(int simIndex, String queryCode, String queryPort);
     void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes);
     void onError(int simIndex, int errorCode);
}