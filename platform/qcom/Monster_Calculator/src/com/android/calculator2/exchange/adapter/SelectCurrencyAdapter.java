package com.android.calculator2.exchange.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.calculator2.R;
import com.android.calculator2.exchange.bean.CurrencyBean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SelectCurrencyAdapter extends BaseAdapter implements SectionIndexer {

    private List<CurrencyBean> m_list;
    private Context m_context;
    private LayoutInflater inflater;
    private String allCode="";
    private String thisCode = "";
    
    private boolean isHeadView = false;
    private boolean isShowChinese = true;

    public void setIsHeadView(boolean is){
        isHeadView = is;
    }

    public SelectCurrencyAdapter(Context context) {
        m_context = context;
        m_list = new ArrayList<CurrencyBean>();
        inflater = LayoutInflater.from(m_context);
        isShowChinese = context.getResources().getConfiguration().locale.getCountry().equals("CN");

    }
    public void setAllCode(String codes,String code){
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
            convertView = inflater.inflate(R.layout.cell_select_currency, parent,false);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.RL_list_item = (RelativeLayout) convertView.findViewById(R.id.RL_list_item);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_short_name = (TextView) convertView.findViewById(R.id.tv_short_name);
            viewHolder.iv_is_checked = (ImageView) convertView.findViewById(R.id.iv_is_checked);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
//        viewHolder.tv_name.setText(m_data.currency_ch);

        if(isShowChinese){
            viewHolder.tv_name.setText(m_data.currency_ch);
        } else {
            viewHolder.tv_name.setText(m_data.currency_en);
        }


        viewHolder.tv_short_name.setText(m_data.currency_code);
        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (!isHeadView && (position == 0 || position == getPositionForSection(section))) {
            viewHolder.tv_title.setVisibility(View.VISIBLE);
            if(m_data.getSortLetters().equals("★")){
                viewHolder.tv_title.setText(m_context.getString(R.string.str_fav_currency));
                viewHolder.tv_title.setTextColor(m_context.getColor(R.color.currency_fav_head));
                viewHolder.tv_title.setTextSize(10);
            } else {
                viewHolder.tv_title.setText(m_data.getSortLetters());
                viewHolder.tv_title.setTextColor(m_context.getColor(R.color.currency_head));
                viewHolder.tv_title.setTextSize(16);

            }
        } else {
            viewHolder.tv_title.setVisibility(View.GONE);
        }
        if(thisCode.equals(m_data.currency_code)){
            viewHolder.iv_is_checked.setVisibility(View.VISIBLE);
            viewHolder.iv_is_checked.setImageResource(R.drawable.img_select_this);
            viewHolder.tv_name.setTextColor(m_context.getColor(R.color.calculator_green_clolor));
            viewHolder.tv_short_name.setTextColor(m_context.getColor(R.color.calculator_green_clolor));
        } else if(allCode.contains(m_data.currency_code)){
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

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int section) {//根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
        for (int i = 0; i < getCount(); i++) {
            String sortStr = m_list.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {//根据ListView的当前位置获取分类的首字母的Char ascii值
        if (m_list.size() <= 0) {
            return -1;
        }
        if (position < 0 || position > m_list.size()) {
            return -1;
        }
        return m_list.get(position).getSortLetters().charAt(0);
    }
    
    public String charAt(int position) {
        return m_list.get(position).getSortLetters();
    }

    class ViewHolder {
        TextView tv_title;
        RelativeLayout RL_list_item;
        TextView tv_name;
        TextView tv_short_name;
        ImageView iv_is_checked;
    }

}
