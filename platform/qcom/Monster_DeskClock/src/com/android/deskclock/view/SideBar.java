package com.android.deskclock.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.android.deskclock.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SideBar extends View {
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public String[] b = { "★","A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z", "#" };

    public List<String> m_list = new ArrayList<String>();
    public HashMap<String, String> m_catalog_hashmap;

    private int choose = 0;// 选中
    private Paint paint = new Paint();

    private TextView mTextDialog;

    public void setDialogTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }
    
    public TextView getDialogTextView(){
        return mTextDialog;
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }

    /**
     * 重写这个方法
     */
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        // 获取焦点改变背景颜色.
//        int height = getHeight();// 获取对应高度
//        int width = getWidth(); // 获取对应宽度
//        int singleHeight = height / b.length;// 获取每一个字母的高度
//
//        for (int i = 0; i < b.length; i++) {
//            paint.setColor(Color.rgb(33, 65, 98));
//            // paint.setColor(Color.WHITE);
//            paint.setTypeface(Typeface.DEFAULT_BOLD);
//            paint.setAntiAlias(true);
//            paint.setTextSize(20);
//            // 选中的状态
//            if (i == choose) {
//                paint.setColor(Color.parseColor("#3399ff"));
//                paint.setFakeBoldText(true);
//            }
//            // x坐标等于中间-字符串宽度的一半.
//            float xPos = width / 2 - paint.measureText(b[i]) / 2;
//            float yPos = singleHeight * i + singleHeight;
//            canvas.drawText(b[i], xPos, yPos, paint);
//            paint.reset();// 重置画笔
//        }
//
//    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        if(m_list.size() == 0){
            return;
        }
        
        int singleHeight = height / b.length;// 获取每一个字母的高度
        int off_set = (height - singleHeight*m_list.size())/2;//为了居中添加offset

        for (int i = 0; i < m_list.size(); i++) {
            paint.setColor(getContext().getColor(R.color.currency_head));
            // paint.setColor(Color.WHITE);
            
//            Typeface tf = Typeface.create("sans-serif-thin", -1);
//            mPaintText.setTypeface(tf);

            
            paint.setTypeface( Typeface.create("sans-serif-light", -1));
            paint.setAntiAlias(true);
            float text_size = singleHeight*7/10;
            paint.setTextSize(text_size);//字体大小为整个高度的0.8
            // 选中的状态
            
            if(i == choose){
                float cx = width / 2;
                float cy = singleHeight * (i+1) + off_set;
//                float r = width*7/20;
                float r = singleHeight*5/10;//chg zouxu 20160809
                paint.setColor(getContext().getColor(R.color.clock_red));
//                canvas.drawCircle(cx,cy-r/2,r, paint);//屏蔽掉暂时不要
            }
            //paint.setTextAlign(Paint.Align.CENTER);
            if (i == choose) {
//                paint.setColor(getContext().getColor(R.color.white));
                paint.setColor(getContext().getColor(R.color.clock_red));
                paint.setFakeBoldText(true);
            }
            paint.setStrokeWidth(3.0f);
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(m_list.get(i)) / 2;
            float yPos = singleHeight * (i+1) + off_set;//+singleHeight/10;//+singleHeight/10 是为了向下位移0.1
            canvas.drawText(m_list.get(i), xPos, yPos, paint);
            paint.reset();// 重置画笔
        }

    }

    

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        
        if(m_list.size() == 0){
            return false;
        }
        int height = getHeight();
        int singleHeight = height / b.length;// 获取每一个字母的高度
        int off_set = (height - singleHeight*m_list.size())/2;

        
        int c=0;
        
        if(y<=off_set){
            c = 0;
        } else if(y>=off_set+singleHeight*m_list.size()){
            c = m_list.size()-1;
        } else {
            c = (int)(y-off_set)/singleHeight;
        }
        
        
//        final int c = (int) (y / getHeight() * m_list.size());// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

        switch (action) {
        case MotionEvent.ACTION_UP:
            //setBackgroundDrawable(new ColorDrawable(0x00000000));
            //choose = -1;//
            invalidate();
            if (mTextDialog != null) {
                mTextDialog.setVisibility(View.INVISIBLE);
            }
            break;

        default:
            //setBackgroundResource(R.drawable.sidebar_background);//去掉背景
            if (oldChoose != c) {
                if (c >= 0 && c < m_list.size()) {
                    if (listener != null) {
                        listener.onTouchingLetterChanged(m_list.get(c));
                    }
                    if (mTextDialog != null) {
                        mTextDialog.setText(m_list.get(c));
                        mTextDialog.setVisibility(View.VISIBLE);
                    }

                    choose = c;
                    invalidate();
                }
            }

            break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     * 
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     * 
     * @author coder
     * 
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }

    public void updateCataLogList(HashMap<String, String> catalog_hashmap) {
        m_list.clear();
        boolean flag = false;
        m_catalog_hashmap = catalog_hashmap;
        if(m_catalog_hashmap.get("#")!=null){
            m_catalog_hashmap.remove("#");
            flag = true;
        }
        boolean is_fav = false;
        if(m_catalog_hashmap.get("★")!=null){
            m_catalog_hashmap.remove("★");
            is_fav = true;
        }
        
        if (m_catalog_hashmap == null) {
            for (int i = 0; i < b.length; i++) {
                m_list.add(b[i]);
            }
        } else {
            Iterator iter = m_catalog_hashmap.keySet().iterator();
            while (iter.hasNext()) {
                Object key = iter.next();
                Object val = m_catalog_hashmap.get(key);
                m_list.add((String) val);
            }
        }
        
        Collections.sort(m_list, new ComparatorCataLog());
        if(is_fav){
            m_list.add(0, "★");
        }
        if(flag){
            m_list.add("#");
        }
        invalidate();
    }
    
    public class ComparatorCataLog implements Comparator {

        public int compare(Object arg0, Object arg1) {
            String str0 = (String) arg0;
            String str1 = (String) arg1;
            return str0.compareTo(str1);
        }
    }
    
    //获取下一个section的位置
    public int getNextPosition(int section){
        for(int i=0;i<m_list.size();i++){
            if(m_list.get(i).charAt(0) == section){
                if(i != m_list.size()-1) {
                    return m_list.get(i+1).charAt(0);
                } else {
                    return m_list.get(i).charAt(0);
                }
            }
        }
        return 0;
    }
    
    public void setSelction(int section){
        for(int i=0;i<m_list.size();i++){
            if(m_list.get(i).charAt(0) == section){
                choose = i;
                invalidate();
                return;
            }
        }
    }
    
    
    public void setSelectedSelction(char section){
    	 for(int i=0;i<m_list.size();i++){
             if(m_list.get(i).charAt(0) == section){
                 choose = i;
                 invalidate();
                 return;
             }
         }
    }


}