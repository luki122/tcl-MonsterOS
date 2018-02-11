package cn.download.mie.base.update;

public interface URLDownloader {
	int download(URLParams params);
	void cancel();
}
