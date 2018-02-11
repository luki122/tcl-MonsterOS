package mst.view.menu.bottomnavigation;

import mst.utils.ViewGroupUtils;
import mst.view.menu.MstMenuBuilder;
import mst.view.menu.MstMenuView;
import mst.view.menu.bottomnavigation.BottomNavigationMenuPresenter.NavigationMenuAdapter;
import mst.view.menu.bottomnavigation.BottomNavigationMenuPresenter.NormalViewHolder;
import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerView.ViewHolder;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class BottomNavigationMenuView extends LinearLayout implements MstMenuView {

	private NavigationMenuAdapter mAdapter;
	
	private int mItemCount = 0;
	
    public BottomNavigationMenuView(Context context) {
        this(context, null);
    }

    public BottomNavigationMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    @Override
    public void initialize(MstMenuBuilder menu) {

    }

    @Override
    public int getWindowAnimations() {
        return 0;
    }
    
    
    public void setAdapter(NavigationMenuAdapter adapter){
    	mAdapter = adapter;
    	addMenuItem();
    }
    
    
    
    private void addMenuItem(){
    	if(mAdapter != null){
    		mItemCount = mAdapter.getItemCount();
    		if(mItemCount <= 0){
    			return;
    		}
    		
    		for(int i = 0;i < mItemCount;i++){
    			int itemViewType = mAdapter.getItemViewType(i);
    			NormalViewHolder holder = mAdapter.onCreateViewHolder(this, itemViewType);
    			if(holder != null){
    				mAdapter.onBindViewHolder(holder, i);
    			}
    		}
    		
    		
    	}
    	
    }
    
    @Override
    protected void onDetachedFromWindow() {
    	// TODO Auto-generated method stub
    	super.onDetachedFromWindow();
    	if(mAdapter != null){
    		
    	}
    }

	public void update(NavigationMenuAdapter adapter) {
		// TODO Auto-generated method stub
		removeAllViews();
		setAdapter(mAdapter);
	}
    
    

}