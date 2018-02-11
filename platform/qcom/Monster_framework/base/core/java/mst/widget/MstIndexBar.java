package mst.widget;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.mst.internal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 */
public class MstIndexBar extends View {  
    private static final String TAG = "MstIndexBar";
    public static interface OnSelectListener{
        public void onSelect(int index,int layer,Letter letter);
    }
    private OnSelectListener mListener;

    public static interface OnTouchStateChangedListener{
        public void onStateChanged(TouchState old, TouchState news);
    }
    private OnTouchStateChangedListener mChangedListener;

    public static enum TouchState {
        NONE,DOWN,UP,SUB
    }
    private TouchState mTouchState = TouchState.NONE;


    public static final int TYPE_BITMAP = 1;
    public static final int TYPE_TEXT = 0;

    public static final int POSITION_HEADER = 0;
    public static final int POSITION_CHARS = 1;
    public static final int POSITION_FOOTER = 2;

    private final char[] CHARS = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    //test code start
//    private final char[] SUBCHARS = {'A','B','C','D'};
//    private final char[] SUBCHARS1 = {'L','M','N','O','P','Q','X','Y','Z'};
//    private final char[] SUBCHARS2 = {'O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    //test code end
    private final char OTHER_CHAR = '#';
    private int mCharsSpace = 10;
    private int mGap = 30;
    private String mTypeface = "monster-normal";
    private int mTypeStyle = Typeface.NORMAL;
    private ArrayList<Letter> mHeader;
    private ArrayList<Letter> mFooter;
    private ArrayList<Letter> mChars;

    private boolean isSub = false;
    private boolean isDisplay = false;
    private boolean subCanGetFocus = true;

    private PopupWindow mBalloon;
    private BalloonTextView mBalloonView;
    private int mBalloonGap = 0;
    private int[] mBalloonLayerColors;
    private int[] mBalloonFocusColors;

    private PopupWindow mPopup;
    private MstIndexBar mPopupBar;
    private HashMap<Integer,List> mSub;
    private int mCurrentPopupIndex = -1;
    private int mCurrentClickIndex = -1;
    private int mFocusIndex = -1;
    private boolean mActive = false;

    private int mPopupLeft = 0;
    private int mPopupTop = 0;

    private float mItemHeight = 0;
    private int mWidth = 0;
    private int mLayer = 0;

    private int shareBackgroundId = 0;
    private static final int DEFAULT_COLOR = Color.GRAY;
    private ColorStateList letterColorList;
    private int letterTextSize = -1;
    private int subTextSize = -1;
    private float mFontWeight = 0;

    private int mBalloonLeft = 0;
    private int mBalloonTop = 0;
    private int mBalloonDiameter = 240;
    private float mBalloonDispersion = 1f;

    private int mBalloonGravity = Gravity.NO_GRAVITY;

    public MstIndexBar(Context context) {
        super(context);
        init();
    }

    public MstIndexBar(Context context, AttributeSet attrs) {
        this(context, attrs,0,com.mst.R.style.Widget_MstIndexBar);
    }

    public MstIndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,com.mst.R.style.Widget_MstIndexBar);
    }

    public MstIndexBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        saveAttrs(attrs,defStyleAttr,defStyleRes);
        init();
    }

    private void saveAttrs(AttributeSet attrs,int styleAttr,int styleRes){
        TypedArray a = getContext().obtainStyledAttributes(attrs,com.mst.R.styleable.MstIndexBar,styleAttr,styleRes);
        final int N = a.getIndexCount();
        for(int i=0;i<N;i++){
            int attr = a.getIndex(i);
            if (attr == com.mst.R.styleable.MstIndexBar_shareBackground) {
                setShareBackgroundId(a.getResourceId(attr, 0));
            }else if(attr == com.mst.R.styleable.MstIndexBar_letterColor) {
                letterColorList = a.getColorStateList(attr);
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonLeft){
                mBalloonLeft = a.getDimensionPixelOffset(attr,0);
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonTop){
                mBalloonTop = a.getDimensionPixelOffset(attr,0);
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonDiameter){
                mBalloonDiameter = a.getDimensionPixelOffset(attr,0);
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonDispersion){
                mBalloonDispersion = a.getFloat(attr,mBalloonDispersion);
            }else if(attr == com.mst.R.styleable.MstIndexBar_letterTextSize){
                letterTextSize = a.getDimensionPixelOffset(attr,letterTextSize);
            }else if(attr == com.mst.R.styleable.MstIndexBar_letterSpace){
                mCharsSpace = a.getDimensionPixelOffset(attr,mCharsSpace);
            }else if(attr == com.mst.R.styleable.MstIndexBar_subGap){
                mGap = a.getDimensionPixelOffset(attr,mGap);
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonGap){
                mBalloonGap = a.getDimensionPixelOffset(attr,mBalloonGap);
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonGravity){
                mBalloonGravity = a.getInt(attr, Gravity.NO_GRAVITY);
            }else if(attr == com.mst.R.styleable.MstIndexBar_android_textStyle){
                mTypeStyle = a.getInt(attr, mTypeStyle);
            }else if(attr == com.mst.R.styleable.MstIndexBar_android_fontFamily){
                mTypeface = a.getString(attr);
            }else if(attr == com.mst.R.styleable.MstIndexBar_subTextSize){
                subTextSize = a.getDimensionPixelOffset(attr,subTextSize);
            }else if(attr == com.mst.R.styleable.MstIndexBar_fontWeight){
                mFontWeight = a.getFloat(attr,mFontWeight);
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonLayerColor){
                int resid = a.getResourceId(attr,0);
                if(resid != 0) {
                    TypedArray ca = getContext().getResources().obtainTypedArray(resid);
                    final int size = ca.length();
                    mBalloonLayerColors = new int[size];
                    for(int j=0;j<size;j++){
                        mBalloonLayerColors[j] = ca.getColor(j,DEFAULT_COLOR);
                    }
                    ca.recycle();
                }
            }else if(attr == com.mst.R.styleable.MstIndexBar_balloonFocusColor){
                int resid = a.getResourceId(attr,0);
                if(resid != 0) {
                    TypedArray ca = getContext().getResources().obtainTypedArray(resid);
                    final int size = ca.length();
                    mBalloonFocusColors = new int[size];
                    for(int j=0;j<size;j++){
                        mBalloonFocusColors[j] = ca.getColor(j,DEFAULT_COLOR);
                    }
                    ca.recycle();
                }
            }
        }
        if(letterColorList == null){
            letterColorList = getContext().getResources().getColorStateList(R.color.indexbar_letter_color,null);
        }
        a.recycle();
    }

    private void init(){
        mSub = new HashMap<>();
        mHeader = new ArrayList<>();
        mFooter = new ArrayList<>();
        mChars = new ArrayList<>();

        Letter collect = new Letter();
        collect.type = TYPE_BITMAP;
        collect.bitmap = getContext().getResources().getDrawable(R.drawable.indexbar_star_normal_holo_light,null);
        mHeader.add(collect);

        Letter other = new Letter();
        other.text = String.valueOf(OTHER_CHAR);
        mFooter.add(other);

        for(int i=0;i<CHARS.length;i++){
            Letter addition = new Letter();
            addition.text = String.valueOf(CHARS[i]);
            mChars.add(addition);
        }

        //test code start
//        ArrayList<Letter> test = new ArrayList<>();
//        for(int i=0;i<SUBCHARS.length;i++){
//            Letter addition = new Letter();
//            addition.text = String.valueOf(SUBCHARS[i]);
//            test.add(addition);
//        }
//        mSub.put(6,test);
//        ArrayList<Letter> test1 = new ArrayList<>();
//        for(int i=0;i<SUBCHARS1.length;i++){
//            Letter addition = new Letter();
//            addition.text = String.valueOf(SUBCHARS1[i]);
//            test1.add(addition);
//        }
//        mSub.put(7,test1);
//        ArrayList<Letter> test2 = new ArrayList<>();
//        for(int i=0;i<SUBCHARS2.length;i++){
//            Letter addition = new Letter();
//            addition.text = String.valueOf(SUBCHARS2[i]);
//            test2.add(addition);
//        }
//        mSub.put(12,test2);
        //test code end

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(isSub){
            Log.d(TAG,"onMeasure : mWidth = "+mWidth+"; mHeight = "+mItemHeight*size());
            int measuredWidth = MeasureSpec.makeMeasureSpec(mWidth,MeasureSpec.EXACTLY);
            int measuredHeight = MeasureSpec.makeMeasureSpec((int)(mItemHeight*size()),MeasureSpec.EXACTLY) + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(measuredWidth,measuredHeight);
        }else{
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        float itemHeight = getItemHeight();
        int space = mCharsSpace;
        if(!isAutoSize()){
            space = (int)(itemHeight/2f - letterTextSize/2f);
        }


        drawLetter(canvas,itemHeight,space);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"onTouchEvent : ("+isSub+")("+event.getX()+","+event.getY()+"),isDisplay = "+isDisplay);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(x < 0){
                    if(isPopupShow()){
                        setTouchState(TouchState.SUB);
                        mCurrentClickIndex = -1;
                        isDisplay = true;
                        if(isPopupShow() && mPopupBar != null) {
                            event.setLocation(x-mPopupLeft,y-mPopupTop);
                            mPopupBar.onTouchEvent(event);
                        }
                    }
                }else{
                    isDisplay = false;
                    if(getTouchState() != TouchState.DOWN) {
                        setTouchState(TouchState.DOWN);
                        if(isPopupShow() && mPopupBar != null) {
                            event.setAction(MotionEvent.ACTION_UP);
                            mPopupBar.onTouchEvent(event);
                            mPopupBar.clearLetterFocus();
                        }
                    }
                }
                if(!isDisplay) {
                    displayPopup(x, y);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setTouchState(TouchState.UP);
                mCurrentClickIndex = -1;
                if(isPopupShow()){
                    event.setLocation(x-mPopupLeft,y-mPopupTop);
                    mPopupBar.onTouchEvent(event);
                }
                isDisplay = false;
                dismissPopup(getPopup(-1));
                dismissBalloon();
                break;
        }
        return true;
    }

    private void drawLetter(Canvas canvas,float itemHeight,int space){
        int size = size();
        for (int index = 0; index < size; index++) {
            Letter h = getLetter(index);
            if (h != null) {
                if (!subCanGetFocus && isSub) {
                    h.clearFocus();
                    h.clearFocusColor();
                } else {
                    if (h.hasFocus() && index != mFocusIndex) {
                        h.clearFocus();
                    } else if (!h.hasFocus() && index == mFocusIndex) {
                        h.requestFocus();
                    }
                }
                int textcolor = DEFAULT_COLOR;
                if (h.focusUsed && h.hasFocus()) {
                    textcolor = h.focus_color;
                } else {
                    if (h.handleColor) {
                        textcolor = h.text_color;
                    } else {
                        if (letterColorList != null) {
                            int colorEmpty = letterColorList.getColorForState(View.EMPTY_STATE_SET, DEFAULT_COLOR);
                            int colorEnable = letterColorList.getColorForState(View.ENABLED_STATE_SET, DEFAULT_COLOR);

                            int colorCurrent = h.enable ? colorEnable : colorEmpty;
                            int colorSelected = letterColorList.getColorForState(View.SELECTED_STATE_SET, colorCurrent);
                            int colorDefault = mActive ? colorSelected : colorCurrent;
                            int colorFocus = letterColorList.getColorForState(View.FOCUSED_STATE_SET, colorDefault);

                            if (h.focus) {
                                textcolor = colorFocus;
                            } else {
                                textcolor = colorDefault;
                            }
                        }
                    }
                }
                switch (h.type) {
                    case TYPE_BITMAP: {
                        h.height = h.width = Math.min((int) (itemHeight - 2 * space), getWidth());
                        float x = getWidth() / 2f - h.width / 2f;
                        float y = itemHeight * index + space + getPaddingTop();
                        h.y = y - space;

                        h.bitmap.setTint(textcolor);
                        h.bitmap.setBounds((int) x, (int) y, (int) x + h.width, (int) y + h.height);
                        h.bitmap.draw(canvas);
                        break;
                    }
                    case TYPE_TEXT: {
                        h.height = (int) (itemHeight - 2 * space);
                        Paint textPaint = new Paint();
                        Rect textBound = new Rect();

                        textPaint.setColor(textcolor);
                        textPaint.setTypeface(Typeface.create(mTypeface,mTypeStyle));
                        textPaint.setTextSize(letterTextSize);
                        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                        textPaint.setStrokeWidth(mFontWeight);
                        textPaint.getTextBounds(h.text, 0, h.text.length(), textBound);
                        textPaint.setAntiAlias(true);

                        h.width = textBound.width();
                        float x = getWidth() / 2f - h.width / 2f;
                        float y = itemHeight * index + (itemHeight / 2f - textBound.height() / 2f) + getPaddingTop();
                        h.y = itemHeight * index + getPaddingTop();
                        Log.d(TAG, "onDraw : " + h.text + "(" + x + "," + y + ")itemHeight = " + itemHeight + "; textBound = (" + textBound.width() + "," + textBound.height() + ")");
                        canvas.drawText(h.text, x, y + textBound.height(), textPaint);
                        break;
                    }
                }
            }
        }
    }

    private void setTouchState(TouchState state){
        boolean changed = mTouchState != state;
        if(changed && mChangedListener != null){
            mChangedListener.onStateChanged(mTouchState,state);
        }
        if(state == TouchState.DOWN){
            setActive(true);
        }else{
            setActive(false);
        }
        mTouchState = state;
    }

    public TouchState getTouchState(){
        return mTouchState;
    }

    private int getIndexByY(float y){
        float itemHeight = getItemHeight();
        int index = ((int)y-getPaddingTop())/(int)itemHeight;
        if(index < 0 || index >= size()){
            index = -1;
        }
        return index;
    }

    public int getIndex(String str){
        int ret = -1;
        if((ret = indexOf(mHeader,str)) != -1){
            return ret;
        }
        if((ret = indexOf(mChars,str)) != -1){
            return ret + mHeader.size();
        }
        if((ret = indexOf(mFooter,str)) != -1){
            return ret + mHeader.size() + mChars.size();
        }
        return ret;
    }

    private int indexOf(ArrayList<Letter> list, String str){
        if(list == null || str == null) return -1;
        int N = list.size();
        for(int i=0;i<N;i++){
            Letter addition = list.get(i);
            if(str.equals(addition.text)){
                return i;
            }
        }
        return -1;
    }

    public void setBalloonLocation(int left, int top){
        mBalloonLeft = left;
        mBalloonTop = top;
    }

    public void setBalloonDiameter(int d){
        mBalloonDiameter = d;
    }

    public void setBalloonDispersion(float d){
        mBalloonDispersion = d;
    }

    public String getString(int index){
        Letter addition = getLetter(index);
        return addition.text;
    }

    public void setShareBackgroundId(int resid){
        shareBackgroundId = resid;
        if(resid != 0){
            setBackground(getContext().getResources().getDrawable(resid));
        }
    }

    public void setColor(int color){
        if(color < 0){
            color = DEFAULT_COLOR;
        }
        letterColorList = ColorStateList.valueOf(color);
    }

    public void setColor(ColorStateList list){
        if(list == null){
            letterColorList = ColorStateList.valueOf(DEFAULT_COLOR);
        }else{
            letterColorList = list;
        }
    }

    public void setEnables(boolean enable,int... indexs){
        for(int po : indexs){
            if(po >= 0 && po < size()) {
                Letter addition = getLetter(po);
                addition.enable = enable;
            }
        }
        update();
    }

    public void setLetterColor(int color,int... indexs){
        for(int po : indexs){
            if(po >= 0 && po < size()) {
                Letter addition = getLetter(po);
                addition.setColor(color);
            }
        }
        update();
    }

    public void setLetterTextSize(int textSize){
        letterTextSize = textSize;
    }

    public void setSubTextSize(int textSize){
        subTextSize = textSize;
    }

    public void setFontWeight(float weight){
        mFontWeight = weight;
    }

    public void setLetterSpace(int space){
        mCharsSpace = space;
    }

    /**
     * 根据索引设置第二层的字符列表。
     * @param index 索引。
     * @param list 字母列表，此列表只能为String的列表或者Letter的列表。
     */
    public void setSubList(int index,List list){
        if(list != null && list.size() > 0) {
            Object obj = list.get(0);
            if(obj instanceof String) {
                ArrayList<Letter> array = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    Letter addition = new Letter();
                    addition.text = (String)list.get(i);
                    addition.enable = true;
                    array.add(addition);
                }
                mSub.put(index, array);
            }else if(obj instanceof Letter){
                mSub.put(index,list);
            }
        }
    }

    public void setActive(boolean active){
        mActive = active;
        update();
    }

    public void clearLetterColor(int... indexs){
        for(int po : indexs){
            if(po >= 0 && po < size()) {
                Letter addition = getLetter(po);
                addition.clearColor();
            }
        }
        update();
    }

    public void setOnSelectListener(OnSelectListener listener){
        mListener = listener;
    }

    public void setOnTouchStateChangedListener(OnTouchStateChangedListener listener){
        mChangedListener = listener;
    }

    public int size(){
        return mHeader.size() + mChars.size() + mFooter.size();
    }

    public void update(){
        postInvalidate();
    }

    public void clear(int position){
        switch (position){
            case POSITION_HEADER:
                mHeader.clear();
                break;
            case POSITION_CHARS:
                mChars.clear();
                break;
            case POSITION_FOOTER:
                mFooter.clear();
                break;
        }
    }

    public void clearAll(){
        mHeader.clear();
        mChars.clear();
        mFooter.clear();
    }

    public void release(){
        clearAll();
        mSub.clear();
    }

    public void addLetter(int position,int index,Letter addition){
        List list = null;
        switch (position){
            case POSITION_HEADER:
                list = mHeader;
                break;
            case POSITION_CHARS:
                list = mChars;
                break;
            case POSITION_FOOTER:
                list = mFooter;
                break;
        }
        list.add(index,addition);
    }

    public void addLetter(int position,Letter... addition){
        List list = null;
        switch (position){
            case POSITION_HEADER:
                list = mHeader;
                break;
            case POSITION_CHARS:
                list = mChars;
                break;
            case POSITION_FOOTER:
                list = mFooter;
                break;
        }
        for(Letter a : addition){
            list.add(a);
        }
    }

    public void addLetter(int position,List<Letter> additions){
        List list = null;
        switch (position){
            case POSITION_HEADER:
                list = mHeader;
                break;
            case POSITION_CHARS:
                list = mChars;
                break;
            case POSITION_FOOTER:
                list = mFooter;
                break;
        }
        list.addAll(additions);
    }

    public void deleteLetter(int... indexs){
        for(int index : indexs) {
            int charlist_index = 0;
            int footlist_index = 0;
            if (index < 0) {
                return;
            } else if (index < mHeader.size()) {
                mHeader.remove(index);
            } else if ((charlist_index = index - mHeader.size()) < mChars.size()) {
                mChars.remove(charlist_index);
            } else if ((footlist_index = index - mHeader.size() - mChars.size()) < mFooter.size()) {
                mFooter.remove(footlist_index);
            }
        }
    }

    public void deleteLetter(String str){
        int index = getIndex(str);
        deleteLetter(index);
    }

    public Letter getLetter(int index){
        Letter h = null;
        int charlist_index = 0;
        int footlist_index = 0;
        if(index < 0){
            h = null;
        }else if(index < mHeader.size()){
            h = mHeader.get(index);
        }else if((charlist_index = index - mHeader.size()) < mChars.size()){
            h = mChars.get(charlist_index);
        }else if((footlist_index = index - mHeader.size() - mChars.size()) < mFooter.size()){
            h = mFooter.get(footlist_index);
        }
        return h;
    }


    private PopupWindow getPopup(int index){
        Log.d(TAG,"getPopup : index = " + index + ";isSub = "+isSub);
        if(index != -1) {
            List sub = mSub.get(index);
            if (sub != null && sub.size() > 0) {
                Log.d(TAG,"getPopup : sub = " + sub.size() + ";isSub = "+isSub);
                float itemHeight = getItemHeight();
                if(mPopup == null) {
                    mPopup = new PopupWindow();
                    mPopup.setTouchable(true);
                    mPopup.setAnimationStyle(R.style.MstIndexBar_popup_anim);
                    mPopupBar = new MstIndexBar(getContext());
                    mPopupBar.setOnSelectListener(mListener);
                    mPopupBar.setItemHeight(itemHeight);
                    mPopupBar.setWdith(getWidth());
                    mPopupBar.setIsSub(true);
                    mPopupBar.setLayer(mLayer + 1);
                    mPopupBar.setBalloon(getBalloon(),mBalloonView);
                    mPopupBar.setShareBackgroundId(shareBackgroundId);
                    mPopupBar.setColor(letterColorList);
                    mPopupBar.setPadding(getPaddingLeft(),getPaddingTop(),getPaddingRight(),getPaddingBottom());
                    mPopupBar.setBalloonLocation(mBalloonLeft,mBalloonTop);
                    mPopupBar.setBalloonDiameter(mBalloonDiameter);
                    mPopupBar.setBalloonDispersion(mBalloonDispersion);
                    mPopupBar.setLetterTextSize(subTextSize != -1 ? subTextSize : letterTextSize);
                    mPopupBar.setLetterSpace(mCharsSpace);
                    mPopupBar.setFontWeight(mFontWeight);
                    mPopupBar.setSubCanGetFocus(subCanGetFocus);
                    mPopupBar.clearAll();
                    mPopup.setContentView(mPopupBar);
                    mPopup.setWidth(getWidth()+mGap);
                }
                mPopupBar.clear(POSITION_CHARS);
                mPopupBar.addLetter(POSITION_CHARS, sub);
            }else{
                return null;
            }
        }
        return mPopup;
    }

    private void displayPopup(float x,float y){
        int index = getIndexByY(y);
        Letter addition = getLetter(index);
        PopupWindow popupWindow = index != -1?getPopup(index):null;
        Log.d(TAG,"displayPopup : popupWindow = "+popupWindow + ";index = "+index+";mCurrentPopupIndex = "+mCurrentPopupIndex);
        if(popupWindow != null && addition != null && addition.enable) {
            if(mCurrentPopupIndex != index) {
                mCurrentPopupIndex = index;
                int[] position = new int[2];
                this.getLocationOnScreen(position);
                int popupHeight = mPopupBar.getSubHeight();
                int height = getHeight();
                mPopupLeft =  - popupWindow.getWidth();
                mPopupTop =  (int) addition.y - (int) (popupHeight / 2f - mPopupBar.getItemHeight() / 2f);
                if(mPopupTop + popupHeight > height){
                    mPopupTop = height - popupHeight;
                }
                if(mPopupTop < 0){
                    mPopupTop = 0;
                }
                Log.d(TAG,"displayPopup : popupWindow("+position[0]+","+position[1]+")");
                if(position[0] != 0  && position[1] != 0) {
                    int popX = position[0] + mPopupLeft;
                    int popY = position[1] + mPopupTop;
                    showPopup(popupWindow, popX, popY);
                }
            }
        }else{
            popupWindow = getPopup(-1);
            dismissPopup(popupWindow);
        }
        Log.d(TAG,"displayPopup : mCurrentClickIndex = "+mCurrentClickIndex + ";index = "+index+";addition = "+(addition!=null?addition.text:"null"));

        if (index != -1 && mCurrentClickIndex != index) {
            if(addition != null) {
                int focusindex = -1;
                if(addition.enable) {
                    if (mListener != null) {
                        mListener.onSelect(index, mLayer, addition);
                    }
                    showBalloon(addition, mBalloonLeft, mBalloonTop);
                    focusindex = index;
                }
                setFocus(focusindex);
            }
        }
        mCurrentClickIndex = index;
    }

    private void showPopup(PopupWindow popupWindow,int x,int y){
        if(popupWindow != null && mPopupBar != null) {
            try {
                if(popupWindow.isShowing()){
                    mPopupBar.update();
                    popupWindow.update(x, y, -1, mPopupBar.getSubHeight(), true);
                }else {
                    popupWindow.setHeight(mPopupBar.getSubHeight());
                    popupWindow.showAtLocation(((Activity) this.getContext()).getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void dismissPopup(PopupWindow popupWindow){
        if(mPopupBar != null) {
            if (mPopupBar.isPopupShow()) {
                mPopupBar.dismissPopup();
            }
            mPopupBar.setTouchState(TouchState.UP);
            mPopupBar.clearLetterFocus();
        }
        mCurrentPopupIndex = -1;
        if(popupWindow != null) {
            if(mBalloonView != null) {
                mBalloonView.clear(mLayer + 1);
            }
            if(popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }
    }

    private void dismissPopup(){
        dismissPopup(getPopup(-1));
        mPopupBar.release();
        mPopup = null;
        mPopupBar = null;
    }

    public boolean isPopupShow(){
        PopupWindow popupWindow = getPopup(-1);
        if(popupWindow != null){
            return popupWindow.isShowing();
        }
        return false;
    }

    public void setSubCanGetFocus(boolean canGetFocus){
        subCanGetFocus = canGetFocus;
    }

    public void clearLetterFocus(){
        mFocusIndex = -1;
    }

    public void setFocus(int index,int color){
        Letter letter = getLetter(index);
        letter.setFocusColor(color);
        setFocus(index);
    }

    public void setFocus(int index){
        boolean changed = false;
        if(mFocusIndex != index){
            changed = true;
        }
        mFocusIndex = index;
        if(changed) {
            update();
        }
    }

    public void setBalloonGravity(int gravity){
        mBalloonGravity = gravity;
    }

    public void setBalloonLayerColor(int... colors){
        mBalloonLayerColors = colors;
    }

    public void setBalloonFocusColor(int... colors){
        mBalloonFocusColors = colors;
    }

    private PopupWindow getBalloon(){
        if(mBalloon == null && !isSub){
            mBalloon = new PopupWindow();
            mBalloon.setAnimationStyle(0);
            mBalloon.setTouchable(false);
            mBalloonView = new BalloonTextView(getContext());
            mBalloonView.requestColorFocus(true);
            mBalloonView.setDiameter(mBalloonDiameter);
            mBalloonView.setDispersion(mBalloonDispersion);
            mBalloonView.setGap(mBalloonGap);
            mBalloonView.setLayerColor(mBalloonLayerColors);
            mBalloonView.setFocusColor(mBalloonFocusColors);
            mBalloon.setContentView(mBalloonView);
            mBalloon.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            mBalloon.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return mBalloon;
    }

    private void showBalloon(Letter addition,int x,int y){
        Log.d(TAG,"showBalloon : (x,y) = ("+x+","+y+")");
        PopupWindow balloon = getBalloon();
        try {
            if(mBalloonView != null){
                if(addition.type == TYPE_TEXT) {
                    mBalloonView.setBalloon(mLayer, addition.text);
                }else if(addition.type == TYPE_BITMAP){
                    mBalloonView.setBalloon(mLayer, addition.bitmap.mutate());
                }
            }
            if(balloon.isShowing()){
                balloon.update(x,y,-1,-1);
            }else {
                balloon.showAtLocation(((Activity) this.getContext()).getWindow().getDecorView(), mBalloonGravity, x, y);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void dismissBalloon(){
        PopupWindow balloon = getBalloon();
        boolean dismissBallon = true;
        if(mBalloonView != null){
            if(mLayer > 0) {
                mBalloonView.clear(mLayer);
                dismissBallon = false;
            }
        }
        if(dismissBallon) {
            balloon.dismiss();
        }
    }

    private void setBalloon(PopupWindow balloon,BalloonTextView view){
        mBalloon = balloon;
        mBalloonView = view;
    }

    private void setItemHeight(float itemheight){
        mItemHeight = itemheight;
    }

    public float getItemHeight(){
        if(isSub){
            return mItemHeight;
        }else{
            return (getHeight()-getPaddingTop()-getPaddingBottom())/(float)size();
        }
    }

    private int getSubWidth(){
        return mWidth;
    }

    private int getSubHeight(){
        return (int)(mItemHeight * size() + getPaddingTop() + getPaddingBottom());
    }

    private void setWdith(int width){
        mWidth = width;
    }

    private void setIsSub(boolean sub){
        isSub = sub;
    }

    private void setLayer(int layer){
        mLayer = layer;
    }

    private boolean isAutoSize(){
        return letterTextSize == -1;
    }

    public static class Letter{
        public static Letter valueOf(String str){
            Letter l = new Letter();
            l.text = str;
            return l;
        }
        public float x = 0;
        public float y = 0;
        public int width=0;
        public int height=0;

        public int type=0;//类型，0：字符；1：图片
        public String text;
        public int text_color = DEFAULT_COLOR;

        private int focus_color = DEFAULT_COLOR;
        public int text_size;
        public Drawable bitmap;//当type为0时此字段无效
        public int list_index = -1;
        public boolean enable = false;
        private boolean handleColor = false;
        private boolean focus = false;
        private boolean focusUsed = false;
        private boolean focusChanged = false;

        public void setColor(int color){
            handleColor = true;
            text_color = color;
        }

        public void setFocusColor(int color){
            focusUsed = true;
            focus_color = color;
            requestFocus();
        }

        public void requestFocus(){
            focusChanged = !focus;
            focus = true;
        }

        public boolean hasFocus(){
            return focus;
        }

        public boolean focusChanged(){
            if(focusChanged){
                focusChanged = false;
                return true;
            }
            return false;
        }

        public void clearColor(){
            handleColor = false;
        }

        public void clearFocusColor(){
            focusUsed = false;
        }

        public void clearFocus(){
            focusChanged = focus;
            focus = false;
        }

        @Override
        public boolean equals(Object letter) {
            if(text == null || letter == null || !(letter instanceof Letter)) return false;
            return text.equals(((Letter) letter).text);
        }
    }
}
