package com.android.systemui.recents.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;  
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;  
import android.view.MotionEvent;
import android.view.View;  
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;  
import android.widget.TextView; 

import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerView.OnScrollListener;
  
public class MstTaskAdapter extends  RecyclerView.Adapter<MstTaskAdapter.ViewHolder>  {  
	
    private LayoutInflater mInflater;  
    private ArrayList<Task> mMstTaskList = null;
    private Context mContext;
    private SystemServicesProxy mSsp;
    private ArrayList<String> mSplitScreenPackageList;
    
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mTouchExplorationEnabled;
    // The current display bounds
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mDisplayRect = new Rect();
    // The current display orientation
    @ViewDebug.ExportedProperty(category="recents")
    private int mDisplayOrientation = Configuration.ORIENTATION_UNDEFINED;
    
    private static final int RECENTS_ITEM = 1;
    private static final int PADDINGFIRST_ITEM = 2;

    public interface OnItemListener  {  
        void onItemClick(View view, int position); 
        void onItemDismiss(View view, int position); 
        void onAllItemDismiss(); 
        boolean onItemTouchListener(View view, int position, MotionEvent event);
        void onItemSplitScreenImageClick(int position);
    }
    private OnItemListener mOnItemLitener;  
    public void setOnItemListener(OnItemListener onItemLitener)  {  
        this.mOnItemLitener = onItemLitener;  
    }
  
    public MstTaskAdapter(Context context, ArrayList<Task> taskList)  {  
    	mContext = context;
        mInflater = LayoutInflater.from(context);  
        mMstTaskList = taskList;
        mSsp = Recents.getSystemServices();
    }  
  
    public static class ViewHolder extends RecyclerView.ViewHolder  {  
        public ViewHolder(View arg0, int viewType)  {  
            super(arg0); 
            if(viewType == RECENTS_ITEM) {
            	mMstTaskView = (MstTaskView) arg0;
            	mSplitScreenView = (ImageView)mMstTaskView.findViewById(R.id.splitscreen_image);
            }
        }
        MstTaskView mMstTaskView; 
        ImageView mSplitScreenView;
    }  
    
    @Override  
    public int getItemCount()  {
        return mMstTaskList.size() + 1;  
    } 
    
    @Override
    public int getItemViewType(int position) {
    	// TODO Auto-generated method stub
    	if(position == 0) {
    		return PADDINGFIRST_ITEM;
    	} else {
    		return RECENTS_ITEM;
    	}
    }
  
    @Override  
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)  {
    	//Log.d("181818", "MstTaskAdapter--- onCreateViewHolder");
    	View view;
    	if(viewType == RECENTS_ITEM) {
    		view = mInflater.inflate(R.layout.recylerview_task_item, viewGroup, false);
    	} else {
    		view = mInflater.inflate(R.layout.recylerview_paddingfirst_item, viewGroup, false);
    	}
        ViewHolder viewHolder = new ViewHolder(view, viewType);
        
        return viewHolder;  
    }  

    @Override  
    public void onBindViewHolder(final ViewHolder viewHolder, final int position)  {
    	mTouchExplorationEnabled = mSsp.isTouchExplorationEnabled();
    	mDisplayOrientation = Utilities.getAppConfiguration(mContext).orientation;
    	mDisplayRect = mSsp.getDisplayRect();
    	if(position == 0) {
    		return;
    	}
    	final int index = position - 1;
    	final Task task = mMstTaskList.get(index);
    	//Log.d("181818", "MstTaskAdapter--- position = " + position + "，onBindViewHolder task.thumbnail = " + task.thumbnail);
    	//Log.d("181818", "MstTaskAdapter--- position = " + position + "，onBindViewHolder task.title = " + task.title);
    	viewHolder.mMstTaskView.onTaskBound(task, mTouchExplorationEnabled, mDisplayOrientation, mDisplayRect);
    	// Load the task data
        Recents.getTaskLoader().loadTaskData(task, mContext);
        if(index == mMstTaskList.size() - 1) {
        	viewHolder.itemView.setPadding(mContext.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft), viewHolder.itemView.getPaddingTop(), 
        			mContext.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_last_paddingright), viewHolder.itemView.getPaddingBottom());
        } else {
        	viewHolder.itemView.setPadding(mContext.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft), viewHolder.itemView.getPaddingTop(), 
        			mContext.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingright), viewHolder.itemView.getPaddingBottom());
        }
        
        //如果设置了回调，则设置点击事件  
        if (mOnItemLitener != null)  {  
            viewHolder.itemView.setOnClickListener(new OnClickListener()  { 
                @Override  
                public void onClick(View v)  {
                	mOnItemLitener.onItemClick(viewHolder.itemView, index); 
                }  
            });
            viewHolder.itemView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					return mOnItemLitener.onItemTouchListener(viewHolder.itemView, index, event);
				}
			});
            //Log.d("181818", "MstTaskAdapter--- viewHolder.mSplitScreenView = " + viewHolder.mSplitScreenView);
            //TODO why be null when splitscreen tangjun 2016.11.15
            if(viewHolder.mSplitScreenView != null) {
            	if(mSplitScreenPackageList != null && mSplitScreenPackageList.contains(task.key.baseIntent.getComponent().getPackageName())) {
            		viewHolder.mSplitScreenView.setVisibility(View.VISIBLE);
		            viewHolder.mSplitScreenView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							mOnItemLitener.onItemSplitScreenImageClick(index);
						}
					});
            	} else {
            		viewHolder.mSplitScreenView.setVisibility(View.GONE);
            	}
            }
        }  
    }
    
    public void setTaskList(ArrayList<Task> taskList) {
    	mMstTaskList = taskList;
    }
    
    public void setSplitScreenPackageNames(ArrayList<String> splitScreenPackageList) {
    	mSplitScreenPackageList = splitScreenPackageList;
    }
}  
