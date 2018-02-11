package com.mst.tms;
import com.mst.tms.NetInfoEntity;
interface INetworkChangeCallBackListener {
      void onClosingDateReached();
      void onDayChanged();
      void onNormalChanged(in NetInfoEntity networkInfoEntity);
}