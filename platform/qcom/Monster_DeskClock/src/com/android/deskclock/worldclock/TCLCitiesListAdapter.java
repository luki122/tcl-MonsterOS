package com.android.deskclock.worldclock;

import java.util.ArrayList;
import java.util.List;
import com.android.deskclock.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class TCLCitiesListAdapter extends BaseAdapter implements SectionIndexer {

    private List<CityObj> m_list;
    private Context m_context;
    private LayoutInflater inflater;
    private boolean isSearchMode = false;

    public TCLCitiesListAdapter(Context context) {
        m_context = context;
        m_list = new ArrayList<CityObj>();
        inflater = LayoutInflater.from(m_context);
    }
    
    public void setSearchMode(boolean is){
        isSearchMode = is;
    }

    public void updateList(List<CityObj> list) {
        if (list != null) {
//            m_list.clear();
//            m_list.addAll(list);
//            notifyDataSetChanged();
            m_list = list;
            notifyDataSetChanged();
        }
    }
    
    public List<CityObj>  getList(){
        return m_list;
    }

    @Override
    public int getCount() {
        return m_list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return m_list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        CityObj mCity = m_list.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.cell_tcl_cities, parent, false);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tv_city_name = (TextView) convertView.findViewById(R.id.tv_city_name);
            viewHolder.select_checkbox = (CheckBox) convertView.findViewById(R.id.select_checkbox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.select_checkbox.setChecked(mCity.isChoose);

        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);

        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (!isSearchMode && (position == 0 || position == getPositionForSection(section))) {
            viewHolder.tv_title.setVisibility(View.VISIBLE);
            viewHolder.tv_title.setText(mCity.getSortLetters());
        } else {
            viewHolder.tv_title.setVisibility(View.GONE);
        }
        
        viewHolder.tv_city_name.setText(mCity.mCityName);
        

        return convertView;
    }

    @Override
    public int getPositionForSection(int section) {// 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
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
    public int getSectionForPosition(int position) {// 根据ListView的当前位置获取分类的首字母的Char ascii值
        if (m_list.size() <= 0) {
            return -1;
        }
        if (position < 0 || position > m_list.size()) {
            return -1;
        }
        return m_list.get(position).getSortLetters().charAt(0);
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    public String charAt(int position) {
        return m_list.get(position).getSortLetters();
    }

    class ViewHolder {
        TextView tv_title;
        TextView tv_city_name;
        CheckBox select_checkbox;
    }

}
