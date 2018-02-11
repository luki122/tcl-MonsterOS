package com.mst.wallpaper.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mst.wallpaper.activity.WallpaperPreviewActivity;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.presenter.DatabasePresenter;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract;
import com.mst.wallpaper.presenter.KeyguardWallpaperDbPresenterImpl;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract.Presenter;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.IntentUtils;
import com.mst.wallpaper.utils.ToastUtils;
import com.mst.wallpaper.utils.WallpaperManager;

import mst.app.dialog.ProgressDialog;
import mst.widget.SliderLayout;
import mst.widget.SliderLayout.SwipeListener;
import mst.widget.recycleview.RecyclerView.ViewHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.mst.wallpaper.MainWorker.OnRequestListener;
import com.mst.wallpaper.R;

public class KeyguardWallpaperListAdapter extends WallpaperAdapter implements WallpaperDatabaseContract.View
,OnRequestListener<Integer, Wallpaper>{

	private static final String LISTENER_NAME = KeyguardWallpaperListAdapter.class.getName();
	private static final int MSG_UPDATE_CURRENT_WALLPAPER_APPLIED_STATUS = 100;
	private static final int MSG_UPDATE_WALLPAPER_FROM_PICKER = 101;
	private static final int RANGE_SET_RIGHT_IMAGE = 2;
	
	private static final int OFFSET_BOTTOM_IMAGE = 2;
	
	private static final int MAX_BOTTOM_IMAGES = 3;
	
	private static final String TAG = "KeyguardWallpaperListAdapter";
	private DatabasePresenter<Wallpaper> mPresenter;
	
	private List<Wallpaper> mWallpaper;
	
	private HashMap<Integer,ViewHolder> mHolderMap = new HashMap<Integer, ViewHolder>();
	private Handler mHandler = new Handler(){
	public void handleMessage(android.os.Message msg) {
		if(msg.what == MSG_UPDATE_CURRENT_WALLPAPER_APPLIED_STATUS){
			try{
			int appliedId = (int) msg.obj;
			dismissProgress();
			setAppliedPosition(appliedId);
			notifyDataSetChanged();
			}catch (Exception e) {
				// do nothing
			}
			
		}else if(msg.what == MSG_UPDATE_WALLPAPER_FROM_PICKER){
			Wallpaper wallpaper = (Wallpaper) msg.obj;
			if(wallpaper != null){
				addWallPaperNotRefresh(wallpaper);
				notifyDataSetChanged();
			}
			
		}
	}
	};
	
	
	public KeyguardWallpaperListAdapter(Context context,WallpaperManager wallpaperManager) {
		super(context);
		// TODO Auto-generated constructor stub
		mPresenter = new KeyguardWallpaperDbPresenterImpl(this,context);
		setWallpaperManager(wallpaperManager);
		mHolderMap.clear();
	}
	
	
	
	private DatabasePresenter<Wallpaper> getPresenter(){
		return mPresenter;
	}
	

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// TODO Auto-generated method stub
		Holder holder = (Holder)viewHolder;
		holder.bindItemData(holder.itemView, position);
		mHolderMap.put(position, holder);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int itemViewType) {
		// TODO Auto-generated method stub
		int layoutId;
			layoutId = R.layout.keyguard_system_wallpaper_list_item;
		View itemView = getInflater().inflate(layoutId, null);
		
		return new Holder(itemView, this, itemViewType);
	}


	public void addWallpaeprManagerListener() {
		// TODO Auto-generated method stub
		getWallpaperManager().addWallpaperHandleListener(LISTENER_NAME, this);
	}

	
	
	@Override
	public void updateView(Wallpaper wallpaper) {
		// TODO Auto-generated method stub
		if(Config.DEBUG)
		Log.d(TAG, "updateView:"+wallpaper.toString());
		wallpaper.type = Wallpaper.TYPE_KEYGUARD;
		Message msg = new Message();
		msg.what = MSG_UPDATE_WALLPAPER_FROM_PICKER;
		msg.obj = wallpaper;
		mHandler.sendMessage(msg);
		
		
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onSuccess(Wallpaper wallpaper, Integer id,
			int statusCode) {
		// TODO Auto-generated method stub
			Message msg = new Message();
			msg.what = MSG_UPDATE_CURRENT_WALLPAPER_APPLIED_STATUS;
			msg.obj = id;
			mHandler.sendMessage(msg);
	}



	@Override
	public void onStartRequest(Wallpaper wallpaper, int statusCode) {
		// TODO Auto-generated method stub
		
	}
	
	public void onResume(boolean updateFromPicker) {
		// TODO Auto-generated method stub
		mPresenter.queryAll();
	}


	public void onDestory(){
		getWallpaperManager().removeWallpaperHandleListener(LISTENER_NAME, this);
	}
	
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		Wallpaper w = getWallpaper(position);
		if(w.systemFlag == Wallpaper.FLAG_SYSTEM ){
			return Wallpaper.FLAG_SYSTEM;
		}else if(w.id == getWallpaperManager().getAppliedKeyguardWallpaperId(getContext())){
			return Wallpaper.FLAG_APPLIED;
		}else{
			return Wallpaper.FLAG_CUSTOM;
		}
	}
	
	@Override
	public ViewHolder getHolder(int position) {
		// TODO Auto-generated method stub
		return mHolderMap.get(position);
	}

	@Override
	public void updateView(List<Wallpaper> wallpaperList) {
		// TODO Auto-generated method stub
		if(wallpaperList != null){
			if(Config.DEBUG){
				Log.d(TAG, "Query all keyguard wallpaper-->"+wallpaperList.size());
			}
			mWallpaper = wallpaperList;
			clearWallPapers();
			int appliedId = getWallpaperManager().getAppliedKeyguardWallpaperId(getContext());
			if(mWallpaper != null){
				for(Wallpaper w: mWallpaper){
					w.type = Wallpaper.TYPE_KEYGUARD;
					addWallPaperNotRefresh(w);
				}
			}
			setAppliedPosition(appliedId);
			notifyDataSetChanged();
		}
	}
	
	
	
	private static final class Holder extends RecylerHolder implements View.OnClickListener
	,SwipeListener,OnLongClickListener{

		public HashMap<Integer,Integer> mViewMapedImagePosition = new HashMap<Integer,Integer>();
		public ImageResizer mImageResizer;
		public LinearLayout.LayoutParams mBottomLayoutParams;
		public ImageView leftImage;
		public ImageView topRightImageView;
		public LinearLayout bottomLayout;
		public TextView titleView;
		public TextView countView;
		public Button mDeleteButton;
		public CheckBox mDeleteCheck;
		public CheckBox mApplyCheck;
//		public SliderLayout mSlider;
		public View mImagesParent;
		public int mCurrentPosition;
		private final View itemView;
		private final View mTitleParent;
		private boolean mHandleLongClicked = true;
		private int mSingleImageWidth;
		private int mSingleImageHeight;
		public Holder(View itemView, WallpaperAdapter adapter,int itemViewType) {
			super(itemView, adapter);
			// TODO Auto-generated constructor stub
			this.itemView = itemView;
			mImageResizer = getAdapter().getImageResizer();
			mBottomLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			mBottomLayoutParams.weight = 1;
			mBottomLayoutParams.leftMargin = itemView.getResources().getDimensionPixelSize(R.dimen.keyguard_list_image_space);
			 leftImage = (ImageView)itemView.findViewById(R.id.keyguard_list_item_left_image);
			 topRightImageView = (ImageView)itemView.findViewById(R.id.keyguard_list_item_top_right_image);
			 bottomLayout = (LinearLayout)itemView.findViewById(R.id.keyguard_list_item_bottom_right_parent);
			 mDeleteCheck = (CheckBox)itemView.findViewById(R.id.keyguard_list_item_delete_check);
			 mApplyCheck = (CheckBox)itemView.findViewById(R.id.keyguard_list_item_check);
			 titleView = (TextView)itemView.findViewById(R.id.keyguard_wallpaper_list_item_title);
			 countView = (TextView)itemView.findViewById(R.id.keyguard_wallpaper_list_item_image_count);
			 mTitleParent = itemView.findViewById(R.id.title_parent);
			 mDeleteButton = (Button)itemView.findViewById(R.id.keyguard_wallpaper_item_delete_btn);
			 if(mSingleImageWidth == 0){
				 mSingleImageWidth = itemView.getResources().getDimensionPixelSize(R.dimen.keyguard_preview_first_image_width);
			 }
			 if(mSingleImageHeight == 0){
				 mSingleImageHeight = itemView.getResources().getDimensionPixelSize(R.dimen.keyguard_list_image_height);
			 }
			 mImagesParent = itemView.findViewById(R.id.keyguard_list_image_parent);
			 mImagesParent.setOnClickListener(this);
			 itemView.setOnLongClickListener(this);
			 itemView.setOnClickListener(this);
			 if(itemViewType == Wallpaper.FLAG_CUSTOM){
				 mTitleParent.setOnClickListener(this);
			 }
			 mImagesParent.setOnLongClickListener(this);
			 leftImage.setOnClickListener(this);
			 leftImage.setOnLongClickListener(this);
			 topRightImageView.setOnClickListener(this);
			 topRightImageView.setOnLongClickListener(this);
			 mViewMapedImagePosition.clear();
			 
			 if(mDeleteButton != null){
				 mDeleteButton.setOnClickListener(this);
			 }
		}

		
		
		@Override
		protected void bindItemData(View itemView, int position) {
			// TODO Auto-generated method stub
			Wallpaper wallpaper = getAdapter().getWallpaper(position);
			mCurrentPosition = position;
			visibleCheckBox();
			if(wallpaper != null){
				int imageCount = wallpaper.getWallpaperCount();
				if(imageCount > 0){
					mImageResizer.loadImage(wallpaper.getObjectByKey(0), leftImage);
					mViewMapedImagePosition.put(leftImage.getId(),0);
					//Setup title information
					updateTitle(itemView, wallpaper, imageCount);
					if(imageCount > 1){
						mImageResizer.loadImage(wallpaper.getObjectByKey(1), topRightImageView);
						mViewMapedImagePosition.put(topRightImageView.getId(),1);
					}
					if(imageCount == 1){
						LinearLayout.LayoutParams leftImageParams = (LayoutParams) topRightImageView.getLayoutParams();
						leftImageParams.width = mSingleImageWidth;
						topRightImageView.setLayoutParams(leftImageParams);
						topRightImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					}
					if(imageCount == 2){
						LinearLayout.LayoutParams p = (LayoutParams) topRightImageView.getLayoutParams();
						p.height = mSingleImageHeight;
						topRightImageView.setLayoutParams(p);
						bottomLayout.setVisibility(View.GONE);
					}
					if(imageCount  > RANGE_SET_RIGHT_IMAGE){
						int leftCount = imageCount - RANGE_SET_RIGHT_IMAGE;
						if(leftCount > MAX_BOTTOM_IMAGES){
							leftCount = MAX_BOTTOM_IMAGES;
						}
						
						bottomLayout.removeAllViews();
						/*
						 * 设置右下角的图片，根据图片适量显示相应的图片，但是最多显示
						 * 三张。在这个过程中，由于第一张和第二张已经显示了，所以在显示
						 * 的时候从图片列表的第三张开始显示，到第五张结束.
						 */
						for(int i = 0 ;i< leftCount;i++){
							updateBottomImages(i,bottomLayout, wallpaper.getObjectByKey(i+OFFSET_BOTTOM_IMAGE),leftCount);
					  }
					}
				}
				Boolean seletedToDelete = getAdapter().mMapForSelect.get(position);
				mDeleteCheck.setChecked((seletedToDelete==null)?false:seletedToDelete);
				if(getAdapter().getAppliedPosition() == -1){
					if(wallpaper.isDefaultTheme == 1){
						mApplyCheck.setChecked(true);
					}
				}
			}
		}
		
		
		private void updateBottomImages(int position,LinearLayout bottomLayout,Object imagePath,int count){
			ImageView bottomImageView = new ImageView(bottomLayout.getContext());
			bottomImageView.setId(position);
			bottomImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			bottomImageView.setOnClickListener(this);
			bottomImageView.setOnLongClickListener(this);
			mImageResizer.loadImage(imagePath, bottomImageView);
			mViewMapedImagePosition.put(bottomImageView.getId(),position+OFFSET_BOTTOM_IMAGE);
			bottomLayout.addView(bottomImageView, mBottomLayoutParams);
		}
		
		private void updateTitle(View parentView,Wallpaper wallpaper,int count){
			
			titleView.setText(wallpaper.name+"");
			countView.setText(countView.getResources().getString(R.string.keyguard_wallpaper_count, count));
			
		}

		private void visibleCheckBox(){
			boolean applied = getAdapter().getWallpaper(mCurrentPosition).id == getAdapter().getAppliedPosition();
			if(getAdapter().getDeleteMode()){
				mDeleteCheck.setVisibility(View.VISIBLE);
				mApplyCheck.setVisibility(View.INVISIBLE);
			}else{
				mApplyCheck.setVisibility(View.VISIBLE);
				mDeleteCheck.setVisibility(View.INVISIBLE);
			}
			
			if(Config.DEBUG)
			Log.d(TAG, "appliedPositon:"+getAdapter().getAppliedPosition()+" currentPosition:"+mCurrentPosition
					+" \n"+"  currentId:"+getAdapter().getWallpaper(mCurrentPosition).id+"  appliedId:"+getAdapter().getAppliedPosition()
					+" checked:"+(getAdapter().getWallpaper(mCurrentPosition).id == getAdapter().getAppliedPosition()));
			mApplyCheck.setChecked(applied);
			
			mApplyCheck.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			final int position =  mCurrentPosition;
			
			
			if(v == mDeleteButton){
				getAdapter().slidePosition = -1;
				if(getAdapter().getSlideListener() != null){
					getAdapter().getSlideListener().onSlideDeleteSelect(position);
				}
			}else if(v == mApplyCheck){
				getAdapter().dismissProgress();
				if(getAdapter().getAppliedPosition() == getAdapter().getWallpaper(mCurrentPosition).id){
					ToastUtils.showShortToast(getAdapter().getContext(), 
							getAdapter().getContext().getResources().getString(R.string.tip_current_wallpaper_is_applied));
					mApplyCheck.setChecked(true);
					return;
				}
				getAdapter().showProgressDialog(v.getContext(), v.getResources().getString(R.string.msg_apply_keyguard_wallpaper));
				getAdapter().getWallpaperManager().applyWallpaper(getAdapter().getWallpaper(mCurrentPosition),
						null, LISTENER_NAME, mCurrentPosition);
				
			
			}else{
				if(getAdapter().getDeleteMode()){
					if(getAdapter().getItemClickListener() != null){
						getAdapter().getItemClickListener().onItemClicked(this.itemView, mCurrentPosition);
					}
				}else{
					startPreview(position,mViewMapedImagePosition.get(v.getId()));
				}
				
			}
			
		}
		
		private void startPreview(int position,Integer imagePosition){
			Intent intent = IntentUtils.buildWallpaperPreviewIntent(position, Wallpaper.TYPE_KEYGUARD,getAdapter().getWallpapers().get(position));
			if(imagePosition != null){
				intent.putExtra(Config.Action.KEY_KEYGUARD_WALLPAPER_PREVIEW_IAMGE_POSITION, imagePosition);
				intent.putExtra(Config.Action.KEY_KEYGUARD_WALLPAPER_PREVIEW_POSITION_IN_LIST, position);
			}
	    	intent.setClass(getAdapter().getContext(),WallpaperPreviewActivity.class);
	    	getAdapter().getContext().startActivity(intent);
		}



		@Override
		public void onClosed(SliderLayout arg0) {
			// TODO Auto-generated method stub
			mHandleLongClicked = true;
		}



		@Override
		public void onOpened(SliderLayout arg0) {
			// TODO Auto-generated method stub
			int oldPosition = getAdapter().slidePosition;
			getAdapter().slidePosition = mCurrentPosition;
			getAdapter().notifyItemChanged(mCurrentPosition);
			Holder holder = (Holder) getAdapter().getHolder(oldPosition);
//			if(holder.mSlider != null){
//				holder.mSlider.close(true);
//			}
			mHandleLongClicked = false;
		}



		@Override
		public void onSlide(SliderLayout slider, float offset) {
			// TODO Auto-generated method stub
			mHandleLongClicked = false;
		}



		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			if(getAdapter().getItemClickListener() != null && mHandleLongClicked){
				getAdapter().getItemClickListener().onItemLongClicked(this.itemView, mCurrentPosition);
			}
			return true;
		}
		
		
		
	}







	
}
