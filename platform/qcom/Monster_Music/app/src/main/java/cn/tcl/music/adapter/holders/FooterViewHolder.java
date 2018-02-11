package cn.tcl.music.adapter.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.tcl.music.view.FooterView;


/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/26 8:43
 * @copyright TCL-MIE
 */
public class FooterViewHolder extends RecyclerView.ViewHolder {

    public FooterView footerView;
    public FooterViewHolder(View view) {
        super(view);
        footerView = (FooterView)view;
    }

}
