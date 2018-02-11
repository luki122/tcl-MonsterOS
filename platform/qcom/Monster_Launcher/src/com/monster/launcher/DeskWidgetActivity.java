package com.monster.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * 创建于 cailiuzuo on 16-9-12 上午11:16.
 * 作者
 */
public class DeskWidgetActivity extends Activity implements View.OnClickListener, LauncherAppState.WallpaperChameleon {



    private RelativeLayout mRelativeLayout;
    private RelativeLayout mActionBar;
    private DeskWidgetEditTextView mEditText;
    private WidgetPagedView mLinearLayout;
    private static final int NUM_ROW = 1;
    private static final int NUM_COLUMNS = 3;
    private static final int TEXTSIZE = 12;
    private static final int DEFINE_TEXT_INDEX = 0;
    private int mCount=1;
    private boolean mNotSetTextInEdit;
    private Toast mToast;

    private String mTextTime;

    private static SharedPreferences mPreferences;



    protected ArrayList<TextView> mTextViews;
    private TextView mChoseTextView;
    private TextView mDayTextView;
    private TextView mMonthTextView;
    private TextView mYearTextView;
    private ImageView mConfirmView;
    private ImageView mBackView;
    private ImageView mCross;
    private LinearLayout mContainer;
    private RelativeLayout mcontent;
    private ImageButton mImageButton;
    protected PageIndicator mPageIndicator;

    private boolean isDeletetMode;

    private boolean isNotFirst;
    private String tempString;
    private String mEditString;
    private boolean isEditMode;
    protected String[] mDefaultText;
    private ArrayList<String> mDefaultTextArray;
    private static final String USER_TEXT="USER_DEFINED";
    private static final String USER_DTAE="USER_DEFINED_TIME";
    private int mMaterialExpandDuration;
    private int mExpandDuration;
    private int mMaterialExpandStagger;
    Iterator<String> mIterator;
    private int mTextColor;
    public static final String ACTION_DESKWIDGET_CUSTOM_TEXT =
            "com.android.launcher.action.ACTION_DESK_WIDGET_CUSTOM_TEXT";
    public static final String ACTION_DESKWIDGET_COLOR_CHANGE =
            "com.android.launcher.action.ACTION_DESK_WIDGET_COLOR_CHANGE";
    public static final String mKey = "text";
    private static final int  FORMAT_SPLIT_NUM = 6;
    public static final String SEPARATOR = " ";
    public static final String SEPARATOR_TEXT = "-";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_desk_widget);
        setupTransparentSystemBarsForLollipop();
        init();
        LauncherAppState app = LauncherAppState.getInstance();
        app.addWallpaperChameleon(this);
        //mEditText.setEnabled(false);
        mEditText.setFocusable(false);
        //startAnimation();
        //setLayoutParams();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    private void init() {
        Resources res = getResources();
        mTextViews = new ArrayList<>();
        mDefaultTextArray =new ArrayList<>();
        mRelativeLayout = (RelativeLayout) findViewById(R.id.desk_activity_container);
        mEditText = (DeskWidgetEditTextView) findViewById(R.id.appwidget_text);
        mContainer= (LinearLayout) findViewById(R.id.desk_widget_content);
        mLinearLayout = (WidgetPagedView) findViewById(R.id.desk_view);
        mcontent = (RelativeLayout) findViewById(R.id.desk_view_container);
        mChoseTextView = (TextView) findViewById(R.id.desk_chose);
        mConfirmView = (ImageView) findViewById(R.id.desk_confirm);
        mBackView = (ImageView) findViewById(R.id.desk_widget_activity_back);
        mDayTextView = (TextView) findViewById(R.id.appwidget_day);
        mMonthTextView = (TextView) findViewById(R.id.appwidget_month);
        mYearTextView = (TextView) findViewById(R.id.appwidget_year);
        mCross = (ImageView) findViewById(R.id.widget_cross_activity);
        mActionBar= (RelativeLayout) findViewById(R.id.desk_container);
        mImageButton = (ImageButton) findViewById(R.id.desk_widget_button);
        mPageIndicator = (PageIndicatorCircle) findViewById(R.id.widget_page_indicator);
        mPageIndicator.setpagedView(mLinearLayout);
        mLinearLayout.setIndicator(mPageIndicator);
        /*mChoseTextView.setOnClickListener(this);*/
        mImageButton.setOnClickListener(this);
        mBackView.setOnClickListener(this);
        mConfirmView.setOnClickListener(this);
        mRelativeLayout.setOnClickListener(this);
        mEditText.setOnClickListener(this);
        mDefaultText =res.getStringArray(R.array.activity_widget_default_text);
//        mMonthTextView.setTypeface(Typeface.createFromAsset(getAssets(),"AdobeHebrew-Regular001.ttf"));
//        mDayTextView.setTypeface(Typeface.createFromAsset(getAssets(),"AdobeHebrew-Regular001.ttf"));
//        mYearTextView.setTypeface(Typeface.createFromAsset(getAssets(),"AdobeHebrew-Regular001.ttf"));
        mExpandDuration = res.getInteger(R.integer.config_folderExpandDuration);
        mMaterialExpandDuration = (int) (res.getInteger(R.integer.config_materialFolderExpandDuration)*0.90f);
        mMaterialExpandStagger = res.getInteger(R.integer.config_materialFolderExpandStagger);
        for(String text:mDefaultText){
            mDefaultTextArray.add(text);
        }
        mIterator = mDefaultTextArray.iterator();
        //mRelativeLayout.setBackground(new BitmapDrawable(getWallpaperBitmap()));
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int lineCount = mEditText.getLineCount();
                if (before != count&&lineCount<3&&mCount==2&& !mNotSetTextInEdit) {
                    int startSelection = mEditText.getSelectionStart();
                    mEditText.setText(s);
                    mEditText.setSelection(startSelection);
                    mNotSetTextInEdit = true;
                }else if(lineCount==1){
                    mNotSetTextInEdit =false;
                }
                mCount = lineCount;
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("liuzuo911","afterTextChanged ="+s.toString()+"size="+s.toString().length());
                String string = s.toString();
                if(mEditText.getLineCount()>2){
                    //      String substring = string.substring(0, 14);
                    mEditText.setText(mEditString);
                    mEditText.setSelection(mEditString.length());
                    showToast();
                }else {
                    mEditString = string;
                }
            }
        });
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d("liuzuo56","onEditorAction="+actionId);
                if(EditorInfo.IME_NULL==actionId&&mEditText!=null&&mEditText.getLineCount()>1){
                    mConfirmView.callOnClick();
                    return true;
                }
                return false;
            }
        });
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String stringExtra = null;
        if(extras!=null) {
            stringExtra = extras.getString(mKey);
        }else {
            stringExtra = new Date().toString();
        }
        String[] strings = stringExtra.split(SEPARATOR);
        mDayTextView.setText(strings[2]);
        String month = strings[1].toUpperCase();
        mMonthTextView.setText(month);
        mYearTextView.setText(strings[5]);
        if(stringExtra.length()>FORMAT_SPLIT_NUM) {
            String text=null;
            for(int i=0;i<strings.length;i++){
                if(i==FORMAT_SPLIT_NUM){
                    text=strings[FORMAT_SPLIT_NUM];
                }else if(i>FORMAT_SPLIT_NUM){
                    text+=SEPARATOR+strings[i];
                }
            }
            mEditText.setText(text);
        }
        String time = getPreferences(this).getString(USER_DTAE, null);
        mEditText.setTag(time);
        for (int i =0;i<NUM_ROW;i++) {
            getLinearLayout();
        }
        mLinearLayout.arrangeChildren(mTextViews,mTextViews.size());
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        Log.d("liuzuo182","params="+params.height);

        //mRelativeLayout.setTranslationY(WindowGlobalValue.getStatusbarHeight());
        firstColorSet();
    }

    private void setLayoutParams() {
        int containerHeight = mContainer.getHeight();
        int paddingTop = mContainer.getPaddingTop();
        int paddingBottom = mContainer.getPaddingBottom();
        int mYearTextViewHeight = mYearTextView.getHeight();
        int mDayTextViewHeight = mDayTextView.getHeight();
        int mMonthTextViewHeight = mMonthTextView.getHeight();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mEditText.getLayoutParams();
        int topMargin = containerHeight-paddingTop-paddingBottom-mYearTextViewHeight-mDayTextViewHeight-mMonthTextViewHeight;
        Log.d("liuzuo123","topMargin="+topMargin+" containerHeight="+containerHeight);
        //layoutParams.topMargin = 0;
    }

    private LinearLayout getLinearLayout(){

//        LinearLayout layout= new LinearLayout(this);
//        layout.setOrientation(LinearLayout.HORIZONTAL);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0);
//        layoutParams.height= LinearLayout.LayoutParams.WRAP_CONTENT;
//        layoutParams.width= 0;
//        layoutParams.weight=1;
//        layout.setLayoutParams(layoutParams);
        for (int i =0;i<mDefaultText.length;i++){
            //layout.addView(getTextView(i));
            getTextView(i);
        }
        loadUserText();
        return null;
    }

    private void loadUserText() {
        Set<String> stringSet = getPreferences(this).getStringSet(USER_TEXT, null);
        if (stringSet != null) {
            TreeSet<String> treeSet = new TreeSet<String>(new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    if (lhs != null && rhs != null && lhs.contains(SEPARATOR_TEXT) && rhs.contains(SEPARATOR_TEXT)) {
                        String[] lastStrings = lhs.split(SEPARATOR_TEXT);
                        String[] oldStrings = rhs.split(SEPARATOR_TEXT);
                        if (lastStrings != null && oldStrings != null) {
                            Log.d("liuzuo83", "compare" + (Long.parseLong(lastStrings[0]) - Long.parseLong(oldStrings[0])));
                            return Long.parseLong(lastStrings[0]) - Long.parseLong(oldStrings[0]) > 0 ? -1 : 1;
                        }
                    }
                    return -1;
                }
            });
            Iterator<String> iterator = stringSet.iterator();

            while (iterator.hasNext()) {
                String next = iterator.next();
                treeSet.add(next);
            }
            Log.d("liuzuo83", "treeSet=" + treeSet.toString());
            if (treeSet.size() > 0) {
                Iterator<String> stringIterator = treeSet.iterator();
                while (stringIterator.hasNext()) {
                    String next = stringIterator.next();
                    if (next != null && next.contains(SEPARATOR_TEXT)) {
                        //String text = new String();
                        String[] texts = next.split(SEPARATOR_TEXT);

                        String text=next.substring(texts[0].length()+1,next.length());
                       /* for (int i = 0; i < texts.length; i++) {
                            if (i != 0) {
                                text += texts[i];
                            }
                        }*/
                        Log.d("liuzuo83","createNewTextView ="+text);
                        createNewTextView(text, texts[0]);

                    }
                }
            }

        }
        transitionText();
    }

    private TextView getTextView(int i){
        final TextView view= new WidgetTextView(this);
        int lineSpacing = getResources().getDimensionPixelSize(R.dimen.widget_text_lineSpacing);
        int padding = getResources().getDimensionPixelSize(R.dimen.widget_text_padding);
        view.setTextSize(TEXTSIZE);
        view.setLines(2);
        String text=null;
        if(mIterator.hasNext()){
            text=mIterator.next();
        }else {
            text = mDefaultText[i];
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.height= LinearLayout.LayoutParams.WRAP_CONTENT;
//        layoutParams.width= 0;
        layoutParams.weight=1;
        view.setLayoutParams(layoutParams);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0,padding,0,padding);
        view.setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));
        view.setTypeface(Typeface.create("monster-normal",Typeface.NORMAL));
        view.setBackground(getResources().getDrawable(R.drawable.desk_widget_text_view_stroke));
      /*  int width=getResources().getDimensionPixelSize(R.dimen.widget_text_view_stroke_width);
        if(mTextViews.size()<3)
        view.setTranslationY(width);
        if(!(mTextViews.size()==1||mTextViews.size()==4)){
            if(mTextViews.size()%3==0){
                view.setTranslationX(width+0.5f);
            }else {
                view.setTranslationX(-width-0.5f);
            }
        }*/

        if(i==0&&mTextViews.size()==0){
            //view..setLines(1);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isDeletetMode) {
                        isNotFirst =false;
                        setTextTimeFromTag(view);
                        enterEditText();
                        for (TextView textView : mTextViews)
                            textView.setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));
                        int color = getResources().getColor(R.color.desk_widget_text_color_check);
                        changeTextDrawableColorFilter(color);

                        TextView textView = (TextView) v;
                        textView.setTextColor(getResources().getColor(R.color.desk_widget_text_color_check));
                        //changeTextView(v);
                    }
                }
            });
            view.setLineSpacing(lineSpacing*1.2f,1);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_widget_add);
            if(drawable!=null)
            drawable.setColorFilter(getResources().getColor(R.color.desk_widget_text_color_default),PorterDuff.Mode.SRC_IN);
            view.setCompoundDrawablesWithIntrinsicBounds(null,drawable,null,null);
            view.setCompoundDrawablePadding(-getResources().getDimensionPixelSize(R.dimen.widget_cross_padding));
        }else {
            view.setLineSpacing(lineSpacing,1);


            view.setOnClickListener(this);
        }
        view.setText(text);

        if(text!=null&&text.equals(mEditString))
            view.setTextColor(getResources().getColor(R.color.desk_widget_text_color_check));


        if(mTextViews!=null){
            mTextViews.add(view);
        }
        return view;
    }

    private void changeTextDrawableColorFilter(int color) {
        Log.d("liuzuo126","changeTextDrawableColorFilter   color="+color);
        if(mTextViews!=null&&mTextViews.size()>0){
            TextView textView = mTextViews.get(DEFINE_TEXT_INDEX);
            Drawable[] drawables = textView.getCompoundDrawables();
            Drawable drawable = drawables[1];
            if(drawable!=null)
                drawable.setColorFilter(color,PorterDuff.Mode.SRC_IN);
        }
    }

    private void transitionText() {

          int width=getResources().getDimensionPixelSize(R.dimen.widget_text_view_stroke_width);
          int gridCountX=mLinearLayout.getGridCountX();
          int gridCountY=mLinearLayout.getGridCountY();
          int itemsPerPage = mLinearLayout.getMaxItemsPerPage();
        if (mTextViews != null) {
            for (int i = 0; i < mTextViews.size(); i++) {
                TextView view = mTextViews.get(i);
                int position = i % itemsPerPage;
               /* if (position < gridCountX) {
                    view.setTranslationY(width);
                } else if (position > gridCountX * 2-1 ) {
                    view.setTranslationY(-width);
                }*/

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                layoutParams.topMargin=0;
                view.setTranslationX(0);
                if (position > gridCountX-1) {
                    //view.setTranslationY(-width);
                    layoutParams.topMargin=-width;
                }/*else {
                    if((position == gridCountX - 2 || position == gridCountX * 2 - 2 || position == gridCountX * 3 - 2))
                    view.setTranslationX(0);
                }*/
                /*if(position>gridCountX*2-1) {
                    view.setTranslationY(-width);
                }*/
                if (!(position == gridCountX - 2 || position == gridCountX * 2 - 2 || position == gridCountX * 3 - 2)) {
                    if (position % gridCountX == 0) {
                        Log.d("liuzuo83","left position ="+position);
                        view.setTranslationX(width );
                    } else {
                        Log.d("liuzuo83","right position ="+position);
                        view.setTranslationX(-width );
                    }
                }/*else {
                    if (!(position > gridCountX-1))
                    layoutParams.topMargin=0;
                }*/
                Log.d("liuzuo84","i="+i+"  position="+position+"  setTranslationX="+view.getTranslationX()+" layoutParams.topMargin= "+layoutParams.topMargin);
            }

        }
    }

    @Override
    public void onClick(View v) {
        if(v==mConfirmView){
            changeWidgetText(true,true);
            exitEditText(false);
        }else if(v==mBackView){
            mBackView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            },50);

        }else if(v==mImageButton){
            removeCheckView();
            exitDeleteMode();
        }else if(v==mEditText){
            Log.d("liuzuo53","onclick");
            isNotFirst=true;
            if(!isEditMode)
            enterEditText();
        } else {
        if(v instanceof TextView){
            WidgetTextView textView= (WidgetTextView) v;
            if(isDeletetMode){
                if(textView.isCheckable()){
                    textView.setChecked(!textView.isChecked());
                }
                if(mTextViews!=null){
                    int checkNum = 0;
                    for(TextView view : mTextViews){
                        WidgetTextView textview = (WidgetTextView) view;
                        if(textview.isChecked()){
                            checkNum++;
                        }
                    }
                    if(checkNum==0){
                        exitDeleteMode();
                    }
                }
            }else {
                changeTextView(v);
                setTextTimeFromTag(v);
                if(textView.isCheckable()){
                    changeWidgetText(false,false);
                    mEditText.setEnabled(true);
                }else {
                    //mConfirmView.callOnClick();
                    changeWidgetText(true,false);
                    mEditText.setEnabled(false);
                }
            }
        }else if(v==mRelativeLayout){
            exitEditText(false);
        }
        }
    }

    private void changeWidgetText(boolean save,boolean closed){
        Bundle extras = new Bundle();
        Intent intent = new Intent(ACTION_DESKWIDGET_CUSTOM_TEXT);
        String key = mKey;
        String value = mEditText.getText().toString();
        if (value != null && value.length() > 0) {
            extras.putString(key, value);
            intent.putExtras(extras);
            sendBroadcast(intent);
            if (save&&mDefaultTextArray != null && !mDefaultTextArray.contains(value)) {
                saveTextInSharedPreferences(value);
            }
            int color = getResources().getColor(R.color.desk_widget_text_color_default);
            changeTextDrawableColorFilter(color);

            if(closed) {
                if (mTextViews!=null)
                    mTextViews.clear();
                getLinearLayout();
                mLinearLayout.arrangeChildren(mTextViews, mTextViews.size());
            }
            //finish();

        }else {
        Toast.makeText(this,"Content cannot be empty",Toast.LENGTH_SHORT).show();
    }
    }

    private void removeCheckView() {
        Log.d("liuzuo182","removeCheckView");
        Set<String> stringSet = getPreferences(this).getStringSet(USER_TEXT, null);
        if(mTextViews!=null&&mTextViews.size()!=0){
            for(int i=mTextViews.size()-1;i>=0;i-- ){
               WidgetTextView textView = (WidgetTextView) mTextViews.get(i);
               if(textView.isChecked()){
                   mTextViews.remove(textView);

                if (textView.getTag()!=null) {
                    String time = (String) textView.getTag();

                    String text = time+SEPARATOR_TEXT+ textView.getText();
                   if(stringSet!=null&&stringSet.contains(text)){
                       stringSet.remove(text);
                       Log.d("liuzuo83","remove  text="+text);
                   }
                 }
               }
            }
            getPreferences(this).edit().putStringSet(USER_TEXT,stringSet).apply();
        }
        mLinearLayout.arrangeChildren(mTextViews,mTextViews.size());
        transitionText();
    }

    private void saveTextInSharedPreferences(String value) {
        Set<String> stringSet =  getPreferences(this).getStringSet(USER_TEXT, null);
        if(stringSet==null){
            stringSet=new LinkedHashSet<String>();
        }

        if(getTextTime()!=null){
            String textTime = getTextTime();
            String[] split = textTime.split(SEPARATOR_TEXT);
            stringSet.remove(textTime);
            value = ""+split[0]+SEPARATOR_TEXT+value;
        }else {
            value = ""+System.currentTimeMillis()+SEPARATOR_TEXT+value;
        }
        stringSet.add(value);
     //   for(int i=0;i<10;i++){
     //        stringSet.add(value+i);
     //     }
        getPreferences(this).edit().putStringSet(USER_TEXT,stringSet).apply();
        Log.d("liuzuo83","saveTextInSharedPreferences="+stringSet.toString());
    }

    private TextView createNewTextView(String value, String time) {
        Log.d("liuzuo182","createNewTextView");
        WidgetTextView view= new WidgetTextView(this);
        int lineSpacing = getResources().getDimensionPixelSize(R.dimen.widget_text_lineSpacing);
        int padding = getResources().getDimensionPixelSize(R.dimen.widget_text_padding);
        view.setTextSize(TEXTSIZE);
        String text=value;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.height= LinearLayout.LayoutParams.WRAP_CONTENT;
//        layoutParams.width= 0;
        layoutParams.weight=1;
        view.setLayoutParams(layoutParams);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        view.setLineSpacing(lineSpacing,1);
        view.setPadding(0,padding,0,padding);
        view.setMinLines(2);
        view.setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));
        view.setTypeface(Typeface.create("monster-normal",Typeface.NORMAL));
        view.setBackground(getResources().getDrawable(R.drawable.desk_widget_text_view_stroke));
       /* int width=getResources().getDimensionPixelSize(R.dimen.widget_text_view_stroke_width);
        if(mTextViews.size()<3)
        view.setTranslationY(width);
        if(!(mTextViews.size()==1||mTextViews.size()==4)){
            if(mTextViews.size()%3==0){
                view.setTranslationX(width+0.5f);
            }else {
                view.setTranslationX(-width-0.5f);
            }
        }*/

            view.setLines(2);
            view.setOnClickListener(this);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isDeletetMode){
                    enterDeleteMode();
                    WidgetTextView view = (WidgetTextView) v;
                    view.setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));
                    view.setChecked(true);
                    return true;
                }
                return false;
            }
        });
        view.setCheckable(true);
        view.setChecked(false);
        view.setText(text);
        view.setTag(time);
        Object tag = mEditText.getTag();
        if(tag==null) {
            if (text != null && text.equals(mEditString)) {
                view.setTextColor(getResources().getColor(R.color.desk_widget_text_color_check));
                setTextTimeFromTag(view);
            }
        }else {
            if (text != null && text.equals(mEditString) && tag.equals(time)) {
                view.setTextColor(getResources().getColor(R.color.desk_widget_text_color_check));
                setTextTimeFromTag(view);
            }
        }
        if(mTextViews!=null){
            mTextViews.add(view);
        }
        return view;
    }

    private void enterDeleteMode() {
        buttonOpenAnimation();
        int color = getResources().getColor(R.color.desk_widget_text_color_check_delete);
        for(TextView view:mTextViews){
            WidgetTextView textView = (WidgetTextView) view;
            if(!textView.isCheckable()){
                Log.d("liuzuo85","setTextColor");
                textView.setTextColor(color);

            }
        }
        changeTextDrawableColorFilter(color);
    }
    private void exitDeleteMode() {
        int color = getResources().getColor(R.color.desk_widget_text_color_default);
        if(mImageButton!=null)
        mImageButton.setClickable(false);
        buttonCloseAnimation();
        for(TextView view:mTextViews){
            WidgetTextView textView = (WidgetTextView) view;
            if(textView.isCheckable()&&textView.isChecked()){
                textView.setChecked(false);
            }else if(!textView.isCheckable()){
                textView.setTextColor(color);
            }
        }
        changeTextDrawableColorFilter(color);
    }

    private void buttonOpenAnimation() {
        final int transY=mImageButton.getHeight();
        mImageButton.setTranslationY(transY);
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        PropertyValuesHolder ty = PropertyValuesHolder.ofFloat("translationY", transY, 0);

        Animator drift = ObjectAnimator.ofPropertyValuesHolder(mImageButton, ty);
        drift.setDuration(mMaterialExpandDuration);
        drift.setStartDelay(mMaterialExpandStagger);

        drift.setInterpolator(PathInterpolatorCompat.create(0.4f,0.0f,0.11f,1f));
        drift.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isDeletetMode=true;
                mImageButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mImageButton.setClickable(true);
            }
        });

        PropertyValuesHolder alp = PropertyValuesHolder.ofFloat("alpha", 1f, 0);
        Animator alpha = ObjectAnimator.ofPropertyValuesHolder(mPageIndicator, alp);
        alpha.setDuration(mMaterialExpandDuration);
        //alpha.setStartDelay(mMaterialExpandStagger);

        alpha.setInterpolator(new DecelerateInterpolator());
        anim.play(drift);
        anim.play(alpha);
        anim.start();
    }


    private void buttonCloseAnimation() {
        int transY=mImageButton.getHeight();
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        PropertyValuesHolder ty = PropertyValuesHolder.ofFloat("translationY", 0, transY);

        Animator drift = ObjectAnimator.ofPropertyValuesHolder(mImageButton, ty);
        drift.setDuration(mMaterialExpandDuration);
        drift.setStartDelay(mMaterialExpandStagger);

        drift.setInterpolator(new AccelerateInterpolator());
        drift.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mImageButton.setVisibility(View.INVISIBLE);
                isDeletetMode=false;
            }
        });
        PropertyValuesHolder alp = PropertyValuesHolder.ofFloat("alpha", 0, 1f);
        Animator alpha = ObjectAnimator.ofPropertyValuesHolder(mPageIndicator, alp);
        alpha.setDuration(mMaterialExpandDuration);
        //alpha.setStartDelay(mMaterialExpandStagger);

        alpha.setInterpolator(new DecelerateInterpolator());
        anim.play(drift);
        anim.play(alpha);
        anim.start();
    }
    protected void enterEditText(){
        tempString = mEditText.getText().toString();
       // if(!isEditMode)
        mEditText.setEdit(true);
        if(isNotFirst&&mEditString!=null&&mEditString.length()>0){
            mEditText.setText(mEditString);
        }else {
            mEditText.setText("");
        }
        mEditText.setEnabled(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.setFocusable(true);
        mEditText.setSelection(mEditText.getText().length());
        InputMethodManager service = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        service.showSoftInput(mEditText, 0);

        isEditMode = true;
        mConfirmView.setVisibility(View.VISIBLE);
        mConfirmView.setClickable(true);
    }

    private void exitEditText(boolean result){
        mEditText.setEdit(false);
        if(result||"".equals(mEditText.getText().toString()))
        mEditText.setText(tempString);
        mEditText.setFocusable(false);
        //mEditText.setEnabled(false);
        isEditMode =false;
        if(mTextViews!=null){
            int color = getResources().getColor(R.color.desk_widget_text_color_default);
            mTextViews.get(DEFINE_TEXT_INDEX).setTextColor(color);
            changeTextDrawableColorFilter(color);
        }
        mConfirmView.setVisibility(View.INVISIBLE);
        mConfirmView.setClickable(false);
        InputMethodManager service = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        service.hideSoftInputFromWindow(mEditText.getWindowToken(),0);
    }

    @Override
    public void onBackPressed() {
        if(mToast!=null){
            mToast.cancel();
        }
        if(isEditMode){
            exitEditText(true);
        }
        if(isDeletetMode){
            exitDeleteMode();
            return;
        }
        super.onBackPressed();
    }

/*    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d("liuzuo911","onFocusChange ="+hasFocus);
        if (v == mEditText && hasFocus) {
          //  enterEditText();
        }else {
            exitEditText(false);
        }
    }*/
    private void changeTextView(View view){
        for(int i=0;i<mTextViews.size();i++){
            mTextViews.get(i).setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));
            if(i!=0){
                exitEditText(false);
            }
        }
        Log.d("liuzuo911","mTextViews ="+mTextViews.size());
        /*view.setBackgroundColor(getResources().getColor(R.color.buttonDropTarget_bg_color));*/
        TextView textView= (TextView) view;
        textView.setTextColor(getResources().getColor(R.color.desk_widget_text_color_check));
        mEditText.setText(((TextView) view).getText());
    }
    private void showToast(){
        if(mToast==null) {
            mToast = Toast.makeText(this, getString(R.string.activity_desk_widget_toast), Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    @Override
    public void onWallpaperChameleon(int[] colors) {
        int color = colors[0];
        setTextColor(color);
        setImageColor(color);
        mEditText.setPaintColor();
    }

    private void setTextColor(int color) {
        mChoseTextView.setTextColor(color);
//        mDayTextView.setTextColor(color);
//        mMonthTextView.setTextColor(color);
//        mYearTextView.setTextColor(color);
//        mEditText.setTextColor(color);
        Resources res = getResources();
         boolean isBlackText = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
        if(isBlackText){
            int colorText = res.getColor(R.color.desk_widget_text_color_black);
            mDayTextView.setTextColor(colorText);
            mMonthTextView.setTextColor(colorText);
            mYearTextView.setTextColor(colorText);
            mEditText.setTextColor(colorText);
        }else{
            int colorText = res.getColor(R.color.desk_widget_text_color_white);
            mDayTextView.setTextColor(colorText);
            mMonthTextView.setTextColor(colorText);
            mYearTextView.setTextColor(colorText);
            mEditText.setTextColor(colorText);
        }


    }

    private void setImageColor(int color){
        Drawable mConfirmViewDrawable = mConfirmView.getDrawable();
        Drawable mBackViewDrawable = mBackView.getDrawable();
        mTextColor =LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor();
        boolean isBlackText = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
        if(isBlackText){
           // mDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            mContainer.setBackground(getResources().getDrawable(R.drawable.desk_widget_stroke_black));
            mCross.setBackground(getResources().getDrawable(R.drawable.cross_shaped_black));
        }else{
            mContainer.setBackground(getResources().getDrawable(R.drawable.desk_widget_stroke_white));
            mCross.setBackground(getResources().getDrawable(R.drawable.cross_shaped_white));
            mConfirmViewDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            mBackViewDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
       /* if (Utilities.ATLEAST_JB_MR1) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(mDrawable, null, null, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
        }*/
    }
    private void firstColorSet(){

       int color =LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor();
        setTextColor(color);
        setImageColor(color);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Object tag = mEditText.getTag();
        if(tag!=null)
        getPreferences(this).edit().putString(USER_DTAE, (String) tag).apply();
        LauncherAppState app = LauncherAppState.getInstance();
        app.removeWallpaperChameleon(this);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setupTransparentSystemBarsForLollipop() {
        if (Utilities.ATLEAST_LOLLIPOP) {
            Window window = getWindow();
//            window.getAttributes().systemUiVisibility |=
//                    (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//            if(LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText(true)){
//               window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|0x00000010);
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            }else{
 //               window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
 //                       |0x00000010);
//            }
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);

        }
    }
    private void startAnimation(){
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
       // mcontent.setAlpha(0);

        mContainer.setScaleX(0);
        mContainer.setScaleY(0);
        mContainer.setAlpha(0);
        PropertyValuesHolder alphaContainer = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0f, 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0f, 1.0f);
        Animator containerAlpha=ObjectAnimator.ofPropertyValuesHolder(mContainer, alphaContainer,scaleX,scaleY);
        containerAlpha.setDuration((long) (mMaterialExpandDuration));
        containerAlpha.setStartDelay(mMaterialExpandStagger*2);
        containerAlpha.setInterpolator(new DecelerateInterpolator(1.5f));
        anim.play(containerAlpha);

        mcontent.setVisibility(View.INVISIBLE);
        mActionBar.setVisibility(View.INVISIBLE);
        for(View  view:mTextViews){
             view.setAlpha(0);
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
            Animator animAlpha = ObjectAnimator.ofPropertyValuesHolder(view, alpha);
            animAlpha.setDuration((long) (mMaterialExpandDuration*2.5f));
         //   animAlpha.setStartDelay(mMaterialExpandStagger);
            animAlpha.setInterpolator(new AccelerateInterpolator());
            anim.play(animAlpha);
        }
        mcontent.setTranslationY(getResources().getDimensionPixelSize(R.dimen.widget_container_height)*1.5f);
        PropertyValuesHolder ty = PropertyValuesHolder.ofFloat("translationY", 0);
        Animator drift = ObjectAnimator.ofPropertyValuesHolder(mcontent, ty);
        drift.setDuration((long) (mMaterialExpandDuration*2.5f));
        //drift.setStartDelay(mMaterialExpandStagger);
        drift.setInterpolator(new DecelerateInterpolator());
        anim.play(drift);

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
        Animator animAlpha = ObjectAnimator.ofPropertyValuesHolder(mcontent, alpha);
        animAlpha.setDuration((long) (mMaterialExpandDuration*2.5f));
        //animAlpha.setStartDelay(mMaterialExpandStagger);
        animAlpha.setInterpolator(new AccelerateInterpolator(5.5f));
        anim.play(animAlpha);


        PropertyValuesHolder alpha1 = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
        Animator animAlpha1 = ObjectAnimator.ofPropertyValuesHolder(mActionBar, alpha);
        animAlpha1.setDuration((long) (mMaterialExpandDuration*2.5f));
        //animAlpha.setStartDelay(mMaterialExpandStagger);
        animAlpha1.setInterpolator(new AccelerateInterpolator());
        anim.play(animAlpha1);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mcontent.setVisibility(View.VISIBLE);
                mActionBar.setVisibility(View.VISIBLE);
            }
        });
        anim.start();
    }
    public ArrayList<TextView> getTextViews() {
        return mTextViews;
    }


    private static SharedPreferences getPreferences(Context context){
        if(mPreferences==null){
            mPreferences=context.getSharedPreferences(DeskWidgetActivity.ACTION_DESKWIDGET_CUSTOM_TEXT,Context.MODE_PRIVATE);
        }
        return mPreferences;
    }
    private void setTextTimeFromTag(View view){
        Object tag = view.getTag();
        if(tag!=null&&tag instanceof String){
            setTextTime((String) tag);
        }else {
            setTextTime(null);
        }
    }

    private String getTextTime() {
        return mTextTime;
    }

    private void setTextTime(String textTime) {
        if(textTime!=null) {
            mTextTime = textTime + SEPARATOR_TEXT + mEditString;
        }else {
            mTextTime = textTime;
        }
        mEditText.setTag(textTime);
        Log.d("liuzuo53","mTextTime="+mTextTime);
    }


}
