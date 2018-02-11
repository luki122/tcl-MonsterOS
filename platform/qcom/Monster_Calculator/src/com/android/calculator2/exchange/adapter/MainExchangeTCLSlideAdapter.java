package com.android.calculator2.exchange.adapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import mst.widget.SliderLayout;

import com.android.calculator2.R;
import com.android.calculator2.exchange.SelectCurrencyActivity;
import com.android.calculator2.exchange.bean.MainExchangeBean;
import com.android.calculator2.exchange.bean.RateBean;
import com.android.calculator2.exchange.view.SwipeItemLayout;
import com.android.calculator2.utils.AppConst;
import com.android.calculator2.utils.Utils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainExchangeTCLSlideAdapter extends BaseAdapter {

    public List<MainExchangeBean> data_list = new ArrayList<MainExchangeBean>();
    public LayoutInflater mInflater;
    public Activity m_context;
    private int select_pos = 0;
    public List<RateBean> rate_list  = new ArrayList<RateBean>();
    private String hintData = "100.00";
    private boolean isShowChinese = true;


    public MainExchangeTCLSlideAdapter(Activity context) {
        mInflater = LayoutInflater.from(context);
        m_context = context;
        isShowChinese = context.getResources().getConfiguration().locale.getCountry().equals("CN");
    }
    
    public void setRateList(List<RateBean> list){
        if(list !=null){
            rate_list.clear();
            rate_list.addAll(list);
            updateRate();
        }
    }
    
    public List<MainExchangeBean> getAdapterList(){
        return data_list;
    }

    public void initSelect(int pos){//初始化选择，不检测重复点击
        
        String select_currency_code = data_list.get(pos).currency_code;
        
        for (int i = 0; i < data_list.size(); i++) {
            MainExchangeBean data = data_list.get(i);
            if (pos == i) {
                data.isSelected = true;
                data.exchange_rate =1.0f;
            } else {
                data.isSelected = false;
                data.str_formula = data.str_result = Utils.remainTwoPoint(m_context, data.str_result);;//复位其他的数据
                setRate(select_currency_code,data);
            }
        }
        select_pos = pos;
        notifyDataSetChanged();
    }
    
    public void setRate(String select_currency_code,MainExchangeBean data){
        for(int i=0;i<rate_list.size();i++){
            RateBean rate = rate_list.get(i);
            if(rate.n1.equals(select_currency_code) && rate.n2.equals(data.currency_code)){
                data.exchange_rate = rate.r;
                return;
            } else if(rate.n2.equals(select_currency_code) && rate.n1.equals(data.currency_code)){
                data.exchange_rate = 1.0f/rate.r;
                return;
            }
        }
        data.exchange_rate = 1.0f;//没匹配到就默认为１
        return;
    }
    
    public void setSelect(int pos) {
        if(select_pos == pos){
            return;
        }
        String select_currency_code = data_list.get(pos).currency_code;
        for (int i = 0; i < data_list.size(); i++) {
            MainExchangeBean data = data_list.get(i);
            if (pos == i) {
                data.isSelected = true;
                data.exchange_rate =1.0f;
            } else {
                data.isSelected = false;
                setRate(select_currency_code,data);
            }
            data.str_formula = data.str_result  = Utils.remainTwoPoint(m_context, data.str_result);//复位数据
        }
        select_pos = pos;
        notifyDataSetChanged();
    }
    

    public void setSelctFormula(String formula) {

        MainExchangeBean data = data_list.get(select_pos);
        data.str_formula = formula;
        if (TextUtils.isEmpty(formula)) {
            clearResult();
        }
        notifyDataSetChanged();
    }
    
    public void clearResult(){
        for (int i = 0; i < data_list.size(); i++) {
            MainExchangeBean data = data_list.get(i);
            data.str_formula="";
            data.str_result="";
        }
        notifyDataSetChanged();
    }
    

    public void setSelectResult(String result) {
        MainExchangeBean data = data_list.get(select_pos);
        String point = m_context.getString(R.string.dec_point);
        if(result.startsWith(point)){
            result= result.replace(point, "0"+point);
        }
        boolean is_empty = TextUtils.isEmpty(result);
        if(is_empty){
             clearResult();
             return;
        }
        data.str_result = result;
        BigDecimal bd;
        String op_sub = m_context.getString(R.string.op_sub);

        if(result.contains(op_sub)){
            bd = new BigDecimal(result.replace(op_sub, ""));
            bd = bd.multiply(new BigDecimal(-1));
        } else {
            bd = new BigDecimal(result);
        }
        for (int i = 0; i < data_list.size(); i++) {// 更新其他汇率的数据
            MainExchangeBean other_data = data_list.get(i);
            if (i != select_pos) {
                other_data.str_formula = "";
                if (!is_empty) {
                    Float rete =  other_data.exchange_rate / data.exchange_rate;
                    BigDecimal  result_data = bd.multiply(new BigDecimal(rete)).add(new BigDecimal(0.005));//加0.005 四舍五入
                    other_data.str_result =  Utils.remainTwoPoint(m_context, "" + result_data);
                } else {
                    other_data.str_result = "";
                }
            }
        }

        notifyDataSetChanged();
    }

    public void updaetList(List<MainExchangeBean> list) {
        if (list != null) {
            data_list.clear();
            data_list.addAll(list);
            notifyDataSetChanged();
            updateRate();
        }
    }
    
    public void updateRate(){
        String select_currency_code = data_list.get(select_pos).currency_code;
        for(int i=0;i<data_list.size();i++){
            MainExchangeBean data = data_list.get(i);
            if(i == select_pos){
                data.exchange_rate = 1.0f;
            } else {
                setRate(select_currency_code,data);
            }
        }
    }

    @Override
    public int getCount() {
        return data_list.size();
    }

    @Override
    public Object getItem(int position) {
        return data_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
       final  MainExchangeBean m_data = data_list.get(position);
         ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.tcl_slide_cell_exchange, parent, false);
            holder = new ViewHolder();
            holder.main_ll = (LinearLayout) convertView.findViewById(R.id.main_ll);
            holder.contry_flag = (ImageView) convertView.findViewById(R.id.contry_flag);
            holder.currency_code = (TextView) convertView.findViewById(R.id.currency_short_name);
            holder.result_layout = (LinearLayout) convertView.findViewById(R.id.result_layout);
            holder.text_result = (TextView) convertView.findViewById(R.id.text_result);
            holder.result_cursor_view = convertView.findViewById(R.id.result_cursor_view);
            holder.formula_layout = (LinearLayout) convertView.findViewById(R.id.formula_layout);
            holder.text_formula = (TextView) convertView.findViewById(R.id.text_formula);
            holder.formula_cursor_view = convertView.findViewById(R.id.formula_cursor_view);
            holder.currency_ch = (TextView) convertView.findViewById(R.id.currency_full_name);
            
            holder.main_swipe_change_layout = (LinearLayout)convertView.findViewById(R.id.main_swipe_change_layout);
            holder.change_layout = (RelativeLayout)convertView.findViewById(R.id.change_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder.result_cursor_view.getTag() == null) {// 开启cusor动画
                                                         // 一个view只开启一次动画
            setAnim(holder.result_cursor_view);
            holder.result_cursor_view.setTag("");
        }

        if (holder.formula_cursor_view.getTag() == null) {// 开启cusor动画
                                                          // 一个view只开启一次动画
            setAnim(holder.formula_cursor_view);
            holder.formula_cursor_view.setTag("");
        }

        if (m_data.isSelected) {

            holder.main_ll.setBackgroundColor(m_context.getColor(R.color.currency_select));
            if (!Utils.hasOps(m_context, m_data.str_formula)) {
                holder.formula_layout.setVisibility(View.INVISIBLE);
                holder.result_cursor_view.setVisibility(View.VISIBLE);
            } else {
                holder.formula_layout.setVisibility(View.VISIBLE);
                holder.result_cursor_view.setVisibility(View.INVISIBLE);
            }
            
            holder.text_result.setHint(hintData);
            
            if(Utils.hasOps(m_context, m_data.str_formula) &&!TextUtils.isEmpty(m_data.str_result) ){
                String op_sub = m_context.getString(R.string.op_sub);
                BigDecimal  result_data;
                if(m_data.str_result.contains(op_sub)){
                    result_data = new BigDecimal(m_data.str_result.replace(op_sub, ""));
                    result_data = result_data.multiply(new BigDecimal(-1));
                    
                    String str_result_data = Utils.remainTwoPoint(m_context, "" + result_data);
                    holder.text_result.setText(str_result_data);

                } else {
                    
//                    result_data = new BigDecimal(m_data.str_result);
//                    String str_result_data = Utils.remainTwoPoint(m_context, "" + result_data);
                    result_data = new BigDecimal(Utils.remainTwoPoint(m_context, m_data.str_result));//先保留两位小数后再转换成BigDecimal以免在0.0000000的时候变成0.00E-50这样
                    holder.text_result.setText(result_data.toString());

                }
                
//                String str_result_data = Utils.remainTwoPoint(m_context, "" + result_data);
//                holder.text_result.setText(str_result_data);

            } else {
                holder.text_result.setText(m_data.str_result);
            }
        } else {
            
            holder.result_cursor_view.setVisibility(View.INVISIBLE);

//            main_height = dip2px(m_context,82);
            
            holder.main_ll.setBackgroundColor(m_context.getColor(R.color.currency_not_select));
            holder.formula_layout.setVisibility(View.INVISIBLE);
            
            //处理其非选中项的hint数据
            BigDecimal this_hint_data = new BigDecimal(hintData);
            MainExchangeBean other_data = data_list.get(select_pos);
            Float rete =  m_data.exchange_rate / other_data.exchange_rate;
            BigDecimal  result_data = this_hint_data.multiply(new BigDecimal(rete)).add(new BigDecimal(0.005));//加0.005 四舍五入
            holder.text_result.setHint(Utils.remainTwoPoint(m_context, "" + result_data));
            
            
            if(!TextUtils.isEmpty(m_data.str_result) ){
//                BigDecimal  result_data_new = new BigDecimal(m_data.str_result);
                
                String op_sub = m_context.getString(R.string.op_sub);
                BigDecimal  result_data_new;
                if(m_data.str_result.contains(op_sub)){
                    result_data_new = new BigDecimal(m_data.str_result.replace(op_sub, ""));
//                    result_data_new = result_data.multiply(new BigDecimal(-1));
                    result_data_new = result_data_new.multiply(new BigDecimal(-1));
                } else {
                    result_data_new = new BigDecimal(m_data.str_result);
                }
                
                holder.text_result.setText(Utils.remainTwoPoint(m_context, "" + result_data_new));
            } else {
                holder.text_result.setText(m_data.str_result);
            }
        }
        
        Log.i("zouxu", "m_data.str_result = "+m_data.str_result);
        
//        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) holder.change_layout.getLayoutParams(); //取控件textView当前的布局参数  
//        LinearLayout.LayoutParams main_linearParams =(LinearLayout.LayoutParams) holder.main_ll.getLayoutParams(); //取控件textView当前的布局参数  
//        linearParams.height = main_height;
//        main_linearParams.height = main_height;
//        
//        holder.change_layout.setLayoutParams(linearParams);
//        holder.change_layout.invalidate();
//        holder.change_layout.requestLayout();
//        holder.main_ll.setLayoutParams(main_linearParams);
        

        holder.text_formula.setText(m_data.str_formula);
        
        final SliderLayout  m_view = (SliderLayout)convertView;


        holder.change_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                m_view.close(true);
                Intent i = new Intent();
                i.setClass(m_context, SelectCurrencyActivity.class);
                i.putExtra("allCode", getAllCurrenyCode());
                i.putExtra("thisCode", m_data.currency_code);
                i.putExtra("position", position);
                m_context.startActivityForResult(i, AppConst.CHANGE_CURRENCY_REQUST);
            }
        });


        holder.contry_flag.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                m_view.close(true);
                Intent i = new Intent();
                i.setClass(m_context, SelectCurrencyActivity.class);
                i.putExtra("allCode", getAllCurrenyCode());
                i.putExtra("position", position);
                i.putExtra("thisCode", m_data.currency_code);
                m_context.startActivityForResult(i, AppConst.CHANGE_CURRENCY_REQUST);
            }
        });
        
        
        holder.contry_flag.setImageResource(m_data.flag_id);
        holder.currency_code.setText(m_data.currency_code);
        if(isShowChinese){
            holder.currency_ch.setText(m_data.currency_ch);
        } else {
            holder.currency_ch.setText(m_data.currency_en);
        }

        return convertView;
    }

    class ViewHolder {
        LinearLayout main_ll;
        ImageView contry_flag;
        TextView currency_code;

        LinearLayout result_layout;
        TextView text_result;
        View result_cursor_view;

        LinearLayout formula_layout;
        TextView text_formula;
        View formula_cursor_view;

        TextView currency_ch;
        
        //右滑菜单
        LinearLayout main_swipe_change_layout;
        RelativeLayout change_layout;
        
    }

    private boolean is_anim = true;

    public void cancleAnim(){
        is_anim = false;
    }

    private void setAnim(final View view) {// 从明到暗的变化

        ObjectAnimator anim = ObjectAnimator.ofFloat("zx", "zx", 1.0f, 0.0F).setDuration(1000);
        anim.start();
        anim.setInterpolator(new LinearInterpolator());
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                if (cVal > 0.5) {
                    float alp = (cVal-0.5f)/0.5f; //透明度变化1~0
                    view.setAlpha(alp);
                } else {
                    float alp= 1.0f-2.0f*cVal;//透明度变化 0~1
                    view.setAlpha(alp);
                }
            }

        });
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(is_anim){
                    setAnim(view);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
    }
    
    public String getAllCurrenyCode(){
        String all_code="";
        for(int i =0;i<data_list.size();i++){
            MainExchangeBean m_data = data_list.get(i);
            all_code = all_code+","+m_data.currency_code;
        }
        return all_code;
    }
    
    public void exchangeData(String currency_code,String currency_ch,String currency_en,int flag_id,int this_select_position){
        MainExchangeBean this_data = data_list.get(this_select_position);
        this_data.currency_ch = currency_ch;
        this_data.currency_code = currency_code;
        this_data.flag_id = flag_id;
        this_data.currency_en = currency_en;
        switch (this_select_position) {
        case 0:
            Utils.saveExchangeBean1(m_context, this_data);
            break;
        case 1:
            Utils.saveExchangeBean2(m_context, this_data);
            break;
        case 2:
            Utils.saveExchangeBean3(m_context, this_data);
            break;
        }
        
        MainExchangeBean select_data = data_list.get(select_pos);
        initSelect(select_pos);
        String select_result = select_data.str_result;
        setSelectResult(select_result);
        notifyDataSetChanged();
    }
    
    public void updateRateFromNet(List<RateBean> list){
        if(list !=null && list.size()>0){
            rate_list.clear();
            rate_list.addAll(list);
            updateRate();
            
            MainExchangeBean select_data = data_list.get(select_pos);
            initSelect(select_pos);
            String select_result = select_data.str_result;
            setSelectResult(select_result);
            notifyDataSetChanged();
        }

    }
    
    public static int dip2px(Context context,float dipValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dipValue*scale+0.5f);
   }
}
