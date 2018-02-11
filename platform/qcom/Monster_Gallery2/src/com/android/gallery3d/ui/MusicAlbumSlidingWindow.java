package com.android.gallery3d.ui;

import android.graphics.Bitmap;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.Texture;
import com.android.gallery3d.glrenderer.TiledTexture;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.JobLimiter;


public class MusicAlbumSlidingWindow {
	
    public static interface Listener {
        public void onSizeChanged(int size);
        public void onContentChanged();
    }
	
	public static class MusicAlbumEntry {
	    public MediaItem item;
	    public Path path;
	    public boolean isPanorama;
	    public int rotation;
	    public int mediaType;
	    public boolean isWaitDisplayed;
	    public TiledTexture bitmapTexture;
	    public Texture content;
	    private BitmapLoader contentLoader;
	}
	
	private Listener mListener;
	 private final JobLimiter mThreadPool;
	 private final MusicAlbumEntry mData[];
	
	 public MusicAlbumSlidingWindow(AbstractGalleryActivity activity,int cacheSize) {
		 mData = new MusicAlbumEntry[cacheSize];
		 
		 mThreadPool = new JobLimiter(activity.getThreadPool(), 2);
	 }
	 
	 public void setListener(Listener listener) {
	     mListener = listener;
	 }
	 
	 
	    private class ThumbnailLoader extends BitmapLoader  {
	        private final int mSlotIndex;
	        private final MediaItem mItem;

	        public ThumbnailLoader(int slotIndex, MediaItem item) {
	            mSlotIndex = slotIndex;
	            mItem = item;
	        }

	        @Override
	        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
	            return mThreadPool.submit(mItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL), this);
	        }

	        @Override
	        protected void onLoadComplete(Bitmap bitmap) {
	        	
	        }

	        public void updateEntry() {
	        }
	    }
	 
}