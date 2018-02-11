package com.android.calculator2.exchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import mst.app.MstActivity;
import mst.widget.MstIndexBar;
import mst.widget.MstIndexBar.Letter;
import mst.widget.MstIndexBar.TouchState;

import com.android.calculator2.R;
import com.android.calculator2.exchange.adapter.SearchCurrencyAdapter;
import com.android.calculator2.exchange.adapter.SelectCurrencyAdapter;
import com.android.calculator2.exchange.bean.CurrencyBean;
import com.android.calculator2.exchange.view.MyListView;
import com.android.calculator2.exchange.view.SideBar;
import com.android.calculator2.exchange.view.SideBar.OnTouchingLetterChangedListener;
import com.android.calculator2.utils.MyDatabase;
import com.android.calculator2.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MstSearchView;
import android.widget.MstSearchView.OnQueryTextListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class SelectCurrencyActivity extends MstActivity implements OnClickListener,MstIndexBar.OnSelectListener, MstIndexBar.OnTouchStateChangedListener {

    private MyDatabase m_database;
    private List<CurrencyBean> m_list;

    private ImageView img_back;
    private ImageView bt_search;
    private EditText my_edit;
    private RelativeLayout all_data_layout;
    private ListView my_listview;
    private TextView mid_sidebar_text;
    private ListView search_listview;
    private TextView text_title;
    private ImageView iv_clear;
    private TextView text_no_seach_data;
    private RelativeLayout search_data_layout;

    private SelectCurrencyAdapter m_adapter;

    private String allCode;
    private int select_position;
    private HashMap<String, String> hexunAllCurrencyMap;

    private boolean is_search_mode = false;
    private SearchCurrencyAdapter search_adapter;
    
    private View headView;
    private List<CurrencyBean> m_head_list;
    private MyListView head_list;
    private SelectCurrencyAdapter head_adatper;
    
    private String thisCode;
    private MstSearchView mSearchView;
    
    
    private MstIndexBar index_bar;
    private LinearLayout titleLayout;
    private TextView title;
    private String star_letter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.select_currency_activity);
        
        setMstContentView(R.layout.select_currency_activity);
        index_bar = (MstIndexBar)findViewById(R.id.index_bar);
        index_bar.deleteLetter(27);//删除#
        star_letter ="★";
        getIntentData();
        initData();
        initView();
        setListener();
        initIndexBar(m_list);
//       View view =  getWindow().getDecorView();
//       setVisibility(View.GONE);
      
    }

    public void getIntentData() {
        Intent i = getIntent();
        if (i != null) {
            allCode = i.getStringExtra("allCode");
            thisCode = i.getStringExtra("thisCode");
            select_position = i.getIntExtra("position", 0);
        }
    }
    
    private void initIndexBar(List array){
        ArrayList<Letter> sub = null;
        String last = "";
        int lastindex = -1;
        int otherindex = -1;
        boolean changed = false;
        for(int p=0;p<array.size();p++){
            CurrencyBean c = (CurrencyBean) array.get(p);
            int namesize = c.getSortLetters().length();
            String firletter = "",secletter = "";
            for(int i=0;i<namesize;i++) {
                if(i == 0) {
                    firletter = c.getSortLetters();
                    changed = !firletter.equals(last);
                    last = firletter;
                }else if(i == 1){
//                    secletter = c.firstLetter.get(1);
                    secletter = c.getSortLetters();
                }
            }
            if(changed){
                if(sub != null && lastindex != -1){
                    //index_bar.setSubList(lastindex,sub);//不显示二次搜索
                }
                if(!"".equals(firletter)) {
                    int index = index_bar.getIndex(firletter);
                    if(index != -1) {
                        lastindex = index;
                        sub = new ArrayList<>();
                    }else{
                        sub = null;
                    }
                    if(index == -1){//其他（#）的索引
                        index = index_bar.size()-1;
                        if(otherindex == -1){
                            otherindex = p;
                        }
                    }
                    //设置第一个字母对应的列表索引
                    Letter letter = index_bar.getLetter(index);
                    if(letter != null){
                        letter.list_index = index == index_bar.size()-1 ? otherindex : p;
                    }
                    if(firletter.equals(star_letter)){
                        index_bar.setEnables(true,0);
                    } else {
                        index_bar.setEnables(true,index);
                    }
                }
            }
            //设置第二个字母的列表索引
            if(sub != null && secletter != "") {
                if(!sub.contains(Letter.valueOf(secletter))) {
                    Letter letter = Letter.valueOf(secletter);
                    letter.enable = true;
                    letter.list_index = p;
                    sub.add(letter);
                }
            }
        }
        if(sub != null && lastindex != -1){
            //index_bar.setSubList(lastindex,sub); //不显示第二搜索
        }

    }


    public void initData() {
        
        m_database = new MyDatabase(this);
        hexunAllCurrencyMap = Utils.getHeXunCurrencyMap(this);
        m_list = new ArrayList<CurrencyBean>();
        m_list .addAll(m_database.getCurrencyList(null, hexunAllCurrencyMap,star_letter));
        m_head_list = m_database.getHeadLis();
        m_adapter = new SelectCurrencyAdapter(this);
        m_adapter.setAllCode(allCode,thisCode);
        search_adapter = new SearchCurrencyAdapter(this);
        search_adapter.setAllCode(allCode,thisCode);
        head_adatper = new SelectCurrencyAdapter(this);
        head_adatper.setIsHeadView(true);
        head_adatper.setAllCode(allCode,thisCode);
    }

    public void initView() {
        
        index_bar.setOnSelectListener(this);
        index_bar.setOnTouchStateChangedListener(this);
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.title);

        
        img_back = (ImageView) findViewById(R.id.img_back);
        bt_search = (ImageView) findViewById(R.id.bt_search);
        my_edit = (EditText) findViewById(R.id.my_edit);
        all_data_layout = (RelativeLayout) findViewById(R.id.all_data_layout);
        my_listview = (ListView) findViewById(R.id.my_listview);
        my_listview.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mid_sidebar_text = (TextView) findViewById(R.id.mid_sidebar_text);
//        sidebar = (SideBar) findViewById(R.id.sidebar);
        search_listview = (ListView) findViewById(R.id.search_listview);
        search_listview.setOverScrollMode(View.OVER_SCROLL_NEVER);
        text_title = (TextView) findViewById(R.id.text_title);
        iv_clear = (ImageView) findViewById(R.id.iv_clear);
        text_no_seach_data = (TextView) findViewById(R.id.text_no_seach_data);
        search_data_layout = (RelativeLayout) findViewById(R.id.search_data_layout);

        my_listview.setAdapter(m_adapter);
//        sidebar.setDialogTextView(mid_sidebar_text);
        
        headView = LayoutInflater.from(this).inflate(R.layout.select_currency_headview, null);
        head_list = (MyListView)headView.findViewById(R.id.head_list);
        head_list.setAdapter(head_adatper);
        head_adatper.updateList(m_head_list);
        //my_listview.addHeaderView(headView);
        
        search_listview.setAdapter(search_adapter);

//        sidebar.updateCataLogList(m_database.getSortLetterMap());
        m_adapter.updateList(m_list);

        img_back.setOnClickListener(this);
        bt_search.setOnClickListener(this);
        iv_clear.setOnClickListener(this);

    }
    
    private int lastFirstVisibleItem = -1;

    private void setListener() {
//        sidebar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
//
//            @Override
//            public void onTouchingLetterChanged(String s) {
//                int position = m_adapter.getPositionForSection(s.charAt(0));
//                position = position + my_listview.getHeaderViewsCount();
//                if (position >= 0) {
//                    my_listview.setSelection(position);
//                }
//            }
//        });

        my_listview.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int arg1) {
                switch (arg1) {
                    case OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:// 滚动状态
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动
                        HideKeyboard(my_listview);
                        break;
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int first = firstVisibleItem - my_listview.getHeaderViewsCount();
                int section = m_adapter.getSectionForPosition(first);
//                if (sidebar.getDialogTextView().isShown()) {
//                } else {
//                    if (section > 0) {
//                        sidebar.setSelction(section);
//                    }
//                }
                
                CurrencyBean city = (CurrencyBean) m_adapter.getItem(firstVisibleItem);
                if(city != null) {
                    int index = -1;
                    if (city.getSortLetters() != null && city.getSortLetters().length()> 0) {
                        String fir = city.getSortLetters().substring(0, 1);
                        index = index_bar.getIndex(fir);
                    } else {
//                        String fir = contact.name;//这里貌似有点问题
                        String fir = city.getSortLetters();
                        index = index_bar.getIndex(fir);
                    }

                    if(index == -1){
                        index = index_bar.size() - 1;
                    }
                    
                    if(star_letter.charAt(0) ==section ){
                        index_bar.setFocus(0);
                    } else {
                        index_bar.setFocus(index);
                    }

                }


                int nextSection = m_adapter.getSectionForPosition(first + 1);
                int nextSecPosition = m_adapter.getPositionForSection(nextSection);

                if (first != lastFirstVisibleItem) {
                    MarginLayoutParams params = (MarginLayoutParams) titleLayout.getLayoutParams();
                    params.topMargin = 0;
                    titleLayout.setLayoutParams(params);
                    if (String.valueOf((char) section) != null) {
                        title.setText(String.valueOf((char) section));
                        
                        if(String.valueOf((char) section).equals(star_letter)){
                            title.setText(getString(R.string.str_fav_currency));
                            title.setTextColor(getColor(R.color.currency_fav_head));
                            title.setTextSize(10);
                        } else {
                            title.setText(String.valueOf((char) section));
                            title.setTextColor(getColor(R.color.currency_head));
                            title.setTextSize(16);
                        }
                        
                    }
                }
                if (nextSecPosition == first + 1) {
                    View childView = view.getChildAt(0);
                    if (childView != null) {
                        int titleHeight = titleLayout.getHeight();
                        int bottom = childView.getBottom();
                        MarginLayoutParams params = (MarginLayoutParams) titleLayout.getLayoutParams();
                        if (bottom < titleHeight) {
                            float pushedDistance = bottom - titleHeight;
                            params.topMargin = (int) pushedDistance;
                            titleLayout.setLayoutParams(params);

                        } else {
                            if (params.topMargin != 0) {
                                params.topMargin = 0;
                                titleLayout.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = first;

            }
        });

        my_listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CurrencyBean> list = m_adapter.getList();
                CurrencyBean select_bean = list.get(position-my_listview.getHeaderViewsCount());
                String currency_code = select_bean.currency_code;
                String currency_ch = select_bean.currency_ch;
                String currency_en = select_bean.currency_en;
                int flag_id = select_bean.flag_id;

                Intent i = new Intent();
                i.putExtra("currency_code", currency_code);
                i.putExtra("currency_ch", currency_ch);
                i.putExtra("currency_en", currency_en);
                i.putExtra("flag_id", flag_id);
                i.putExtra("position", select_position);
                setResult(Activity.RESULT_OK, i);
                finish();

            }
        });
        search_listview.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CurrencyBean> list = search_adapter.getList();
                CurrencyBean select_bean = list.get(position);
                String currency_code = select_bean.currency_code;
                String currency_ch = select_bean.currency_ch;
                String currency_en = select_bean.currency_en;
                int flag_id = select_bean.flag_id;
                
                Intent i = new Intent();
                i.putExtra("currency_code", currency_code);
                i.putExtra("currency_ch", currency_ch);
                i.putExtra("currency_en", currency_en);
                i.putExtra("flag_id", flag_id);
                i.putExtra("position", select_position);
                setResult(Activity.RESULT_OK, i);
                finish();
                
            }
        });
        head_list.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<CurrencyBean> list = head_adatper.getList();
                CurrencyBean select_bean = list.get(position);
                String currency_code = select_bean.currency_code;
                String currency_ch = select_bean.currency_ch;
                int flag_id = select_bean.flag_id;
                
                Intent i = new Intent();
                i.putExtra("currency_code", currency_code);
                i.putExtra("currency_ch", currency_ch);
                i.putExtra("flag_id", flag_id);
                i.putExtra("position", select_position);
                setResult(Activity.RESULT_OK, i);
                finish();
                
            }
        });
        
        my_edit.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString().trim();
                if(key.length()>0){
                    iv_clear.setVisibility(View.VISIBLE);
                    all_data_layout.setVisibility(View.GONE);
                    search_data_layout.setVisibility(View.VISIBLE);
                    List<CurrencyBean> list = m_database.getCurrencyList(key, hexunAllCurrencyMap,star_letter);
                    search_adapter.updateList(list);
                    if(list.size() == 0){
                        text_no_seach_data.setVisibility(View.VISIBLE);
                    } else {
                        text_no_seach_data.setVisibility(View.GONE);
                    }
                } else {
                    iv_clear.setVisibility(View.GONE);
                    all_data_layout.setVisibility(View.VISIBLE);
                    search_data_layout.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                
            }
        });

        index_bar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HideKeyboard(index_bar);
                return false;
            }
        });

        search_listview.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {


                switch (arg1) {
                    case OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:// 滚动状态
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动
                        HideKeyboard(search_listview);
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {


            }
        });




    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.img_back:
            ClickTextBack();
            break;
        case R.id.bt_search:
            btSearchClick();
            break;
        case R.id.iv_clear:
            my_edit.setText("");
            break;
        }
    }

    private void ClickTextBack() {
        if (is_search_mode) {
            iv_clear.setVisibility(View.GONE);
            my_edit.setVisibility(View.GONE);
            bt_search.setVisibility(View.VISIBLE);
            text_title.setVisibility(View.VISIBLE);
            all_data_layout.setVisibility(View.VISIBLE);
            search_data_layout.setVisibility(View.GONE);
            is_search_mode = false;
            Utils.hideKeyBoard(my_edit);
        } else {
            finish();
        }
    }
    
    private void btSearchClick(){
        iv_clear.setVisibility(View.GONE);
        my_edit.setVisibility(View.VISIBLE);
        bt_search.setVisibility(View.GONE);
        text_title.setVisibility(View.GONE);
        my_edit.setText("");
        is_search_mode = true;
        my_edit.requestFocus();
        Utils.showKeyBoard(my_edit);
    }
    
    @Override
    public void onNavigationClicked(View view) {
        //在这里处理Toolbar上的返回按钮的点击事件
        onBackPressed();
    }

    @Override
    protected void initialUI(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.initialUI(savedInstanceState);
        inflateToolbarMenu(R.menu.toolbar_menu);
        
        MenuItem searchMenu = getToolbar().getMenu().findItem(R.id.menu_search);
        mSearchView = (MstSearchView) MenuItemCompat.getActionView(searchMenu);
        
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getString(R.string.str_search_hint));
        mSearchView.needHintIcon(false);
        
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            
            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return false;
            }
            
            @Override
            public boolean onQueryTextChange(String arg0) {
                
                String key =arg0.toString().trim();
                if(key.length()>0){
                    iv_clear.setVisibility(View.VISIBLE);
                    all_data_layout.setVisibility(View.GONE);
                    search_data_layout.setVisibility(View.VISIBLE);
                    List<CurrencyBean> list = m_database.getCurrencyList(key, hexunAllCurrencyMap,star_letter);
                    search_adapter.updateList(list);
                    if(list.size() == 0){
                        text_no_seach_data.setVisibility(View.VISIBLE);
                    } else {
                        text_no_seach_data.setVisibility(View.GONE);
                    }
                } else {
                    iv_clear.setVisibility(View.GONE);
                    all_data_layout.setVisibility(View.VISIBLE);
                    search_data_layout.setVisibility(View.GONE);
                }

                
                return false;
            }
        });

        
    }
    
    @Override
    public void onBackPressed() {
        ClickTextBack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        return super.onCreateOptionsMenu(menu);
        
    }

    @Override
    public void onStateChanged(TouchState arg0, TouchState arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSelect(int arg0, int arg1, Letter letter) {
        
        Log.i("zouxu", "arg0 = "+arg0+",arg1="+arg1);
        int position;
        if(arg0 == 0 ){
            position = 0;
        } else {
            position = m_adapter.getPositionForSection(letter.text.charAt(0));
        }

        
      
      
      position = position + my_listview.getHeaderViewsCount();
      if (position >= 0) {
          my_listview.setSelection(position);
      }
        
    }


    public void HideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            if(mSearchView !=null){
                mSearchView.clearFocus();
            }
        }
    }

}
