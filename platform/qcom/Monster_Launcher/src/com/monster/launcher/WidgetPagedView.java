package com.monster.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 创建于 on 16-10-19 下午1:32.
 * 作者 cailiuzuo
 */
public class WidgetPagedView extends PagedView {

    public int getGridCountX() {
        return mGridCountX;
    }

    public int getGridCountY() {
        return mGridCountY;
    }

    public int getMaxItemsPerPage() {
        return mMaxItemsPerPage;
    }

    private final int mGridCountX=3;
    private final int mGridCountY=3;

    private int mMaxItemsPerPage;
    private DeskWidgetActivity mContext;
    private ArrayList<View> mViews;
    private LayoutInflater mInflater;
    protected PageIndicator mPageIndicator;

    public WidgetPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext= (DeskWidgetActivity)context;
        mInflater=LayoutInflater.from(context);
        setupViews();
//        mPageIndicator = (PageIndicatorCircle) mContext.findViewById(R.id.widget_page_indicator);
//        mPageIndicator.setpagedView(this);
    }

    @Override
    protected void getEdgeVerticalPostion(int[] pos) {
        pos[0] = 0;
        pos[1] = getViewportHeight();
    }
    protected void arrangeChildren(ArrayList<TextView> list, int itemCount) {
        ArrayList<LinearLayout> pages = new ArrayList();
        Log.d("liuzuo182","getChildCount()="+getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            LinearLayout page = (LinearLayout) getChildAt(i);
            for(int j = 0; j < page.getChildCount(); j++){
                LinearLayout pageChild = (LinearLayout) page.getChildAt(j);
                if(pageChild!=null)
                pageChild.removeAllViews();
                Log.d("liuzuo182","pageChild.removeAllViews()");
            }

            page.removeAllViews();
            pages.add(page);
        }
        setupContentDimensions(itemCount);

        Iterator<LinearLayout> pageItr = pages.iterator();
        LinearLayout currentPage = null;
        LinearLayout childView = null ;
        int position = 0;
        int newX, newY, rank;

        rank = 0;
        for (int i = 0; i < itemCount; i++) {
            View v = list.size() > i ? list.get(i) : null;
            if (currentPage == null || position >= mMaxItemsPerPage) {
                // Next page
                if (pageItr.hasNext()) {
                    currentPage = pageItr.next();
                } else {
                    currentPage = createAndAddNewPage();
                }
                position = 0;
            }

            if (v != null) {
              /*  CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
                newX = position % mGridCountX;
                newY = position / mGridCountX;
                ItemInfo info = (ItemInfo) v.getTag();
                if (info.cellX != newX || info.cellY != newY || info.rank != rank) {
                    info.cellX = newX;
                    info.cellY = newY;
                    info.rank = rank;
                }
                lp.cellX = info.cellX;
                lp.cellY = info.cellY;*/
                //currentPage.removeAllViews();
                Log.d("liuzuo181","rank="+rank+"  posstion="+position);
                if(childView == null||position%3==0) {
                    childView = createAndAddNewChildView(currentPage, position);
                }
                Log.d("liuzuo182","childView.addView(list.get(i))="+i);
                childView.addView(list.get(i));
                if (rank < FolderIcon.NUM_ITEMS_IN_PREVIEW && v instanceof BubbleTextView) {
                    ((BubbleTextView) v).verifyHighRes();
                }
            }

            rank ++;
            position++;
        }

        // Remove extra views.
        boolean removed = false;
        while (pageItr.hasNext()) {
            removeView(pageItr.next());
            removed = true;
        }
        if (removed) {
            setCurrentPage(0);
        }

        setEnableOverscroll(getPageCount() > 1);

        // Update footer
        mPageIndicator.setVisibility(getPageCount() > 1 ? VISIBLE : GONE);
        // Set the gravity as LEFT or RIGHT instead of START, as START depends on the actual text.
        //M:liuzuo change the indicator  begin
       /* mFolder.mFolderName.setGravity(getPageCount() > 1 ?
                (mIsRtl ? Gravity.RIGHT : Gravity.LEFT) : Gravity.CENTER_HORIZONTAL);*/
        //M:liuzuo change the indicator  end
    }

    private LinearLayout createAndAddNewChildView(LinearLayout currentPage, int position) {
        LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.widgets_container_pageview_celllayout_child,currentPage,false);
        //page.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        currentPage.addView(page);
        return page;
    }

    private void setupViews(){
        mMaxItemsPerPage=mGridCountX*mGridCountY;
    }
    private void setupContentDimensions(int count) {
    //    mAllocatedContentSize = count;
    //    boolean done;
        //M:liuzuo change folder size begin
       // mGridCountX = mMaxCountX;
        //mGridCountY = mMaxCountY;
      //  //M:liuzuo change folder size end
        // Update grid size
       /* for (int i = getPageCount() - 1; i >= 0; i--) {
            getPageAt(i).setGridSize(mGridCountX, mGridCountY);
        }*/
    }
    @Override
    public LinearLayout getPageAt(int index) {
        return (LinearLayout) getChildAt(index);
    }
    private LinearLayout createAndAddNewPage() {
        Log.d("liuzuo181","createAndAddNewPage mMaxItemsPerPage="+mMaxItemsPerPage);
        LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.widgets_text_container_pageview_celllayout,this,false);
        //M:liuzuo begin
        //page.setCellDimensions(grid.folderCellWidthPx, grid.folderCellHeightPx);
        /*int folderCellWidthPx=(grid.widthPx-getRight()-getPaddingLeft()-
                page.getPaddingLeft()-page.getPaddingRight()
        -(mMaxCountX-1 )* mWidthGap)/mMaxCountX;*/
       // page.setCellDimensions(grid.folderCellWidthPx, grid.folderCellHeightPx);
        //    page.setPadding(getPaddingLeft(),getPaddingTop(),getPaddingRight(),0);
        //M:liuzuo end
      //  page.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
        //page.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
       //  page.setClipChildren(false);
        //page.setInvertIfRtl(true);
        //page.setGridSize(mGridCountX, mGridCountY);

        addView(page);
        return page;
    }
   /* private TextView getTextView(int i){

        TextView view= new TextView(mContext);
        int lineSpacing = getResources().getDimensionPixelSize(R.dimen.widget_text_lineSpacing);
        int padding = getResources().getDimensionPixelSize(R.dimen.widget_text_padding);
        view.setTextSize(13);
        String text=null;
        if(mContext.mIterator.hasNext()){
            text=mContext.mIterator.next();
        }else {
            text = mContext.mDefaultText[i];
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.height= LinearLayout.LayoutParams.WRAP_CONTENT;
//        layoutParams.width= 0;
        layoutParams.weight=1;
        view.setLayoutParams(layoutParams);
        view.setGravity(Gravity.CENTER);
        view.setLineSpacing(lineSpacing,1);
        view.setPadding(0,padding,0,padding);
        view.setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));
        view.setTypeface(Typeface.create("monster-normal",Typeface.NORMAL));
        *//*view.setBackground(getResources().getDrawable(R.drawable.desk_widget_text_view_stroke));
        int width=getResources().getDimensionPixelSize(R.dimen.widget_text_view_stroke_width);
        if(mTextViews.size()<3)
        view.setTranslationY(width);
        if(!(mTextViews.size()==1||mTextViews.size()==4)){
            if(mTextViews.size()%3==0){
                view.setTranslationX(width+0.5f);
            }else {
                view.setTranslationX(-width-0.5f);
            }
        }*//*
        if(i==0&&mContext.mTextViews.size()==0){
            view.setLines(1);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.enterEditText();
                    for (TextView textView : mContext.mTextViews)
                        textView.setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));
                    TextView textView= (TextView) v;
                    textView.setTextColor(getResources().getColor(R.color.desk_widget_text_color_check));
                    //changeTextView(v);
                }
            });
        }else {
            view.setLines(2);
            view.setOnClickListener(mContext);
        }
        view.setText(text);
        if(mContext.mTextViews!=null){
            mContext.mTextViews.add(view);
        }
        return view;
    }*/
   @Override
   protected PageIndicator.PageMarkerResources getPageIndicatorMarker(int pageIndex) {
       Log.d("liuzuo83","getPageIndicatorMarker");
           return new PageIndicator.PageMarkerResources(R.drawable.ic_pageindicator_current_black,
                   R.drawable.ic_pageindicator_default_black);

   }
    @Override
    protected void onUnhandledTap(MotionEvent ev) {
        //super.onUnhandledTap(ev);
    }

    public void setIndicator(PageIndicator indicator) {
        mPageIndicator = indicator;
    }
}
