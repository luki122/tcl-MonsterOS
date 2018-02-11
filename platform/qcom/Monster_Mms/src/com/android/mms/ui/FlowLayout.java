package com.android.mms.ui;

/*tangyisen*/
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.data.ContactList;
import com.android.mms.ui.StretchyTextView.OnDoubleClickListener;
import com.android.mms.ui.StretchyTextView.OnSingleClickListener;

public class FlowLayout extends ViewGroup {

    private static final String TAG = "FlowLayout";

    /**
     * 存储所有的View，按行记录
     */
    private List<List<View>> mAllViews = new ArrayList<List<View>>();
    private float tvChildTextSize;
    private int tvChildTextMargin;
    /**
     * 记录每一行的最大高度
     */
    private List<Integer> mLineHeight = new ArrayList<Integer>();
    private RecipientsEditor mRecipientsEditor;

    /*private ArrayList<TextView> mTextView = new ArrayList<>();
    private ArrayList<>*/
    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        tvChildTextSize = context.getResources().getDimensionPixelSize(R.dimen.compose_message_title_size);
        tvChildTextMargin = (int)context.getResources().getDimension(R.dimen.flowlayout_margin);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * 负责设置子控件的测量模式和大小 根据所有子控件设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获得它的父容器为它设置的测量模式和大小
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        Log.e(TAG, sizeWidth + "," + sizeHeight);

        // 如果是warp_content情况下，记录宽和高
        int width = 0;
        int height = 0;
        /**
         * 记录每一行的宽度，width不断取最大宽度
         */
        int lineWidth = 0;
        /**
         * 每一行的高度，累加至height
         */
        int lineHeight = 0;

        int cCount = getChildCount();

        // 遍历每个子元素
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            // 测量每一个child的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 得到child的lp
            MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();
            // 当前子空间实际占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            // 当前子空间实际占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            /**
             * 如果加入当前child，则超出最大宽度，则的到目前最大宽度给width，类加height 然后开启新行
             */
            if (lineWidth + childWidth > sizeWidth) {
                width = Math.max(lineWidth, childWidth);// 取最大的
                lineWidth = childWidth; // 重新开启新行，开始记录
                // 叠加当前高度，
                height += lineHeight;
                // 开启记录下一行的高度
                lineHeight = childHeight;
            } else {
            // 否则累加值lineWidth,lineHeight取最大高度
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == cCount - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }
        setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width,
            (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;
        // 存储每一行所有的childView
        List<View> lineViews = new ArrayList<View>();
        int cCount = getChildCount();
        // 遍历所有的孩子，mRecipientsEditor放在最后面
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (child instanceof RecipientsEditor) {
                continue;
            }
            MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                // 记录这一行所有的View以及最大高度
                mLineHeight.add(lineHeight);
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllViews.add(lineViews);
                lineWidth = 0;// 重置行宽
                lineViews = new ArrayList<View>();
            }
            /**
             * 如果不需要换行，则累加
             */
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        }
        if(mRecipientsEditor != null) {
            MarginLayoutParams lp = (MarginLayoutParams)mRecipientsEditor.getLayoutParams();
            int childWidth = mRecipientsEditor.getMeasuredWidth();
            int childHeight = mRecipientsEditor.getMeasuredHeight();

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                // 记录这一行所有的View以及最大高度
                mLineHeight.add(lineHeight);
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllViews.add(lineViews);
                lineWidth = 0;// 重置行宽
                lineViews = new ArrayList<View>();
            }
            /**
             * 如果不需要换行，则累加
             */
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(mRecipientsEditor);
        }
        // 记录最后一行
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);

        int left = 0;
        int top = 0;
        // 得到总行数
        int lineNums = mAllViews.size();
        for (int i = 0; i < lineNums; i++) {
            // 每一行的所有的views
            lineViews = mAllViews.get(i);
            // 当前行的最大高度
            lineHeight = mLineHeight.get(i);

            Log.e(TAG, "第" + i + "行 ：" + lineViews.size() + " , " + lineViews);
            Log.e(TAG, "第" + i + "行， ：" + lineHeight);

            // 遍历当前行所有的View
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();

                // 计算childView的left,top,right,bottom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                Log.e(TAG, child + " , l = " + lc + " , t = " + t + " , r =" + rc + " , b = " + bc);

                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
            }
            left = 0;
            top += lineHeight;
        }
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mRecipientsEditor = (RecipientsEditor) findViewById(R.id.recipients_editor);
    }

    public void addTextViewChild(String nameContent, String numberTag) {
        Context context = getContext();
        /*LayoutInflater inflater = LayoutInflater.from(context);
        TextView tv = (TextView)inflater.inflate(R.layout.recipients_editor_flowlayout_child, null);*/
        //tv.setTextAppearance(context, R.style.recipients_editor_tv_style);
        TextView tv = new TextView(context);
        MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);//(MarginLayoutParams)tv.getLayoutParams();
        /*lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;*/
        lp.bottomMargin = tvChildTextMargin;
        lp.leftMargin = tvChildTextMargin;
        lp.topMargin = tvChildTextMargin;
        lp.rightMargin = tvChildTextMargin;
        tv.setLayoutParams(lp);
        tv.setBackgroundResource(R.drawable.recipients_editor_tv_bg);
        tv.setTextColor(R.color.color_black);
        tv.setSingleLine(true);
        tv.setEllipsize(TruncateAt.END);
        //tv.setTextSize(tvChildTextSize);
        tv.setText(nameContent);
        tv.setTag(numberTag);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if(mOnClickTVChildListener != null) {
                    mOnClickTVChildListener.onClick(view);
                }
                //removeView(view);
            }
        });
        addView(tv);
        if(mRecipientsEditor.getHint() != null) {
            mRecipientsEditor.setHint(null);
        }
    }

    private OnClickTVChildListener mOnClickTVChildListener;
    public interface OnClickTVChildListener {
        void onClick(View view);
    }

    public void setOnClickTVChildListener(OnClickTVChildListener listener) {
        mOnClickTVChildListener = listener;
    }

    public String removeLastChildView() {
        String number = null;
        for(int i = getChildCount() - 1; i > -1; i--) {
            View child = getChildAt(i);
            if(child instanceof RecipientsEditor) {
                continue;
            } else {
                number = (String)child.getTag();
                if(mOnClickTVChildListener != null) {
                    mOnClickTVChildListener.onClick(child);
                }
                removeView(child);
                break;
            }
        }
        if(getChildCount() == 1) {
            mRecipientsEditor.setHint(R.string.to_hint);
        }
        return number;
    }
}