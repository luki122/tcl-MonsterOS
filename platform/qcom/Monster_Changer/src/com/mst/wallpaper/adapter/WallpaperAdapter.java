package com.mst.wallpaper.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mst.wallpaper.BasePresenter;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.WallpaperManager;

import mst.app.dialog.ProgressDialog;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerView.ViewHolder;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemLongClickListener;

public abstract class WallpaperAdapter extends RecyclerView.Adapter<ViewHolder>{

	private Context mContext;
	private ArrayList<Wallpaper> mWallpapers = new ArrayList<Wallpaper>();
	private LayoutInflater mInflater;
	private OnItemClickListener mListener;
	protected HashMap<Integer,Boolean> mMapForSelect = new HashMap<Integer,Boolean>();
	private boolean mDeleteMode = false;
	private ImageResizer mImageResizer;
	private int mAppliedPosition = -1;
	protected int slidePosition = -1;
	private OnSliderDeleteListener mSlideListener;
	
	private WallpaperManager mWallpaperManager;
	
	private ProgressDialog mApplyProgress;
	
	/**
	 *Callback of user click item of recycler view. 
	 *
	 */
	public interface OnItemClickListener{
		
		/**
		 * See{@link AdapterView.OnItemClickListener}
		 * @param view
		 * @param position
		 */
		public void onItemClicked(View view,int position);
		
		public void onItemLongClicked(View view,int position);
	}
	
	public void setOnItemClickListener(OnItemClickListener listener){
		mListener = listener;
	}
	
	public OnItemClickListener getItemClickListener(){
		return mListener;
	}
	
	protected void showProgressDialog(Context context,CharSequence msg){
		mApplyProgress = ProgressDialog.show(context, null, msg, true);
	}
	
	protected void dismissProgress(){
		if(mApplyProgress != null && mApplyProgress.isShowing()){
			mApplyProgress.dismiss();
		}
	}
	
	
	public void setWallpaperManager(WallpaperManager manager){
		this.mWallpaperManager = manager;
	}
	
	public WallpaperManager getWallpaperManager(){
		return mWallpaperManager;
	}

	public ViewHolder getHolder(int position){
		return null;
	}

	public interface OnSliderDeleteListener {
		public void onSlideDeleteSelect(int position);
	}

	public void setSlideDeleteListener(OnSliderDeleteListener listener) {
		mSlideListener = listener;
	}
	
	public OnSliderDeleteListener getSlideListener(){
		return mSlideListener;
	}
	public  WallpaperAdapter(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}
	
	
	public void clearWallPapers(){
		if(mWallpapers.size() > 0){
			for(Wallpaper w : mWallpapers){
				w.clearWallpaper();
				w = null;
			}
			mWallpapers.clear();
		}
		notifyDataSetChanged();
	}
	
	public Boolean isItemSelected(int position){
		return mMapForSelect.get(position);
	}
	public void setImageResizer(ImageResizer resizer){
		mImageResizer = resizer;
	}
	
	public void setAppliedPosition(int position){
		mAppliedPosition = position;
	}
	
	/**
	 * Get all wallpapers that selected to delete
	 * @return
	 */
	public List<Wallpaper> getSelectedItems(){
    	ArrayList<Wallpaper> results = new ArrayList<Wallpaper>();
    	Set<Integer> keys = mMapForSelect.keySet();
    	Iterator< Integer> in = keys.iterator();
		while(in.hasNext()){
			Integer position = in.next();
			Boolean selected = mMapForSelect.get(position);
			if(selected){
				Wallpaper item = (Wallpaper) getWallpaper(position);
				results.add(item);
			}
		}
    	return results;
    }
	
	/**
	 * Selete an wallpaper item or not
	 * @param position
	 */
	public void selectedItemOrNot(int position) {
		// TODO Auto-generated method stub
		Boolean select = mMapForSelect.get(position);
		if(select == null){
			select = false;
		}
		Wallpaper wallpaper = (Wallpaper) getWallpaper(position);
		 boolean editable = wallpaper.systemFlag == Wallpaper.FLAG_SYSTEM;
		 if(editable){
			 mMapForSelect.put(position, false);
		 }else{
			 
			 mMapForSelect.put(position, !select);
		 }
		 notifyItemChanged(position);
	}
	
	
	/**
	 * Select all wallpaper to delete
	 * @param selectAll
	 */
	public void selectAll(boolean selectAll) {
		// TODO Auto-generated method stub
		selectAllOrNot(selectAll);
	}
	


	private void selectAllOrNot(boolean select){
		int size = getItemCount();
		for(int i = 0;i< size;i++){
			Wallpaper wallpaper = (Wallpaper) getWallpaper(i);
			 boolean notEditable = wallpaperEditable(wallpaper);
			 if(notEditable){
				 Log.d("select", "position-->"+i+"  applied-->"+wallpaper.applied+"  systemFlag-->"+wallpaper.systemFlag);
				 mMapForSelect.put(i, false);
			 }else{
				 mMapForSelect.put(i, select);
			 }
		}
		notifyDataSetChanged();
	}
	
	private boolean wallpaperEditable(Wallpaper wallpaper){
		boolean editable = wallpaper.systemFlag == Wallpaper.FLAG_SYSTEM || wallpaper.applied;
		if(wallpaper.type == Wallpaper.TYPE_DESKTOP){
			return  editable;
		}
		
		return editable ||wallpaper.id == getKeyguardWallpaperAppliedId();
	}
	
	protected int getKeyguardWallpaperAppliedId(){
		return getWallpaperManager().getAppliedKeyguardWallpaperId(getContext());
	}
	
	
	/**
	 * Has wallpaper selected to delete or not
	 * @return
	 */
	public boolean hasSelectedData(){
		int size = mMapForSelect.size();
		if(size < 1){
			return false;
		}
		Set<Integer> keys = mMapForSelect.keySet();
		Iterator<Integer> in = keys.iterator();
		while(in.hasNext()){
			int key = in.next();
			if(mMapForSelect.get(key)){
				return true;
			}
		}
		return false;
	}
	
	
	
	public int getAppliedPosition(){
		
		return mAppliedPosition;
	}
	
	public void addWallPaper(Wallpaper w){
		addWallPaperNotRefresh(w);
		notifyItemInserted(mWallpapers.indexOf(w));
	}
	
	public void addWallPaperNotRefresh(Wallpaper w){
		synchronized (mWallpapers) {
			if(!mWallpapers.contains(w)){
				mWallpapers.add(w);
			}
		}
		
	}
	
	
	
	public void removeWallPaper(Wallpaper w){
		if(mWallpapers.contains(w)){
			int position = mWallpapers.indexOf(w);
			mWallpapers.remove(w);
			mMapForSelect.remove(position);
			notifyItemRemoved(position);
			notifyDataSetChanged();
		}
		
	}
	
	protected Context getContext(){
		return mContext;
	}
	
	protected LayoutInflater getInflater(){
		return mInflater;
	}
	
	@Override
	public  int getItemCount() {
		// TODO Auto-generated method stub
			return mWallpapers.size();
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return mWallpapers.get(position).id;
	}
	

	public Wallpaper getWallpaper(int position) {
		// TODO Auto-generated method stub
		return mWallpapers.get(position);
	}
	
	public ArrayList<Wallpaper> getWallpapers(){
		return mWallpapers;
	}
	
	public ImageResizer getImageResizer(){
		return mImageResizer;
	}
	
	
	public boolean getDeleteMode() {
		// TODO Auto-generated method stub
		return mDeleteMode;
	}

	
	public void enterDeleteMode(boolean enter){
		mDeleteMode = enter;
		if(!mDeleteMode){
			mMapForSelect.clear();
		}
		notifyDataSetChanged();
	}
	
	public static abstract class RecylerHolder extends ViewHolder{

		public View itemView;
		
		private  WeakReference<WallpaperAdapter> mAdapter;
		
		public RecylerHolder(View itemView,WallpaperAdapter adapter) {
			super(itemView);
			this.itemView = itemView;
			mAdapter = new WeakReference<WallpaperAdapter>(adapter);
		}
		
		protected abstract void bindItemData(View itemView,int position) ;
		
		public WallpaperAdapter getAdapter(){
			return mAdapter.get();
		}
		
		
	}




	

}
