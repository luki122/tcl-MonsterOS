package com.android.calculator2.exchange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import mst.app.MstActivity;
import mst.widget.MstListView;

import com.android.calculator2.CalculatorResult;
import com.android.calculator2.Evaluator;
import com.android.calculator2.KeyMaps;
import com.android.calculator2.R;
import com.android.calculator2.exchange.adapter.MainExchangeTCLSlideAdapter;
import com.android.calculator2.exchange.bean.MainExchangeBean;
import com.android.calculator2.exchange.bean.RateBean;
import com.android.calculator2.exchange.net.MyRateRequest;
import com.android.calculator2.exchange.net.MyRunnaleBase;
import com.android.calculator2.exchange.net.ResponseResultInterface;
import com.android.calculator2.exchange.view.MyListView;
import com.android.calculator2.exchange.view.SwipeListView;
import com.android.calculator2.utils.AppConst;
import com.android.calculator2.utils.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TCLExchangeActivity extends MstActivity implements ResponseResultInterface {

    private MstListView m_list;
    //    private MainExchangeAdapter m_adapter;
    private MainExchangeTCLSlideAdapter m_adapter;
    private List<MainExchangeBean> init_data_list = new ArrayList<MainExchangeBean>();
    // 以下用于计算
    private ExchangeCalculatorResult mResultText;
    private ExchangeEvaluator mEvaluator;
    private ImageButton bt_del;

    private MyRateRequest m_requset;
    private TextView text_update_time;

    private String get_data_from_calculator;

    private List<RateBean> mRateList;
    private View main_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if(ActivityManager.isUserAMonkey()){
//            return;
//        }

        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.black));

//        int data = getWindow().getDecorView().getSystemUiVisibility();
//        getWindow().getDecorView().setSystemUiVisibility(data & 0xffffff0f);//虚拟按键图标设为白色

        main_view = LayoutInflater.from(this).inflate(R.layout.activity_exchange_layout,null);

        setMstContentView(main_view);

        getToolbar().setVisibility(View.GONE);

        initData();
        initView();
        getRateFromNet();
    }

    private void getRateFromNet() {

        if (Utils.isUpdateToday(this)) {
            return;
        }

        new MyRunnaleBase(this, 0, this) {
            @Override
            public Object executeRunnableRequestData() {
                return m_requset.getRateFromNet(TCLExchangeActivity.this);
            }
        };

    }

    private void initData() {
//        m_adapter = new MainExchangeAdapter(this);

        Intent i = getIntent();
        if (i != null) {
            get_data_from_calculator = i.getStringExtra("data");
        }

        m_adapter = new MainExchangeTCLSlideAdapter(this);
        init_data_list.add(Utils.getExchangeBean1(this));
        init_data_list.add(Utils.getExchangeBean2(this));
        init_data_list.add(Utils.getExchangeBean3(this));
        m_adapter.updaetList(init_data_list);

        mRateList = Utils.getLocalRateList(this);
//        mRateList = Utils.getRateListByJson(getRateJson());

        m_adapter.setRateList(mRateList);
        m_adapter.initSelect(0);

        mResultText = new ExchangeCalculatorResult(this, null);
        mResultText.setTextSize(18);
        mEvaluator = new ExchangeEvaluator(this, mResultText);
        mResultText.setEvaluator(mEvaluator);
        m_requset = new MyRateRequest();

        if (!TextUtils.isEmpty(get_data_from_calculator)) {
            if (get_data_from_calculator.contains(getString(R.string.dec_point))) {
                get_data_from_calculator = Utils.remainTwoPoint(this, get_data_from_calculator);
            }
            m_adapter.setSelectResult(get_data_from_calculator);
        }

    }

    private void initView() {
        m_list = (MstListView) findViewById(R.id.my_listview);
        text_update_time = (TextView) findViewById(R.id.text_update_time);
        m_list.setAdapter(m_adapter);
        m_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                m_adapter.setSelect(position);
                MainExchangeBean data = m_adapter.getAdapterList().get(position);
                mEvaluator.clear();
                addChars(data.str_result, false);
            }
        });
        bt_del = (ImageButton) findViewById(R.id.del);
        bt_del.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                mEvaluator.clear();
                m_adapter.clearResult();
                return false;
            }
        });

        String get_update_info = Utils.getRateUpDataInfo(this);
        if (TextUtils.isEmpty(get_update_info)) {
            text_update_time.setVisibility(View.GONE);
        } else {
            text_update_time.setVisibility(View.VISIBLE);
            text_update_time.setText(get_update_info);
        }

        if (!TextUtils.isEmpty(get_data_from_calculator)) {//add zouxu 20160914
            addChars(get_data_from_calculator, false);
        }

    }

    public void onButtonClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_back:
                finish();
                break;
            case R.id.del:
                onDelete();
                break;
            default:
                addExplicitKeyToExpr(id);
                redisplayFormula();
                break;
        }
    }

    private void addExplicitKeyToExpr(int id) {
        if (id == R.id.op_sub) {
            mEvaluator.getExpr().removeTrailingAdditiveOperators();
        }
        addKeyToExpr(id);
    }

    private void addKeyToExpr(int id) {
        if (!mEvaluator.append(id)) {
        }
    }

    private void onDelete() {
        mEvaluator.delete();
        redisplayFormula();
    }

    void redisplayFormula() {
        SpannableStringBuilder formula = mEvaluator.getExpr().toSpannableStringBuilder(this);
        String str_formula = formula.toString();
        m_adapter.setSelctFormula(str_formula);
        if (Utils.hasOps(this, str_formula)) {
            mEvaluator.evaluateAndShowResult();
        } else {
            m_adapter.setSelectResult(str_formula);
        }
    }

    // 计算ExchangeEvaluator的回调
    public void onCancelled() {
        Log.i("zouxu", "onCancelled = !!");

    }

    public void onError(final int errorResourceId) {
        Log.i("zouxu", "onError = " + errorResourceId);

    }

    public void onEvaluate(int initDisplayPrec, int msd, int leastDigPos, String truncatedWholeNumber) {// 计算结果的回调
        mResultText.displayResult(initDisplayPrec, msd, leastDigPos, truncatedWholeNumber);
        Log.i("zouxu", "truncatedWholeNumber = " + truncatedWholeNumber);

//        int currentCharOffset = 10;// mResultText.getCurrentCharOffset();//将结果保留到10位数
//        int maxChars = mResultText.getMaxChars();
//        int lastDisplayedOffset[] = new int[1];
//        String result = mResultText.getFormattedResult(currentCharOffset, maxChars, lastDisplayedOffset, false);
//        KeyMaps.translateResult(result);
//        int expIndex = result.indexOf('E');
//        if (expIndex > 0 && result.indexOf('.') == -1) {
//            SpannableString formattedResult = new SpannableString(result);
//            formattedResult.setSpan(mResultText.mExponentColorSpan, expIndex, result.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            result = formattedResult.toString();
//        }

//        Log.i("zouxu", "result = "+result);

        String op_sub = getString(R.string.op_sub);
//        String getResult = mResultText.getResualt();
        String getResult = mResultText.getEvaluatorResult();//chg zouxu 20160914
        try {
            BigDecimal bd = new BigDecimal(getResult.replace(op_sub, ""));
        } catch (Exception e) {
            //Toast.makeText(this, "计算错误 请稍后重试！getResult ="+getResult, Toast.LENGTH_SHORT).show();
            Log.i("zouxu", "计算错误 getResult = " + getResult);
            return;
        }

        m_adapter.setSelectResult(getResult);
    }

    public void onReevaluate() {
        // Log.i("zouxu","mResultText="+mResultText.getFullText());
    }

    private void addChars(String moreChars, boolean explicit) {// 替换输入(参考计算器粘贴的代码)
        int current = 0;
        int len = moreChars.length();
        boolean lastWasDigit = false;
        View mCurrentButton;
        while (current < len) {
            char c = moreChars.charAt(current);
            int k = KeyMaps.keyForChar(c);
            if (!explicit) {
                int expEnd;
                if (lastWasDigit && current != (expEnd = Evaluator.exponentEnd(moreChars, current))) {
                    // Process scientific notation with 'E' when pasting, in
                    // spite of ambiguity
                    // with base of natural log.
                    // Otherwise the 10^x key is the user's friend.
                    mEvaluator.addExponent(moreChars, current, expEnd);
                    current = expEnd;
                    lastWasDigit = false;
                    continue;
                } else {
                    boolean isDigit = KeyMaps.digVal(k) != KeyMaps.NOT_DIGIT;
                    if (current == 0 && (isDigit || k == R.id.dec_point) && mEvaluator.getExpr().hasTrailingConstant()) {
                        // Refuse to concatenate pasted content to trailing
                        // constant.
                        // This makes pasting of calculator results more
                        // consistent, whether or
                        // not the old calculator instance is still around.
                        addKeyToExpr(R.id.op_mul);
                    }
                    lastWasDigit = (isDigit || lastWasDigit && k == R.id.dec_point);
                }
            }
            if (k != View.NO_ID) {
                mCurrentButton = findViewById(k);
                if (explicit) {
                    addExplicitKeyToExpr(k);
                } else {
                    addKeyToExpr(k);
                }
                if (Character.isSurrogate(c)) {
                    current += 2;
                } else {
                    ++current;
                }
                continue;
            }
            int f = KeyMaps.funForString(moreChars, current);
            if (f != View.NO_ID) {
                mCurrentButton = findViewById(f);
                if (explicit) {
                    addExplicitKeyToExpr(f);
                } else {
                    addKeyToExpr(f);
                }
                if (f == R.id.op_sqrt) {
                    // Square root entered as function; don't lose the
                    // parenthesis.
                    addKeyToExpr(R.id.lparen);
                }
                current = moreChars.indexOf('(', current) + 1;
                continue;
            }
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case AppConst.CHANGE_CURRENCY_REQUST:
                String currency_code = data.getStringExtra("currency_code");
                String currency_ch = data.getStringExtra("currency_ch");
                String currency_en = data.getStringExtra("currency_en");
                int flag_id = data.getIntExtra("flag_id", 0);
                int select_pos = data.getIntExtra("position", 0);
                m_adapter.exchangeData(currency_code, currency_ch, currency_en, flag_id, select_pos);// 更新切换货币
                break;
        }
    }

    @Override
    public void OnResponseResults(int funcid, Object object) {
        Boolean is_success = (Boolean) object;
        if (!is_success) {
            return;
        }

        m_adapter.updateRateFromNet(m_requset.getRateList());
        text_update_time.setVisibility(View.VISIBLE);
        text_update_time.setText(m_requset.getUpdateInfo());

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            m_adapter.cancleAnim();
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        m_adapter.cancleAnim();
        super.onDestroy();
    }

}
