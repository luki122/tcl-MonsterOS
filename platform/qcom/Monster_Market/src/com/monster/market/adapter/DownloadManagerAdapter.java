package com.monster.market.adapter;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.market.R;
import com.monster.market.activity.DownloadManagerActivity;
import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.AppDownloader;
import com.monster.market.download.DownloadManagerBean;
import com.monster.market.install.InstallAppManager;
import com.monster.market.utils.ProgressBtnUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.monster.market.views.ProgressBtn;
import com.monster.market.views.stickylistheaders.StickyListHeadersAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mst.app.dialog.AlertDialog;
import mst.widget.SliderLayout;

public class DownloadManagerAdapter extends BaseAdapter implements
		StickyListHeadersAdapter, SectionIndexer {

	private static final int TYPE_DOWNLOADING = 0;
	private static final int TYPE_DOWNLOADED = 1;
	private static final int TYPE_DOWNLOADED_SHOW = 2;

	private DownloadManagerActivity managerActivity;
	private LayoutInflater inflater;
	private ProgressBtnUtil progressBtnUtil;

	private int[] sectionIndices;
	private Integer[] sectionsHeaders;

	private int downloadingCount;
	private int downloadedCount;

	private List<DownloadManagerBean> listData;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage;

	public boolean mDeleteFlag = false;
	private int mTotalHeight;
	private boolean editMode = false;
	private boolean isNeedAnim = false;

	private Set<String> selectSet;

	public DownloadManagerAdapter(DownloadManagerActivity managerActivity,
			List<DownloadManagerBean> listData) {
		this.managerActivity = managerActivity;
		inflater = LayoutInflater.from(managerActivity);
		progressBtnUtil = new ProgressBtnUtil();

		this.listData = listData;
		updateSectionIndice();
		selectSet = new HashSet<String>();

		mTotalHeight = Math.round(managerActivity.getResources().getDimension(
				R.dimen.down_man_item_height));

		optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.icon_app_default)
				.showImageForEmptyUri(R.drawable.icon_app_default)
				.showImageOnFail(R.drawable.icon_app_default)
				.displayer(new RoundedBitmapDisplayer(10)).cacheInMemory(true)
				.cacheOnDisk(true).build();
	}

	private int[] getSectionIndices() {
		ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
		if (getCount() > 0) {
			int type = listData.get(0).getType();
			sectionIndices.add(0);
			for (int i = 1; i < getCount(); i++) {
				DownloadManagerBean bean = listData.get(i);
				if (bean.getType() != type) {
					type = bean.getType();
					sectionIndices.add(i);
				}
			}
		}
		int[] sections = new int[sectionIndices.size()];
		for (int i = 0; i < sectionIndices.size(); i++) {
			sections[i] = sectionIndices.get(i);
		}
		return sections;
	}

	private Integer[] getSectionHeaders() {
		Integer[] sectionHeaders = new Integer[sectionIndices.length];
		for (int i = 0; i < sectionIndices.length; i++) {
			sectionHeaders[i] = Integer.valueOf(listData.get(i).getType());
		}
		return sectionHeaders;
	}

	public void updateSectionIndice() {
		sectionIndices = getSectionIndices();
		sectionsHeaders = getSectionHeaders();
	}

	@Override
	public int getCount() {
		return listData == null ? 0 : listData.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		DownloadManagerBean bean = listData.get(position);
		if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADING) {
			return TYPE_DOWNLOADING;
		} else if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADED
				&& bean.getDownloadData() == null) {
			return TYPE_DOWNLOADED_SHOW;
		}
		return TYPE_DOWNLOADED;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_DOWNLOADED_SHOW + 1;
	}
	
	private void doOperationContinue(final int status,
			final DownloadManagerBean bean) {
		if (status == AppDownloader.STATUS_PAUSE_NEED_CONTINUE) {
			AlertDialog mWifiConDialog = new AlertDialog.Builder(
					managerActivity)
					.setTitle(managerActivity.getResources().getString(
									R.string.dialog_prompt))
					.setMessage(managerActivity
									.getResources()
									.getString(R.string.downloadman_continue_download_by_mobile))
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									AppDownloadService.pauseOrContinueDownload(
											managerActivity,
											bean.getDownloadData());
								}

							}).create();
			mWifiConDialog.show();
		} else {
			AppDownloadService.pauseOrContinueDownload(managerActivity,
					bean.getDownloadData());
		}
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		BaseHolder baseHolder = null;
		DownloadingHolder downloadingHolder = null;
		DownloadedHolder downloadedHolder = null;
		final DownloadManagerBean bean = listData.get(position);
		int type = getItemViewType(position);
		if (convertView == null) {
			switch (type) {
			case TYPE_DOWNLOADING:
				convertView = inflater.inflate(R.layout.item_download_manager_downloading, null);

				downloadingHolder = createDownloadingHolder(convertView);
				convertView.setTag(downloadingHolder);
				baseHolder = downloadingHolder;
				
				expandViewTouchDelegate(downloadingHolder.btn_operation);
				break;
			case TYPE_DOWNLOADED:
				convertView = inflater.inflate(R.layout.item_download_manager_downloaded, null);

				downloadedHolder = createDownloadedHolder(convertView);
				convertView.setTag(downloadedHolder);
				baseHolder = downloadedHolder;

				if (bean.getDownloadData() != null) {
					downloadedHolder.progressBtn.setTag(bean.getDownloadData()
							.getTaskId());
				} else {
					downloadedHolder.progressBtn.setTag("0");
				}
				break;
			case TYPE_DOWNLOADED_SHOW:
				convertView = inflater.inflate(R.layout.item_download_man_footer, null);
				break;
			}
		} else {
			switch (type) {
			case TYPE_DOWNLOADING:
				downloadingHolder = (DownloadingHolder) convertView.getTag();
				baseHolder = downloadingHolder;
				break;
			case TYPE_DOWNLOADED:
				downloadedHolder = (DownloadedHolder) convertView.getTag();
				baseHolder = downloadedHolder;
				break;
			}
		}

		if (type == TYPE_DOWNLOADED_SHOW) {
			return convertView;
		}

		checkSliderLayout(baseHolder);
		checkCheck(bean, baseHolder);
		checkShowCheckBox(bean, baseHolder);
		checkButtonEnable(bean, baseHolder);

		switch (type) {
		case TYPE_DOWNLOADING:
			downloadingHolder.tv_appname.setText(bean.getDownloadData()
					.getApkName());

			// 开始头像图片异步加载
			imageLoader.displayImage(bean.getDownloadData()
					.getApkLogoPath(), downloadingHolder.iv_icon,
					optionsImage, animateFirstListener);

			downloadingHolder.download_progress_grey.setProgress(bean
					.getProgress());
			downloadingHolder.download_progress.setProgress(bean
					.getProgress());

			if (bean.getFileSize() != 0) {
				downloadingHolder.tv_progress.setText(SystemUtil.bytes2kb(bean
						.getDownloadSize())
						+ "/"
						+ SystemUtil.bytes2kb(bean.getFileSize()));
			} else {
				downloadingHolder.tv_progress.setText("");
			}

			final int status = bean.getDownloadStatus();
			
			AppDownloader downloader = AppDownloadService.getDownloaders()
					.get(bean.getDownloadData().getTaskId());
			// 显示暂停
			if (status == AppDownloader.STATUS_DOWNLOADING
					|| status == AppDownloader.STATUS_CONNECTING
					|| status == AppDownloader.STATUS_NO_NETWORK) {
				downloadingHolder.download_progress_grey.setVisibility(View.GONE);
				downloadingHolder.download_progress.setVisibility(View.VISIBLE);
				
				downloadingHolder.btn_operation.setText(managerActivity
						.getString(R.string.downloadman_pause));
				
				downloadingHolder.tv_speed.setTextColor(managerActivity
						.getResources().getColor(
								R.color.down_man_item_speed_text));
				downloadingHolder.tv_speed.setText(getStatusString(status,
						downloader));

				downloadingHolder.btn_operation.setBackgroundResource(R.drawable.button_default_selector);
			} else if (status == AppDownloader.STATUS_WAIT) {	// 显示等待
				downloadingHolder.download_progress_grey.setVisibility(View.GONE);
				downloadingHolder.download_progress.setVisibility(View.VISIBLE);
				
				downloadingHolder.btn_operation.setText(managerActivity
						.getString(R.string.downloadman_pause));
				
				downloadingHolder.tv_speed.setTextColor(managerActivity
						.getResources().getColor(
								R.color.down_man_item_status_text));
				downloadingHolder.tv_speed.setText(getStatusString(status,
						downloader));

				downloadingHolder.btn_operation.setBackgroundResource(R.drawable.button_default_selector);
			} else if (status == AppDownloader.STATUS_FAIL) { // 显示重试
				downloadingHolder.download_progress_grey.setVisibility(View.VISIBLE);
				downloadingHolder.download_progress.setVisibility(View.GONE);
				
				downloadingHolder.tv_speed.setText(managerActivity
						.getString(R.string.downloadman_down_fail));
				downloadingHolder.tv_speed.setTextColor(managerActivity
						.getResources().getColor(
								R.color.down_man_item_status_text));
				downloadingHolder.btn_operation.setText(managerActivity
						.getString(R.string.downloadman_retry));

				downloadingHolder.btn_operation.setBackgroundResource(R.drawable.button_default_selector);
			} else { // 显示继续
				downloadingHolder.download_progress_grey.setVisibility(View.VISIBLE);
				downloadingHolder.download_progress.setVisibility(View.GONE);
				
				downloadingHolder.tv_speed.setText(getStatusString(status,
						downloader));
				downloadingHolder.tv_speed.setTextColor(managerActivity
						.getResources().getColor(
								R.color.down_man_item_status_text));
				downloadingHolder.btn_operation.setText(managerActivity
						.getString(R.string.downloadman_continue));

				downloadingHolder.btn_operation.setBackgroundResource(R.drawable.button_default_selector);
			}
			OnClickListener clickListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!SettingUtil.canDownload(managerActivity)
							&& status != AppDownloader.STATUS_DOWNLOADING
							&& status != AppDownloader.STATUS_CONNECTING) {
						AlertDialog mWifiConDialog = new AlertDialog.Builder(
								managerActivity)
								.setTitle(managerActivity.getResources()
												.getString(R.string.dialog_prompt))
								.setMessage(managerActivity.getResources()
												.getString(R.string.no_wifi_download_message))
								.setNegativeButton(android.R.string.cancel, null)
								.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											SharedPreferences sp = PreferenceManager
													.getDefaultSharedPreferences(managerActivity);
											Editor ed = sp.edit();
											ed.putBoolean("wifi_download_key",
													false);
											ed.commit();
											doOperationContinue(status,bean);
										}

									}).create();
						mWifiConDialog.show();
					} else if ((status == AppDownloader.STATUS_PAUSE || status == AppDownloader.STATUS_FAIL)
							&& !SystemUtil.hasNetwork()) {
						Toast.makeText(managerActivity, managerActivity
								.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
					} else {
						doOperationContinue(status, bean);
					}
				}
			};
			downloadingHolder.btn_operation.setOnClickListener(clickListener);

			break;
		case TYPE_DOWNLOADED:
			if (bean.getDownloadData() == null) {
				downloadedHolder.normal.setVisibility(View.GONE);
				downloadedHolder.footer.setVisibility(View.VISIBLE);
			} else {
				downloadedHolder.normal.setVisibility(View.VISIBLE);
				downloadedHolder.footer.setVisibility(View.GONE);

				downloadedHolder.tv_appname.setText(bean.getDownloadData()
						.getApkName());

				// 开始头像图片异步加载
				imageLoader.displayImage(bean.getDownloadData()
						.getApkLogoPath(), downloadedHolder.iv_icon,
						optionsImage, animateFirstListener);

				progressBtnUtil.updateFinishProgressBtn(
						downloadedHolder.progressBtn, bean.getDownloadData());

				// 如果已安装
				InstalledAppInfo installedAppInfo = InstallAppManager
						.getInstalledAppInfo(managerActivity, bean
								.getDownloadData().getPackageName());
				if (installedAppInfo != null
						&& installedAppInfo.getVersionCode() // 显示打开
						>= bean.getDownloadData().getVersionCode()) {
					downloadedHolder.tv_status.setText(managerActivity
							.getString(R.string.downloadman_installed));
				} else { // 显示安装
					if (bean.getDownloadStatus() == AppDownloader.STATUS_INSTALLING) {
						downloadedHolder.tv_status.setText(managerActivity
								.getString(R.string.downloadman_installing));
					} else if (bean.getDownloadStatus() == AppDownloader.STATUS_INSTALLFAILED) {
						downloadedHolder.tv_status.setText(managerActivity
								.getString(R.string.downloadman_install_failed));
					} else {
						downloadedHolder.tv_status.setText(managerActivity
								.getString(R.string.downloadman_no_install));
					}
				}
			}
			break;
		}

		switch (type) {
		case TYPE_DOWNLOADING:
			downloadingHolder.position = position;
			break;
		case TYPE_DOWNLOADED:
			downloadedHolder.position = position;
			break;
		}

		if (mDeleteFlag) {
			resetListItemHeight(convertView);
		}

		baseHolder.tv_operation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				managerActivity.showDeleteDialog(listData.get(position));
			}
		});

		return convertView;
	}

	@Override
	public int getPositionForSection(int sectionIndex) {
		if (sectionIndex >= sectionIndices.length) {
			sectionIndex = sectionIndices.length - 1;
		} else if (sectionIndex < 0) {
			sectionIndex = 0;
		}
		return sectionIndices[sectionIndex];
	}

	@Override
	public int getSectionForPosition(int position) {
		for (int i = 0; i < sectionIndices.length; i++) {
			if (position < sectionIndices[i]) {
				return i - 1;
			}
		}
		return sectionIndices.length - 1;
	}

	@Override
	public Object[] getSections() {
		return sectionsHeaders;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderHolder headerHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.header_downloading_manager,
					parent, false);
			headerHolder = new HeaderHolder();
			headerHolder.tv_title = (TextView) convertView
					.findViewById(R.id.tv_title);
			headerHolder.iv_clean = (ImageView) convertView
					.findViewById(R.id.iv_clean);
			convertView.setTag(headerHolder);
		} else {
			headerHolder = (HeaderHolder) convertView.getTag();
		}

		String downloadingStr = managerActivity
				.getString(R.string.downloadman_downloading);
		String downloadedStr = managerActivity
				.getString(R.string.downloadman_downloaded);
		if (sectionIndices.length > 1) {
			long id = getHeaderId(position);
			if (id == DownloadManagerBean.TYPE_DOWNLOADING) {
				headerHolder.tv_title.setText(downloadingStr + "（"
						+ downloadingCount + "）");
				headerHolder.iv_clean.setVisibility(View.GONE);
			} else {
				headerHolder.tv_title.setText(downloadedStr + "（"
						+ downloadedCount + "）");
				headerHolder.iv_clean.setVisibility(View.VISIBLE);
				if (editMode) {
					headerHolder.iv_clean.setImageResource(R.drawable.icon_delete_grey);
				} else {
					headerHolder.iv_clean.setImageResource(R.drawable.icon_delete);
				}
				headerHolder.iv_clean.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						managerActivity.closeSliderView(true);
						managerActivity.showDeleteAllFinishDialog();
					}
				});
			}
		} else {
			long id = getHeaderId(position);
			if (id == DownloadManagerBean.TYPE_DOWNLOADING) {
				headerHolder.tv_title.setText(downloadingStr + "（"
						+ downloadingCount + "）");
				headerHolder.iv_clean.setVisibility(View.GONE);
			} else {
				headerHolder.tv_title.setText(downloadedStr + "（"
						+ downloadedCount + "）");
				headerHolder.iv_clean.setVisibility(View.VISIBLE);
				if (editMode) {
					headerHolder.iv_clean.setImageResource(R.drawable.icon_delete_grey);
				} else {
					headerHolder.iv_clean.setImageResource(R.drawable.icon_delete);
				}
				headerHolder.iv_clean.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						managerActivity.closeSliderView(true);
						managerActivity.showDeleteAllFinishDialog();
					}
				});
			}
		}

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		return listData.get(position).getType();
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	static class BaseHolder {
		SliderLayout sliderLayout;
		int position;
		CheckBox cb;
		TextView tv_operation;
	}

	static class DownloadingHolder extends BaseHolder {
		ImageView iv_icon;
		TextView tv_appname;
		ProgressBar download_progress_grey;
		ProgressBar download_progress;
		TextView tv_speed;
		TextView tv_progress;
		Button btn_operation;
	}

	static class DownloadedHolder extends BaseHolder {
		ImageView iv_icon;
		TextView tv_appname;
		TextView tv_status;
		ProgressBtn progressBtn;
		RelativeLayout normal;
		LinearLayout footer;
	}

	static class HeaderHolder {
		TextView tv_title;
		ImageView iv_clean;
	}

	private DownloadingHolder createDownloadingHolder(View view) {
		DownloadingHolder downloadingHolder = new DownloadingHolder();
		downloadingHolder.sliderLayout = (SliderLayout) view.findViewById(com.mst.R.id.slider_view);
		downloadingHolder.cb = (CheckBox) view.findViewById(R.id.checkbox);
		downloadingHolder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
		downloadingHolder.tv_appname = (TextView) view
				.findViewById(R.id.tv_appname);
		downloadingHolder.download_progress_grey = (ProgressBar) view
				.findViewById(R.id.download_progress_grey);
		downloadingHolder.download_progress = (ProgressBar) view
				.findViewById(R.id.download_progress);
		downloadingHolder.tv_speed = (TextView) view
				.findViewById(R.id.tv_speed);
		downloadingHolder.tv_progress = (TextView) view
				.findViewById(R.id.tv_progress);
		downloadingHolder.btn_operation = (Button) view
				.findViewById(R.id.btn_operation);
		downloadingHolder.tv_operation = (TextView) view.findViewById(R.id.tv_operation);
		return downloadingHolder;
	}

	private DownloadedHolder createDownloadedHolder(View view) {
		DownloadedHolder downloadedHolder = new DownloadedHolder();
		downloadedHolder.sliderLayout = (SliderLayout) view.findViewById(com.mst.R.id.slider_view);
		downloadedHolder.cb = (CheckBox) view.findViewById(R.id.checkbox);
		downloadedHolder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
		downloadedHolder.tv_appname = (TextView) view
				.findViewById(R.id.tv_appname);
		downloadedHolder.tv_status = (TextView) view
				.findViewById(R.id.tv_status);
		downloadedHolder.progressBtn = (ProgressBtn) view
				.findViewById(R.id.progressBtn);
		downloadedHolder.tv_operation = (TextView) view.findViewById(R.id.tv_operation);
		downloadedHolder.normal = (RelativeLayout) view
				.findViewById(R.id.normal);
		downloadedHolder.footer = (LinearLayout) view.findViewById(R.id.footer);
		return downloadedHolder;
	}

	private void resetListItemHeight(View convertView) {
		Object convertTag = convertView.getTag();
		if (null == convertTag || !(convertTag instanceof BaseHolder)) {
			return;
		}

		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) convertView
				.getLayoutParams();

		if (null != lp) {
			lp.height = mTotalHeight + 1;
		}
	}

	private void checkSliderLayout(BaseHolder holder) {
		if (editMode) {
			if (holder.sliderLayout != null && holder.sliderLayout.isOpened()) {
				holder.sliderLayout.close(true);
			}
			holder.sliderLayout.setLockDrag(true);
		} else {
			holder.sliderLayout.setLockDrag(false);
		}
	}

	private void checkShowCheckBox(DownloadManagerBean bean, BaseHolder holder) {
		if (bean.getDownloadData() != null) {
			if (editMode) {
				holder.cb.setVisibility(View.VISIBLE);
			} else {
				holder.cb.setVisibility(View.GONE);
			}
		}
	}

	private void checkCheck(DownloadManagerBean bean, BaseHolder holder) {
		if (bean.getDownloadData() != null) {
			if (selectSet.contains(bean.getDownloadData()
					.getTaskId())) {
				holder.cb.setChecked(true);
			} else {
				holder.cb.setChecked(false);
			}
		}
	}
	
	private void checkButtonEnable(DownloadManagerBean bean, BaseHolder holder) {
		if (bean.getDownloadData() != null) {
			if (holder.position == managerActivity.flagPostion) {
				return;
			}
			if (editMode) {
				if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADED) {
					((DownloadedHolder) holder).progressBtn.setEnabled(false);
				} else if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADING) {
					((DownloadingHolder) holder).btn_operation.setEnabled(false);
				}
			} else {
				if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADED) {
					((DownloadedHolder) holder).progressBtn.setEnabled(true);
				} else if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADING) {
					((DownloadingHolder) holder).btn_operation.setEnabled(true);
				}
			}
		}
	}

	private String getStatusString(int status, AppDownloader downloader) {
		String str = "";
		switch (status) {
		case AppDownloader.STATUS_DOWNLOADING:
			if (downloader != null) {
				str = SystemUtil.bytes2kb(downloader.getSpeed()) + "/S";
			}
			break;
		case AppDownloader.STATUS_CONNECTING:
			str = managerActivity.getString(R.string.downloadman_connecting);
			break;
		case AppDownloader.STATUS_NO_NETWORK:
			str = managerActivity.getString(R.string.downloadman_no_network);
			break;
		case AppDownloader.STATUS_WAIT:
			str = managerActivity.getString(R.string.downloadman_wait_download);
			break;
		case AppDownloader.STATUS_PAUSE:
			str = managerActivity.getString(R.string.downloadman_paused);
			break;
		case AppDownloader.STATUS_PAUSE_NEED_CONTINUE:
			str = managerActivity.getString(R.string.downloadman_wait_wifi);
			break;
		case AppDownloader.STATUS_DEFAULT:
			str = managerActivity.getString(R.string.downloadman_paused);
			break;
		}
		return str;
	}

	public int getDownloadingCount() {
		return downloadingCount;
	}

	public void setDownloadingCount(int downloadingCount) {
		this.downloadingCount = downloadingCount;
	}

	public int getDownloadedCount() {
		return downloadedCount;
	}

	public void setDownloadedCount(int downloadedCount) {
		this.downloadedCount = downloadedCount;
	}

	public void setmDeleteFlag(boolean mDeleteFlag) {
		this.mDeleteFlag = mDeleteFlag;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public boolean isNeedAnim() {
		return isNeedAnim;
	}

	public void setNeedAnim(boolean isNeedAnim) {
		this.isNeedAnim = isNeedAnim;
	}

	public Set<String> getSelectSet() {
		return selectSet;
	}

	public void updateListData(ListView listView,
			List<DownloadManagerBean> list) {
		if (listView == null) {
			return;
		}
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		for (int i = 0; i < listView.getChildCount(); i++) {
			int position = i + firstVisiblePosition;
			View view = listView.getChildAt(i);
			int type = getItemViewType(position);
			final DownloadManagerBean bean = list.get(position);
			switch (type) {
			case TYPE_DOWNLOADING:
				DownloadingHolder downloadingHolder = createDownloadingHolder(view);

				downloadingHolder.tv_appname.setText(bean.getDownloadData()
						.getApkName());

				// 开始头像图片异步加载
				imageLoader.displayImage(bean.getDownloadData()
						.getApkLogoPath(), downloadingHolder.iv_icon,
						optionsImage, animateFirstListener);

				downloadingHolder.download_progress_grey.setVisibility(View.GONE);
				downloadingHolder.download_progress.setVisibility(View.VISIBLE);

				downloadingHolder.tv_progress.setText(bean.getDownloadSize()
						+ "/" + bean.getFileSize());

				final int status = bean.getDownloadStatus();
				AppDownloader downloader = AppDownloadService
						.getDownloaders().get(
								bean.getDownloadData().getTaskId());
				// 显示暂停
				if (status == AppDownloader.STATUS_DOWNLOADING
						|| status == AppDownloader.STATUS_CONNECTING
						|| status == AppDownloader.STATUS_NO_NETWORK
						|| status == AppDownloader.STATUS_WAIT) {
					downloadingHolder.download_progress_grey.setVisibility(View.GONE);
					downloadingHolder.download_progress.setVisibility(View.VISIBLE);
					
					downloadingHolder.btn_operation.setText(managerActivity
							.getString(R.string.downloadman_pause));
					
					downloadingHolder.tv_speed.setTextColor(managerActivity
							.getResources().getColor(
									R.color.down_man_item_speed_text));
					downloadingHolder.tv_speed.setText(getStatusString(status,
							downloader));

					downloadingHolder.btn_operation.setBackgroundResource(R.drawable.button_default_selector);
				} else if (status == AppDownloader.STATUS_FAIL) { // 显示重试
					downloadingHolder.download_progress_grey.setVisibility(View.VISIBLE);
					downloadingHolder.download_progress.setVisibility(View.GONE);
					
					downloadingHolder.tv_speed.setText(managerActivity
							.getString(R.string.downloadman_down_fail));
					downloadingHolder.tv_speed.setTextColor(managerActivity
							.getResources().getColor(
									R.color.down_man_item_status_text));
					downloadingHolder.btn_operation.setText(managerActivity
							.getString(R.string.downloadman_retry));

					downloadingHolder.btn_operation.setBackgroundResource(R.drawable.button_default_selector);
				} else { // 显示继续
					downloadingHolder.download_progress_grey.setVisibility(View.VISIBLE);
					downloadingHolder.download_progress.setVisibility(View.GONE);
					
					downloadingHolder.tv_speed.setText(getStatusString(status,
							downloader));
					downloadingHolder.tv_speed.setTextColor(managerActivity
							.getResources().getColor(
									R.color.down_man_item_status_text));
					downloadingHolder.btn_operation.setText(managerActivity
							.getString(R.string.downloadman_continue));

					downloadingHolder.btn_operation.setBackgroundResource(R.drawable.button_default_selector);
				}
				OnClickListener clickListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (status == AppDownloader.STATUS_PAUSE_NEED_CONTINUE) {
							AlertDialog mWifiConDialog = new AlertDialog.Builder(
									managerActivity)
									.setTitle(managerActivity.getResources().getString(R.string.dialog_prompt))
									.setMessage(managerActivity.getResources().getString(
													R.string.downloadman_continue_download_by_mobile))
									.setNegativeButton(android.R.string.cancel, null)
									.setPositiveButton(android.R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													
													AppDownloadService.pauseOrContinueDownload(managerActivity,
															bean.getDownloadData());
												}

											}).create();
							mWifiConDialog.show();
						} else {
							AppDownloadService.pauseOrContinueDownload(managerActivity,
									bean.getDownloadData());
						}
					}
				};
				downloadingHolder.btn_operation
						.setOnClickListener(clickListener);
				break;
			case TYPE_DOWNLOADED:
				DownloadedHolder downloadedHolder = createDownloadedHolder(view);

				if (bean.getDownloadData() == null) {
					downloadedHolder.normal.setVisibility(View.GONE);
					downloadedHolder.footer.setVisibility(View.VISIBLE);
				} else {
					downloadedHolder.normal.setVisibility(View.VISIBLE);
					downloadedHolder.footer.setVisibility(View.GONE);

					downloadedHolder.tv_appname.setText(bean.getDownloadData()
							.getApkName());

					// 开始头像图片异步加载
					imageLoader.displayImage(bean.getDownloadData()
							.getApkLogoPath(), downloadedHolder.iv_icon,
							optionsImage, animateFirstListener);

					progressBtnUtil.updateProgressBtn(
							downloadedHolder.progressBtn,
							bean.getDownloadData());

					// 如果已安装
					InstalledAppInfo installedAppInfo = InstallAppManager
							.getInstalledAppInfo(managerActivity, bean
									.getDownloadData().getPackageName());
					if (installedAppInfo != null
							&& installedAppInfo.getVersionCode() // 显示打开
							>= bean.getDownloadData().getVersionCode()) {
						downloadedHolder.tv_status.setText(managerActivity
								.getString(R.string.downloadman_installed));
					} else { // 显示安装
						if (bean.getDownloadStatus() == AppDownloader.STATUS_INSTALLING) {
							downloadedHolder.tv_status.setText(managerActivity
									.getString(R.string.downloadman_installing));
						} else if (bean.getDownloadStatus() == AppDownloader.STATUS_INSTALLFAILED) {
							downloadedHolder.tv_status.setText(managerActivity
									.getString(R.string.downloadman_install_failed));
						} else {
							downloadedHolder.tv_status.setText(managerActivity
									.getString(R.string.downloadman_no_install));
						}
					}
				}
				break;
			}
		}
	}
	
	//====================加大按钮点击区域start====================//
	
	private void expandViewTouchDelegate(final View view) {
		view.post(new Runnable() {
			@Override
			public void run() {
				Rect bounds = new Rect();
				view.setEnabled(true);
				view.getHitRect(bounds);

		        bounds.top -= 500;
		        bounds.bottom += 500;
		        bounds.left -= 500;
		        bounds.right += 500;

		        TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

		        if (View.class.isInstance(view.getParent())) {
		            ((View) view.getParent()).setTouchDelegate(touchDelegate);
		        }
		        
			}
		});
	}
	
	private void restoreViewTouchDelegate(final View view) {
		view.post(new Runnable() {
            @Override
            public void run() {
            	if (view != null) {
            		Rect bounds = new Rect();
            		bounds.setEmpty();
            		TouchDelegate touchDelegate = new TouchDelegate(bounds, view);
            		
            		if (View.class.isInstance(view.getParent())) {
            			((View) view.getParent()).setTouchDelegate(touchDelegate);
            		}
            	}
            	
            }
        });
    }
		
	//====================加大按钮点击区域end====================//

	public void clearProgressBtnTag(ListView listView) {
		if (listView == null) {
			return;
		}
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		for (int i = 0; i < listView.getChildCount(); i++) {
			int position = i + firstVisiblePosition;
			View view = listView.getChildAt(i);
			int type = getItemViewType(position);
			if (type == TYPE_DOWNLOADED) {
				DownloadedHolder downloadedHolder = createDownloadedHolder(view);
				downloadedHolder.progressBtn.setTag("0");
			}
		}
	}
	
}
