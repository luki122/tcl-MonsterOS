package com.monster.market.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.monster.market.constants.HttpConstant;
import com.monster.market.constants.WandoujiaDownloadConstant;

public class AppDownloadData implements Parcelable {

	private String taskId; // 唯一ID,用于标示下砸任务,包名+版本号组合. 例如:com.monster.market_1
	private int apkId; // 软件ID，可用在发送通知图标用
	private String apkName; // 软件名字，显示在UI上
	private String apkDownloadPath; // 下载地址
	private String versionName; // 版本，用于对比手机上的
	private int versionCode; // 版本码，用于对比手机上的
	private String packageName; // 包名，用于检查手机是否已安装
	private String apkLogoPath; // 图标位置

	// 以下字段只有在数据库查找时才会有
	private int status; // 状态
	private String fileDir; // 文件存放目录
	private String fileName; // 文件名称
	private long finishTime; // 任务完成时间

	// 以下字段为豌豆荚合作需要的字段
	private String pos;		// 应用展示位置
	private String download_type = WandoujiaDownloadConstant.TYPE_NORMAL;	// 是更新还是自然下载 download update


	// 以下为临时字段, 用于上报下载安装的作用
	/*
	1	广告推荐
	2	主页应用推荐
	3	重点推广应用
	4	新品推荐
	5	游戏排行
	6	应用排行
	7	应用分类
	8	搜索
	9	设计奖
	10  游戏必备
	11  应用必备
	12  云服务
	*/
	private int reportModulId = HttpConstant.REPORT_MODULID_HOMEPAGE;
	private int reportInstallType = 0; // 0 安装  1 更新

	// 以下为临时字段，用于云服务显示
	private long showAppSize = 0;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public int getApkId() {
		return apkId;
	}

	public void setApkId(int apkId) {
		this.apkId = apkId;
	}

	public String getApkName() {
		return apkName;
	}

	public void setApkName(String apkName) {
		this.apkName = apkName;
	}

	public String getApkDownloadPath() {
		return apkDownloadPath;
	}

	public void setApkDownloadPath(String apkDownloadPath) {
		this.apkDownloadPath = apkDownloadPath;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getApkLogoPath() {
		return apkLogoPath;
	}

	public void setApkLogoPath(String apkLogoPath) {
		this.apkLogoPath = apkLogoPath;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getDownload_type() {
		return download_type;
	}

	public void setDownload_type(String download_type) {
		this.download_type = download_type;
	}

	public int getReportModulId() {
		return reportModulId;
	}

	public void setReportModulId(int reportModulId) {
		this.reportModulId = reportModulId;
	}

	public int getReportInstallType() {
		return reportInstallType;
	}

	public void setReportInstallType(int reportInstallType) {
		this.reportInstallType = reportInstallType;
	}

	public long getShowAppSize() {
		return showAppSize;
	}

	public void setShowAppSize(long showAppSize) {
		this.showAppSize = showAppSize;
	}

	public  static final Parcelable.Creator<AppDownloadData> CREATOR = new Creator<AppDownloadData>() {
		@Override
		public AppDownloadData createFromParcel(Parcel source) {
			return new AppDownloadData(source);
		}
		@Override
		public AppDownloadData[] newArray(int size) {
			return new AppDownloadData[size];
		}
	};

	public AppDownloadData() {

	}

	public AppDownloadData(Parcel in) {
		taskId = in.readString();
		apkId = in.readInt();
		apkName = in.readString();
		apkDownloadPath = in.readString();
		versionName = in.readString();
		versionCode = in.readInt();
		packageName = in.readString();
		apkLogoPath = in.readString();

		status = in.readInt();
		fileDir = in.readString();
		fileName = in.readString();
		finishTime = in.readLong();

		pos = in.readString();
		download_type = in.readString();

		reportModulId = in.readInt();
		reportInstallType = in.readInt();

		showAppSize = in.readLong();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(this.taskId);
		dest.writeInt(this.apkId);
		dest.writeString(this.apkName);
		dest.writeString(this.apkDownloadPath);
		dest.writeString(this.versionName);
		dest.writeInt(this.versionCode);
		dest.writeString(this.packageName);
		dest.writeString(this.apkLogoPath);

		dest.writeInt(this.status);
		dest.writeString(this.fileDir);
		dest.writeString(this.fileName);
		dest.writeLong(this.finishTime);

		dest.writeString(this.pos);
		dest.writeString(this.download_type);

		dest.writeInt(this.reportModulId);
		dest.writeInt(this.reportInstallType);

		dest.writeLong(this.showAppSize);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
