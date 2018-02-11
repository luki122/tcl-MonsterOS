package com.android.calculator2.exchange.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.calculator2.R;
import com.android.calculator2.exchange.adapter.SelectCurrencyAdapter.ViewHolder;
import com.android.calculator2.exchange.bean.CurrencyBean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchCurrencyAdapter extends BaseAdapter {

    private List<CurrencyBean> m_list;
    private Context m_context;
    private LayoutInflater inflater;
    private String allCode = "";
    private String thisCode = "";
    private boolean isShowChinese = true;

    public SearchCurrencyAdapter(Context context) {
        m_context = context;
        m_list = new ArrayList<CurrencyBean>();
        inflater = LayoutInflater.from(m_context);
        isShowChinese = context.getResources().getConfiguration().locale.getCountry().equals("CN");
    }

    public void setAllCode(String codes,String code) {
        allCode = codes;
        thisCode = code;
    }

    public void updateList(List<CurrencyBean> list) {
        if (list != null) {
            m_list.clear();
            m_list.addAll(list);
            notifyDataSetChanged();
        }
    }

    public List<CurrencyBean> getList() {
        return m_list;
    }

    @Override
    public int getCount() {
        return m_list.size();
    }

    @Override
    public Object getItem(int position) {
        return m_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        CurrencyBean m_data = m_list.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.cell_search_currency, parent, false);
            viewHolder.RL_list_item = (RelativeLayout) convertView.findViewById(R.id.RL_list_item);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_short_name = (TextView) convertView.findViewById(R.id.tv_short_name);
            viewHolder.iv_is_checked = (ImageView) convertView.findViewById(R.id.iv_is_checked);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(isShowChinese){
            viewHolder.tv_name.setText(m_data.currency_ch);
        } else {
            viewHolder.tv_name.setText(m_data.currency_en);
        }
        viewHolder.tv_short_name.setText(m_data.currency_code);

        if(thisCode.equals(m_data.currency_code)){
            viewHolder.iv_is_checked.setVisibility(View.VISIBLE);
            viewHolder.iv_is_checked.setImageResource(R.drawable.img_select_this);
            viewHolder.tv_name.setTextColor(m_context.getColor(R.color.calculator_green_clolor));
            viewHolder.tv_short_name.setTextColor(m_context.getColor(R.color.calculator_green_clolor));
        } else if (allCode.contains(m_data.currency_code)) {
            viewHolder.iv_is_checked.setVisibility(View.VISIBLE);
            viewHolder.iv_is_checked.setImageResource(R.drawable.img_select);
            viewHolder.tv_name.setTextColor(m_context.getColor(R.color.currency_cell));
            viewHolder.tv_short_name.setTextColor(m_context.getColor(R.color.currency_cell));
        } else {
            viewHolder.iv_is_checked.setVisibility(View.GONE);
            viewHolder.tv_name.setTextColor(m_context.getColor(R.color.currency_cell));
            viewHolder.tv_short_name.setTextColor(m_context.getColor(R.color.currency_cell));
        }

        return convertView;
    }

    class ViewHolder {
        RelativeLayout RL_list_item;
        TextView tv_name;
        TextView tv_short_name;
        ImageView iv_is_checked;
    }

}
