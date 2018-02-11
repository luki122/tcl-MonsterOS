package mst.widget.recycleview;

import android.graphics.Rect;
import android.view.View;

public class RecyclerViewItemEmptySpace extends RecyclerView.ItemDecoration{  
	  
    private int space;  

    public RecyclerViewItemEmptySpace(int space) {  
        this.space = space;  
    }  

    @Override  
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {  

        if(parent.getChildPosition(view) != 0)  
            outRect.top = space;  
    }  
}
