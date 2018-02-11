package com.mst.tms;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.ITrafficCorrectionListener;

public class TmsService extends Service {

	protected static final String TAG = "TmsService";
	private ITrafficCorrectListener mTrafficCorrectListener;

	public IBinder onBind(Intent t) {
		TrafficCorrectionWrapper.getInstance().init(getApplicationContext());
		return mBinder;
	}

	private final ITmsService.Stub mBinder = new ITmsService.Stub() {
		@Override
		public String getArea(String number) {
			return AreaManager.getArea(number);
		}

		public void updateDatabaseIfNeed() {
			UpdataManagerMst.updateDatabaseIfNeed();
		}

		public MarkResult getMark(int type, String number) {
			return MarkManager.getMark(type, number);
		}

		public List<UsefulNumberResult> getUsefulNumber(String number) {
			return UsefulNumbersManager.getUsefulNumber(number);
		}

		@Override
		public List<CodeNameInfo> getAllProvinces() throws RemoteException {
			TrafficCorrectionWrapper correctionWrapper = TrafficCorrectionWrapper.getInstance();
			ArrayList<CodeName> allProvinces = correctionWrapper.getAllProvinces();
			ArrayList<CodeNameInfo> codeNameInfos = getCodeNameList(allProvinces);
			return codeNameInfos;
		}

		@Override
		public ArrayList<CodeNameInfo> getCities(String provinceCode) throws RemoteException {
			ArrayList<CodeName> allCitys = TrafficCorrectionWrapper.getInstance().getCities(provinceCode);
			ArrayList<CodeNameInfo> codeNameInfos = getCodeNameList(allCitys);
			return codeNameInfos;
		}

		@Override
		public ArrayList<CodeNameInfo> getCarries() throws RemoteException {
			ArrayList<CodeName> allCarries = TrafficCorrectionWrapper.getInstance().getCarries();
			ArrayList<CodeNameInfo> codeNameInfos = getCodeNameList(allCarries);
			return codeNameInfos;
		}

		@Override
		public ArrayList<CodeNameInfo> getBrands(String carryId) throws RemoteException {
			ArrayList<CodeName> allBrands = TrafficCorrectionWrapper.getInstance().getBrands(carryId);
			ArrayList<CodeNameInfo> codeNameInfos = getCodeNameList(allBrands);
			return codeNameInfos;
		}

		@Override
		public int setConfig(int simIndex, String provinceId, String cityId, String carryId, String brandId,
				int closingDay) throws RemoteException {
			int code;
			try {
				code = TrafficCorrectionWrapper.getInstance().setConfig(simIndex, provinceId, cityId, carryId, brandId,
						closingDay);
			} catch (RuntimeException e) {
				throw e;
			}
			return code;
		}

		@Override
		public int startCorrection(int simIndex) throws RemoteException {
			return TrafficCorrectionWrapper.getInstance().startCorrection(simIndex);
		}

		@Override
		public int analysisSMS(int simIndex, String queryCode, String queryPort, String smsBody)
				throws RemoteException {
			return TrafficCorrectionWrapper.getInstance().analysisSMS(simIndex, queryCode, queryPort, smsBody);
		}

		@Override
		public void trafficCorrectListener(ITrafficCorrectListener listener) throws RemoteException {
			mTrafficCorrectListener = listener;
			TrafficCorrectionWrapper.getInstance().setTrafficCorrectionListener(new ITrafficCorrectionListener() {
				@Override
				public void onNeedSmsCorrection(int simIndex, String queryCode, String queryPort) {
					// 需要发查询短信校正
					try {
						if (null != mTrafficCorrectListener) {
							mTrafficCorrectListener.onNeedSmsCorrection(simIndex, queryCode, queryPort);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) {
					// 解析短信成功
					try {
						if (null != mTrafficCorrectListener) {
							mTrafficCorrectListener.onTrafficInfoNotify(simIndex, trafficClass, subClass, kBytes);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onError(int simIndex, int errorCode) {
					// 校正出错
					try {
						if (null != mTrafficCorrectListener) {
							mTrafficCorrectListener.onError(simIndex, errorCode);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			});
		}

		@Override
		public int[] getTrafficInfo(int simIndex) throws RemoteException {
			return TrafficCorrectionWrapper.getInstance().getTrafficInfo(simIndex);
		}

		@Override
		public void updateSimInfo(String[] simImsiArray) throws RemoteException {
			TmsManager.setDualPhoneInfoFetcher(simImsiArray);
		}

	      @Override
	        public boolean canRejectSms(String number, String smscontent){
	            return SmartSmsManager.canRejectSms(number, smscontent);
	        }
	};

	private ArrayList<CodeNameInfo> getCodeNameList(ArrayList<CodeName> codeNames) {
		ArrayList<CodeNameInfo> codeNameInfos = new ArrayList<CodeNameInfo>();
		for (int i = 0; i < codeNames.size(); i++) {
			CodeName codeName = codeNames.get(i);
			CodeNameInfo codeNameInfo = new CodeNameInfo(codeName.mCode, codeName.mName);
			codeNameInfos.add(codeNameInfo);
		}
		return codeNameInfos;
	}

}
