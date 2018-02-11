/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import java.util.List;

import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.view.AbsMeetingActivity;
import cn.tcl.meetingassistant.view.DecisionListAdapter;
import cn.tcl.meetingassistant.view.DecisionPageScrollLayout;
import mst.widget.SliderView;
import mst.widget.toolbar.Toolbar;

public class DecisionListActivity extends AbsMeetingActivity implements View.OnClickListener{

    private final String TAG = DecisionListActivity.class.getSimpleName();

    private DecisionPageScrollLayout mContent;
    private RecyclerView mDecisionListView;
    private View mTransparent;
    private List<MeetingDecisionData> mDecisionlist;
    private int mToolBarHeight;
    private DecisionListAdapter.OnDataChangedListener mOnDataChangedListener;

    public static final int REQUEST_CODE = 10086;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_decision_list);
        getToolbar().setAlpha(0);
        getToolbar().inflateMenu(R.menu.decision_list_menu);
        getToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.decision_list_add_menu){
                    Intent intent = new Intent(DecisionListActivity.this,EditDecisionActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });
        getToolbar().setTitle(R.string.decision);
        getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mContent = (DecisionPageScrollLayout) findViewById(R.id.decision_list_content);
        mDecisionListView = (RecyclerView) findViewById(R.id.decision_list_list);
        mTransparent = findViewById(R.id.decision_list_transparent);

        mTransparent.setOnClickListener(this);
        mContent.setRecyclerView(mDecisionListView);

        mContent.setOnScrollProgressListener(new DecisionPageScrollLayout.OnScrollProgressListener() {
            @Override
            public void progress(float progress) {
                if(progress < 0.5){
                    setThisTitleAlpha(0);
                }else if(progress != 0 ) {
                    double progressTitle = (progress - 0.5) * 2;
                    setThisTitleAlpha(progressTitle);
                }
            }
        });

        initToolBarHeight();
        listenLayout();
        setThisTitleAlpha(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.decision_list_transparent:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0,R.anim.slide_out_bottom);
    }

    boolean hasMeasured;

    // TODO should change the method to measure the size
    private void listenLayout() {
        ViewTreeObserver viewTreeObserver = findViewById(android.R.id.content).getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!hasMeasured) {
                    DisplayMetrics displaysMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);

                    Rect frame = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

                    int itemCount = mDecisionListView.getChildCount();
                    int itemHeight = 0;
                    if(itemCount == 1){
                        itemHeight = mDecisionListView.getChildAt(0).getHeight();
                    }else if(itemCount >= 2){
                        itemHeight = mDecisionListView.getChildAt(0).getHeight() * 2;
                    }
                    mContent.setFullHeight(displaysMetrics.heightPixels - frame.top);
                    mContent.setY(displaysMetrics.heightPixels - mToolBarHeight - frame.top - itemHeight);
                    mContent.setHalfHeight(mToolBarHeight + itemHeight);
                    hasMeasured = true;
                }
            }
        });

    }

    private void initData(){
        mDecisionlist = MeetingStaticInfo.getCurrentMeeting().getMeetingDecisions();
        DecisionListAdapter adapter = new DecisionListAdapter(this);
        adapter.setMeetingDecisionList(mDecisionlist);
        mDecisionListView.setLayoutManager(new LinearLayoutManager(this));
        mDecisionListView.setAdapter(adapter);
        mOnDataChangedListener = new DecisionListAdapter.OnDataChangedListener() {
            @Override
            public void onDataChanged(int itemNumber) {
                if(itemNumber == 1){
                    DisplayMetrics displaysMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
                    Rect frame = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                    int itemHeight = mDecisionListView.getChildAt(0).getHeight();
                    mContent.setHalfHeight(mToolBarHeight + itemHeight);
                }else if(itemNumber == 0){
                    mContent.dismiss();
                }
            }
        };
        adapter.setOnDataChangedListener(mOnDataChangedListener);
    }

    private void initToolBarHeight(){
        //get ToolBar's height
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            mToolBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,getResources().getDisplayMetrics());
        }
    }

    private int getColorWithAlpha(int color,float alpha){
        int green   = Color.green(color);
        int red     = Color.red(color);
        int blue    = Color.blue(color);
        int alphaInt = (int) (255 * alpha);
        return Color.argb(alphaInt,red,green,blue);
    }

    private void setThisTitleAlpha(double alpha) {
        getToolbar().setAlpha((float) alpha);
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        int statusBarColor = getColorWithAlpha(getColor(R.color.colorPrimaryDark), (float) alpha);
        window.setStatusBarColor(statusBarColor);
    }

}
