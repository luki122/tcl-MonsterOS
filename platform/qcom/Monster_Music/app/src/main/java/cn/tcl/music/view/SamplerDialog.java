/* ----------|----------------------|---------------------|-------------------*/
/* 13/05/2015|zhongrui.guo1         |PR996296             |[Force Close] Popup Mix force close when click "Record your own sample    */
/* ----------|----------------------|---------------------|-------------------*/
package cn.tcl.music.view;

import android.app.Fragment;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import cn.tcl.music.R;

public class SamplerDialog extends Fragment {

	private WeakReference<View> mAnchor = new WeakReference<View>(null);
	
	private int mXOffset;
	private int mYOffset;
	
	private int mWidth;
	private int mHeight;
	private Drawable mContentBackground;
	private int mBackgroundColor;
	private DialogInterface.OnDismissListener mOnDismissListener;
	
	private View mCloseBtn;
	
	private Button mRecordBtn = null;
	private ImageButton mRecordImageBtn = null;
	private ImageView mRemovePadImageView = null;
	
	private int mGravity = Gravity.BOTTOM | Gravity.START;
	
	private boolean mAdvancedFeatures = false;
	private int mUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
	
	private boolean mIsRecording = false;//[bugfix] -Add by TCTNJ-zhongrui.guo1,PR996296,2015-05-13
	//private boolean mLandscape = false;

	private class OnDragEventListener implements OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			
			switch(event.getAction())
			{
				case DragEvent.ACTION_DRAG_STARTED:
				{
					return true;
				}
				case DragEvent.ACTION_DRAG_ENTERED:
				{
					return true;
				}
				case DragEvent.ACTION_DRAG_EXITED:
				{
					return true;
				}
				case DragEvent.ACTION_DRAG_LOCATION:
				{
					return true;
				}
				case DragEvent.ACTION_DROP:
				{
					ClipData data = event.getClipData();
					Intent dataFromPad = data.getItemAt(0).getIntent();

					
					String bankName = dataFromPad.getStringExtra("bankName");
					int padIdxDragged = dataFromPad.getIntExtra("padIdx", -1);
					
//					if (!TextUtils.isEmpty(bankName) && padIdxDragged >= 0)
//						MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).deleteSampleAndUnload(0, padIdxDragged, bankName);
					
					return true;
				}
			}
			
			return true;
		}
		
	}
	
//	private DjMixSamplesManager.OnSampleRecordingListener mOnSampleRecordingListener = new DjMixSamplesManager.OnSampleRecordingListener() {
//
//		@Override
//		public void recordHasStarted(int padIndex) {
//			mRecordBtn.setVisibility(View.GONE);
//			mRecordImageBtn.setVisibility(View.VISIBLE);
//			mRemovePadImageView.setVisibility(View.GONE);
//			mIsRecording = true;//[bugfix] -Add by TCTNJ-zhongrui.guo1,PR996296,2015-05-13
//
//			Drawable d = mRecordImageBtn.getDrawable();
//			if (d instanceof AnimationDrawable)
//				((AnimationDrawable) d).start();
//		}
//
//		@Override
//		public void recordHasFinished(String name, int padIndex) {
//			mRemovePadImageView.setVisibility(View.GONE);
//			mRecordBtn.setVisibility(View.VISIBLE);
//			mRecordImageBtn.setVisibility(View.GONE);
//			mIsRecording = false;//[bugfix] -Add by TCTNJ-zhongrui.guo1,PR996296,2015-05-13
//			Drawable d = mRecordImageBtn.getDrawable();
//			if (d instanceof AnimationDrawable)
//				((AnimationDrawable) d).stop();
//		}
//
//		@Override
//		public void recordHasBeenCancelled(String name, int padIndex) {
//			mRemovePadImageView.setVisibility(View.GONE);
//			mRecordBtn.setVisibility(View.VISIBLE);
//			mRecordImageBtn.setVisibility(View.GONE);
//			mIsRecording = false;//[bugfix] -Add by TCTNJ-zhongrui.guo1,PR996296,2015-05-13
//			Drawable d = mRecordImageBtn.getDrawable();
//			if (d instanceof AnimationDrawable)
//				((AnimationDrawable) d).stop();
//		}
//
//	};
	
//	private SamplerFragment.OnEditModeListener mOnEditModeListener = new SamplerFragment.OnEditModeListener() {
//
//		@Override
//		public void onEditMode(boolean editModeEnabled) {
//			if (editModeEnabled)
//			{
//				mRemovePadImageView.setVisibility(View.GONE);
//				mRecordBtn.setVisibility(View.GONE);
//				mRecordImageBtn.setVisibility(View.GONE);
//			}
//			else
//			{
//				if(!mIsRecording){//[bugfix] -Add judgement by TCTNJ-zhongrui.guo1,PR996296,2015-05-13
//					mRemovePadImageView.setVisibility(View.GONE);
//					mRecordBtn.setVisibility(View.VISIBLE);
//					mRecordImageBtn.setVisibility(View.GONE);
//				}
//			}
//
//		}
//	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.layout_sampler_oneshot, container,false);
//        if (MixSession.getInstance() == null)
//            return rootView;
//		final List<Pair<String,Boolean>> banks = MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).getBankList();
//		mAdvancedFeatures = rootView.findViewById(R.id.advancedSamplerControl) != null;     //原横屏时的sampler样式
//		int numBanks = mAdvancedFeatures ? banks.size() : 3;
//		final SamplerPagerAdapter samplerAdapter = new SamplerPagerAdapter(getChildFragmentManager(), numBanks, 0,
//																			mAdvancedFeatures ? SamplerPagerAdapter.DEFAULT_BANKS_PER_PAGE : 1,
//																		    mAdvancedFeatures);
//		MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).registerOnSampleRecordingListener(mOnSampleRecordingListener);
//		samplerAdapter.setOnEditModeListener(mOnEditModeListener);
//
//		if (mContentBackground != null)
//			rootView.setBackground(mContentBackground);
//		else
//			rootView.setBackgroundColor(mBackgroundColor);
//
//		//manageWindowParams(getDialog().getWindow());
//
//		// It should not happen. The only case here is when Sample banks are manually deleted, or not fully loaded
//		if (banks.size() < numBanks)
//			return rootView;
//
//		if (!mAdvancedFeatures)  //原竖屏时的sampler样式
//		{
//			// TCT: should check ArrayIndexOutOfBondsException
//			final int length = banks.size() > 3 ? 3 : banks.size();
//			for (int i = 0; i < /*3*/length; i++)
//			{
//				samplerAdapter.addTitleFor(MusicUtils.getBankname(getActivity(), banks.get(i).first), i); //Add by TCTNJ,xiaoyan.xu, 2015-03-31,PR937048
//			}
//			final CrossViewPager crossViewPager = (CrossViewPager) rootView.findViewById(R.id.cross_sampler_view_pager);
//			crossViewPager.setAdapter(samplerAdapter);
//			PagerSlidingTabStrip pageIndicator = (PagerSlidingTabStrip) rootView.findViewById(R.id.page_sliding_tab_strip);
//			pageIndicator.setViewPager(crossViewPager);
//
//			return rootView;
//		}
//
//		CirclePageIndicator pageIndicator = (CirclePageIndicator) rootView.findViewById(R.id.page_indicator);
//		final ViewPager pager = (ViewPager) rootView.findViewById(R.id.sampler_view_pager);
//		pager.setAdapter(samplerAdapter);
//
//		pageIndicator.setViewPager(pager);
//
//		mRecordImageBtn = (ImageButton) rootView.findViewById(R.id.record_sample_image_btn);
//		mRemovePadImageView = (ImageView) rootView.findViewById(R.id.pad_remove_image_view);
//		mRemovePadImageView.setOnDragListener(new OnDragEventListener());
//
//		mRecordBtn = (Button) rootView.findViewById(R.id.record_sample_btn);
//		mRecordBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				int recordBankPageOffset = banks.size() - 1;
//				if (recordBankPageOffset < 0)
//					return;
//				Pair<String,Boolean> bank = banks.get(recordBankPageOffset);
//				if (bank.second)
//					return;
//
//				pager.setCurrentItem(samplerAdapter.getCount() - 1);
//				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//
//				int userBankPadOffset = sharedPrefs.getInt(SamplerFragment.USER_BANK_PAD_OFFSET_SETTINGS, 0);
//
//				int firstEmptyPadIdx = sharedPrefs.getInt(SamplerFragment.FIRST_EMPTY_PAD_SETTINGS, userBankPadOffset);
//                // When samples bar is full, user press record your own sample.
//                // There is a popup to warn user:
//                if (4 == firstEmptyPadIdx) {
//                    new AlertDialog.Builder(getActivity())
//                    .setMessage(R.string.sample_is_full)
//                    .setPositiveButton(R.string.ok, null)
//                    .create()
//                    .show();
//                    return;
//                }
//				if (firstEmptyPadIdx >= 4 + userBankPadOffset)
//					firstEmptyPadIdx = userBankPadOffset;
//				MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).deleteSampleAndUnload(0, firstEmptyPadIdx, SamplerFragment.USER_BANK_NAME);
//				MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).initializeRecordProcess();
//				MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).startRecord(SamplerFragment.USER_BANK_NAME, DjMixSampler.SAMPLER_A, firstEmptyPadIdx, getActivity());
//			}
//		});
//
//		mRecordImageBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				//Add by TCTNJ,xiaoyan.xu, 2015-03-31,PR962686 Begin
//				MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).stopRecordAndLoad(getActivity(), SamplerFragment.USER_BANK_NAME, DjMixSampler.SAMPLER_A);
//				//Add by TCTNJ,xiaoyan.xu, 2015-03-31,PR962686 End
//				MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).releaseRecordProcess();
//			}
//		});
		

		return rootView;
		
	}
	
    //[bugfix] -add by TCTNJ-zhongrui.guo1,PR1043824,2015-07-24 begin
    @Override
    public void onStop() {
        super.onStop();
    }
    //[bugfix] -add by TCTNJ-zhongrui.guo1,PR1043824,2015-07-24 end
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
//	@Override
//	public void onDismiss(DialogInterface dialog) {
//		super.onDismiss(dialog);
//
//		MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).cancelRecordAndRemoveFile(SamplerFragment.USER_BANK_NAME, DjMixSampler.SAMPLER_A);
//		MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).releaseRecordProcess();
//		MixSession.getInstance().mediaLoader().cancelTasksForSampler();
//		MixSession.getInstance().samplesManager(DjMixSampler.SAMPLER_A).unloadBank(0, 4, mAdvancedFeatures ? SamplerPagerAdapter.DEFAULT_BANKS_PER_PAGE : 1);
//		
//		if (mOnDismissListener != null)
//			mOnDismissListener.onDismiss(dialog);
//	}
	

	private void manageWindowParams(Window window)
	{
		WindowManager.LayoutParams lp = window.getAttributes();
		{
			lp.y = window.getDecorView().getPaddingBottom() + mYOffset;
			lp.gravity = mGravity;
		}
		lp.x = mXOffset;
		lp.format = PixelFormat.TRANSLUCENT;
		lp.width = mWidth;
		lp.height= mHeight;
		lp.horizontalMargin = 0;
		//lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		//lp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
		
	}
	
	public void setContentViewBackground(Drawable background, int backgroundColor)
	{
		mContentBackground = background;
		mBackgroundColor = backgroundColor;
	}

}
