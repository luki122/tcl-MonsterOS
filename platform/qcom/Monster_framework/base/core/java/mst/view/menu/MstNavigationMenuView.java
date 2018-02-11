package mst.view.menu;

import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import android.content.Context;
import android.util.AttributeSet;

public class MstNavigationMenuView extends RecyclerView implements MstMenuView {

    public MstNavigationMenuView(Context context) {
        this(context, null);
    }

    public MstNavigationMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MstNavigationMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void initialize(MstMenuBuilder menu) {

    }

    @Override
    public int getWindowAnimations() {
        return 0;
    }

}