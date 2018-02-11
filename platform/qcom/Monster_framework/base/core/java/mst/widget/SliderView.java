package mst.widget;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mst.internal.R;

import java.lang.reflect.Field;


/**
 *
 */
public class SliderView extends SliderLayout {
    private static final String TAG="SliderView";

    public static final int CUSTOM_BACKGROUND_RIPPLE = -1;

    public static interface OnSliderButtonLickListener{
        public void onSliderButtonClick(int id,View view,ViewGroup parent);
    }
    private OnSliderButtonLickListener mListener;

    private FrameLayout mCustomLayout;
    private LinearLayout mSliderShowLayout;

    private LayoutInflater mInflater;


    public SliderView(Context context) {
        super(context);
        init();
    }

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        saveAttrs(attrs,0,0);
    }

    public SliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        saveAttrs(attrs,defStyleAttr,0);
    }

    private void saveAttrs(AttributeSet attrs, int styleAttr, int styleRes){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SliderView,styleAttr,styleRes);
        mInflater = LayoutInflater.from(getContext());
        final int N = a.getIndexCount();
        for(int i=0;i<N;i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SliderView_layout) {
                int layoutResid = a.getResourceId(attr,0);
                setCustomLayout(layoutResid);
            }
        }
        a.recycle();
    }

    private void init(){
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setDragEdge(DRAG_EDGE_RIGHT);

        try {
            Field mMode = SliderLayout.class.getDeclaredField("mMode");
            mMode.setAccessible(true);
            mMode.set(this,MODE_SAME_LAYER);
            mMode.setAccessible(false);
        } catch (Exception e){
            e.printStackTrace();
        }


        mSliderShowLayout = new LinearLayout(getContext());
        mSliderShowLayout.setOrientation(LinearLayout.HORIZONTAL);
        mSliderShowLayout.setGravity(Gravity.CENTER_VERTICAL);
        mSliderShowLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        addView(mSliderShowLayout);

        mCustomLayout = new FrameLayout(getContext());
//        mCustomLayout.setBackgroundColor(Color.WHITE);
        mCustomLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mCustomLayout);

    }

    public View findCustomViewById(int id){
        return mCustomLayout.findViewById(id);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if(mCustomLayout != null){
            mCustomLayout.setOnClickListener(l);
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l){
        if(mCustomLayout != null){
            mCustomLayout.setOnLongClickListener(l);
            setCancelLongClickView(mCustomLayout);
        }
    }

    public void setCustomBackground(int resid){
        if(mCustomLayout != null){
            if(resid == CUSTOM_BACKGROUND_RIPPLE){
                mCustomLayout.setBackgroundResource(com.mst.R.drawable.item_background_material);
            }else{
                mCustomLayout.setBackgroundResource(resid);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int wmode = MeasureSpec.getMode(widthMeasureSpec);
        int hmode = MeasureSpec.getMode(heightMeasureSpec);

        Log.d(TAG,"onMeasure : width = "+width+"; height = "+height+"; mode = ("+wmode+","+hmode+")"+
                "-----"+
                "UNSPECIFIED = "+MeasureSpec.UNSPECIFIED+
                ";AT_MOST = "+MeasureSpec.AT_MOST+
                ";EXACTLY = "+MeasureSpec.EXACTLY
        );

        if(mCustomLayout.getChildCount() > 0) {
            View custom = mCustomLayout.getChildAt(0);
            LayoutParams clp = getLayoutParams();
            if(clp.width == LayoutParams.WRAP_CONTENT || clp.height == LayoutParams.WRAP_CONTENT) {
                measureChild(custom, widthMeasureSpec, heightMeasureSpec);
                if (clp.width == LayoutParams.WRAP_CONTENT) {
                    mCustomLayout.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(custom.getMeasuredWidth(), MeasureSpec.EXACTLY);
                }
                if (clp.height == LayoutParams.WRAP_CONTENT) {
                    mCustomLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(custom.getMeasuredHeight(), MeasureSpec.EXACTLY);
                }
            }

        }
        LayoutParams lp =  mSliderShowLayout.getLayoutParams();
        if(lp.height != LayoutParams.MATCH_PARENT){
            lp.height = LayoutParams.MATCH_PARENT;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnSliderButtonClickListener(OnSliderButtonLickListener listener){
        mListener = listener;
    }

    public void setCustomLayout(int resourceId){
        View customView = mInflater.inflate(resourceId,mCustomLayout,true);
    }

    public void setCustomLayout(View v){
        mCustomLayout.addView(v,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void addImageButton(final int id,Drawable image){
        View button = mInflater.inflate(R.layout.slider_button,mSliderShowLayout,true);
        ImageView imgButton = (ImageView) button.findViewById(android.R.id.button1);
        imgButton.setImageDrawable(image);

        TextView txtButton = (TextView) button.findViewById(android.R.id.button2);
        txtButton.setVisibility(GONE);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onSliderButtonClick(id,v,SliderView.this);
                }
            }
        });
    }

    public void addTextButton(final int id,String text){
        addTextButton(id,text,-1,null);
    }

    public void addTextButton(final int id,String text,float textSize,int textColor){
        addTextButton(id,text,textSize,ColorStateList.valueOf(textColor));
    }

    public void addTextButton(final int id,String text,float textSize,ColorStateList textColor){
        View button = mInflater.inflate(R.layout.slider_button,mSliderShowLayout,false);
        ImageView imgButton = (ImageView) button.findViewById(android.R.id.button1);
        imgButton.setVisibility(GONE);

        TextView txtButton = (TextView) button.findViewById(android.R.id.button2);
        if(textSize != -1){
            txtButton.setTextSize(textSize);
        }
        if(textColor != null){
            txtButton.setTextColor(textColor);
        }
        txtButton.setText(text);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onSliderButtonClick(id,v,SliderView.this);
                }
            }
        });

        mSliderShowLayout.addView(button);
    }

    public void addButton(final int id,String text,Drawable image){
        addButton(id,text,image,-1,null);
    }

    public void addButton(final int id,String text,Drawable image,float textSize,int textColor){
        addButton(id,text,image,textSize,ColorStateList.valueOf(textColor));
    }

    public void addButton(final int id,String text,Drawable image,float textSize,ColorStateList textColor){
        View button = mInflater.inflate(R.layout.slider_button,mSliderShowLayout,true);
        ImageView imgButton = (ImageView) button.findViewById(android.R.id.button1);
        imgButton.setImageDrawable(image);

        TextView txtButton = (TextView) button.findViewById(android.R.id.button2);
        if(textSize != -1){
            txtButton.setTextSize(textSize);
        }
        if(textColor != null){
            txtButton.setTextColor(textColor);
        }
        txtButton.setText(text);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onSliderButtonClick(id,v,SliderView.this);
                }
            }
        });

    }

    public void addCustomButton(final int id,View v){
        mSliderShowLayout.addView(v);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onSliderButtonClick(id,v,SliderView.this);
                }
            }
        });
    }

    public void addCustomButton(final int id,int layout){
        View v = mInflater.inflate(layout,mSliderShowLayout,false);
        mSliderShowLayout.addView(v);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onSliderButtonClick(id,v,SliderView.this);
                }
            }
        });
    }

    public void setButtonBackground(int index,Drawable drawable){
        View button = mSliderShowLayout.getChildAt(index);
        if(button != null){
            button.setBackground(drawable);
        }
    }

    public void setButtonBackgroundColor(int index,int color){
        View button = mSliderShowLayout.getChildAt(index);
        if(button != null){
            Drawable background = button.getBackground().mutate();
            if(background != null) {
                background.setTint(color);
                button.setBackground(background);
            }else{
                button.setBackgroundColor(color);
            }
        }
    }


}
