package cn.tcl.music.view;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import cn.tcl.music.R;

public class SpacerDividerItemDecoration extends ItemDecoration {
	
    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    
    public static final int GRID_LAYOUT = LinearLayoutManager.VERTICAL + 1;

    private int mOrientation;
    private int mSpaceBetweenItems;
	
	public SpacerDividerItemDecoration(int orientation, int spaceBetweenItems) {
		setOrientation(orientation);
		mSpaceBetweenItems = spaceBetweenItems;
	}

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }
    
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
    		State state) {
    	int viewPosition = parent.getChildPosition(view);
    	ViewHolder vh = parent.getChildViewHolder(view);

    	if (vh == null || (vh.getItemViewType() != R.id.media_content && 
    					   vh.getItemViewType() != R.id.media_favorite_playlist &&
    					   vh.getItemViewType() != R.id.media_record_playlist &&
    					   vh.getItemViewType() != R.id.media_last_add_playlist &&
    					   vh.getItemViewType() != R.id.media_hidden_playlist)) {
    		outRect.set(0, 0, 0, 0);
    	}
    	else {
        	switch(mOrientation) {
	        	case HORIZONTAL_LIST: {
	        		outRect.set(0, 0, mSpaceBetweenItems, 0);
	        		break;
	        	}
	        	case VERTICAL_LIST: {
					outRect.set(0, mSpaceBetweenItems, 0, 0);
	        		break;
	        	}
	        	case GRID_LAYOUT: {
	        		//TODO: This is only adapted for Span Count = 2.
	            	GridLayoutManager glm = (GridLayoutManager) parent.getLayoutManager();
	            	int spanCount = glm.getSpanCount();
	            	int spanIndex = glm.getSpanSizeLookup().getSpanIndex(viewPosition, spanCount);
            		outRect.set((int)(mSpaceBetweenItems * ( 1 - spanIndex * 0.5)), mSpaceBetweenItems, (int)(mSpaceBetweenItems * (0.5 * (1 + spanIndex))), 0);

	        		break;
	        	}
        	}
    	}
    }

}
