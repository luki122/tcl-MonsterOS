package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.CheckBox;

import cn.tcl.music.R;

/**
 * Created by jiangyuanxi on 3/1/16.
 */
public class OperateViewHolder extends MediaViewHolder{

    public OperateViewHolder(View itemView,
                            ClickableViewHolder.OnViewHolderClickListener onViewHolderClickListener) {
        super(itemView, onViewHolderClickListener);

        itemPickRadioBtn = (CheckBox) itemView.findViewById(R.id.item_pick_radio_btn);
        itemPickRadioBtn.setOnClickListener(this);
    }

    public CheckBox itemPickRadioBtn;

}

